package com.zjj.fishPlugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zjj.fishPlugin.service.NovelService;
import com.zjj.fishPlugin.ui.ReadUI;
import org.jetbrains.annotations.NotNull;

/**
 * Created by zhongjiajie on 2025/9/30 16:24.
 */
public class ReadFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        NovelService novelService = NovelService.getInstance(project);
        ReadUI readUI = novelService.getReadUI();
        readUI.loadChapterPage(true);
        Content content = ContentFactory.getInstance().createContent(readUI.getComponent(), "Book", false);
        toolWindow.getContentManager().addContent(content);

        // 每次工具窗口被显示时，重新定位并高亮当前阅读章节
        project.getMessageBus().connect(project).subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
            @Override
            public void toolWindowShown(@NotNull ToolWindow window) {
                if (window.getId().equals(toolWindow.getId())) {
                    NovelService.getInstance(project).getReadUI().loadChapterPage(true);
                }
            }
        });
    }

}
