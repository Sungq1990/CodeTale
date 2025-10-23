package com.zjj.fishPlugin.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.zjj.fishPlugin.factory.AutoPageFactory;
import com.zjj.fishPlugin.ui.ReadUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * 每个项目独立的小说服务
 * Created by zhongjiajie on 2025/1/27
 */
@Service
@State(name = "NovelService", storages = @Storage("novel-service.xml"))
public final class NovelService implements PersistentStateComponent<NovelService.NovelState> {
    
    private final Project project;
    private ReadUI readUI;
    private List<String> chapters;               // 所有章节标题
    private List<List<String>> chapterPages = null;     // 每章分页内容
    private int currentChapterIndex;             // 当前章节
    private int currentPageIndex;                // 当前章节的页码
    private String lineNumber;                   // 每页字数
    private String novelPath;                    // 小说路径
    private String autoPageTime;                 // 自动翻页时间

    public NovelService(Project project) {
        this.project = project;
    }

    public static NovelService getInstance(Project project) {
        return project.getService(NovelService.class);
    }

    public StatusBar getStatusBar() {
        return WindowManager.getInstance().getStatusBar(project);
    }

    public void displayNovel() {
        StatusBar statusBar = getStatusBar();
        if (statusBar != null && chapterPages != null && 
            currentChapterIndex < chapterPages.size() && 
            currentPageIndex < chapterPages.get(currentChapterIndex).size()) {
            statusBar.setInfo(chapterPages.get(currentChapterIndex).get(currentPageIndex));
        }
    }

    public void nextPage() {
        StatusBar statusBar = getStatusBar();
        if (chapterPages == null) {
            JOptionPane.showMessageDialog(null, "小说为空，请先去选择小说");
            stopAutoPage();
            return;
        }
        
        if (currentPageIndex + 1 < chapterPages.get(currentChapterIndex).size()) {
            currentPageIndex++;
            statusBar.setInfo(chapterPages.get(currentChapterIndex).get(currentPageIndex));
        } else {
            // 跳到下一章第一页
            if (currentChapterIndex + 1 < chapterPages.size()) {
                currentPageIndex = 0;
                currentChapterIndex++;
                statusBar.setInfo(chapterPages.get(currentChapterIndex).get(currentPageIndex));
            }
        }
        
        // 自动保存进度
        saveProgress();
    }

    public void prePage() {
        StatusBar statusBar = getStatusBar();
        if (currentPageIndex > 0) {
            currentPageIndex--;
            statusBar.setInfo(chapterPages.get(currentChapterIndex).get(currentPageIndex));
        } else if (currentChapterIndex - 1 >= 0) {
            // 跳到上一章的最后一页
            currentChapterIndex--;
            int size = chapterPages.get(currentChapterIndex).size();
            currentPageIndex = size - 1;
            statusBar.setInfo(chapterPages.get(currentChapterIndex).get(currentPageIndex));
        }
        
        // 自动保存进度
        saveProgress();
    }

    private void stopAutoPage() {
        if (AutoPageFactory.NovelStatusWidget.autoPageTimer != null && 
            AutoPageFactory.NovelStatusWidget.autoPageTimer.isRunning()) {
            AutoPageFactory.NovelStatusWidget.autoPageTimer.stop();
        }
        AutoPageFactory.NovelStatusWidget.isAutoPaging = false;
    }

    // Getters and Setters
    public ReadUI getReadUI() {
        if (readUI == null) {
            readUI = new ReadUI(this);
        }
        return readUI;
    }

    public void setReadUI(ReadUI readUI) {
        this.readUI = readUI;
    }

    public List<String> getChapters() {
        return chapters;
    }

    public void setChapters(List<String> chapters) {
        this.chapters = chapters;
    }

    public List<List<String>> getChapterPages() {
        return chapterPages;
    }

    public void setChapterPages(List<List<String>> chapterPages) {
        this.chapterPages = chapterPages;
    }

    public int getCurrentChapterIndex() {
        return currentChapterIndex;
    }

    public void setCurrentChapterIndex(int currentChapterIndex) {
        this.currentChapterIndex = currentChapterIndex;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        this.currentPageIndex = currentPageIndex;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getNovelPath() {
        return novelPath;
    }

    public void setNovelPath(String novelPath) {
        this.novelPath = novelPath;
    }

    public String getAutoPageTime() {
        return autoPageTime;
    }

    public void setAutoPageTime(String autoPageTime) {
        this.autoPageTime = autoPageTime;
    }

    public Project getProject() {
        return project;
    }

    // 持久化状态类
    public static class NovelState {
        public String novelPath = "";
        public String lineNumber = "60 字";
        public String autoPageTime = "5000";
        public int currentChapterIndex = 0;
        public int currentPageIndex = 0;
    }

    private NovelState state = new NovelState();

    @Override
    public @Nullable NovelState getState() {
        // 将当前状态保存到state对象
        state.novelPath = novelPath;
        state.lineNumber = lineNumber;
        state.autoPageTime = autoPageTime;
        state.currentChapterIndex = currentChapterIndex;
        state.currentPageIndex = currentPageIndex;
        return state;
    }

    @Override
    public void loadState(@NotNull NovelState state) {
        // 从state对象恢复状态
        XmlSerializerUtil.copyBean(state, this.state);
        this.novelPath = state.novelPath;
        this.lineNumber = state.lineNumber;
        this.autoPageTime = state.autoPageTime;
        this.currentChapterIndex = state.currentChapterIndex;
        this.currentPageIndex = state.currentPageIndex;
    }

    // 自动加载上次的小说
    public void autoLoadLastNovel() {
        if (novelPath != null && !novelPath.isEmpty()) {
            try {
                // 这里需要调用SettingFactory的loadNovelWithPagination方法
                // 但为了避免循环依赖，我们直接在这里实现加载逻辑
                loadNovelFromPath(novelPath);
            } catch (Exception e) {
                // 如果加载失败，清空状态
                novelPath = "";
                currentChapterIndex = 0;
                currentPageIndex = 0;
            }
        }
    }

    // 从路径加载小说（简化版本，避免循环依赖）
    private void loadNovelFromPath(String filePath) {
        // 这里可以调用SettingFactory的loadNovelWithPagination方法
        // 或者实现一个简化的加载逻辑
        // 暂时留空，让SettingFactory来处理
    }

    // 保存阅读进度
    private void saveProgress() {
        // IntelliJ IDEA会自动调用getState()方法来保存状态
        // 这里我们只需要确保状态是最新的即可
        // 状态会在适当的时机自动保存
    }
}
