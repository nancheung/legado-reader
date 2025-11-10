# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Legado Reader 是 [开源阅读APP](https://github.com/gedoor/legado) 的 JetBrains IDE 插件版本。允许开发者在编码过程中直接在 IDE 的 ToolWindow 或编辑器行内阅读书籍，通过 HTTP API 与阅读 APP 或服务器版阅读进行通信。

## 构建和开发命令

```bash
# 构建插件
./gradlew build

# 在沙箱 IDE 中运行插件（用于开发测试）
./gradlew runIde

# 构建插件发布包（生成 .zip 文件）
./gradlew buildPlugin

# 清理构建产物
./gradlew clean
```

构建产物位于 `build/distributions/` 目录下。

## 核心架构
### 设计理念
1. 阅读数据无状态：所有阅读数据均通过 API 获取，不在本地存储书籍内容
2. 插件数据有状态：通过持久化配置保存用户设置和阅读地址历史等数据
3. 多个IDE窗口共享状态：IDE打开多个项目时，共享同一套插件数据和状态
4. 模块化设计：UI 层、服务层、数据层分离，便于维护和扩展

### 1. 双阅读模式架构

插件支持两种阅读模式，通过 **策略模式** 实现：

- **ToolWindow 阅读模式**（`ToolWindowReaderService`）：在 IDE 右侧工具窗口中显示书籍内容
- **编辑器行内阅读模式**（`EditorLineReaderService`）：在编辑器代码行中显示阅读内容（待完善）

两种模式都实现 `IReader` 接口，通过 `ReaderFactory` 枚举统一管理。快捷键操作（上一章/下一章）会调用当前激活模式的 reader 实例。

### 2. 数据管理层次

项目采用分层数据管理：

**全局配置数据** (`Data.java`)：
- 使用 IntelliJ Platform 的 `PropertiesComponent` 进行持久化
- 管理阅读地址历史（最多 4 条，使用 `{nc}` 分隔）
- 存储用户配置（字体、颜色、API 自定义参数等）
- 静态代码块在插件加载时自动初始化

**当前阅读会话数据** (`CurrentReadData.java`)：
- 临时存储当前书籍、章节列表、章节索引、正文内容
- 不持久化，仅在运行时使用

**行内阅读数据** (`BodyInLineData.java`)：
- 用于编辑器行内阅读模式的分页管理

### 3. API 通信机制

`ApiUtil.java` 负责与阅读 APP 的 Web 服务通信：

- 使用 Hutool 的 `HttpUtil` 进行 HTTP 请求
- 支持自定义参数 (`Data.apiCustomParam`)，用于服务器版的 token 认证
- 自定义参数格式：`参数名:@参数值`（每行一个），例如：`accessToken:@test:token123`
- 所有 API 调用在 UI 层通过 `CompletableFuture` 异步执行

API 端点定义在 `AddressEnum` 中：
- `/getBookshelf` - 获取书架列表
- `/getChapterList` - 获取章节列表
- `/getBookContent` - 获取正文内容
- `/saveBookProgress` - 同步阅读进度

### 4. UI 层设计

**IndexUI.java** 是 ToolWindow 的核心单例类，包含两个主面板：

- **书架面板**：显示书籍列表（JTable）、地址输入框、历史记录下拉框
- **正文面板**：显示章节内容（JTextPane）、工具栏（返回、上/下章、书籍信息）

面板切换通过 `setVisible()` 实现，避免重复创建组件。

**SettingUI.java** 管理插件设置界面：
- 配置注册在 `plugin.xml` 中的 `<projectConfigurable>` 扩展点
- 设置路径：Settings → Tools → Legado Reader
- 支持字体颜色选择（ColorPicker）、字体大小、API 自定义参数、错误日志开关

### 5. 插件扩展点注册

在 `src/main/resources/META-INF/plugin.xml` 中定义：

```xml
<!-- ToolWindow 注册 -->
<toolWindow id="Legado Reader" anchor="right"
            factoryClass="...IndexWindowFactory" />

<!-- 设置页面注册 -->
<projectConfigurable groupId="tools"
                     instance="...SettingFactory"/>

<!-- 编辑器行绘制器（用于行内阅读） -->
<editor.linePainter implementation="...ReaderEditorLinePainter"/>

<!-- 快捷键 Actions -->
<action id="...previousChapter" class="...PreviousChapterAction">
    <keyboard-shortcut keymap="$default" first-keystroke="shift alt LEFT"/>
</action>
```

### 6. 阅读进度同步

每次显示章节内容时（`IndexUI.setTextBodyUIData()`），会：
1. 异步获取正文内容并显示
2. 设置光标位置到上次阅读进度 (`durChapterPos`)
3. **异步调用 `ApiUtil.saveBookProgress()` 同步进度到服务器**（不等待响应）

进度数据包括：书名、作者、章节索引、章节标题、光标位置、时间戳。

## 重要常量和约定

- 插件 ID 前缀：`com.nancheung.legado-reader`
- 持久化数据 key：`com.nancheung.legado-reader.persistence.data`
- 默认 IDE 版本：IntelliJ IDEA 2024.2 (IC)
- Java 版本：21
- 地址历史分隔符：`{nc}`
- API 自定义参数分隔符：`:@`

## 开发注意事项

1. **异步处理**：所有 API 调用必须使用 `CompletableFuture` 异步执行，避免阻塞 EDT（Event Dispatch Thread）

2. **单例模式**：`IndexUI` 和 `SettingUI` 都是单例，全局共享状态

3. **配置持久化**：使用 `PropertiesComponent.getInstance().setValue/getValue` 进行配置的读写

4. **错误处理**：API 调用失败时显示错误提示面板，可通过设置启用详细日志

5. **UI 更新**：所有 UI 操作必须在 EDT 线程执行，异步任务完成后使用 `thenAccept` 回调更新 UI

6. **行内阅读功能**：当前处于未完成状态（见 EditorLineReaderService 中的 TODO 注释）
