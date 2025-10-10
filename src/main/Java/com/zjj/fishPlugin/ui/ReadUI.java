package com.zjj.fishPlugin.ui;

import com.zjj.fishPlugin.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by zhongjiajie on 2025/10/9 10:11.
 */
public class ReadUI {
    private JPanel mainPanel;
    private JList bookList;
    private JTextField jumpPage;
    private JButton preButton;
    private JButton nextButton;

    private int currentChapterPage = 0; // 当前章节页码
    private static final int CHAPTERS_PER_PAGE = 30; // 每页显示多少个章节

    public ReadUI() {
        preButton.addActionListener(e -> prevChapterPage());
        nextButton.addActionListener(e -> nextChapterPage());
        // 添加页码跳转监听
        jumpPage.addActionListener(e -> jumpToPage());

        //监听鼠标双击
        bookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 判断是否是双击
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    handleMouseDoubleClick(e.getPoint());
                }
            }
        });
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    // 保证单例
    public static ReadUI getReadUI() {
        if (Config.readUI == null) {
            Config.readUI = new ReadUI();
        }
        return Config.readUI;
    }

    public JList getBookList() {
        return bookList;
    }

    /**
     * 加载章节列表
     */
    public void loadChapterPage() {
        if (Config.chapters == null || Config.chapters.isEmpty()) {
            bookList.setListData(new String[]{"请先去设置里选择小说"});
            return;
        }

        int totalChapters = Config.chapters.size();
        int fromIndex = currentChapterPage * CHAPTERS_PER_PAGE;
        int toIndex = Math.min(fromIndex + CHAPTERS_PER_PAGE, totalChapters);

        List<String> subList = Config.chapters.subList(fromIndex, toIndex);
        bookList.setListData(subList.toArray(new String[0]));
    }

    /**
     * 下一页
     */
    public void nextChapterPage() {
        int totalPages = (int) Math.ceil(Config.chapters.size() / (double) CHAPTERS_PER_PAGE);
        if (currentChapterPage < totalPages - 1) {
            currentChapterPage++;
            loadChapterPage();
        } else {
            JOptionPane.showMessageDialog(mainPanel, "已经是最后一页");
        }
    }

    /**
     * 上一页
     */
    public void prevChapterPage() {
        if (currentChapterPage > 0) {
            currentChapterPage--;
            loadChapterPage();
        } else {
            JOptionPane.showMessageDialog(mainPanel, "已经是第一页");
        }
    }

    /**
     * 章节列表跳转到对应页码
     */
    public void jumpToPage() {
        String text = jumpPage.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "请输入页码！");
            return;
        }

        try {
            int pageNum = Integer.parseInt(text);
            int totalPages = (int) Math.ceil(Config.chapters.size() / (double) CHAPTERS_PER_PAGE);

            if (pageNum < 1 || pageNum > totalPages) {
                JOptionPane.showMessageDialog(mainPanel, "页码超出范围，1 - " + totalPages);
                return;
            }

            currentChapterPage = pageNum - 1;
            loadChapterPage();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainPanel, "请输入正确的数字页码！");
        }
    }


    /**
     * 鼠标双击跳转到对应章节
     * @param point
     */
    public void handleMouseDoubleClick(Point point){
        int index = bookList.locationToIndex(point);
        if (index >= 0 && index < Config.chapters.size()) {
            // 更新当前章节索引
            Config.currentChapterIndex = this.currentChapterPage* CHAPTERS_PER_PAGE + index;
            //从章节开头开始
            Config.currentPageIndex = 0;
            //展示小说
            Config.displayNovel();
        }
    }
}
