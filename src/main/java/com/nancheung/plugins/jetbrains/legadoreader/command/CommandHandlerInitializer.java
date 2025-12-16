package com.nancheung.plugins.jetbrains.legadoreader.command;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.nancheung.plugins.jetbrains.legadoreader.command.handler.*;
import com.nancheung.plugins.jetbrains.legadoreader.common.ReaderFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 指令处理器初始化器
 * 在插件启动时自动注册所有处理器
 * 使用 IntelliJ Platform 的 StartupActivity 机制
 *
 * @author NanCheung
 */
@Slf4j
public class CommandHandlerInitializer implements StartupActivity {

    /**
     * 项目打开时执行
     * 只在第一次打开时初始化一次（Application 级别）
     */
    @Override
    public void runActivity(@NotNull Project project) {
        // 使用 invokeLater 确保在 Application 初始化完成后执行
        ApplicationManager.getApplication().invokeLater(() -> {
            initializeHandlers();
        });
    }

    /**
     * 初始化并注册所有处理器
     */
    public static void initializeHandlers() {
        CommandHandlerRegistry registry = CommandHandlerRegistry.getInstance();

        // 检查是否已经初始化过
        if (registry.size() > 0) {
            log.debug("处理器已初始化，跳过");
            return;
        }

        log.info("开始注册指令处理器...");

        // ========== 初始化 ReaderFactory（触发 EditorLineReaderService 事件订阅）==========
        ReaderFactory[] factories = ReaderFactory.values();
        log.info("初始化 {} 个 Reader 实例", factories.length);

        // ========== 章节切换处理器 ==========
        registry.register(new NextChapterHandler());
        registry.register(new PreviousChapterHandler());

        // ========== 翻页处理器 ==========
        registry.register(new NextPageHandler());
        registry.register(new PreviousPageHandler());

        // ========== 书籍选择处理器 ==========
        registry.register(new SelectBookHandler());

        // ========== 会话管理处理器 ==========
        registry.register(new BackToBookshelfHandler());
        registry.register(new ToggleReadingModeHandler());

        // TODO: 待实现的处理器
        // registry.register(new FetchBookshelfHandler());
        // registry.register(new JumpToChapterHandler());
        // registry.register(new RefreshBookshelfHandler());
        // registry.register(new GetReadingInfoHandler());

        log.info("指令处理器注册完成，共 {} 个处理器", registry.size());
    }
}
