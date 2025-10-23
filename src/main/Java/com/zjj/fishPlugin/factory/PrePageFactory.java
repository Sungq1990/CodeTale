package com.zjj.fishPlugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.util.Consumer;
import com.zjj.fishPlugin.service.NovelService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class PrePageFactory implements StatusBarWidgetFactory {

    @Override
    public @NotNull @NonNls String getId() {
        return "novel.status.preWidget";
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Pre";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true; // 控制是否显示
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new NovelStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    /**
     * 实际的 Widget 实现
     */
    public static class NovelStatusWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {
        private final Project project;
        private StatusBar statusBar;

        public NovelStatusWidget(Project project) {
            this.project = project;
        }

        @Override
        public @NotNull String ID() {
            return "novel.status.preWidget";
        }

        @Override
        public void install(@NotNull StatusBar statusBar) {
            this.statusBar = statusBar;
        }

        @Override
        public void dispose() {}

        @Override
        public @Nullable WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @Nullable Icon getIcon() {
            return UIManager.getIcon("FileView.directoryIcon");
        }

        @Override
        public @Nullable @Nls String getTooltipText() {
            return "点击显示上一行";
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return e -> showNextLine();
        }

        private void showNextLine() {
            NovelService novelService = NovelService.getInstance(project);
            novelService.prePage();
        }
    }
}
