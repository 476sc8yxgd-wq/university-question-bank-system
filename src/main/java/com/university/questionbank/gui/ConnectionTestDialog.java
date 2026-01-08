package com.university.questionbank.gui;

import com.university.questionbank.config.DatabaseConfig;
import com.university.questionbank.util.ConnectionTester;
import com.university.questionbank.util.NetworkAdapter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 数据库连接测试对话框
 * 提供用户友好的连接测试界面
 */
public class ConnectionTestDialog extends JDialog {
    private DatabaseConfig config;
    
    // 界面组件
    private JTextField hostField;
    private JTextField portField;
    private JTextField databaseField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> sslModeComboBox;
    private JSpinner timeoutSpinner;
    
    private JTextArea resultArea;
    private JButton testAllButton;
    private JButton testDefaultButton;
    private JButton testPoolButton;
    private JButton autoSelectButton;
    private JButton applyButton;
    private JButton cancelButton;
    
    // 进度条和状态标签
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public ConnectionTestDialog(Frame parent) {
        super(parent, "数据库连接测试", true);
        this.config = new DatabaseConfig();
        initUI();
        loadConfigToUI();

        // 自动测试连接
        SwingUtilities.invokeLater(() -> {
            updateButtonStates(); // 根据当前模式更新按钮状态
            testConnectionMode();
        });
    }
    
