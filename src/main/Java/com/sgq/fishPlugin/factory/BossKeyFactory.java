package com.sgq.fishPlugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.util.Consumer;
import com.sgq.fishPlugin.service.NovelService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * 老板键状态栏按钮
 * 点击可在 隐藏/显示 小说文本之间切换
 */
public class BossKeyFactory implements StatusBarWidgetFactory {

    public static final String WIDGET_ID = "novel.status.bossKeyWidget";

    @Override
    public @NotNull @NonNls String getId() {
        return WIDGET_ID;
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Boss Key";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
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
            return WIDGET_ID;
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
            NovelService novelService = NovelService.getInstance(project);
            return novelService.isBossHidden()
                    ? UIManager.getIcon("FileChooser.upFolderIcon")
                    : UIManager.getIcon("FileView.directoryIcon");
        }

        @Override
        public @Nullable @Nls String getTooltipText() {
            NovelService novelService = NovelService.getInstance(project);
            return novelService.isBossHidden()
                    ? "BossKey：文本已隐藏"
                    : "BossKey：文本已显示";
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return e -> toggleBoss();
        }

        private void toggleBoss() {
            NovelService novelService = NovelService.getInstance(project);
            novelService.toggleBossHide();
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
        }
    }
}
