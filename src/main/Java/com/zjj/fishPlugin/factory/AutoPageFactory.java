package com.zjj.fishPlugin.factory;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.ui.AnimatedIcon;
import com.intellij.util.Consumer;
import com.zjj.fishPlugin.config.Config;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * 自动翻页状态栏开关
 * 点击可切换 开/关 状态
 */
public class AutoPageFactory implements StatusBarWidgetFactory {

    @Override
    public @NotNull @NonNls String getId() {
        return "novel.status.autoPageWidget";
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Auto Page Switch";
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

    // --------------------------
    // 实际的 Widget 实现
    // --------------------------
    public static class NovelStatusWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {
        private final Project project;
        private StatusBar statusBar;
        public static boolean isAutoPaging = false; // 当前状态

        private final Icon onIcon = new AnimatedIcon.Default();
        private final Icon offIcon = UIManager.getIcon("FileChooser.upFolderIcon"); // 关闭图标
        public static Timer autoPageTimer;

        public NovelStatusWidget(Project project) {
            this.project = project;
        }

        @Override
        public @NotNull String ID() {
            return "novel.status.autoPageWidget";
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
        public @Nullable Icon getIcon() {
            return isAutoPaging ? onIcon : offIcon;
        }

        @Override
        public @Nullable @Nls String getTooltipText() {
            return isAutoPaging ? "自动翻页：已开启（点击关闭）" : "自动翻页：已关闭（点击开启）";
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return e -> toggleAutoPage();
        }

        /**
         * 切换自动翻页状态
         */
        private void toggleAutoPage() {
            if (isAutoPaging) {
                stopAutoPage();
            } else {
                startAutoPage();
            }

            // 更新状态栏显示
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
        }

        /**
         * 开启自动翻页
         */
        private void startAutoPage() {
            try {
                String time = Config.autoPageTime;
                if(StrUtil.isBlank(time)){
                    JOptionPane.showMessageDialog(null, "请先去设置里输入自动翻页时间");
                    return;
                }
                int seconds = Integer.parseInt(time);
                if (seconds <= 0) {
                    JOptionPane.showMessageDialog(null, "自动翻页时间必须大于0秒");
                    return;
                }

                if (autoPageTimer != null && autoPageTimer.isRunning()) {
                    autoPageTimer.stop();
                }

                autoPageTimer = new Timer(seconds, e -> Config.nextPage());
                autoPageTimer.start();

                isAutoPaging = true;
//                JOptionPane.showMessageDialog(null, "自动翻页已启动，每 " + seconds + " 秒翻一页");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "自动翻页时间格式错误，请输入数字");
            }
        }

        /**
         * 停止自动翻页
         */
        private void stopAutoPage() {
            if (autoPageTimer != null) {
                autoPageTimer.stop();
            }
            isAutoPaging = false;
//            JOptionPane.showMessageDialog(null, "自动翻页已停止");
        }
    }
}
