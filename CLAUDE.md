# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Legado Reader 是 [开源阅读APP](https://github.com/gedoor/legado) 的 JetBrains IDE 插件版本。允许开发者在编码过程中直接在 IDE 的 ToolWindow 或编辑器行内阅读书籍,通过 HTTP API 与阅读 APP 或服务器版阅读进行通信。

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

# 代码覆盖率
./gradlew koverReport
```

构建产物位于 `build/distributions/` 目录下。

## 核心架构

### 设计理念
1. **阅读数据无状态**：所有阅读数据均通过 API 获取,不在本地存储书籍内容
2. **插件数据有状态**：通过持久化配置保存用户设置和阅读地址历史等数据
3. **多个IDE窗口共享状态**：IDE打开多个项目时,共享同一套插件数据和状态
4. **事件驱动架构**：使用 IntelliJ MessageBus 实现组件解耦,所有操作通过事件传递
5. **指令模式**：采用 CommandBus 统一管理用户操作,支持指令拦截、日志和状态追踪
6. **模块化设计**：Command 层、Event 层、Service 层、UI 层严格分离

### 1. 指令驱动 + 事件响应架构（新架构 v2.0+）

#### 1.1 指令系统 (Command Pattern)

所有用户操作通过 **CommandBus** 统一分发：

```
用户快捷键 (Shift+Alt+Right)
    ↓
NextChapterAction.actionPerformed()
    ↓
CommandBus.dispatch(Command.of(CommandType.NEXT_CHAPTER))
    ↓
CommandHandlerRegistry.getHandler(NEXT_CHAPTER)
    ↓
NextChapterHandler.handle()
    ├─ 前置检查（状态验证、章节边界检查）
    ├─ 状态转换（READING → LOADING）
    ├─ 发布 ReadingEvent(CHAPTER_LOADING)
    ├─ 异步加载章节数据
    ├─ 发布 ReadingEvent(CHAPTER_LOADED)
    ├─ 异步同步进度到服务器
    └─ 发布 CommandEvent.completed()
```

**支持的指令类型**（`CommandType`）：
- **书架操作**：`FETCH_BOOKSHELF`, `REFRESH_BOOKSHELF`
- **阅读操作**：`SELECT_BOOK`, `NEXT_CHAPTER`, `PREVIOUS_CHAPTER`, `NEXT_PAGE`, `PREVIOUS_PAGE`, `JUMP_TO_CHAPTER`
- **会话管理**：`BACK_TO_BOOKSHELF`, `TOGGLE_READING_MODE`, `GET_READING_INFO`

**关键类**：
- `Command`：不可变指令对象（Record），包含 id、type、payload、timestamp
- `CommandBus`：指令分发中心（Application Service）
- `CommandHandlerRegistry`：处理器注册表
- `CommandHandler`：处理器接口，所有处理器实现 `handle(Command)` 方法

#### 1.2 事件系统 (Event-Driven Architecture)

**事件类型层次**（sealed interface）：
```
ReaderEvent (基接口)
├── ReadingEvent          # 章节加载事件
│   └── Type: CHAPTER_LOADING, CHAPTER_LOADED, CHAPTER_LOAD_FAILED, SESSION_ENDED
├── CommandEvent          # 指令生命周期事件
│   └── Type: DISPATCHED, EXECUTING, SUCCESS, FAILED
├── PaginationEvent       # 分页事件
│   └── Type: PAGE_CHANGED, PAGE_NAVIGATION_FAILED
└── BookshelfEvent        # 书架事件
    └── Type: LOADING, LOADED, LOAD_FAILED
```

**事件发布和订阅**：
```java
// 发布事件
EventPublisher.getInstance().publish(
    ReadingEvent.loaded(book, chapter, content, direction)
);

// 订阅事件
ApplicationManager.getApplication()
    .getMessageBus()
    .connect()
    .subscribe(ReaderEventListener.TOPIC, new ReaderEventListener() {
        @Override
        public void onEvent(ReaderEvent event) {
            // 使用 pattern matching
            switch (event) {
                case ReadingEvent e -> handleReading(e);
                case PaginationEvent e -> handlePagination(e);
                default -> {}
            }
        }
    });
```

**关键设计**：
- 所有事件都是不可变 Record，线程安全
- 事件在 Application 级别广播，多窗口共享
- UI 组件只订阅事件，不主动调用业务逻辑
- 支持 Java 21 的 pattern matching 和 sealed types

#### 1.3 状态机 (State Machine)

**阅读会话状态**（`ReadingSessionState`）：
- `IDLE`：未开始阅读
- `LOADING`：正在加载章节
- `READING`：正在阅读
- `ERROR`：加载失败
- `SESSION_ENDED`：会话结束

**状态转换规则**（`ReadingSessionStateMachine`）：
```
IDLE → LOADING → READING → LOADING → READING → ... → SESSION_ENDED
                     ↓
                   ERROR → LOADING (重试)
