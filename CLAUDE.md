# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Legado Reader 是 [开源阅读APP](https://github.com/gedoor/legado) 的 JetBrains IDE 插件版本。允许开发者在编码过程中直接在 IDE 的 ToolWindow 或编辑器行内阅读书籍，通过 HTTP API 与阅读 APP 或服务器版阅读（[hectorqin/reader](https://github.com/hectorqin/reader)）通信。书籍内容不落盘（无状态），仅持久化插件设置和地址历史（有状态）。

## 构建和开发命令

```bash
./gradlew build          # 构建插件
./gradlew runIde         # 在沙箱 IDE 中运行插件（开发测试）
./gradlew buildPlugin    # 构建发布包（build/distributions/*.zip）
./gradlew clean          # 清理构建产物
./gradlew koverReport    # 代码覆盖率报告
```

注意：当前 `src/test` 不存在，仓库暂无测试代码。README.md 的 `<!-- Plugin description -->` 区块会被构建时抽取为插件描述，删除该标记会导致构建失败。

## 技术栈

- **Java 21**（Record、sealed interface、pattern matching switch）+ 少量 Kotlin 配置；源码全部为 Java
- **IntelliJ Platform** 2024.2+（sinceBuild 242，platformType IC），IntelliJ Platform Gradle Plugin 2.x
- **Hutool**（HTTP + JSON）、**Lombok**（`@Slf4j`、`@Data` 等，io.freefair.lombok 插件）
- 依赖版本统一在 `gradle/libs.versions.toml`（Version Catalog）管理；插件坐标、版本、平台版本在 `gradle.properties`

## 核心架构

包根：`com.nancheung.plugins.jetbrains.legadoreader`

### 数据流总览（指令驱动 + 事件响应）

所有用户操作走同一条链路，UI 只发指令、只收事件，不直接调用业务逻辑：

```
Action（快捷键/按钮，action 包）
    ↓ CommandBus.dispatch(Command.of(CommandType.NEXT_CHAPTER))
CommandBus → CommandHandlerRegistry → 对应 Handler（command/handler 包）
    ├─ 前置检查（状态机验证、章节边界）
    ├─ 状态转换（READING → LOADING）
    ├─ 异步调用 ApiUtil 加载数据（CompletableFuture）
    ├─ 发布领域事件 ReadingEvent.loaded(...) / loadFailed(...)
    ├─ 异步同步进度到服务器（不等待响应）
    └─ 发布 CommandEvent completed/failed
    ↓ EventPublisher（Application 级 MessageBus，多窗口共享）
UI 订阅者（MainReaderPanel、EditorLineReaderService）响应事件更新界面
```

### 指令系统（command 包）

- `Command`：不可变 Record（id、type、payload、timestamp）；`CommandType` 枚举定义所有指令
- `CommandBus`（Application Service）分发到 `CommandHandlerRegistry` 中注册的 `CommandHandler`
- **已实现的 Handler**：`NEXT_CHAPTER`、`PREVIOUS_CHAPTER`、`NEXT_PAGE`、`PREVIOUS_PAGE`、`SELECT_BOOK`、`BACK_TO_BOOKSHELF`、`TOGGLE_READING_MODE`
- **仅有枚举、Handler 未实现（TODO）**：`FETCH_BOOKSHELF`、`REFRESH_BOOKSHELF`、`JUMP_TO_CHAPTER`、`GET_READING_INFO`——书架刷新目前由 `BookshelfPanel.refreshBookshelf()` 直接完成，不走指令链路
- 带参数指令使用 `command/payload` 下的 payload 类（如 `SelectBookPayload`）

新增指令处理器的步骤：实现 `CommandHandler` → 在 `handle(Command)` 中做前置检查、发布 `CommandEvent.started()`、异步执行业务、发布领域事件和 `completed()/failed()` → 在 `CommandHandlerInitializer.initializeHandlers()` 中注册。

### 事件系统（event 包）

