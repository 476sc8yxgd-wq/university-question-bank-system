package com.university.questionbank.gui;

import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;

public class SystemSettingsFrame extends JInternalFrame {
    private static final String SETTINGS_FILE = "system.properties";
    
    // 设置项
    private JTextField backupPathField;
    private JCheckBox autoBackupCheckBox;
    private JSpinner backupIntervalSpinner;
    private JCheckBox exportIncludeAnswersCheckBox;
    private JCheckBox exportIncludeCategoriesCheckBox;
    
    private Properties settings;
    
    public SystemSettingsFrame() {
        super("系统设置", true, true, true, true);
        this.settings = loadSettings();
        initUI();
        loadSettingsToUI();
    }
    
    private void initUI() {
        setSize(600, 500);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 标题
        JLabel titleLabel = new JLabel("系统设置");
        titleLabel.setFont(UIStyle.HEADING_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // 重置网格约束
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // 备份设置
        JLabel backupTitleLabel = new JLabel("备份设置");
        backupTitleLabel.setFont(UIStyle.HEADING_FONT);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        mainPanel.add(backupTitleLabel, gbc);
        
        // 备份路径
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(UIComponentFactory.createLabel("备份路径:"), gbc);
        
        gbc.gridx = 1;
        backupPathField = UIComponentFactory.createTextField();
        mainPanel.add(backupPathField, gbc);
        
        gbc.gridx = 2;
        JButton browseButton = UIComponentFactory.createSecondaryButton("浏览");
        browseButton.addActionListener(e -> browseBackupPath());
        mainPanel.add(browseButton, gbc);
        
        // 自动备份
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        autoBackupCheckBox = new JCheckBox("启用自动备份");
        autoBackupCheckBox.setFont(UIStyle.NORMAL_FONT);
        autoBackupCheckBox.addActionListener(e -> updateBackupIntervalEnabled());
        mainPanel.add(autoBackupCheckBox, gbc);
        
        // 备份间隔
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(UIComponentFactory.createLabel("备份间隔 (小时):"), gbc);
        
        gbc.gridx = 1;
        SpinnerModel intervalModel = new SpinnerNumberModel(24, 1, 168, 1);
        backupIntervalSpinner = new JSpinner(intervalModel);
        JComponent spinnerEditor = backupIntervalSpinner.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor)spinnerEditor).getTextField();
        tf.setFont(UIStyle.NORMAL_FONT);
        mainPanel.add(backupIntervalSpinner, gbc);
        
        // 导出设置
        JLabel exportTitleLabel = new JLabel("导出设置");
        exportTitleLabel.setFont(UIStyle.HEADING_FONT);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        mainPanel.add(exportTitleLabel, gbc);
        
        // 导出时包含答案
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        exportIncludeAnswersCheckBox = new JCheckBox("导出题目时包含答案");
        exportIncludeAnswersCheckBox.setFont(UIStyle.NORMAL_FONT);
        mainPanel.add(exportIncludeAnswersCheckBox, gbc);
        
        // 导出时包含分类
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        exportIncludeCategoriesCheckBox = new JCheckBox("导出题目时包含分类和难度");
        exportIncludeCategoriesCheckBox.setFont(UIStyle.NORMAL_FONT);
        mainPanel.add(exportIncludeCategoriesCheckBox, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        JButton saveButton = UIComponentFactory.createPrimaryButton("保存设置");
        saveButton.addActionListener(e -> saveSettings());
        buttonPanel.add(saveButton);
        
        JButton resetButton = UIComponentFactory.createSecondaryButton("重置为默认");
        resetButton.addActionListener(e -> resetToDefaults());
        buttonPanel.add(resetButton);
        
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
    }
    
    // 浏览备份路径
    private void browseBackupPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showDialog(this, "选择备份目录");
        if (result == JFileChooser.APPROVE_OPTION) {
            backupPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    // 更新备份间隔的启用状态
    private void updateBackupIntervalEnabled() {
        backupIntervalSpinner.setEnabled(autoBackupCheckBox.isSelected());
    }
    
    // 加载设置
    private Properties loadSettings() {
        Properties props = new Properties();
        File file = new File(SETTINGS_FILE);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "加载设置失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // 默认设置
            props.setProperty("backup.path", "./backups");
            props.setProperty("backup.auto", "false");
            props.setProperty("backup.interval", "24");
            props.setProperty("export.include.answers", "true");
            props.setProperty("export.include.categories", "true");
        }
        
        return props;
    }
    
    // 加载设置到UI
    private void loadSettingsToUI() {
        backupPathField.setText(settings.getProperty("backup.path"));
        autoBackupCheckBox.setSelected(Boolean.parseBoolean(settings.getProperty("backup.auto")));
        backupIntervalSpinner.setValue(Integer.parseInt(settings.getProperty("backup.interval")));
        exportIncludeAnswersCheckBox.setSelected(Boolean.parseBoolean(settings.getProperty("export.include.answers")));
        exportIncludeCategoriesCheckBox.setSelected(Boolean.parseBoolean(settings.getProperty("export.include.categories")));
        
        // 更新备份间隔的启用状态
        updateBackupIntervalEnabled();
    }
    
    // 保存设置
    private void saveSettings() {
        // 更新设置
        settings.setProperty("backup.path", backupPathField.getText());
        settings.setProperty("backup.auto", String.valueOf(autoBackupCheckBox.isSelected()));
        settings.setProperty("backup.interval", backupIntervalSpinner.getValue().toString());
        settings.setProperty("export.include.answers", String.valueOf(exportIncludeAnswersCheckBox.isSelected()));
        settings.setProperty("export.include.categories", String.valueOf(exportIncludeCategoriesCheckBox.isSelected()));
        
        // 保存到文件
        File file = new File(SETTINGS_FILE);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            settings.store(fos, "System Settings");
            JOptionPane.showMessageDialog(this, "设置保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存设置失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 重置为默认设置
    private void resetToDefaults() {
        backupPathField.setText("./backups");
        autoBackupCheckBox.setSelected(false);
        backupIntervalSpinner.setValue(24);
        exportIncludeAnswersCheckBox.setSelected(true);
        exportIncludeCategoriesCheckBox.setSelected(true);
        updateBackupIntervalEnabled();
    }
    
    // 获取系统设置
    public static Properties getSystemSettings() {
        Properties props = new Properties();
        File file = new File(SETTINGS_FILE);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("加载系统设置失败: " + e.getMessage());
            }
        } else {
            // 默认设置
            props.setProperty("backup.path", "./backups");
            props.setProperty("backup.auto", "false");
            props.setProperty("backup.interval", "24");
            props.setProperty("export.include.answers", "true");
            props.setProperty("export.include.categories", "true");
        }
        
        return props;
    }
}