```

**状态保护**：
- Handler 在执行前检查当前状态是否允许操作
- 防止重复加载（如果已在 LOADING 状态，拒绝新的章节切换请求）
- 状态转换失败时自动回滚

#### 1.4 双阅读模式（策略模式）

通过 `ReaderFactory` 枚举管理两种阅读模式：

- **ToolWindow 模式**（`ToolWindowReaderService`）：在 IDE 右侧工具窗口显示
- **EditorLine 模式**（`EditorLineReaderService`）：在编辑器代码行内显示

两种模式都：
1. 订阅 `ReaderEventListener.TOPIC` 响应事件
2. 使用 `PaginationManager` 统一分页

**重要**：`ReaderFactory` 在插件启动时由 `CommandHandlerInitializer` 强制初始化，确保事件订阅生效。

### 2. 数据管理四层架构

#### 数据模型层 (`model` 包)
使用 Java 21 Record 定义不可变数据对象：
- `ReadingSession`：阅读会话（book, chapters, currentChapterIndex, currentContent）
- `ReadingSessionState`：会话状态枚举

#### 存储层 (`storage` 包) - Light Services
- `PluginSettingsStorage`：插件设置持久化（使用 `PersistentStateComponent`）
- `AddressHistoryStorage`：地址历史持久化（最多 4 条）
- 使用 Jackson JSON 序列化，存储在 `config/options/nancheung-legadoReader-*.xml`

#### 管理层 (`manager` 包) - Light Services
- `ReadingSessionManager`：管理当前阅读会话（使用 `AtomicReference`，线程安全）

#### 服务层 (`service` 包) - Light Services
- `ReadingSessionStateMachine`：状态机，验证状态转换
- `PaginationManager`：统一分页管理，支持 Unicode emoji 正确切分
- `EventPublisher`：事件发布服务

### 3. API 通信机制

`ApiUtil.java` 负责与阅读 APP 的 Web 服务通信：

- 使用 Hutool 的 `HttpUtil` 进行 HTTP 请求
- 支持自定义参数，用于服务器版的 token 认证
- 自定义参数格式：`参数名:@参数值`（每行一个），例如：`accessToken:@test:token123`
- 所有 API 调用必须在 Handler 中异步执行

API 端点定义在 `AddressEnum` 中：
- `/getBookshelf` - 获取书架列表
- `/getChapterList` - 获取章节列表
- `/getBookContent` - 获取正文内容
- `/saveBookProgress` - 同步阅读进度

### 4. UI 层设计

**IndexUI.java**：ToolWindow 的核心单例类
- **书架面板**：显示书籍列表、地址输入框、历史记录下拉框
- **正文面板**：显示章节内容、工具栏（返回、上/下章、书籍信息）
- 订阅 `ReadingEvent`，根据事件类型更新 UI

**EditorLineReaderService**：编辑器行内阅读服务
- 订阅 `ReadingEvent` 和 `PaginationEvent`
- 章节加载成功时，调用 `PaginationManager.paginate()` 重新分页
- 根据切换方向智能定位页码（上一章→最后一页，下一章→第一页）

**ReaderEditorLinePainter**：编辑器行绘制器
- 从 `PaginationManager.getCurrentPage()` 获取当前页数据
- 在光标所在行末尾显示书籍内容和页码

### 5. 插件扩展点和快捷键

#### 5.1 扩展点注册

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

<!-- 指令处理器初始化（启动时自动执行） -->
<postStartupActivity implementation="...CommandHandlerInitializer"/>
```

#### 5.2 快捷键映射

| 操作 | 快捷键 | 指令类型 | 说明 |
|------|--------|---------|------|
| 下一章 | `Shift + Alt + →` | `NEXT_CHAPTER` | 切换到下一章，发布事件驱动 UI 更新 |
| 上一章 | `Shift + Alt + ←` | `PREVIOUS_CHAPTER` | 切换到上一章，发布事件驱动 UI 更新 |
| 下一页 | `Ctrl + 滚轮向下` | `NEXT_PAGE` | 页内翻页，到边界自动触发下一章 |
| 上一页 | `Ctrl + 滚轮向上` | `PREVIOUS_PAGE` | 页内翻页，不跨章节 |
| 显示/隐藏 | `鼠标中键双击` | `TOGGLE_READING_MODE` | 切换行内阅读模式的显示状态 |

