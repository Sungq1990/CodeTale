package com.sgq.fishPlugin.ui;

import com.intellij.openapi.ui.ComboBox;
import com.sgq.fishPlugin.service.NovelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;

/**
 * Created by sgq on 2025/9/30 16:31.
 */
public class SettingUI {
    private final String[] items = {"50 字", "60 字", "70 字", "80 字"};
    private static final Logger log = LoggerFactory.getLogger(SettingUI.class);
    private JTextField textField;
    private JButton urlButton;
    private JLabel urlLabel;
    private JPanel settingPanel;
    private JPanel mainPanel;
    private ComboBox comboBox;
    private JLabel number;
    private JTextField timeField;
    private final NovelService novelService;

    public SettingUI(NovelService novelService) {
        this.novelService = novelService;
        comboBox.setModel(new DefaultComboBoxModel<>(items));
        comboBox.setSelectedItem(novelService.getLineNumber() != null ? novelService.getLineNumber() : "50 字"); // 默认选中
        textField.setText(novelService.getNovelPath());
        timeField.setText(novelService.getAutoPageTime());
        urlButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.showOpenDialog(settingPanel);
            File file = jFileChooser.getSelectedFile();
            if (file == null){
                JOptionPane.showMessageDialog(null, "请选择小说");
                return;
            }
            textField.setText(file.getPath());
        });
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public JTextField getTextField() {
        return textField;
    }

    public JTextField getTimeField() {
        return timeField;
    }

    public String getSelectedCharsPerLine() {
        return (String) comboBox.getSelectedItem();
    }
}