`ReaderEvent` 是 sealed interface，全部事件为不可变 Record，通过 `EventPublisher.getInstance().publish(...)` 发布到 `ReaderEventListener.TOPIC`（Application 级，多 IDE 窗口共享）：

- `ReadingEvent`：CHAPTER_LOADING / CHAPTER_LOADED / CHAPTER_LOAD_FAILED / SESSION_ENDED
- `CommandEvent`：指令生命周期（DISPATCHED / EXECUTING / SUCCESS / FAILED）
- `PaginationEvent`：PAGE_CHANGED / PAGE_NAVIGATION_FAILED（翻到边界也发事件）
- `BookshelfEvent`：LOADING / LOADED / LOAD_FAILED
- `SettingsChangedEvent`：设置保存时发布（FONT_SETTINGS / DISPLAY_SETTINGS / ALL_SETTINGS）
- `ProgressSyncEvent`：进度同步状态（SYNCING / SYNCED / SYNC_FAILED），由 `ProgressSyncService` 发布，正文面板顶部展示提示

**UI 组件不要手动订阅 MessageBus**——继承 `presentation.common.UIEventSubscriber` 并按需重写 `onReadingEvent` / `onPaginationEvent` / `onBookshelfEvent` / `onSettingsChangedEvent` / `onProgressSyncEvent` 即可。基类在构造函数中完成订阅，并保证回调在 EDT 线程执行（内部已 `invokeLater` + 异常兜底）。

### 状态机（service 包）

`ReadingSessionStateMachine`（Application Service，原子转换）管理 `ReadingSessionState`：

```
IDLE → LOADING → READING → LOADING → ... → SESSION_ENDED
                    ↓
                  ERROR → LOADING（重试）
```

Handler 执行前必须检查状态（如 `isLoading()` 时拒绝新的章节切换，防止重复加载），用 `transition(state)` 转换并处理失败回滚。

### 数据与服务分层

- **model**：`ReadingSession`（Record：book、chapters、currentChapterIndex、currentContent）、`ReadingSessionState`
- **manager**：`ReadingSessionManager` 持有当前会话（`AtomicReference`，函数式更新返回新 Record 实例）
- **service**：`ReadingSessionStateMachine`、`PaginationManager`（统一分页，正确处理 Unicode 代理对/emoji 切分）、`EditorLineSchedulerService`（行内模式鼠标延迟判断的单线程调度器）、`ProgressSyncService`（翻页/工具窗口滚动时防抖 2 秒同步页内阅读位置 `durChapterPos` 到服务器；返回书架时 `flush()` 立即同步。工具窗口正文文档为 `标题+\n+内容`，滚动同步时需扣除标题行偏移）
- **storage**：`PluginSettingsStorage`、`AddressHistoryStorage`，均为 `PersistentStateComponent`，用 IntelliJ 的 xmlb 序列化（`@OptionTag` + `storage/converter` 下的 `FontConverter`/`JBColorConverter` 处理 Font/JBColor），存储于 `config/options/nancheung-legadoReader-settings.xml` 和 `-addressHistory.xml`。地址历史最多 4 条，`getMostRecent()` 提供当前 API 根地址
- 以上均为 Application 级 Light Service（`@Service` + `final` 类 + 静态 `getInstance()`，无需在 plugin.xml 注册），多 IDE 窗口共享状态

### API 通信（api 包）

`ApiUtil`（Hutool HttpUtil + JSONUtil）访问 `AddressEnum` 定义的端点：`/getBookshelf`、`/getChapterList`、`/getBookContent`、`/saveBookProgress`。基地址取 `AddressHistoryStorage.getMostRecent()`。

自定义请求参数（服务器版 token 认证）存储为 `PluginSettingsStorage.CustomParam`（name/value 对象列表），设置页用表格（`CustomParamTablePanel`）编辑，每次请求由 `parseCustomParams()` 转成参数 Map 附加。**注意**：旧版 `参数名:@参数值` 文本格式已废弃，现在是结构化列表。

所有 API 调用必须在 Handler 中异步执行（`CompletableFuture`），禁止阻塞 EDT。

