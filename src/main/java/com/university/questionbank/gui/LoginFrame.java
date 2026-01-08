package com.university.questionbank.gui;

import com.university.questionbank.model.User;
import com.university.questionbank.service.UserService;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;
import com.university.questionbank.dao.RoleDAO;
import com.university.questionbank.dao.DAOFactory;
import com.university.questionbank.model.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(LoginFrame.class.getName());

    private UserService userService = new UserService();
    private RoleDAO roleDAO = DAOFactory.createRoleDAO();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton resetButton;
    private JButton registerButton;
    
    public LoginFrame() {
        initUI();
    }
    
    private void initUI() {
        setTitle("大学题目资料库管理系统 - 登录");
        setSize(420, 420);
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // 创建面板
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        JLabel titleLabel = new JLabel("用户登录");
        titleLabel.setFont(UIStyle.HEADING_FONT);
        titleLabel.setForeground(UIStyle.PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(titleLabel, gbc);
        
        // 用户名标签
        JLabel usernameLabel = UIComponentFactory.createLabel("用户名:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        panel.add(usernameLabel, gbc);
        
        // 用户名输入框
        usernameField = UIComponentFactory.createTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        panel.add(usernameField, gbc);
        
        // 密码标签
        JLabel passwordLabel = UIComponentFactory.createLabel("密码:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(passwordLabel, gbc);
        
        // 密码输入框
        passwordField = UIComponentFactory.createPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.7;
        panel.add(passwordField, gbc);
        
        // 登录按钮
        loginButton = UIComponentFactory.createPrimaryButton("登录");
        loginButton.addActionListener(new LoginButtonListener());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(loginButton, gbc);

        // 注册按钮
        registerButton = UIComponentFactory.createButton("注册");
        registerButton.addActionListener(e -> showRegisterDialog());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        // 重置按钮
        resetButton = UIComponentFactory.createButton("重置");
        resetButton.addActionListener(e -> resetForm());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(resetButton, gbc);

        // 注意：已移除"数据库连接测试"按钮
        // 原因：
        // 1. 应用程序使用 REST API 模式（HTTPS 443端口）
        // 2. JDBC 端口（5432/6543）被防火墙阻止
        // 3. 点击此按钮会导致端口超时警告，容易混淆用户
        // 4. REST API 连接会在启动时自动测试，无需手动测试

        add(panel);
    }
    
    // 登录按钮监听器
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            // 验证输入
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFrame.this, "请输入用户名和密码", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // 调用登录服务
                User user = userService.login(username, password);
                logger.info("用户登录成功: " + username);

                // 关闭登录窗口，打开主窗口
                dispose();
                new MainFrame(user).setVisible(true);

            } catch (SQLException ex) {
                logger.severe("登录失败: " + ex.getMessage());
                JOptionPane.showMessageDialog(LoginFrame.this, ex.getMessage(), "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 重置表单
    private void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }

    // 显示注册对话框
    private void showRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }

    // 注册对话框
    private class RegisterDialog extends JDialog {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JPasswordField confirmPasswordField;
        private JButton registerButton;
        private JButton cancelButton;

        public RegisterDialog(Frame parent) {
            super(parent, "用户注册", true);
            initUI();
            setLocationRelativeTo(parent);
        }

        private void initUI() {
            setSize(480, 350);

            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBackground(UIStyle.PANEL_COLOR);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 用户名
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel usernameLabel = UIComponentFactory.createLabel("用户名*:");
            mainPanel.add(usernameLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            usernameField = UIComponentFactory.createTextField();
            usernameField.setPreferredSize(new Dimension(200, 32));
            mainPanel.add(usernameField, gbc);

            // 密码
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel passwordLabel = UIComponentFactory.createLabel("密码*:");
            mainPanel.add(passwordLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            passwordField = UIComponentFactory.createPasswordField();
            passwordField.setPreferredSize(new Dimension(200, 32));
            mainPanel.add(passwordField, gbc);

            // 确认密码
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel confirmPasswordLabel = UIComponentFactory.createLabel("确认密码*:");
            mainPanel.add(confirmPasswordLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            confirmPasswordField = UIComponentFactory.createPasswordField();
            confirmPasswordField.setPreferredSize(new Dimension(200, 32));
            mainPanel.add(confirmPasswordField, gbc);

            // 提示信息
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 25, 0);
            gbc.weightx = 1.0;
            JLabel hintLabel = UIComponentFactory.createLabel("<html><font size='2'>注册后将自动分配学生权限</font></html>");
            mainPanel.add(hintLabel, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setBackground(UIStyle.PANEL_COLOR);

            registerButton = UIComponentFactory.createPrimaryButton("注册");
            registerButton.setPreferredSize(new Dimension(100, 32));
            registerButton.addActionListener(e -> registerUser());
            buttonPanel.add(registerButton);

            cancelButton = UIComponentFactory.createSecondaryButton("取消");
            cancelButton.setPreferredSize(new Dimension(100, 32));
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);

            gbc.gridy = 4;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.anchor = GridBagConstraints.EAST;
            mainPanel.add(buttonPanel, gbc);

            add(mainPanel);
        }

        private void registerUser() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // 验证输入
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterDialog.this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.length() < 3) {
                JOptionPane.showMessageDialog(RegisterDialog.this, "用户名至少需要3个字符", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(RegisterDialog.this, "密码至少需要6个字符", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(RegisterDialog.this, "两次输入的密码不一致", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // 查找"学生"角色
                Role studentRole = findStudentRole();
                if (studentRole == null) {
                    JOptionPane.showMessageDialog(RegisterDialog.this, "未找到学生角色，请联系管理员", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 创建新用户
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setFullName(""); // 姓名可以为空
                newUser.setRoleId(studentRole.getRoleId());
                newUser.setStatus(1); // 默认启用
                newUser.setEmail("");
                newUser.setPhone("");

                // 注册用户
                userService.addUser(newUser);

                JOptionPane.showMessageDialog(RegisterDialog.this, "注册成功！请使用用户名和密码登录", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (SQLException ex) {
                logger.severe("注册失败: " + ex.getMessage());
                JOptionPane.showMessageDialog(RegisterDialog.this, "注册失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                logger.severe("注册失败: " + ex.getMessage());
                JOptionPane.showMessageDialog(RegisterDialog.this, "注册失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        private Role findStudentRole() {
            try {
                List<Role> roles = roleDAO.getAllRoles();
                for (Role role : roles) {
                    if ("学生".equals(role.getRoleName())) {
                        return role;
                    }
                }
                return null;
            } catch (Exception e) {
                logger.severe("查找学生角色失败: " + e.getMessage());
                return null;
            }
        }
    }
}