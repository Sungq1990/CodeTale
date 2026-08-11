package com.zjj.fishPlugin.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 应用级全局阅读状态（所有项目共享）
 * 存储在 PhpStorm 全局配置目录下，切换项目不丢失
 * 首次使用时的默认值从旧项目 assign.cfd_api 迁移而来
 */
@Service
@State(name = "NovelGlobalConfig", storages = @Storage("novel-global.xml"))
public final class NovelGlobalConfig implements PersistentStateComponent<NovelGlobalConfig.State> {

    public static class State {
        public String novelPath = "C:\\Users\\Administrator\\Documents\\江山如此多娇.txt";
        public String lineNumber = "80 字";
        public String autoPageTime = "4000";
        public int currentChapterIndex = 31;
        public int currentPageIndex = 2;
    }

    private State state = new State();

    public static NovelGlobalConfig getInstance() {
        return ApplicationManager.getApplication().getService(NovelGlobalConfig.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}