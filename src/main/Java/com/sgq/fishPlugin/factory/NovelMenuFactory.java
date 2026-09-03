package com.sgq.fishPlugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
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
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * 翻页操作「»」按钮：点击弹出操作菜单
 * 菜单项：上一页、下一页、自动翻页开关
 */
public class NovelMenuFactory implements StatusBarWidgetFactory {

    public static final String WIDGET_ID = "novel.status.menuWidget";

    @Override
    public @NotNull @NonNls String getId() {
        return WIDGET_ID;
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Novel Menu";
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
        return new NovelMenuWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    public static class NovelMenuWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {
        private final Project project;
        private StatusBar statusBar;

        public NovelMenuWidget(Project project) {
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
        public void dispose() {
            stopAutoPage();
        }

        @Override
        public @Nullable WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @NotNull String getText() {
            return "»";
        }

        @Override
        public float getAlignment() {
            return Component.LEFT_ALIGNMENT;
        }

        @Override
        public @Nullable @Nls String getTooltipText() {
            return NovelService.getInstance(project).getTooltipProgressText();
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return e -> showMenu(e.getComponent());
        }

        private void showMenu(Component invoker) {
            boolean autoOn = isAutoPage();
            NovelService novelService = NovelService.getInstance(project);
            String progressText = novelService.getProgressText();
            String[] actions = {
                    progressText,
                    "上一页",
                    "下一页",
                    autoOn ? "关闭自动翻页" : "开启自动翻页"
            };

            JBPopup popup = JBPopupFactory.getInstance()
                    .createPopupChooserBuilder(Arrays.asList(actions))
                    .setTitle("更多选项")
                    .setRequestFocus(true)
                    .setItemChosenCallback(this::performAction)
                    .createPopup();
            popup.showInScreenCoordinates(invoker, invoker.getLocationOnScreen());
        }

        private void performAction(@NotNull String action) {
            if (action.startsWith("当前阅读进度")) {
                return;
            }
            NovelService novelService = NovelService.getInstance(project);
            switch (action) {
                case "上一页":
                    novelService.prePage();
                    break;
                case "下一页":
                    novelService.nextPage();
                    break;
                default:
                    toggleAutoPage();
                    break;
            }
        }

        private boolean isAutoPage() {
            return AutoPageFactory.NovelStatusWidget.isAutoPaging;
        }

        private void toggleAutoPage() {
            if (isAutoPage()) {
                stopAutoPage();
                return;
            }
            NovelService novelService = NovelService.getInstance(project);
            String time = novelService.getAutoPageTime();
            if (time == null || time.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "请先去设置里输入自动翻页时间");
                return;
            }
            try {
                int seconds = Integer.parseInt(time.trim());
                if (seconds <= 0) {
                    JOptionPane.showMessageDialog(null, "自动翻页时间必须大于0秒");
                    return;
                }
                if (AutoPageFactory.NovelStatusWidget.autoPageTimer != null) {
                    AutoPageFactory.NovelStatusWidget.autoPageTimer.stop();
                }
                AutoPageFactory.NovelStatusWidget.autoPageTimer =
                        new Timer(seconds, e -> NovelService.getInstance(project).nextPage());
                AutoPageFactory.NovelStatusWidget.autoPageTimer.start();
                AutoPageFactory.NovelStatusWidget.isAutoPaging = true;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "自动翻页时间格式错误，请输入数字");
            }
        }

        private void stopAutoPage() {
            if (AutoPageFactory.NovelStatusWidget.autoPageTimer != null) {
                AutoPageFactory.NovelStatusWidget.autoPageTimer.stop();
            }
            AutoPageFactory.NovelStatusWidget.isAutoPaging = false;
        }
    }
}