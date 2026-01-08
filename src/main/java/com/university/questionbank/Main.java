package com.university.questionbank;

import com.university.questionbank.dao.DAOFactory;
import com.university.questionbank.gui.LoginFrame;
import com.university.questionbank.util.SupabaseRestAPI;
import com.university.questionbank.util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // 检查是否指定了数据库类型
        String dbType = System.getProperty("db.type", "supabase");

        // 测试数据库连接
        boolean restApiConnected = testDatabaseConnection();

        if ("supabase".equals(dbType)) {
            // 使用 Supabase REST API 模式
            DAOFactory.setMode(DAOFactory.DataAccessMode.REST_API);
            System.out.println("使用 Supabase REST API 模式");
        } else {
            // 使用 JDBC 模式（性能优化：使用索引和批量查询）
            DAOFactory.setMode(DAOFactory.DataAccessMode.JDBC);
            System.out.println("使用 JDBC 模式（性能优化）");
        }

        // 不使用系统默认外观，保留自定义UI样式
        // try {
        //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // 启动登录界面
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    /**
     * 测试数据库连接，返回是否使用 REST API
     */
    private static boolean testDatabaseConnection() {
        System.out.println("正在测试数据库连接...");

        // 首先测试 REST API
        boolean restApiConnected = SupabaseRestAPI.testConnection();
        if (restApiConnected) {
            System.out.println("REST API 连接成功");
            return true;
        }

        System.out.println("REST API 连接失败，尝试本地数据库...");

        // 测试本地数据库连接
        try {
            boolean jdbcConnected = DatabaseUtil.testConnection();
            System.out.println("本地数据库连接成功");
            return false;
        } catch (Exception e) {
            System.out.println("本地数据库连接失败: " + e.getMessage());

            // 显示错误对话框
            int choice = JOptionPane.showConfirmDialog(null,
                "无法连接到数据库！\n\n" +
                "错误信息：" + e.getMessage() + "\n\n" +
                "可能的原因：\n" +
                "1. 网络防火墙阻止了 PostgreSQL 端口（5432/6543）\n" +
                "2. 本地 SQLite 数据库文件不存在或损坏\n" +
                "3. 需要配置代理或 VPN 访问云端数据库\n\n" +
                "是否继续启动应用？(部分功能可能不可用)",
                "数据库连接警告",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.NO_OPTION) {
                System.exit(0);
            }

            // 默认使用 REST API 模式（虽然连接失败，但至少不会崩溃）
            return true;
        }
    }
}