package com.nancheung.plugins.jetbrains.legadoreader.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class IndexWindowFactory implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        //获取内容工厂的实例
        ContentFactory contentFactory = ContentFactory.getInstance();

        // 获取 IndexUI 实例（懒加载）
        IndexUI indexUI = IndexUI.getInstance();

        // 初始化地址历史记录（延迟访问 Service）
        indexUI.initAddressHistory();

        //获取用于toolWindow显示的内容
        Content content = contentFactory.createContent(indexUI.getComponent(), "", false);
        //给toolWindow设置内容
        toolWindow.getContentManager().addContent(content);
    }
}
