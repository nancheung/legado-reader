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

### 2. 数据管理层次（使用 Light Services）

项目采用四层数据管理架构：

**数据模型层** (`model` 包)：
- 使用 Java 21 Record 定义不可变数据对象
- `PluginSettingsData` - 插件设置数据
- `AddressHistoryData` - 历史记录集合
- `AddressHistoryItem` - 历史记录项
- `ReadingSession` - 阅读会话（不可变，线程安全）

**存储层** (`storage` 包) - Light Services：
- 使用 `@Service` 注解定义 Application Service
- `PluginSettingsStorage` - 设置的 JSON 序列化和持久化
- `AddressHistoryStorage` - 历史记录的 JSON 序列化和持久化
- 使用 Jackson 进行 JSON 序列化/反序列化
- 使用 `PropertiesComponent.getInstance()` 进行 Application 级别存储

**管理层** (`manager` 包) - Light Services：
- 使用 `@Service` 注解定义 Application Service
- `PluginSettingsManager` - 管理插件设置的内存缓存和读写
- `AddressHistoryManager` - 管理地址历史（最多 4 条）
- `ReadingSessionManager` - 管理当前阅读会话（内存中，不持久化，使用 AtomicReference 保证线程安全）
- 所有 Manager 使用 Application Service，多窗口共享状态

**兼容层** (`dao` 包)：
- `Data.java` - 代理到各个 Manager，保持向后兼容（标记 @Deprecated）
- `CurrentReadData.java` - 代理到 ReadingSessionManager（标记 @Deprecated）
- 保留原有 API 接口，内部调用新的 Manager 层
- `BodyInLineData.java` - 用于编辑器行内阅读模式的分页管理

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
- 存储键命名空间：`com.nancheung.legado-reader.settings` / `.addressHistory`
- 默认 IDE 版本：IntelliJ IDEA 2024.2 (IC)
- Java 版本：21
- API 自定义参数格式：`参数名:@参数值`（每行一个）
- 数据序列化：使用 Jackson JSON 格式
- 服务类型：Light Services（Application 级别，多窗口共享）

## 开发注意事项

1. **异步处理**：所有 API 调用必须使用 `CompletableFuture` 异步执行，避免阻塞 EDT（Event Dispatch Thread）

2. **Light Services 使用**：
   - 使用 `@Service` 注解定义服务类（类必须是 final 的）
   - 通过 `ApplicationManager.getApplication().getService(ServiceClass.class)` 获取服务实例
   - 不需要在 plugin.xml 中注册
   - Application Service 在多个 IDE 窗口间共享状态

3. **数据不可变性**：
   - 使用 Java 21 Record 定义数据对象，确保不可变性
   - `ReadingSession` 通过返回新实例的方式更新状态
   - 使用 `AtomicReference` 保证线程安全

4. **向后兼容**：
   - `Data.java` 和 `CurrentReadData.java` 作为兼容层保留
   - 新代码应直接使用 Manager 层的 API
   - 兼容层已标记为 @Deprecated

5. **配置持久化**：
   - 新架构使用 JSON 格式存储配置，通过 Jackson 序列化
   - 旧格式数据会丢失，需要重新配置
   - 所有持久化操作由 Storage 层统一管理

6. **错误处理**：API 调用失败时显示错误提示面板，可通过设置启用详细日志

7. **UI 更新**：所有 UI 操作必须在 EDT 线程执行，异步任务完成后使用 `thenAccept` 回调更新 UI

8. **行内阅读功能**：当前处于未完成状态（见 EditorLineReaderService 中的 TODO 注释）

9. **gradle 配置**：
   - 依赖使用 version_catalogs管理，版本号在 [libs.versions.toml](gradle/libs.versions.toml) 定义
   - 