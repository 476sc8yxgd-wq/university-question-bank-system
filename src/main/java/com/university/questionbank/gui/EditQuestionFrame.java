package com.university.questionbank.gui;

import com.university.questionbank.model.*;
import com.university.questionbank.service.QuestionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.awt.Insets;
import javax.swing.SwingWorker;

public class EditQuestionFrame extends JInternalFrame {
    private Question question;
    private User currentUser;
    private QuestionService questionService;
    
    // 界面组件
    private JTextField questionContentField;
    private JComboBox<String> questionTypeComboBox;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JTextField correctAnswerField;
    private JTextArea explanationArea;
    private JComboBox<QuestionCategory> categoryComboBox;
    private JComboBox<QuestionDifficulty> difficultyComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JProgressBar loadingProgressBar;
    private JLabel loadingLabel;
    private JPanel loadingPanel;

public EditQuestionFrame(Question question, User currentUser) {
    super("编辑题目", true, true, true, true);
    this.question = question;
    this.currentUser = currentUser;
    this.questionService = new QuestionService();
    initUI();
    loadData();
    // 异步加载分类和难度数据，避免阻塞UI
    loadCategoriesAndDifficultiesAsync();
}

    private void initUI() {
        setSize(600, 550);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        // 创建表单面板 - 使用 GridBagLayout 替代 GridLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 题目内容
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("题目内容:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        questionContentField = UIComponentFactory.createTextField();
        formPanel.add(questionContentField, gbc);
        row++;

        // 题目类型
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("题目类型:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        String[] questionTypes = {"单选题", "多选题", "判断题", "填空题", "简答题"};
        questionTypeComboBox = UIComponentFactory.createComboBox(questionTypes);
        questionTypeComboBox.addActionListener(new QuestionTypeChangeListener());
        formPanel.add(questionTypeComboBox, gbc);
        row++;

        // 选项A
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("选项A:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        optionAField = UIComponentFactory.createTextField();
        formPanel.add(optionAField, gbc);
        row++;

        // 选项B
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("选项B:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        optionBField = UIComponentFactory.createTextField();
        formPanel.add(optionBField, gbc);
        row++;

        // 选项C
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("选项C:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        optionCField = UIComponentFactory.createTextField();
        formPanel.add(optionCField, gbc);
        row++;

        // 选项D
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("选项D:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        optionDField = UIComponentFactory.createTextField();
        formPanel.add(optionDField, gbc);
        row++;

        // 正确答案
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("正确答案:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        correctAnswerField = UIComponentFactory.createTextField();
        formPanel.add(correctAnswerField, gbc);
        row++;

        // 解析 - 改用 JTextArea
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(UIComponentFactory.createLabel("解析:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        explanationArea = new JTextArea(3, 30);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(UIStyle.NORMAL_FONT);
        JScrollPane explanationScrollPane = new JScrollPane(explanationArea);
        formPanel.add(explanationScrollPane, gbc);
        row++;

        // 分类
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(UIComponentFactory.createLabel("分类:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        categoryComboBox = UIComponentFactory.createComboBox();
        formPanel.add(categoryComboBox, gbc);
        row++;

        // 难度
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(UIComponentFactory.createLabel("难度:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        difficultyComboBox = UIComponentFactory.createComboBox();
        formPanel.add(difficultyComboBox, gbc);

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建加载状态面板（初始隐藏）
        loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UIStyle.PADDING_SMALL, 0));
        loadingPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        loadingLabel = new JLabel("正在加载分类和难度数据...");
        loadingLabel.setFont(UIStyle.NORMAL_FONT);
        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setIndeterminate(true);
        loadingProgressBar.setPreferredSize(new Dimension(200, 20));
        loadingPanel.add(loadingLabel);
        loadingPanel.add(loadingProgressBar);
        loadingPanel.setVisible(false); // 初始隐藏
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_SMALL, 0));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        saveButton = UIComponentFactory.createPrimaryButton("保存");
        saveButton.addActionListener(new SaveButtonListener());
        buttonPanel.add(saveButton);

        cancelButton = UIComponentFactory.createSecondaryButton("取消");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        // 创建南方容器面板，包含加载面板和按钮面板
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        southPanel.add(loadingPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 加载题目数据
    private void loadData() {
        questionContentField.setText(question.getQuestionContent());
        questionTypeComboBox.setSelectedItem(question.getQuestionType());
        optionAField.setText(question.getOptionA());
        optionBField.setText(question.getOptionB());
        optionCField.setText(question.getOptionC());
        optionDField.setText(question.getOptionD());
        correctAnswerField.setText(question.getCorrectAnswer());
        explanationArea.setText(question.getExplanation());

        // 根据题目类型设置选项的可见性
        String questionType = question.getQuestionType();
        boolean showOptions = questionType.equals("单选题") || questionType.equals("多选题") || questionType.equals("判断题");
        optionAField.setEnabled(showOptions);
        optionBField.setEnabled(showOptions);
        optionCField.setEnabled(showOptions);
        optionDField.setEnabled(showOptions);
    }

    // 加载分类和难度数据
    private void loadCategoriesAndDifficulties() {
        try {
            // 加载分类
            List<QuestionCategory> categories = questionService.getAllCategories();
            for (QuestionCategory category : categories) {
                categoryComboBox.addItem(category);
                if (category.getCategoryId() == question.getCategory().getCategoryId()) {
                    categoryComboBox.setSelectedItem(category);
                }
            }
            
            // 加载难度
            List<QuestionDifficulty> difficulties = questionService.getAllDifficulties();
            for (QuestionDifficulty difficulty : difficulties) {
                difficultyComboBox.addItem(difficulty);
                if (difficulty.getDifficultyId() == question.getDifficulty().getDifficultyId()) {
                    difficultyComboBox.setSelectedItem(difficulty);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载分类和难度数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 异步加载分类和难度数据
    private void loadCategoriesAndDifficultiesAsync() {
        // 显示加载面板
        SwingUtilities.invokeLater(() -> {
            loadingPanel.setVisible(true);
            loadingLabel.setText("正在加载分类和难度数据...");
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
        });
        
        // 在后台线程中加载数据
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 模拟网络延迟，让用户看到加载效果
                    Thread.sleep(100);
                    
                    // 加载分类
                    List<QuestionCategory> categories = questionService.getAllCategories();
                    // 加载难度
                    List<QuestionDifficulty> difficulties = questionService.getAllDifficulties();
                    
                    // 在EDT线程中更新UI
                    SwingUtilities.invokeLater(() -> {
                        // 填充分类下拉框
                        categoryComboBox.removeAllItems();
                        for (QuestionCategory category : categories) {
                            categoryComboBox.addItem(category);
                            if (category.getCategoryId() == question.getCategory().getCategoryId()) {
                                categoryComboBox.setSelectedItem(category);
                            }
                        }
                        
                        // 填充难度下拉框
                        difficultyComboBox.removeAllItems();
                        for (QuestionDifficulty difficulty : difficulties) {
                            difficultyComboBox.addItem(difficulty);
                            if (difficulty.getDifficultyId() == question.getDifficulty().getDifficultyId()) {
                                difficultyComboBox.setSelectedItem(difficulty);
                            }
                        }
                    });
                    
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(EditQuestionFrame.this, 
                            "加载分类和难度数据失败: " + e.getMessage(), 
                            "错误", JOptionPane.ERROR_MESSAGE);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // 隐藏加载面板，启用按钮
                SwingUtilities.invokeLater(() -> {
                    loadingPanel.setVisible(false);
                    saveButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                });
            }
        };
        
        worker.execute();
    }

    // 题目类型改变监听器
    private class QuestionTypeChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedType = (String) questionTypeComboBox.getSelectedItem();
            boolean showOptions = selectedType.equals("单选题") || selectedType.equals("多选题") || selectedType.equals("判断题");
            
            optionAField.setEnabled(showOptions);
            optionBField.setEnabled(showOptions);
            optionCField.setEnabled(showOptions);
            optionDField.setEnabled(showOptions);
        }
    }

    // 保存按钮监听器
    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // 验证输入
                if (questionContentField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(EditQuestionFrame.this, "题目内容不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (correctAnswerField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(EditQuestionFrame.this, "正确答案不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 更新题目对象
                question.setQuestionContent(questionContentField.getText());
                question.setQuestionType((String) questionTypeComboBox.getSelectedItem());
                question.setOptionA(optionAField.getText());
                question.setOptionB(optionBField.getText());
                question.setOptionC(optionCField.getText());
                question.setOptionD(optionDField.getText());
                question.setCorrectAnswer(correctAnswerField.getText());
                question.setExplanation(explanationArea.getText());
                question.setCategory((QuestionCategory) categoryComboBox.getSelectedItem());
                question.setDifficulty((QuestionDifficulty) difficultyComboBox.getSelectedItem());

                // 保存题目
                questionService.updateQuestion(question);
                JOptionPane.showMessageDialog(EditQuestionFrame.this, "题目更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(EditQuestionFrame.this, "更新题目失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}