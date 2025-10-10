package com.zjj.fishPlugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.zjj.fishPlugin.factory.AutoPageFactory;
import com.zjj.fishPlugin.ui.ReadUI;

import javax.swing.*;
import java.util.List;

/**
 * Created by zhongjiajie on 2025/10/9 10:25.
 */
public class Config {
    public static ReadUI readUI;
    public static List<String> chapters;               // 所有章节标题
    public static List<List<String>> chapterPages = null;     // 每章分页内容
    public static int currentChapterIndex;             // 当前章节
    public static int currentPageIndex;                // 当前章节的页码
    public static String LineNumber;                   // 每页字数
    public static String novelPath;                    // 小说路径
    public static String autoPageTime;                 //自动翻页时间
    public static StatusBar getStatusBar() {
        // 获取当前工程项目
        Project project = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0];
        return WindowManager.getInstance().getStatusBar(project);
    }

    public static void displayNovel(){
        StatusBar statusBar = getStatusBar();
        if (statusBar != null) {
            statusBar.setInfo(Config.chapterPages.get(Config.currentChapterIndex).get(Config.currentPageIndex));
        }
    }

    public static void nextPage(){
        StatusBar statusBar = getStatusBar();
        if(Config.chapterPages == null){
            JOptionPane.showMessageDialog(null, "小说为空，前先去选择小说");
            if(AutoPageFactory.NovelStatusWidget.autoPageTimer.isRunning()){
                AutoPageFactory.NovelStatusWidget.autoPageTimer.stop();
            }
            AutoPageFactory.NovelStatusWidget.isAutoPaging = false;
            return;
        }
        if (Config.currentPageIndex + 1 < Config.chapterPages.get(Config.currentChapterIndex).size()) {
            Config.currentPageIndex++;
            statusBar.setInfo(Config.chapterPages.get(Config.currentChapterIndex).get(Config.currentPageIndex));
        }else{
            //跳到下一章第一页
            Config.currentPageIndex = 0;
            Config.currentChapterIndex++;
            statusBar.setInfo(Config.chapterPages.get(Config.currentChapterIndex).get(Config.currentPageIndex));
        }
    }

    public static void prePage(){
        StatusBar statusBar = getStatusBar();
        if (Config.currentPageIndex > 0) {
            Config.currentPageIndex--;
            statusBar.setInfo(Config.chapterPages.get(Config.currentChapterIndex).get(Config.currentPageIndex));
        }
        if(Config.currentChapterIndex - 1 >= 0 && Config.currentPageIndex - 1 < 0){
            Config.currentChapterIndex--;
            //获取上一章的页数大小，跳到上一章的最后一页
            int size = Config.chapterPages.get(Config.currentChapterIndex).size();
            Config.currentPageIndex = size - 1;
            statusBar.setInfo(Config.chapterPages.get(Config.currentChapterIndex).get(Config.currentPageIndex));
        }
    }
}
