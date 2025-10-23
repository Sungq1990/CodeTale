package com.zjj.fishPlugin.factory;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import com.zjj.fishPlugin.service.NovelService;
import com.zjj.fishPlugin.ui.ReadUI;
import com.zjj.fishPlugin.ui.SettingUI;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.universalchardet.UniversalDetector;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhongjiajie on 2025/10/9 10:20.
 */
public class SettingFactory implements SearchableConfigurable {
    private SettingUI settingUI;
    
    private NovelService getNovelService() {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        return NovelService.getInstance(project);
    }
    
    private SettingUI getSettingUI() {
        if (settingUI == null) {
            settingUI = new SettingUI(getNovelService());
        }
        return settingUI;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "test.id";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "test-config";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return getSettingUI().getComponent();
    }

    @Override
    public boolean isModified() {
        NovelService novelService = getNovelService();
        SettingUI ui = getSettingUI();
        if(!Objects.equals(novelService.getNovelPath(), ui.getTextField().getText())){
            return true;
        }
        if(!Objects.equals(novelService.getLineNumber(), ui.getSelectedCharsPerLine())){
            return true;
        }
        if(!Objects.equals(novelService.getAutoPageTime(), ui.getTimeField().getText().trim())){
            return true;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        NovelService novelService = getNovelService();
        SettingUI ui = getSettingUI();
        String url = ui.getTextField().getText();
        novelService.setLineNumber(ui.getSelectedCharsPerLine());
        novelService.setAutoPageTime(ui.getTimeField().getText().trim());
        if(!Objects.equals(novelService.getNovelPath(), ui.getTextField().getText())){
            novelService.setNovelPath(url);
            this.loadNovelWithPagination(novelService, url);
        }
        ReadUI readUI = novelService.getReadUI();
        readUI.loadChapterPage();
    }

    // 自动加载上次的小说
    public void autoLoadLastNovel() {
        NovelService novelService = getNovelService();
        if (novelService.getNovelPath() != null && !novelService.getNovelPath().isEmpty()) {
            try {
                // 使用isAutoLoad=true来保持阅读进度
                this.loadNovelWithPagination(novelService, novelService.getNovelPath(), true);
                // 恢复阅读进度
                novelService.displayNovel();
                // 更新ReadUI的章节列表
                ReadUI readUI = novelService.getReadUI();
                readUI.loadChapterPage();
            } catch (Exception e) {
                // 如果加载失败，清空状态
                novelService.setNovelPath("");
                novelService.setCurrentChapterIndex(0);
                novelService.setCurrentPageIndex(0);
            }
        }
    }

    private String preprocessText(String text) {
        if (text == null) return "";
        return text
                // 去掉BOM符号（有些UTF-8文件开头会带）
                .replace("\uFEFF", "")
                // 替换空格
                .replaceAll(" +", "")
                // 替换多个换行为空行
                .replaceAll("[\\r\\n]+", "")
                // 去掉每行开头结尾的空格
                .replaceAll("(?m)^\\s+|\\s+$", "")
                .trim();
    }


    /**
     * 自动检测文件格式并转换
     * @param file
     * @return
     * @throws IOException
     */
    private String detectCharset(File file) throws IOException {
        byte[] buf = new byte[4096];
        try (FileInputStream fis = new FileInputStream(file)) {
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            detector.reset();
            return encoding != null ? encoding : "UTF-8";
        }
    }

    /**
     * 按行数分页
     */
    private List<String> splitByLineCount(String text, int linesPerPage) {
        List<String> result = new ArrayList<>();
        String[] allLines = text.split("\\r?\\n"); // 按行切分
        StringBuilder page = new StringBuilder();
        int count = 0;

        for (String line : allLines) {
            page.append(line).append("\n");
            count++;
            if (count == linesPerPage) {
                result.add(page.toString());
                page.setLength(0);
                count = 0;
            }
        }

        // 最后一页如果有残留
        if (page.length() > 0) {
            result.add(page.toString());
        }

        return result;
    }

    /**
     * 按字数分页
     */
    private List<String> splitByCharCount(String text, int charsPerPage) {
        List<String> result = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += charsPerPage) {
            int end = Math.min(i + charsPerPage, length);
            result.add(text.substring(i, end));
        }
        return result;
    }

    public void loadNovelWithPagination(NovelService novelService, String filePath) {
        loadNovelWithPagination(novelService, filePath, false);
    }
    
    public void loadNovelWithPagination(NovelService novelService, String filePath, boolean isAutoLoad) {
        try {
            File file = new File(filePath);
            String charset = detectCharset(file);
            int charsPerPage = Integer.parseInt(novelService.getLineNumber().substring(0, 2));

            List<String> chapters = new ArrayList<>();
            List<List<String>> chapterPages = new ArrayList<>();
            StringBuilder currentChapter = new StringBuilder();
            String currentTitle = null;

            Pattern chapterPattern = Pattern.compile("^第.{1,10000}[章回卷篇节].*");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), Charset.forName(charset)))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    Matcher matcher = chapterPattern.matcher(line);
                    if (matcher.matches()) {
                        // 保存上一章的分页
                        if (currentTitle != null) {
                            chapters.add(currentTitle);
                            chapterPages.add(paginate(currentChapter.toString(), charsPerPage, currentTitle));
                        }
                        currentTitle = line;
                        currentChapter.setLength(0);
                    } else {
                        currentChapter.append(line).append("\n");
                    }
                }

                // 保存最后一章
                if (currentTitle != null) {
                    chapters.add(currentTitle);
                    chapterPages.add(paginate(currentChapter.toString(), charsPerPage, currentTitle));
                }
            }

            // 如果没匹配章节，把整本书当一章
            if (chapters.isEmpty()) {
                StringBuilder whole = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), Charset.forName(charset)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        whole.append(line).append("\n");
                    }
                }
                chapters.add("全文");
                chapterPages.add(paginate(currentChapter.toString(), charsPerPage, currentTitle));
            }

            // 保存到NovelService
            novelService.setChapters(chapters);
            novelService.setChapterPages(chapterPages);
            
            // 只有在手动选择新小说时才重置进度，自动加载时保持原进度
            if (!isAutoLoad) {
                novelService.setCurrentChapterIndex(0);
                novelService.setCurrentPageIndex(0);
            }

            // 显示当前页面
            if (!chapterPages.isEmpty() && !chapterPages.get(0).isEmpty()) {
                novelService.displayNovel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将一章内容拆分页
     */
    private List<String> paginate(String content, int charsPerPage, @Nullable String chapterTitle) throws IOException {
        List<String> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();

        // 如果有章节标题，则在第一页开头加上标题并换行
        if (chapterTitle != null && !chapterTitle.isEmpty()) {
            page.append(chapterTitle).append("---");
        }

        try (BufferedReader reader = new BufferedReader(new java.io.StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 去掉段首空格
                line = line.replaceAll("^[　\\s]+", "");
                if (line.trim().isEmpty()) continue;

                for (char c : line.toCharArray()) {
                    page.append(c);
                    if (page.length() >= charsPerPage) {
                        pages.add(page.toString());
                        page.setLength(0);
                    }
                }
                // 换行符算作空格
                page.append(' ');
            }
        }

        if (page.length() > 0) pages.add(page.toString());
        return pages;
    }
}
