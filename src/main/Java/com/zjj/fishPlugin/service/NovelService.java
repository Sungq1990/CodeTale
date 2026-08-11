package com.zjj.fishPlugin.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.zjj.fishPlugin.factory.AutoPageFactory;
import com.zjj.fishPlugin.factory.BossKeyFactory;
import com.zjj.fishPlugin.ui.ReadUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 每个项目独立的小说服务，进度由全局 NovelGlobalConfig 持久化（所有项目共享）
 * Created by zhongjiajie on 2025/1/27
 */
@Service
public final class NovelService {

    private final Project project;
    private ReadUI readUI;
    private List<String> chapters;               // 所有章节标题
    private List<ChapterInfo> chapterInfos;       // 章节所在文件行号索引
    private List<String> currentChapterPages;     // 当前章节分页缓存
    private int cachedChapterIndex = -1;
    private int currentChapterIndex;             // 当前章节
    private int currentPageIndex;                // 当前章节的页码
    private String lineNumber;                   // 每页字数
    private String novelPath;                    // 小说路径
    private String charset;                      // 小说文件编码
    private String autoPageTime;                 // 自动翻页时间

    private boolean bossHidden = false;

    public NovelService(Project project) {
        this.project = project;
        // 从全局配置恢复状态（跨项目共享）
        NovelGlobalConfig.State global = NovelGlobalConfig.getInstance().getState();
        if (global != null) {
            this.novelPath = global.novelPath;
            this.lineNumber = global.lineNumber;
            this.autoPageTime = global.autoPageTime;
            this.currentChapterIndex = global.currentChapterIndex;
            this.currentPageIndex = global.currentPageIndex;
            clearCurrentChapterCache();
        }
    }

    public static NovelService getInstance(Project project) {
        return project.getService(NovelService.class);
    }

    public StatusBar getStatusBar() {
        return WindowManager.getInstance().getStatusBar(project);
    }

    /**
     * 是否为老板键隐藏状态
     */
    public boolean isBossHidden() {
        return bossHidden;
    }

    /**
     * 切换老板键：隐藏/恢复当前小说文本
     */
    public void toggleBossHide() {
        bossHidden = !bossHidden;
        List<String> pages = getCurrentChapterPages();
        if (pages != null && currentPageIndex < pages.size()) {
            setStatusInfo(pages.get(currentPageIndex));
        }
        StatusBar statusBar = getStatusBar();
        if (statusBar != null) {
            statusBar.updateWidget(BossKeyFactory.WIDGET_ID);
        }
    }

    /**
     * 显示当前页（受老板键隐藏状态影响）
     */
    private void setStatusInfo(String text) {
        StatusBar statusBar = getStatusBar();
        if (statusBar != null) {
            statusBar.setInfo(bossHidden ? "" : text);
        }
    }

    public void displayNovel() {
        List<String> pages = getCurrentChapterPages();
        if (pages != null && currentPageIndex < pages.size()) {
            setStatusInfo(pages.get(currentPageIndex));
        }
    }

    public void nextPage() {
        List<String> pages = getCurrentChapterPages();
        if (pages == null) {
            JOptionPane.showMessageDialog(null, "小说为空，请先去选择小说");
            stopAutoPage();
            return;
        }
        
        if (currentPageIndex + 1 < pages.size()) {
            currentPageIndex++;
            setStatusInfo(pages.get(currentPageIndex));
        } else {
            // 跳到下一章第一页
            if (currentChapterIndex + 1 < chapterInfos.size()) {
                currentPageIndex = 0;
                currentChapterIndex++;
                pages = getCurrentChapterPages();
                if (pages != null && !pages.isEmpty()) {
                    setStatusInfo(pages.get(currentPageIndex));
                }
            }
        }
        
        // 自动保存进度
        saveProgress();
    }

