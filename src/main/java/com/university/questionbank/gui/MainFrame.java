package com.university.questionbank.gui;

import com.university.questionbank.model.User;
import com.university.questionbank.gui.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private JDesktopPane desktopPane;
    
    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
    }
    
    private void initUI() {
        setTitle("大学题目资料库管理系统");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 创建桌面面板
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(UIStyle.BACKGROUND_COLOR);
        // 设置层叠模式，允许新窗口在前面
        desktopPane.putClientProperty("JDesktopPane.dragMode", "outline");
        add(desktopPane);
        
        // 创建菜单条
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        
        // 显示欢迎信息
        showWelcomePanel();
    }
    
    // 创建菜单条
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitMenuItem = new JMenuItem("退出");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        
        // 题目管理菜单
        JMenu questionMenu = new JMenu("题目管理");

        // 添加题目：只有管理员和教师可以添加题目
        if (currentUser.getRole().getRoleId() <= 2) {
            JMenuItem addQuestionMenuItem = new JMenuItem("添加题目");
            addQuestionMenuItem.addActionListener(e -> openAddQuestionFrame());
            questionMenu.add(addQuestionMenuItem);
        }

        JMenuItem manageQuestionsMenuItem = new JMenuItem("管理题目");
        manageQuestionsMenuItem.addActionListener(e -> openQuestionManagementFrame());
        questionMenu.add(manageQuestionsMenuItem);

        JMenuItem searchQuestionsMenuItem = new JMenuItem("检索题目");
        searchQuestionsMenuItem.addActionListener(e -> openQuestionSearchFrame());
        questionMenu.add(searchQuestionsMenuItem);

        JMenuItem exportQuestionsMenuItem = new JMenuItem("导出题目");
        exportQuestionsMenuItem.addActionListener(e -> openQuestionExportFrame());
        questionMenu.add(exportQuestionsMenuItem);

        menuBar.add(questionMenu);
        
        // 用户管理菜单（仅管理员可见）
        if (currentUser.getRole().getRoleId() == 1) {
            JMenu userMenu = new JMenu("用户管理");
            JMenuItem addUserMenuItem = new JMenuItem("添加用户");
            addUserMenuItem.addActionListener(e -> openAddUserFrame());
            userMenu.add(addUserMenuItem);

            JMenuItem manageUsersMenuItem = new JMenuItem("管理用户");
            manageUsersMenuItem.addActionListener(e -> openUserManagementFrame());
            userMenu.add(manageUsersMenuItem);

            menuBar.add(userMenu);
        }

        // 分类管理菜单（仅管理员可见）
        if (currentUser.getRole().getRoleId() == 1) {
            JMenu categoryMenu = new JMenu("分类管理");
            JMenuItem manageCategoriesMenuItem = new JMenuItem("管理分类");
            manageCategoriesMenuItem.addActionListener(e -> openCategoryManagementFrame());
            categoryMenu.add(manageCategoriesMenuItem);

            menuBar.add(categoryMenu);
        }
        
        // 系统菜单
        JMenu systemMenu = new JMenu("系统");
        JMenuItem settingsMenuItem = new JMenuItem("系统设置");
        settingsMenuItem.addActionListener(e -> openSystemSettingsFrame());
        systemMenu.add(settingsMenuItem);
        
        JMenuItem statisticsMenuItem = new JMenuItem("系统统计");
        statisticsMenuItem.addActionListener(e -> openSystemStatisticsFrame());
        systemMenu.add(statisticsMenuItem);
        
        JMenuItem aboutMenuItem = new JMenuItem("关于");
        aboutMenuItem.addActionListener(e -> showAboutDialog());
        systemMenu.add(aboutMenuItem);
        
        menuBar.add(systemMenu);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem helpMenuItem = new JMenuItem("帮助文档");
        helpMenuItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(helpMenuItem);
        
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    // 显示欢迎面板
    private void showWelcomePanel() {
        // 创建内部窗口来容纳欢迎面板
        JInternalFrame welcomeFrame = new JInternalFrame("欢迎", false, false, false, false);
        welcomeFrame.setSize(700, 450);
        welcomeFrame.setLocation(150, 125);
        
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(UIStyle.PANEL_COLOR);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        welcomePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        JLabel welcomeLabel = new JLabel("欢迎使用大学题目资料库管理系统");
        welcomeLabel.setFont(UIStyle.HEADING_FONT);
        welcomeLabel.setForeground(UIStyle.PRIMARY_COLOR);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        
        JLabel userInfoLabel = new JLabel("当前用户: " + currentUser.getFullName() + " (" + currentUser.getRole().getRoleName() + ")");
        userInfoLabel.setFont(UIStyle.SUBHEADING_FONT);
        userInfoLabel.setForeground(UIStyle.TEXT_SECONDARY);
        userInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userInfoLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        welcomePanel.add(userInfoLabel, BorderLayout.SOUTH);
        
        welcomeFrame.add(welcomePanel);
        desktopPane.add(welcomeFrame);
        welcomeFrame.setVisible(true);
    }
    
    // 打开添加题目窗口
    private void openAddQuestionFrame() {
        AddQuestionFrame frame = new AddQuestionFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(100, 100); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }
    
    // 打开题目管理窗口
    private void openQuestionManagementFrame() {
        QuestionManagementFrame frame = new QuestionManagementFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(150, 150); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }
    
    // 打开题目检索窗口
    private void openQuestionSearchFrame() {
        QuestionSearchFrame frame = new QuestionSearchFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(200, 200); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }
    
    // 打开题目导出窗口
    private void openQuestionExportFrame() {
        QuestionExportFrame frame = new QuestionExportFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(250, 250); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }
    
    // 打开添加用户窗口
    private void openAddUserFrame() {
        UserManagementFrame frame = new UserManagementFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(50, 50); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
        
        // 直接调用公共方法显示添加用户对话框
        SwingUtilities.invokeLater(() -> {
            frame.showAddUserDialog();
        });
    }
    
    // 打开用户管理窗口
    private void openUserManagementFrame() {
        UserManagementFrame frame = new UserManagementFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(50, 50); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }

    // 打开分类管理窗口
    private void openCategoryManagementFrame() {
        CategoryManagementFrame frame = new CategoryManagementFrame(currentUser);
        desktopPane.add(frame);
        frame.setLocation(100, 100); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }

    // 打开系统设置窗口
    private void openSystemSettingsFrame() {
        SystemSettingsFrame frame = new SystemSettingsFrame();
        desktopPane.add(frame);
        frame.setLocation(300, 300); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }
    
    // 打开系统统计窗口
    private void openSystemStatisticsFrame() {
        SystemStatisticsFrame frame = new SystemStatisticsFrame();
        desktopPane.add(frame);
        frame.setLocation(350, 350); // 设置初始位置
        frame.setVisible(true);
        // 确保窗口在最前面
        frame.moveToFront();
    }
    
    // 显示关于对话框
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, 
                "大学题目资料库管理系统 v1.0\n\n" +
                "基于Java Swing开发的本地桌面应用\n" +
                "© 2025 大学题目资料库管理系统", 
                "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // 显示帮助对话框
    private void showHelpDialog() {
        JOptionPane.showMessageDialog(this, 
                "大学题目资料库管理系统帮助\n\n" +
                "1. 题目管理：添加、编辑、删除、检索题目\n" +
                "2. 用户管理：管理员可以添加和管理用户\n" +
                "3. 系统设置：配置系统参数\n" +
                "4. 系统统计：查看系统统计信息\n\n" +
                "如有问题，请联系系统管理员", 
                "帮助", JOptionPane.INFORMATION_MESSAGE);
    }
}