### 6. 阅读进度同步

每次章节加载成功时，Handler 会：
1. 发布 `ReadingEvent.loaded()` 事件
2. UI 订阅者接收事件并显示内容
3. **异步调用** `ApiUtil.saveBookProgress()` 同步进度到服务器（不等待响应）

进度数据包括：书名、作者、章节索引、章节标题、光标位置、时间戳。

## 依赖版本

### 核心依赖
- **Java**: 21（使用 Record、文本块、sealed types、pattern matching）
- **Gradle**: 9.2.0
- **Kotlin**: 2.1.20
- **IntelliJ Platform**: 2024.2+ (Build 242)
- **IntelliJ Platform Gradle Plugin**: 2.10.4

### 主要库
- **Jackson**: 2.15.3（JSON 序列化/反序列化）
- **Hutool**: 5.8.11（HTTP 请求、工具类）
- **Lombok**: 9.1.0（简化模板代码）
- **JUnit**: 4.13.2 + OpenTest4j 1.3.0（测试框架）

### 构建插件
- **Changelog Plugin**: 2.4.0（发版日志管理）
- **Kover**: 0.9.1（代码覆盖率）

版本定义在 `gradle/libs.versions.toml` 中，使用 Gradle Version Catalog 管理。

## 重要常量和约定

- **插件 ID**: `com.nancheung.plugins.jetbrains.legado-reader`
- **存储键命名空间**: `com.nancheung.legado-reader.settings` / `.addressHistory`
- **目标 IDE**: IntelliJ IDEA 2024.2 (IC - Community Edition)
- **事件主题**: `LegadoReader.Event`
- **API 自定义参数格式**: `参数名:@参数值`（每行一个），例如：`accessToken:@test:token123`
- **数据序列化**: 使用 Jackson JSON 格式
- **服务类型**: Light Services（Application 级别，多窗口共享）
- **地址历史限制**: 最多保存 4 条记录

## 开发注意事项

### 1. 异步处理和 EDT 线程

**所有 API 调用必须异步执行**：
```java
CompletableFuture.runAsync(() -> {
    // API 调用在后台线程执行
    String content = ApiUtil.getBookContent(url, index);
}).thenAccept(result -> {
    // UI 更新在 EDT 线程执行
    ApplicationManager.getApplication().invokeLater(() -> {
        updateUI(result);
    });
});
```
- 避免阻塞 EDT（Event Dispatch Thread）
- 所有 Swing 组件操作必须在 EDT 中执行
- 使用 `ApplicationManager.getApplication().invokeLater()` 或 `SwingUtilities.invokeLater()`

### 2. Light Services 使用

**定义服务**：
```java
@Service
public final class MyService {
    public static MyService getInstance() {
        return ApplicationManager.getApplication().getService(MyService.class);
    }
}
```
- 服务类必须是 `final` 的
- 使用 `@Service` 注解，不需要在 plugin.xml 中注册
- Application Service 在多个 IDE 窗口间共享状态

### 3. 不可变数据模型（Java 21 Record）

**正确使用 Record**：
```java
// ✅ 正确：Record 的访问方法不带 get 前缀
ReadingSession session = ...;
List<BookChapterDTO> chapters = session.chapters();  // 正确
int index = session.currentChapterIndex();           // 正确

// ❌ 错误：不要使用 getter 风格
session.getChapters();         // 编译错误！
session.chapterList();         // 编译错误！
```
- Record 通过返回新实例的方式更新状态（函数式风格）
- 使用 `AtomicReference` 保证线程安全的状态更新

### 4. 事件订阅和发布

**订阅事件**（必须使用匿名内部类，避免类型推断问题）：
```java
ApplicationManager.getApplication()
    .getMessageBus()
    .connect()
    .subscribe(ReaderEventListener.TOPIC, new ReaderEventListener() {
        @Override
        public void onEvent(ReaderEvent event) {
            switch (event) {
                case ReadingEvent e -> handleReading(e);
                case PaginationEvent e -> handlePagination(e);
                default -> {}
            }
        }
    });
```

**发布事件**：
```java
EventPublisher publisher = EventPublisher.getInstance();
publisher.publish(ReadingEvent.loaded(book, chapter, content, direction));
```
- 使用 `EventPublisher` 统一发布，内部调用 `syncPublisher`
- 事件在 Application 级别传播，多窗口共享
- 订阅者的回调方法应确保在正确的线程中执行 UI 更新

### 5. 指令处理器实现

**新增指令处理器的步骤**：