    public void prePage() {
        List<String> pages = getCurrentChapterPages();
        if (pages == null) {
            JOptionPane.showMessageDialog(null, "小说为空，请先去选择小说");
            stopAutoPage();
            return;
        }
        if (currentPageIndex > 0) {
            currentPageIndex--;
            setStatusInfo(pages.get(currentPageIndex));
        } else if (currentChapterIndex - 1 >= 0) {
            // 跳到上一章的最后一页
            currentChapterIndex--;
            pages = getCurrentChapterPages();
            if (pages == null || pages.isEmpty()) {
                return;
            }
            int size = pages.size();
            currentPageIndex = size - 1;
            setStatusInfo(pages.get(currentPageIndex));
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

    public List<ChapterInfo> getChapterInfos() {
        return chapterInfos;
    }

    public void setChapterInfos(List<ChapterInfo> chapterInfos) {
        this.chapterInfos = chapterInfos;
        clearCurrentChapterCache();
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
        clearCurrentChapterCache();
    }

    public String getNovelPath() {
        return novelPath;
    }

    public void setNovelPath(String novelPath) {
        this.novelPath = novelPath;
        clearCurrentChapterCache();
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
        clearCurrentChapterCache();
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

    // 将当前状态持久化到全局配置
    public void persistToGlobal() {
        NovelGlobalConfig.State global = NovelGlobalConfig.getInstance().getState();
        if (global == null) {
            return;
        }
        global.novelPath = novelPath;
        global.lineNumber = lineNumber;
        global.autoPageTime = autoPageTime;
        global.currentChapterIndex = currentChapterIndex;
        global.currentPageIndex = currentPageIndex;
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

    private List<String> getCurrentChapterPages() {
        if (chapterInfos == null || chapterInfos.isEmpty()) {
            return null;
        }
        if (currentChapterIndex < 0 || currentChapterIndex >= chapterInfos.size()) {
            currentChapterIndex = 0;
            currentPageIndex = 0;
        }
        if (cachedChapterIndex == currentChapterIndex && currentChapterPages != null) {
            return currentChapterPages;
        }

        try {
            int charsPerPage = getCharsPerPage();
            ChapterInfo chapterInfo = chapterInfos.get(currentChapterIndex);
            currentChapterPages = readChapterPages(chapterInfo, charsPerPage);
            cachedChapterIndex = currentChapterIndex;
            if (currentPageIndex >= currentChapterPages.size()) {
                currentPageIndex = Math.max(currentChapterPages.size() - 1, 0);
            }
            return currentChapterPages;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> readChapterPages(ChapterInfo chapterInfo, int charsPerPage) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(novelPath), Charset.forName(getCharsetOrDefault())))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber < chapterInfo.getStartLine()) {
                    continue;
                }
                if (lineNumber > chapterInfo.getEndLine()) {
                    break;
                }
                content.append(line).append("\n");
            }
        }
        return paginate(content.toString(), charsPerPage, chapterInfo.getTitle());
    }

    private int getCharsPerPage() {
        if (lineNumber == null || lineNumber.trim().isEmpty()) {
            return 60;
        }
        String number = lineNumber.replaceAll("\\D+", "");
        if (number.isEmpty()) {
            return 60;
        }
        int charsPerPage = Integer.parseInt(number);
        if (charsPerPage <= 0) {
            return 60;
        }
        return charsPerPage;
    }

    private String getCharsetOrDefault() {
        if (charset == null || charset.trim().isEmpty()) {
            return "UTF-8";
        }
        return charset;
    }

    private List<String> paginate(String content, int charsPerPage, @Nullable String chapterTitle) throws IOException {
        List<String> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();

        if (chapterTitle != null && !chapterTitle.isEmpty()) {
            page.append(chapterTitle).append("---");
        }

        try (BufferedReader reader = new BufferedReader(new java.io.StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("^[　\\s]+", "");
                if (line.trim().isEmpty()) continue;

                for (char c : line.toCharArray()) {
                    page.append(c);
                    if (page.length() >= charsPerPage) {
                        pages.add(page.toString());
                        page.setLength(0);
                    }
                }
                page.append(' ');
            }
        }

        if (page.length() > 0) pages.add(page.toString());
        return pages;
    }

    private void clearCurrentChapterCache() {
        currentChapterPages = null;
        cachedChapterIndex = -1;
    }

    // 保存阅读进度
    private void saveProgress() {
        persistToGlobal();
    }

    public static class ChapterInfo {
        private final String title;
        private final int startLine;
        private final int endLine;

        public ChapterInfo(String title, int startLine, int endLine) {
            this.title = title;
            this.startLine = startLine;
            this.endLine = endLine;
        }

        public String getTitle() {
            return title;
        }

        public int getStartLine() {
            return startLine;
        }

        public int getEndLine() {
            return endLine;
        }
    }
}
