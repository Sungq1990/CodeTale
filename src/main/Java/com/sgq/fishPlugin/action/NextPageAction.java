package com.sgq.fishPlugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sgq.fishPlugin.service.NovelService;
import org.jetbrains.annotations.NotNull;

public class NextPageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        NovelService novelService = NovelService.getInstance(project);
        novelService.nextPage();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
