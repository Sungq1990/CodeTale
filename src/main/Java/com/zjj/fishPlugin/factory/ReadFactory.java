package com.zjj.fishPlugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zjj.fishPlugin.config.Config;
import com.zjj.fishPlugin.ui.ReadUI;
import org.jetbrains.annotations.NotNull;

/**
 * Created by zhongjiajie on 2025/9/30 16:24.
 */
public class ReadFactory implements ToolWindowFactory {

    private ReadUI readUI = ReadUI.getReadUI();
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Content content = ContentFactory.getInstance().createContent(readUI.getComponent(), "Book", false);
        toolWindow.getContentManager().addContent(content);
        Config.readUI = readUI;
    }

}