    private void initUI() {
        setSize(650, 650);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // 创建配置面板
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        // 创建结果面板
        JPanel resultPanel = createResultPanel();
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建配置面板
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("连接配置"));
        panel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 主机
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(UIComponentFactory.createLabel("主机:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        hostField = UIComponentFactory.createTextField();
        panel.add(hostField, gbc);
        
        // 端口
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(UIComponentFactory.createLabel("端口:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        portField = UIComponentFactory.createTextField();
        portField.setColumns(6);
        panel.add(portField, gbc);
        
        // 数据库
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(UIComponentFactory.createLabel("数据库:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        databaseField = UIComponentFactory.createTextField();
        panel.add(databaseField, gbc);
        
        // 用户名
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(UIComponentFactory.createLabel("用户名:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        usernameField = UIComponentFactory.createTextField();
        panel.add(usernameField, gbc);
        
        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(UIComponentFactory.createLabel("密码:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = UIComponentFactory.createPasswordField();
        panel.add(passwordField, gbc);
        
        // SSL模式
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(UIComponentFactory.createLabel("SSL模式:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        String[] sslModes = {"disable", "allow", "prefer", "require", "verify-ca", "verify-full"};
        sslModeComboBox = new JComboBox<>(sslModes);
        panel.add(sslModeComboBox, gbc);
        
        // 超时
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(UIComponentFactory.createLabel("超时(ms):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10000, 1000, 60000, 1000);
        timeoutSpinner = new JSpinner(spinnerModel);
        panel.add(timeoutSpinner, gbc);
        
        // 快速选择端口按钮
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JPanel quickPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quickPortPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        JButton port5432Button = UIComponentFactory.createSecondaryButton("5432端口");
        port5432Button.addActionListener(e -> setPort(ConnectionTester.DEFAULT_PORT));
        quickPortPanel.add(port5432Button);
        
        JButton port6543Button = UIComponentFactory.createSecondaryButton("6543端口");
        port6543Button.addActionListener(e -> setPort(ConnectionTester.POOL_PORT));
        quickPortPanel.add(port6543Button);
        
        panel.add(quickPortPanel, gbc);
        
        return panel;
    }
    
    /**
     * 创建结果面板
     */
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("测试结果"));
        panel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // 进度条
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        statusLabel = UIComponentFactory.createLabel("准备就绪");
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        
        panel.add(progressPanel, BorderLayout.NORTH);
        
        // 结果文本区
        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        resultArea.setFont(UIStyle.MONOSPACE_FONT);
        resultArea.setBackground(UIStyle.PANEL_COLOR);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.setBackground(UIStyle.BACKGROUND_COLOR);

        // JDBC端口测试按钮（将在对话框显示时根据模式启用/禁用）
        testDefaultButton = UIComponentFactory.createPrimaryButton("测试默认端口(5432)");
        testDefaultButton.addActionListener(e -> testDefaultPort());
        panel.add(testDefaultButton);

        testPoolButton = UIComponentFactory.createPrimaryButton("测试连接池端口(6543)");
        testPoolButton.addActionListener(e -> testPoolPort());
        panel.add(testPoolButton);

        testAllButton = UIComponentFactory.createPrimaryButton("测试所有端口");
        testAllButton.addActionListener(e -> testAllPorts());
        panel.add(testAllButton);

        autoSelectButton = UIComponentFactory.createSecondaryButton("自动选择最佳端口");
        autoSelectButton.addActionListener(e -> autoSelectBestPort());
        panel.add(autoSelectButton);

        // 始终可用的按钮
        applyButton = UIComponentFactory.createButton("应用并保存");
        applyButton.addActionListener(e -> applyAndSave());
        panel.add(applyButton);

        cancelButton = UIComponentFactory.createButton("取消");
        cancelButton.addActionListener(e -> dispose());
        panel.add(cancelButton);

        return panel;
    }
    
    /**
     * 加载配置到界面
     */
    private void loadConfigToUI() {
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        databaseField.setText(config.getDatabase());
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
        sslModeComboBox.setSelectedItem(config.getSslMode());
        timeoutSpinner.setValue(config.getTimeout());
    }
    
    /**
     * 测试连接模式（REST API 或 JDBC）
     */
    private void testConnectionMode() {
        setButtonsEnabled(false);
        progressBar.setIndeterminate(true);
        statusLabel.setText("检测连接模式...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // 检查是否使用REST API模式
                boolean isRestAPIMode = com.university.questionbank.dao.DAOFactory.isRestAPIMode();

                if (isRestAPIMode) {
                    // REST API 模式
                    testRestAPIConnection();
                } else {
                    // JDBC 模式
                    appendResult("当前模式: JDBC (本地数据库)\n\n");
                    appendResult("开始检测JDBC端口可用性...\n\n");
                    testJDBCPorts();
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                statusLabel.setText("测试完成");
                setButtonsEnabled(true);
            }
        };

        worker.execute();
    }

    /**
     * 测试 REST API 连接
     */
    private void testRestAPIConnection() {
        appendResult("当前模式: Supabase REST API\n\n");
        appendResult("正在测试 REST API 连接...\n\n");

        try {
            String response = com.university.questionbank.util.SupabaseRestAPI.get("/users", "limit=1");

            if (response != null && !response.isEmpty()) {
                appendResult("✓ REST API 连接成功！\n\n");
                appendResult("连接详情:\n");
                appendResult("  - URL: https://tjvwymicbizzfibfjvej.supabase.co\n");
                appendResult("  - 端口: 443 (HTTPS)\n");
                appendResult("  - 数据访问: REST API\n");
                appendResult("  - 状态: 正常\n\n");
                appendResult("提示: REST API 模式不需要 JDBC 端口连接。\n");
                appendResult("JDBC 端口测试已被禁用以避免不必要的超时等待。\n\n");
            } else {
                appendResult("✗ REST API 连接失败！\n\n");
                appendResult("错误: 无法获取数据\n\n");
            }
        } catch (Exception e) {
            appendResult("✗ REST API 连接失败！\n\n");
            appendResult("错误: " + e.getMessage() + "\n\n");
        }
    }

    /**
     * 测试 JDBC 端口
     */
    private void testJDBCPorts() {
        try {
            List<NetworkAdapter.PortAvailability> availabilities = NetworkAdapter.checkAllPorts(config.getHost());

            for (NetworkAdapter.PortAvailability availability : availabilities) {
                appendResult(availability.toString() + "\n");
            }

            int recommendedPort = NetworkAdapter.getRecommendedPort(config.getHost());
            appendResult("\n推荐端口: " + recommendedPort + "\n\n");

        } catch (Exception e) {
            appendResult("端口检测失败: " + e.getMessage() + "\n\n");
        }
    }

    /**
     * 根据当前模式更新按钮状态
     */
    private void updateButtonStates() {
        boolean isRestAPIMode = com.university.questionbank.dao.DAOFactory.isRestAPIMode();

        // JDBC端口测试按钮（仅在JDBC模式下可用）
        testDefaultButton.setEnabled(!isRestAPIMode);
        testPoolButton.setEnabled(!isRestAPIMode);
        testAllButton.setEnabled(!isRestAPIMode);
        autoSelectButton.setEnabled(!isRestAPIMode);

        System.out.println("更新按钮状态: isRestAPIMode=" + isRestAPIMode);
    }

    /**
     * 从界面保存到配置
     */
    private boolean saveConfigFromUI() {
        config.setHost(hostField.getText().trim());

        try {
            config.setPort(Integer.parseInt(portField.getText().trim()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        config.setDatabase(databaseField.getText().trim());
        config.setUsername(usernameField.getText().trim());
        config.setPassword(new String(passwordField.getPassword()));
        config.setSslMode((String) sslModeComboBox.getSelectedItem());
        config.setTimeout((Integer) timeoutSpinner.getValue());

        return true;
    }
    
    /**
     * 设置端口号
     */
    private void setPort(int port) {
        portField.setText(String.valueOf(port));
    }
    
    /**
     * 测试默认端口（5432）
     */
    private void testDefaultPort() {
        if (!saveConfigFromUI()) {
            return;
        }
        
        runTest(() -> {
            appendResult("开始测试默认端口 (5432)...\n");
            ConnectionTester.ConnectionTestResult result = ConnectionTester.testDefaultPort(
                config.getHost(), 
                config.getDatabase(), 
                config.getUsername(), 
                config.getPassword()
            );
            appendResult(result.toString() + "\n\n");
        });
    }
    
    /**
     * 测试连接池端口（6543）
     */
    private void testPoolPort() {
        if (!saveConfigFromUI()) {
            return;
        }
        
        runTest(() -> {
            appendResult("开始测试连接池端口 (6543)...\n");
            ConnectionTester.ConnectionTestResult result = ConnectionTester.testPoolPort(
                config.getHost(), 
                config.getDatabase(), 
                config.getUsername(), 
                config.getPassword()
            );
            appendResult(result.toString() + "\n\n");
        });
    }
    
    /**
     * 测试所有端口
     */
    private void testAllPorts() {
        if (!saveConfigFromUI()) {
            return;
        }
        
        runTest(() -> {
            appendResult("开始测试所有端口...\n");
            appendResult("测试端口: " + ConnectionTester.getAllTestPorts() + "\n\n");
            
            List<ConnectionTester.ConnectionTestResult> results = ConnectionTester.testAllPortsDetailed(
                config.getHost(), 
                config.getDatabase(), 
                config.getUsername(), 
                config.getPassword()
            );
            
            for (ConnectionTester.ConnectionTestResult result : results) {
                appendResult(result.toString() + "\n");
            }
            
            appendResult("\n测试完成！\n");
        });
    }
    
    /**
     * 自动选择最佳端口
     */
    private void autoSelectBestPort() {
        if (!saveConfigFromUI()) {
            return;
        }
        
        runTest(() -> {
            appendResult("开始网络环境检测...\n");
            appendResult(NetworkAdapter.getNetworkStatus(config.getHost()) + "\n\n");
            
            int bestPort = NetworkAdapter.getBestPort(config.getHost());
            appendResult("推荐使用端口: " + bestPort + "\n");
            
            setPort(bestPort);
            appendResult("已自动设置为推荐端口，请点击\"应用并保存\"按钮\n\n");
        });
    }
    
    /**
     * 运行测试任务（异步）
     */
    private void runTest(TestTask task) {
        // 禁用所有按钮
        setButtonsEnabled(false);
        
        // 启动进度条
        progressBar.setIndeterminate(true);
        statusLabel.setText("测试中...");
        
        // 异步执行测试
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }
            
            @Override
            protected void done() {
                // 停止进度条
                progressBar.setIndeterminate(false);
                statusLabel.setText("测试完成");
                
                // 恢复按钮
                setButtonsEnabled(true);
            }
        };
        
        worker.execute();
    }
    
    /**
     * 应用并保存配置
     */
    private void applyAndSave() {
        if (!saveConfigFromUI()) {
            return;
        }
        
        config.saveConfig();
        JOptionPane.showMessageDialog(this, 
            "配置已保存！\n\n" + config.getConfigSummary(), 
            "保存成功", 
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    /**
     * 添加结果文本
     */
    private void appendResult(String text) {
        resultArea.append(text);
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }
    
    /**
     * 设置所有按钮的启用状态
     */
    private void setButtonsEnabled(boolean enabled) {
        testDefaultButton.setEnabled(enabled);
        testPoolButton.setEnabled(enabled);
        testAllButton.setEnabled(enabled);
        autoSelectButton.setEnabled(enabled);
        applyButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }
    
    /**
     * 测试任务接口
     */
    @FunctionalInterface
    private interface TestTask {
        void run() throws Exception;
    }
}
