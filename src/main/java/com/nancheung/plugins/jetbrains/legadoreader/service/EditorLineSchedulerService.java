package com.nancheung.plugins.jetbrains.legadoreader.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 编辑器行内阅读调度器服务
 * 管理用于处理鼠标点击延迟判断的线程池生命周期
 */
@Service
public final class EditorLineSchedulerService implements Disposable {
    private static final Logger LOG = Logger.getInstance(EditorLineSchedulerService.class);

    /**
     * 单线程定时任务调度器
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 构造函数（由 IntelliJ Platform 自动调用）
     */
    public EditorLineSchedulerService() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "EditorLineScheduler");
            thread.setDaemon(true); // 设置为守护线程
            return thread;
        });
        LOG.info("EditorLineSchedulerService 已初始化");
    }

    /**
     * 获取服务实例
     *
     * @return 服务实例
     */
    public static EditorLineSchedulerService getInstance() {
        return ApplicationManager.getApplication().getService(EditorLineSchedulerService.class);
    }

    /**
     * 调度一个延迟任务
     *
     * @param command 要执行的任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public void schedule(Runnable command, long delay, TimeUnit unit) {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.schedule(() -> {
                    try {
                        command.run();
                    } catch (Exception e) {
                        LOG.error("调度任务执行失败", e);
                    }
                }, delay, unit);
            } else {
                LOG.warn("线程池已关闭，无法调度新任务");
            }
        } catch (Exception e) {
            LOG.error("调度任务失败", e);
        }
    }

    /**
     * 关闭线程池（由 IntelliJ Platform 在应用关闭时自动调用）
     */
    @Override
    public void dispose() {
        LOG.info("正在关闭 EditorLineSchedulerService");
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warn("线程池在 5 秒内未能正常关闭，强制关闭");
                scheduler.shutdownNow();
            }
            LOG.info("EditorLineSchedulerService 已关闭");
        } catch (InterruptedException e) {
            LOG.error("关闭线程池时被中断", e);
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 检查线程池是否已关闭
     *
     * @return 如果已关闭返回 true
     */
    public boolean isShutdown() {
        return scheduler.isShutdown();
    }
}
