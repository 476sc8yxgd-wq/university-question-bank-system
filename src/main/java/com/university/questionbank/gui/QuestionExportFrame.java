package com.university.questionbank.gui;

import com.university.questionbank.model.*;
import com.university.questionbank.service.QuestionService;
import com.university.questionbank.util.ExportUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class QuestionExportFrame extends JInternalFrame {
    private User currentUser;
    private QuestionService questionService;
    
    // 界面组件
    private JTextField keywordField;
    private JComboBox<QuestionCategory> categoryComboBox;
    private JComboBox<QuestionDifficulty> difficultyComboBox;
    private JComboBox<String> questionTypeComboBox;
    private JComboBox<String> exportFormatComboBox;
    private JTextField exportPathField;
    private JButton browseButton;
    private JButton exportButton;
    private JButton cancelButton;

    public QuestionExportFrame(User currentUser) {
        super("题目导出", true, true, true, true);
        this.currentUser = currentUser;
        this.questionService = new QuestionService();
        initUI();
        loadCategoriesAndDifficulties();

        // 确认所有角色都可以使用导出功能
        System.out.println("导出功能已开启 - 用户角色: " + currentUser.getRole().getRoleName() + " (roleId: " + currentUser.getRole().getRoleId() + ")");
    }

    private void initUI() {
        setSize(850, 650);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        // 创建导出设置面板
        JPanel exportPanel = new JPanel(new GridBagLayout());
        exportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIStyle.DIVIDER_COLOR), "导出设置", 0, 0, UIStyle.SUBHEADING_FONT));
        exportPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        exportPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 导出内容筛选
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel filterLabel = UIComponentFactory.createLabel("导出内容筛选:");
        filterLabel.setFont(UIStyle.SUBHEADING_FONT);
        exportPanel.add(filterLabel, gbc);

        // 增加标题与内容之间的间距
        gbc.insets = new Insets(8, 15, 12, 15);

        gbc.gridwidth = 1;

        // 关键字搜索
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(12, 15, 8, 15);  // 标签的间距
        exportPanel.add(UIComponentFactory.createLabel("关键字:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 8, 15);  // 输入框的间距
        keywordField = UIComponentFactory.createTextField();
        gbc.weightx = 1;
        exportPanel.add(keywordField, gbc);
        
        // 题目分类
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 15, 8, 15);
        exportPanel.add(UIComponentFactory.createLabel("分类:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 8, 15);
        categoryComboBox = UIComponentFactory.createComboBox();
        exportPanel.add(categoryComboBox, gbc);

        // 题目难度
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(12, 15, 8, 15);
        exportPanel.add(UIComponentFactory.createLabel("难度:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 8, 15);
        difficultyComboBox = UIComponentFactory.createComboBox();
        exportPanel.add(difficultyComboBox, gbc);

        // 题目类型
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(12, 15, 8, 15);
        exportPanel.add(UIComponentFactory.createLabel("类型:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 8, 15);
        String[] questionTypes = {"", "单选题", "多选题", "判断题", "填空题", "简答题"};
        questionTypeComboBox = UIComponentFactory.createComboBox(questionTypes);
        exportPanel.add(questionTypeComboBox, gbc);

        // 导出格式
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(12, 15, 8, 15);
        exportPanel.add(UIComponentFactory.createLabel("导出格式:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 8, 15);
        String[] exportFormats = {"Word (.docx)", "Excel (.xlsx)", "文本文件 (.txt)"};
        exportFormatComboBox = UIComponentFactory.createComboBox(exportFormats);
        exportPanel.add(exportFormatComboBox, gbc);

        // 导出路径
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(12, 15, 8, 15);
        exportPanel.add(UIComponentFactory.createLabel("导出路径:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 8, 8);
        exportPathField = UIComponentFactory.createTextField();
        exportPathField.setEditable(false);
        exportPanel.add(exportPathField, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(12, 5, 8, 15);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        browseButton = UIComponentFactory.createSecondaryButton("浏览");
        browseButton.addActionListener(new BrowseButtonListener());
        exportPanel.add(browseButton, gbc);
        
        // 重置 fill 为 HORIZONTAL 用于其他组件
        gbc.fill = GridBagConstraints.HORIZONTAL;

        mainPanel.add(exportPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        exportButton = UIComponentFactory.createPrimaryButton("导出");
        exportButton.addActionListener(new ExportButtonListener());
        buttonPanel.add(exportButton);
        
        cancelButton = UIComponentFactory.createSecondaryButton("取消");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    // 加载分类和难度数据
    private void loadCategoriesAndDifficulties() {
        try {
            // 添加空白选项
            categoryComboBox.addItem(new QuestionCategory(-1, "", ""));
            difficultyComboBox.addItem(new QuestionDifficulty(-1, "", ""));

            // 设置分类下拉框的自定义渲染器
            categoryComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof QuestionCategory) {
                        QuestionCategory category = (QuestionCategory) value;
                        if (category.getCategoryId() == -1) {
                            setText("全部");
                        } else {
                            setText(category.getCategoryName());
                        }
                    }
                    return this;
                }
            });

            // 设置难度下拉框的自定义渲染器
            difficultyComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof QuestionDifficulty) {
                        QuestionDifficulty difficulty = (QuestionDifficulty) value;
                        if (difficulty.getDifficultyId() == -1) {
                            setText("全部");
                        } else {
                            setText(difficulty.getDifficultyLevel());
                        }
                    }
                    return this;
                }
            });

            // 加载分类
            List<QuestionCategory> categories = questionService.getAllCategories();
            for (QuestionCategory category : categories) {
                categoryComboBox.addItem(category);
            }

            // 加载难度
            List<QuestionDifficulty> difficulties = questionService.getAllDifficulties();
            for (QuestionDifficulty difficulty : difficulties) {
                difficultyComboBox.addItem(difficulty);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载分类和难度数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 浏览按钮监听器
    private class BrowseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择导出路径");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            // 使用showOpenDialog而不是showSaveDialog来选择目录
            int userSelection = fileChooser.showOpenDialog(QuestionExportFrame.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                exportPathField.setText(fileToSave.getAbsolutePath());
            }
        }
    }

    // 导出按钮监听器
    private class ExportButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // 验证导出路径
                if (exportPathField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(QuestionExportFrame.this, "请选择导出路径", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // 获取导出条件
                String keyword = keywordField.getText();
                QuestionCategory selectedCategory = (QuestionCategory) categoryComboBox.getSelectedItem();
                Integer categoryId = selectedCategory.getCategoryId() == -1 ? null : selectedCategory.getCategoryId();
                
                QuestionDifficulty selectedDifficulty = (QuestionDifficulty) difficultyComboBox.getSelectedItem();
                Integer difficultyId = selectedDifficulty.getDifficultyId() == -1 ? null : selectedDifficulty.getDifficultyId();
                
                String questionType = (String) questionTypeComboBox.getSelectedItem();
                if (questionType.isEmpty()) {
                    questionType = null;
                }
                
                // 获取导出格式
                String exportFormat = (String) exportFormatComboBox.getSelectedItem();
                String fileExtension = exportFormat.contains("Word") ? ".docx" : 
                                      exportFormat.contains("Excel") ? ".xlsx" : ".txt";
                
                // 执行搜索
                List<Question> questions = questionService.searchQuestions(keyword, categoryId, difficultyId, questionType);
                
                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(QuestionExportFrame.this, "没有找到符合条件的题目", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                // 导出文件
                String exportPath = exportPathField.getText();
                String fileName = "题目导出_" + System.currentTimeMillis() + fileExtension;
                String fullPath = exportPath + File.separator + fileName;
                
                // 根据选择的格式执行导出
                String selectedFormat = (String) exportFormatComboBox.getSelectedItem();
                if (selectedFormat.contains("Word")) {
                    ExportUtil.exportToWord(questions, fullPath);
                } else if (selectedFormat.contains("Excel")) {
                    ExportUtil.exportToExcel(questions, fullPath);
                } else if (selectedFormat.contains("文本文件")) {
                    ExportUtil.exportToTxt(questions, fullPath);
                }
                
                // 显示导出结果
                JOptionPane.showMessageDialog(QuestionExportFrame.this, 
                        "导出成功！\n共导出 " + questions.size() + " 道题目\n文件路径: " + fullPath, 
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(QuestionExportFrame.this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}