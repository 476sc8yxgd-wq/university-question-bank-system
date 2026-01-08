package com.university.questionbank.gui;

import com.university.questionbank.service.UserService;
import com.university.questionbank.service.QuestionService;
import com.university.questionbank.dao.QuestionCategoryDAO;
import com.university.questionbank.dao.QuestionDifficultyDAO;
import com.university.questionbank.dao.DAOFactory;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class SystemStatisticsFrame extends JInternalFrame {
    private UserService userService;
    private QuestionService questionService;
    private QuestionCategoryDAO categoryDAO;
    private QuestionDifficultyDAO difficultyDAO;
    
    // 统计数据标签
    private JLabel totalUsersLabel;
    private JLabel activeUsersLabel;
    private JLabel totalQuestionsLabel;
    private JLabel totalCategoriesLabel;
    private JLabel totalDifficultiesLabel;
    
    public SystemStatisticsFrame() {
        super("系统统计", true, true, true, true);
        this.userService = new UserService();
        this.questionService = new QuestionService();
        this.categoryDAO = DAOFactory.createQuestionCategoryDAO();
        this.difficultyDAO = DAOFactory.createQuestionDifficultyDAO();
        initUI();
        loadStatisticsData();
    }
    
    private void initUI() {
        setSize(600, 400);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        JLabel titleLabel = new JLabel("系统统计信息");
        titleLabel.setFont(UIStyle.HEADING_FONT);
        titleLabel.setForeground(UIStyle.PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        mainPanel.add(titleLabel, gbc);
        
        // 重置网格约束
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.4;
        
        // 总用户数
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel totalUsersTextLabel = UIComponentFactory.createLabel("总用户数:");
        mainPanel.add(totalUsersTextLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        totalUsersLabel = UIComponentFactory.createLabel("0");
        totalUsersLabel.setFont(UIStyle.HEADING_FONT.deriveFont(Font.PLAIN, 18));
        totalUsersLabel.setForeground(UIStyle.SECONDARY_COLOR);
        mainPanel.add(totalUsersLabel, gbc);
        
        // 活跃用户数
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.4;
        JLabel activeUsersTextLabel = UIComponentFactory.createLabel("活跃用户数:");
        mainPanel.add(activeUsersTextLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        activeUsersLabel = UIComponentFactory.createLabel("0");
        activeUsersLabel.setFont(UIStyle.HEADING_FONT.deriveFont(Font.PLAIN, 18));
        activeUsersLabel.setForeground(UIStyle.SECONDARY_COLOR);
        mainPanel.add(activeUsersLabel, gbc);
        
        // 总题目数
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.4;
        JLabel totalQuestionsTextLabel = UIComponentFactory.createLabel("总题目数:");
        mainPanel.add(totalQuestionsTextLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        totalQuestionsLabel = UIComponentFactory.createLabel("0");
        totalQuestionsLabel.setFont(UIStyle.HEADING_FONT.deriveFont(Font.PLAIN, 18));
        totalQuestionsLabel.setForeground(UIStyle.SECONDARY_COLOR);
        mainPanel.add(totalQuestionsLabel, gbc);
        
        // 总分类数
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.4;
        JLabel totalCategoriesTextLabel = UIComponentFactory.createLabel("总分类数:");
        mainPanel.add(totalCategoriesTextLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        totalCategoriesLabel = UIComponentFactory.createLabel("0");
        totalCategoriesLabel.setFont(UIStyle.HEADING_FONT.deriveFont(Font.PLAIN, 18));
        totalCategoriesLabel.setForeground(UIStyle.SECONDARY_COLOR);
        mainPanel.add(totalCategoriesLabel, gbc);
        
        // 总难度级别数
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.4;
        JLabel totalDifficultiesTextLabel = UIComponentFactory.createLabel("总难度级别数:");
        mainPanel.add(totalDifficultiesTextLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        totalDifficultiesLabel = UIComponentFactory.createLabel("0");
        totalDifficultiesLabel.setFont(UIStyle.HEADING_FONT.deriveFont(Font.PLAIN, 18));
        totalDifficultiesLabel.setForeground(UIStyle.SECONDARY_COLOR);
        mainPanel.add(totalDifficultiesLabel, gbc);
        
        add(mainPanel);
    }
    
    // 加载统计数据
    private void loadStatisticsData() {
        try {
            // 用户统计
            int totalUsers = userService.getAllUsers().size();
            int activeUsers = 0;
            for (var user : userService.getAllUsers()) {
                if (user.getStatus() == 1) {
                    activeUsers++;
                }
            }
            
            // 题目统计
            int totalQuestions = questionService.getAllQuestions().size();
            
            // 分类和难度统计
            int totalCategories = categoryDAO.getAllCategories().size();
            int totalDifficulties = difficultyDAO.getAllDifficulties().size();
            
            // 更新标签
            totalUsersLabel.setText(String.valueOf(totalUsers));
            activeUsersLabel.setText(String.valueOf(activeUsers));
            totalQuestionsLabel.setText(String.valueOf(totalQuestions));
            totalCategoriesLabel.setText(String.valueOf(totalCategories));
            totalDifficultiesLabel.setText(String.valueOf(totalDifficulties));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载统计数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}