package com.university.questionbank.gui;

import com.university.questionbank.model.User;
import com.university.questionbank.model.Role;
import com.university.questionbank.service.UserService;
import com.university.questionbank.dao.RoleDAO;
import com.university.questionbank.dao.DAOFactory;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.SwingUtilities;

public class UserManagementFrame extends JInternalFrame {
    private User currentUser;
    private UserService userService;
    private RoleDAO roleDAO;
    
    // 界面组件
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton enableDisableButton;
    private JButton refreshButton;
    
    public UserManagementFrame(User currentUser) {
        super("用户管理", true, true, true, true);
        this.currentUser = currentUser;
        this.userService = new UserService();
        this.roleDAO = DAOFactory.createRoleDAO();
        initUI();
        loadUserData();
    }
    
    private void initUI() {
        setSize(900, 700);
        // 位置由MainFrame设置，这里不再重复设置
        // setLocation(50, 50);

        System.out.println("UserManagementFrame初始化中...");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        // 创建表格模型
        String[] columnNames = {"用户ID", "用户名", "姓名", "角色", "状态", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 创建表格
        userTable = UIComponentFactory.createTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 创建滚动面板
        JScrollPane scrollPane = UIComponentFactory.createScrollPane(userTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        addUserButton = UIComponentFactory.createPrimaryButton("添加用户");
        addUserButton.addActionListener(new AddUserButtonListener());
        buttonPanel.add(addUserButton);
        
        editUserButton = UIComponentFactory.createSecondaryButton("编辑用户");
        editUserButton.addActionListener(new EditUserButtonListener());
        buttonPanel.add(editUserButton);
        
        deleteUserButton = UIComponentFactory.createSecondaryButton("删除用户");
        deleteUserButton.addActionListener(new DeleteUserButtonListener());
        buttonPanel.add(deleteUserButton);
        
        enableDisableButton = UIComponentFactory.createSecondaryButton("启用/禁用");
        enableDisableButton.addActionListener(new EnableDisableButtonListener());
        buttonPanel.add(enableDisableButton);
        
        refreshButton = UIComponentFactory.createSecondaryButton("刷新");
        refreshButton.addActionListener(new RefreshButtonListener());
        buttonPanel.add(refreshButton);
        
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        
        add(mainPanel);
    }
    
    // 公共方法：显示添加用户对话框
    public void showAddUserDialog() {
        new AddUserDialog(this).setVisible(true);
    }

    // 加载用户数据
    private void loadUserData() {
        try {
            System.out.println("开始加载用户数据...");
            List<User> users = userService.getAllUsers();
            System.out.println("获取到用户数量: " + users.size());

            tableModel.setRowCount(0); // 清空表格

            for (User user : users) {
                Object[] row = {
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole() != null ? user.getRole().getRoleName() : "无",
                    user.getStatus() == 1 ? "启用" : "禁用",
                    user.getCreatedAt()
                };
                tableModel.addRow(row);
            }

            System.out.println("用户数据加载完成，表格行数: " + tableModel.getRowCount());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载用户数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 添加用户按钮监听器
    private class AddUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            AddUserDialog addUserDialog = new AddUserDialog(UserManagementFrame.this);
            addUserDialog.setVisible(true);
        }
    }
    
    // 编辑用户按钮监听器
    private class EditUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "请选择要编辑的用户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                User user = userService.getUserById(userId);
                EditUserDialog editUserDialog = new EditUserDialog(UserManagementFrame.this, user);
                editUserDialog.setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "获取用户信息失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 删除用户按钮监听器
    private class DeleteUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "请选择要删除的用户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            String username = (String) tableModel.getValueAt(selectedRow, 1);
            
            // 检查用户是否有关联的题目
            boolean hasQuestions = false;
            try {
                hasQuestions = userService.userHasQuestions(userId);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "检查用户题目失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String confirmMessage;
            if (hasQuestions) {
                // 用户有关联题目，给出警告和选项
                Object[] options = {"取消删除", "级联删除（同时删除题目）"};
                int choice = JOptionPane.showOptionDialog(
                        UserManagementFrame.this,
                        "用户\"" + username + "\"创建了题目。\n\n删除该用户时，您有以下选择：\n" +
                        "• 取消删除：请先手动删除或转移题目\n" +
                        "• 级联删除：同时删除用户及其创建的所有题目",
                        "确认删除",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);
                
                if (choice == 1) {
                    // 用户选择级联删除
                    performDelete(userId, username, true);
                }
                // choice == 0 表示取消删除
            } else {
                // 用户没有关联题目，直接确认删除
                int confirm = JOptionPane.showConfirmDialog(UserManagementFrame.this,
                        "确定要删除用户\"" + username + "\"吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    performDelete(userId, username, false);
                }
            }
        }
        
        private void performDelete(int userId, String username, boolean cascadeDelete) {
            try {
                // 如果是级联删除，先删除用户创建的所有题目
                if (cascadeDelete) {
                    userService.deleteUserQuestions(userId);
                }
                
                // 删除用户
                userService.deleteUser(userId);
                
                String message = "用户\"" + username + "\"删除成功";
                if (cascadeDelete) {
                    message += "（同时删除了该用户创建的题目）";
                }
                
                JOptionPane.showMessageDialog(UserManagementFrame.this, message, "成功", JOptionPane.INFORMATION_MESSAGE);
                loadUserData(); // 刷新用户列表
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "删除用户失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 启用/禁用用户按钮监听器
    private class EnableDisableButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "请选择要操作的用户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            String username = (String) tableModel.getValueAt(selectedRow, 1);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);
            
            try {
                int newStatus = currentStatus.equals("启用") ? 0 : 1;
                userService.updateUserStatus(userId, newStatus);
                JOptionPane.showMessageDialog(UserManagementFrame.this,
                        "用户\"" + username + "\"已" + (newStatus == 1 ? "启用" : "禁用"), 
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                loadUserData(); // 刷新用户列表
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(UserManagementFrame.this, "操作失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 刷新按钮监听器
    private class RefreshButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadUserData();
        }
    }
    
    // 添加用户对话框
    private class AddUserDialog extends JDialog {
        private JTextField usernameField;
        private JTextField fullNameField;
        private JPasswordField passwordField;
        private JComboBox<Role> roleComboBox;
        private JButton saveButton;
        private JButton cancelButton;
        
        public AddUserDialog(Component parent) {
            super(SwingUtilities.getWindowAncestor(parent), "添加用户", ModalityType.APPLICATION_MODAL);
            initUI();
            setLocationRelativeTo(parent);
        }
        
        private void initUI() {
            setSize(500, 400);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(UIStyle.PANEL_COLOR);
            formPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 用户名
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel usernameLabel = UIComponentFactory.createLabel("用户名*:");
            formPanel.add(usernameLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            usernameField = UIComponentFactory.createTextField();
            usernameField.setPreferredSize(new Dimension(200, 32));
            formPanel.add(usernameField, gbc);

            // 姓名
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel fullNameLabel = UIComponentFactory.createLabel("真实姓名:");
            formPanel.add(fullNameLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            fullNameField = UIComponentFactory.createTextField();
            fullNameField.setPreferredSize(new Dimension(200, 32));
            formPanel.add(fullNameField, gbc);

            // 密码
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel passwordLabel = UIComponentFactory.createLabel("密码*:");
            formPanel.add(passwordLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            passwordField = UIComponentFactory.createPasswordField();
            passwordField.setPreferredSize(new Dimension(200, 32));
            formPanel.add(passwordField, gbc);

            // 角色
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel roleLabel = UIComponentFactory.createLabel("角色*:");
            formPanel.add(roleLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            roleComboBox = UIComponentFactory.createComboBox();
            roleComboBox.setPreferredSize(new Dimension(200, 32));
            loadRoles();
            // 设置自定义渲染器显示角色名称
            roleComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Role) {
                        Role role = (Role) value;
                        setText(role.getRoleName());
                    }
                    return this;
                }
            });
            formPanel.add(roleComboBox, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setBackground(UIStyle.PANEL_COLOR);

            saveButton = UIComponentFactory.createPrimaryButton("保存");
            saveButton.setPreferredSize(new Dimension(100, 32));
            saveButton.addActionListener(new SaveUserButtonListener());
            buttonPanel.add(saveButton);

            cancelButton = UIComponentFactory.createSecondaryButton("取消");
            cancelButton.setPreferredSize(new Dimension(100, 32));
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(25, 0, 0, 0);
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 1.0;
            formPanel.add(buttonPanel, gbc);

            add(formPanel);
        }
        
        private void loadRoles() {
            try {
                List<Role> roles = roleDAO.getAllRoles();
                for (Role role : roles) {
                    roleComboBox.addItem(role);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "加载角色数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private class SaveUserButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 验证输入
                    if (usernameField.getText().isEmpty() || 
                        new String(passwordField.getPassword()).isEmpty()) {
                        JOptionPane.showMessageDialog(AddUserDialog.this, "请填写所有必填字段", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Role selectedRole = (Role) roleComboBox.getSelectedItem();
                    if (selectedRole == null) {
                        JOptionPane.showMessageDialog(AddUserDialog.this, "请选择用户角色", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // 创建新用户
                    User newUser = new User();
                    newUser.setUsername(usernameField.getText());
                    newUser.setFullName(fullNameField.getText());
                    newUser.setPassword(new String(passwordField.getPassword()));
                    newUser.setRoleId(selectedRole.getRoleId()); // 设置roleId而不是role对象
                    newUser.setStatus(1); // 默认启用
                    // email和phone不是必填字段，可以为空
                    newUser.setEmail("");
                    newUser.setPhone("");

                    System.out.println("准备添加用户: " + newUser.getUsername());
                    System.out.println("  - 角色ID: " + newUser.getRoleId());
                    System.out.println("  - 状态: " + newUser.getStatus());

                    // 保存用户
                    userService.addUser(newUser);
                    JOptionPane.showMessageDialog(AddUserDialog.this, "用户添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    loadUserData(); // 刷新用户列表
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AddUserDialog.this, "添加用户失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AddUserDialog.this, "添加用户失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // 编辑用户对话框
    private class EditUserDialog extends JDialog {
        private User user;
        private JTextField usernameField;
        private JTextField fullNameField;
        private JPasswordField passwordField;
        private JComboBox<Role> roleComboBox;
        private JButton saveButton;
        private JButton cancelButton;
        
        public EditUserDialog(Component parent, User user) {
            super(SwingUtilities.getWindowAncestor(parent), "编辑用户", ModalityType.APPLICATION_MODAL);
            this.user = user;
            initUI();
            setLocationRelativeTo(parent);
        }
        
        private void initUI() {
            setSize(500, 380);

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
            JLabel usernameLabel = UIComponentFactory.createLabel("用户名:");
            mainPanel.add(usernameLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            usernameField = UIComponentFactory.createTextField();
            usernameField.setPreferredSize(new Dimension(200, 32));
            usernameField.setText(user.getUsername());
            usernameField.setEditable(false); // 用户名不可编辑
            mainPanel.add(usernameField, gbc);

            // 姓名
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel fullNameLabel = UIComponentFactory.createLabel("真实姓名:");
            mainPanel.add(fullNameLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            fullNameField = UIComponentFactory.createTextField();
            fullNameField.setPreferredSize(new Dimension(200, 32));
            fullNameField.setText(user.getFullName());
            mainPanel.add(fullNameField, gbc);

            // 密码
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel passwordLabel = UIComponentFactory.createLabel("密码 (留空不修改):");
            mainPanel.add(passwordLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            passwordField = UIComponentFactory.createPasswordField();
            passwordField.setPreferredSize(new Dimension(200, 32));
            mainPanel.add(passwordField, gbc);

            // 角色
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel roleLabel = UIComponentFactory.createLabel("角色*:");
            mainPanel.add(roleLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            roleComboBox = UIComponentFactory.createComboBox();
            roleComboBox.setPreferredSize(new Dimension(200, 32));
            roleComboBox.setToolTipText("选择用户的系统角色");
            loadRoles();
            // 设置自定义渲染器显示角色名称
            roleComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Role) {
                        Role role = (Role) value;
                        setText(role.getRoleName());
                    }
                    return this;
                }
            });
            // 根据用户当前角色的roleId来设置选中项
            if (user.getRole() != null) {
                for (int i = 0; i < roleComboBox.getItemCount(); i++) {
                    Role role = roleComboBox.getItemAt(i);
                    if (role.getRoleId() == user.getRole().getRoleId()) {
                        roleComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            mainPanel.add(roleComboBox, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setBackground(UIStyle.PANEL_COLOR);

            saveButton = UIComponentFactory.createPrimaryButton("保存");
            saveButton.setPreferredSize(new Dimension(100, 32));
            saveButton.addActionListener(new SaveUserButtonListener());
            buttonPanel.add(saveButton);

            cancelButton = UIComponentFactory.createSecondaryButton("取消");
            cancelButton.setPreferredSize(new Dimension(100, 32));
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(25, 0, 0, 0);
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 1.0;
            mainPanel.add(buttonPanel, gbc);

            add(mainPanel);
        }
        
        private void loadRoles() {
            try {
                List<Role> roles = roleDAO.getAllRoles();
                for (Role role : roles) {
                    roleComboBox.addItem(role);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "加载角色数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private class SaveUserButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Role selectedRole = (Role) roleComboBox.getSelectedItem();
                    if (selectedRole == null) {
                        JOptionPane.showMessageDialog(EditUserDialog.this, "请选择用户角色", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // 更新用户信息
                    user.setFullName(fullNameField.getText());
                    user.setRoleId(selectedRole.getRoleId()); // 设置roleId而不是role对象

                    // 如果密码不为空，则更新密码；否则保留原密码不变
                    String password = new String(passwordField.getPassword());
                    if (!password.isEmpty()) {
                        user.setPassword(password);
                    }
                    // 如果密码为空，不修改 user.password 字段，保留原值

                    System.out.println("准备更新用户: " + user.getUsername());
                    System.out.println("  - 角色ID: " + user.getRoleId());
                    System.out.println("  - 更新密码: " + (password != null && !password.isEmpty()));

                    // 保存用户
                    userService.updateUser(user);
                    JOptionPane.showMessageDialog(EditUserDialog.this, "用户更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    loadUserData(); // 刷新用户列表
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(EditUserDialog.this, "更新用户失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(EditUserDialog.this, "更新用户失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}