1. 创建 Handler 类实现 `CommandHandler` 接口
2. 在 `handle(Command)` 方法中：
   - 前置检查（参数验证、状态检查）
   - 发布 `CommandEvent.started()`
   - 执行业务逻辑（可异步）
   - 发布领域事件（如 `ReadingEvent`）
   - 发布 `CommandEvent.completed()` 或 `failed()`
3. 在 `CommandHandlerInitializer.initializeHandlers()` 中注册

**示例**：
```java
public class MyHandler implements CommandHandler {
    @Override
    public void handle(Command command) {
        // 1. 前置检查
        if (!canExecute()) {
            eventPublisher.publish(CommandEvent.failed(command, "..."));
            return;
        }

        // 2. 发布开始事件
        eventPublisher.publish(CommandEvent.started(command));

        // 3. 执行业务逻辑
        CompletableFuture.runAsync(() -> {
            try {
                doWork();
                eventPublisher.publish(CommandEvent.completed(command, "..."));
            } catch (Exception e) {
                eventPublisher.publish(CommandEvent.failed(command, e.getMessage()));
            }
        });
    }
}
```

### 6. 状态机使用

**状态转换前必须检查**：
```java
ReadingSessionStateMachine stateMachine = ReadingSessionStateMachine.getInstance();

// 检查当前状态
if (stateMachine.isLoading()) {
    log.warn("当前正在加载中，忽略请求");
    return;
}

// 执行状态转换
if (!stateMachine.transition(ReadingSessionState.LOADING)) {
    log.error("状态转换失败");
    return;
}
```

**状态查询方法**：
- `getCurrentState()` - 获取当前状态
- `isLoading()` - 是否正在加载
- `isReading()` - 是否正在阅读
- `canTransitionTo(state)` - 是否可以转换到目标状态
- `transition(state)` - 执行状态转换（原子操作）

### 7. 分页管理

**使用 PaginationManager**：
```java
PaginationManager paginationManager = PaginationManager.getInstance();

// 重新分页
paginationManager.paginate(content, pageSize);

// 翻页
paginationManager.nextPage();
paginationManager.previousPage();

// 跳转
paginationManager.goToFirstPage();
paginationManager.goToLastPage();
paginationManager.goToPage(index);

// 查询
IPaginationManager.PageData currentPage = paginationManager.getCurrentPage();
int totalPages = paginationManager.getTotalPages();
```

**注意**：
- `paginate()` 会自动处理 Unicode 代理对（emoji）的正确切分
- 翻页操作会发布 `PaginationEvent`
- 翻页失败（已到边界）也会发布 `PAGE_NAVIGATION_FAILED` 事件

### 8. 向后兼容

**旧代码（已废弃但仍可用）**：
```java
ReaderGlobalFacade.getInstance().nextChapter();  // 内部调用 CommandBus
```

**新代码（推荐）**：
```java
CommandBus.getInstance().dispatch(Command.of(CommandType.NEXT_CHAPTER));
```

**废弃类和方法**：
- `EditorLineReaderService` 的翻页方法（已迁移到事件订阅）

### 9. 初始化顺序

插件启动时的初始化流程：
1. `CommandHandlerInitializer.runActivity()` 被调用（postStartupActivity）
2. 调用 `ReaderFactory.values()` 强制初始化枚举
3. 创建 `EditorLineReaderService` 和 `ToolWindowReaderService` 实例
4. 在构造函数中订阅 `ReaderEventListener.TOPIC`
5. 注册所有 `CommandHandler` 到 `CommandHandlerRegistry`

**重要**：必须先初始化 `ReaderFactory`，否则 `EditorLineReaderService` 不会订阅事件，行内阅读模式将无法显示。

### 10. 错误处理

- API 调用失败时，Handler 发布 `CommandEvent.failed()` 或 `ReadingEvent.loadFailed()`
- UI 订阅者接收失败事件并显示错误提示
- 可通过设置启用详细日志：`PluginSettingsStorage.getInstance().getState().enableErrorLog`
- 使用 Lombok 的 `@Slf4j` 注解简化日志记录

### 11. 数据持久化

- 使用 `PersistentStateComponent` 接口进行 XML 持久化
- 配置文件位于：`config/options/nancheung-legadoReader-*.xml`
- 设置通过 `PluginSettingsStorage.getInstance().getState()` 访问
- 历史记录通过 `AddressHistoryStorage.getInstance().getState()` 访问

### 12. Gradle 配置

- 依赖版本统一在 `gradle/libs.versions.toml` 中管理
- 使用 Version Catalog：`libs.jackson`, `libs.hutool` 等
- Lombok 插件已集成，可使用 `@Data`, `@Slf4j`, `@RequiredArgsConstructor` 等注解