### UI 层（presentation 包）

- **toolwindow**：`MainReaderPanelFactory`（plugin.xml 注册的 ToolWindow 工厂）→ `MainReaderPanel`（单例，CardLayout 切换 `panel/BookshelfPanel` 和 `panel/TextBodyPanel`；`AddressBarPanel` 为地址栏组件）。事件处理委托给 `handler/MainPanelEventHandler`，正文样式由 `styling/TextBodyStyling` 管理
- **editorline**：`EditorLineReaderService`（订阅事件，章节加载后调用 `PaginationManager.paginate()` 并按切换方向定位页码：上一章→最后一页，下一章→第一页）+ `ReaderEditorLinePainter`（plugin.xml 注册的 editor.linePainter，在光标行末尾绘制当前页内容）
- **settings**：`SettingsConfigurable`（plugin.xml 注册，Tools → Legado Reader）→ `SettingsPanel`（视图）→ `SettingsViewModel`（与 Storage.State 解耦的可编辑副本，负责 load/save/校验协调）→ `validation/SettingsValidator`。保存时发布 `SettingsChangedEvent` 通知各阅读界面刷新样式

### 初始化顺序（关键）

`CommandHandlerInitializer` 是 `ProjectActivity`（postStartupActivity），用 `AtomicBoolean` 保证多项目只初始化一次，`invokeLater` 后执行 `initializeHandlers()`：先 `new EditorLineReaderService()`（否则行内模式收不到事件、无法显示），再注册所有 Handler。

### 快捷键（plugin.xml）

| 操作 | 快捷键 | 指令 |
|------|--------|------|
| 上一章 / 下一章 | `Shift+Alt+←` / `Shift+Alt+→` | `PREVIOUS_CHAPTER` / `NEXT_CHAPTER` |
| 上一页 / 下一页 | `Ctrl+滚轮上` / `Ctrl+滚轮下`（button143/142） | `PREVIOUS_PAGE` / `NEXT_PAGE`（下一页到边界自动触发下一章；上一页不跨章） |
| 行内模式显示/隐藏 | 鼠标中键双击 | `TOGGLE_READING_MODE`（全局开关 `enableShowBodyInLine`） |
| 阅读窗口显示/隐藏 | `Alt+D` | 无指令，`ToggleToolWindowAction` 直接操作 `ToolWindowManager`（纯 IDE 窗口操作，不走指令链路） |

## 开发注意事项

1. **Record 访问器不带 get 前缀**：`session.chapters()`、`session.currentChapterIndex()`，不是 `getChapters()`。Record 状态更新通过返回新实例
2. **线程规则**：API 调用在后台线程（`CompletableFuture`），Swing 更新在 EDT（`invokeLater`）。继承 `UIEventSubscriber` 的类已由基类保证 EDT
3. **发布事件用 `EventPublisher`**，订阅用 `UIEventSubscriber`；直接订阅 MessageBus 时用匿名内部类实现 `ReaderEventListener`（避免 lambda 类型推断问题），事件分发用 pattern matching switch
4. **状态转换前先查状态机**，转换失败要处理（记日志并返回，不要继续执行）
5. **错误处理**：失败时发布 `CommandEvent.failed()` 或 `ReadingEvent.loadFailed()`，由 UI 订阅者展示；详细日志开关为 `PluginSettingsStorage.getState().enableErrorLog`
6. **legacy 代码**：`common` 包下的 `ReadingEvent`/`ReadingEventListener`/`IReader` 是旧架构遗留，无其他代码引用，不要在新代码中使用（事件类以 `event` 包为准）
7. 注释、日志、commit message 均使用中文；commit message 沿用 gitmoji 风格（如 `:recycle: refactor: ...`）

## 重要常量

- 插件 ID：`com.nancheung.plugins.jetbrains.legado-reader`
- 持久化 State 名：`LegadoReaderSettings` / `LegadoReaderAddressHistory`
- 地址历史上限：`AddressHistoryStorage.MAX_SIZE = 4`
