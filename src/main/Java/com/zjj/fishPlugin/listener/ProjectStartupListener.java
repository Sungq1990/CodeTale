package com.zjj.fishPlugin.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.zjj.fishPlugin.factory.SettingFactory;
import org.jetbrains.annotations.NotNull;

/**
 * 项目启动监听器，自动加载上次的小说
 * Created by zhongjiajie on 2025/1/27
 */
public class ProjectStartupListener implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // 延迟执行，确保项目完全加载
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                SettingFactory settingFactory = new SettingFactory(project);
                settingFactory.autoLoadLastNovel();
            } catch (Exception e) {
                // 静默处理错误，避免影响项目启动
                e.printStackTrace();
            }
        });
    }
}
