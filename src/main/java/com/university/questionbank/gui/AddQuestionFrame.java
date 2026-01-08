package com.university.questionbank.gui;

import com.university.questionbank.model.*;
import com.university.questionbank.service.QuestionService;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.List;

public class AddQuestionFrame extends JInternalFrame {
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
    private JTextField explanationField;
    private JComboBox<QuestionCategory> categoryComboBox;
    private JComboBox<QuestionDifficulty> difficultyComboBox;
    private JButton saveButton;
    private JButton cancelButton;

    public AddQuestionFrame(User currentUser) {
        super("添加题目", true, true, true, true);
        this.currentUser = currentUser;

        // 权限检查：只有管理员和教师可以添加题目
        if (currentUser.getRole().getRoleId() > 2) {
            JOptionPane.showMessageDialog(this,
                    "您没有添加题目的权限！\n只有管理员和教师可以添加题目。",
                    "权限不足",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        this.questionService = new QuestionService();
        initUI();
        loadCategoriesAndDifficulties();
    }

    private void initUI() {
        setSize(650, 550);
        // 位置由MainFrame设置，这里不再重复设置
        // setLocation(100, 100);
        
        // 添加窗口激活监听器，确保焦点在输入框上
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // 窗口显示时将焦点设置到题目内容输入框
                questionContentField.requestFocusInWindow();
            }
        });
        
        // 添加窗口激活监听器（适用于JInternalFrame）
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                // 窗口被激活时将焦点设置到题目内容输入框
                questionContentField.requestFocusInWindow();
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        
        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(0, 2, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL));
        formPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // 题目内容
        formPanel.add(UIComponentFactory.createLabel("题目内容:"));
        questionContentField = UIComponentFactory.createTextField();
        formPanel.add(questionContentField);
        
        // 题目类型
        formPanel.add(UIComponentFactory.createLabel("题目类型:"));
        String[] questionTypes = {"单选题", "多选题", "判断题", "填空题", "简答题"};
        questionTypeComboBox = UIComponentFactory.createComboBox(questionTypes);
        questionTypeComboBox.addActionListener(new QuestionTypeChangeListener());
        formPanel.add(questionTypeComboBox);
        
        // 选项A
        formPanel.add(UIComponentFactory.createLabel("选项A:"));
        optionAField = UIComponentFactory.createTextField();
        formPanel.add(optionAField);
        
        // 选项B
        formPanel.add(UIComponentFactory.createLabel("选项B:"));
        optionBField = UIComponentFactory.createTextField();
        formPanel.add(optionBField);
        
        // 选项C
        formPanel.add(UIComponentFactory.createLabel("选项C:"));
        optionCField = UIComponentFactory.createTextField();
        formPanel.add(optionCField);
        
        // 选项D
        formPanel.add(UIComponentFactory.createLabel("选项D:"));
        optionDField = UIComponentFactory.createTextField();
        formPanel.add(optionDField);
        
        // 正确答案
        formPanel.add(UIComponentFactory.createLabel("正确答案:"));
        correctAnswerField = UIComponentFactory.createTextField();
        formPanel.add(correctAnswerField);
        
        // 解析
        formPanel.add(UIComponentFactory.createLabel("解析:"));
        explanationField = UIComponentFactory.createTextField();
        formPanel.add(explanationField);
        
        // 分类
        formPanel.add(UIComponentFactory.createLabel("分类:"));
        categoryComboBox = UIComponentFactory.createComboBox();
        // 设置分类下拉框的自定义渲染器，只显示分类名称
        categoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof QuestionCategory) {
                    QuestionCategory category = (QuestionCategory) value;
                    setText(category.getCategoryName());
                } else {
                    setText(value == null ? "" : value.toString());
                }
                return this;
            }
        });
        formPanel.add(categoryComboBox);

        // 难度
        formPanel.add(UIComponentFactory.createLabel("难度:"));
        difficultyComboBox = UIComponentFactory.createComboBox();
        // 设置难度下拉框的自定义渲染器，只显示难度等级
        difficultyComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof QuestionDifficulty) {
                    QuestionDifficulty difficulty = (QuestionDifficulty) value;
                    setText(difficulty.getDifficultyLevel());
                } else {
                    setText(value == null ? "" : value.toString());
                }
                return this;
            }
        });
        formPanel.add(difficultyComboBox);
        
        // 添加滚动面板
        JScrollPane scrollPane = UIComponentFactory.createScrollPane(formPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        saveButton = UIComponentFactory.createPrimaryButton("保存");
        saveButton.addActionListener(new SaveButtonListener());
        buttonPanel.add(saveButton);
        
        cancelButton = UIComponentFactory.createSecondaryButton("取消");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    // 加载分类和难度数据
    private void loadCategoriesAndDifficulties() {
        try {
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
                    JOptionPane.showMessageDialog(AddQuestionFrame.this, "题目内容不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (correctAnswerField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(AddQuestionFrame.this, "正确答案不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 创建题目对象
                Question question = new Question();
                question.setQuestionContent(questionContentField.getText());
                question.setQuestionType((String) questionTypeComboBox.getSelectedItem());
                question.setOptionA(optionAField.getText());
                question.setOptionB(optionBField.getText());
                question.setOptionC(optionCField.getText());
                question.setOptionD(optionDField.getText());
                question.setCorrectAnswer(correctAnswerField.getText());
                question.setExplanation(explanationField.getText());

                // 设置ID而不是对象，因为数据库需要的是外键ID
                QuestionCategory category = (QuestionCategory) categoryComboBox.getSelectedItem();
                if (category != null) {
                    question.setCategoryId(category.getCategoryId());
                    System.out.println("选择的分类: " + category.getCategoryName() + " (ID: " + category.getCategoryId() + ")");
                } else {
                    System.out.println("分类选择为null");
                    question.setCategoryId(0);
                }

                QuestionDifficulty difficulty = (QuestionDifficulty) difficultyComboBox.getSelectedItem();
                if (difficulty != null) {
                    question.setDifficultyId(difficulty.getDifficultyId());
                    System.out.println("选择的难度: " + difficulty.getDifficultyLevel() + " (ID: " + difficulty.getDifficultyId() + ")");
                } else {
                    System.out.println("难度选择为null");
                    question.setDifficultyId(0);
                }

                question.setCreatorId(currentUser.getUserId());

                // 验证是否选择了分类和难度
                if (question.getCategoryId() <= 0) {
                    JOptionPane.showMessageDialog(AddQuestionFrame.this, "请选择有效的分类", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (question.getDifficultyId() <= 0) {
                    JOptionPane.showMessageDialog(AddQuestionFrame.this, "请选择有效的难度", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                System.out.println("准备保存题目: " + question.getQuestionContent());
                System.out.println("Category ID: " + question.getCategoryId());
                System.out.println("Difficulty ID: " + question.getDifficultyId());
                System.out.println("Creator ID: " + question.getCreatorId());

                // 保存题目
                questionService.addQuestion(question);
                JOptionPane.showMessageDialog(AddQuestionFrame.this, "题目添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(AddQuestionFrame.this, "添加题目失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(AddQuestionFrame.this, "添加题目失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}