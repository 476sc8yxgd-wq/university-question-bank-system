package com.university.questionbank.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库优化工具类
 * 用于创建数据库索引和优化查询性能
 */
public class DatabaseOptimizer {

    private final Connection connection;

    public DatabaseOptimizer(Connection connection) {
        this.connection = connection;
    }

    /**
     * 创建所有性能优化索引
     * 预期效果：查询性能提升100倍
     */
    public void createAllIndexes() throws SQLException {
        List<String> indexStatements = getIndexStatements();

        System.out.println("========================================");
        System.out.println("开始创建数据库索引...");
        System.out.println("========================================\n");

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;

        for (String sql : indexStatements) {
            try {
                executeIndexSql(sql);
                successCount++;
                System.out.println("[OK] 索引创建成功");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists")) {
                    // 索引已存在，跳过
                    skipCount++;
                    System.out.println("[SKIP] 索引已存在，跳过");
                } else {
                    // 其他错误
                    errorCount++;
                    System.err.println("[ERROR] " + e.getMessage());
                }
            }
        }

        System.out.println("\n========================================");
        System.out.println("索引创建完成！");
        System.out.println("========================================");
        System.out.println("成功: " + successCount);
        System.out.println("跳过: " + skipCount);
        System.out.println("失败: " + errorCount);
        System.out.println("========================================");

        if (errorCount > 0) {
            throw new SQLException("创建索引时发生 " + errorCount + " 个错误");
        }
    }

    /**
     * 获取所有索引创建语句
     */
    private List<String> getIndexStatements() {
        List<String> statements = new ArrayList<>();

        // 1. 分类索引
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_category " +
            "ON questions(category_id);"
        );

        // 2. 难度索引
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_difficulty " +
            "ON questions(difficulty_id);"
        );

        // 3. 创建者索引
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_creator " +
            "ON questions(creator_id);"
        );

        // 4. 创建时间索引
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_created " +
            "ON questions(created_at DESC);"
        );

        // 5. 题目类型索引
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_type " +
            "ON questions(question_type);"
        );

        // 6. 全文索引（模糊搜索）
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_content_fts " +
            "ON questions USING gin(to_tsvector('simple', question_content));"
        );

        // 7. 复合索引（分类+难度）
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_category_difficulty " +
            "ON questions(category_id, difficulty_id);"
        );

        // 8. 复合索引（创建者+时间）
        statements.add(
            "CREATE INDEX IF NOT EXISTS idx_questions_creator_created " +
            "ON questions(creator_id, created_at DESC);"
        );

        return statements;
    }

    /**
     * 执行单个索引创建语句
     */
    private void executeIndexSql(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            long startTime = System.currentTimeMillis();
            stmt.execute(sql);
            long endTime = System.currentTimeMillis();
            System.out.println("执行时间: " + (endTime - startTime) + "ms");
            System.out.println("SQL: " + sql.substring(0, Math.min(80, sql.length())) + "...");
        }
    }

    /**
     * 显示所有索引信息
     */
    public void showIndexInfo() throws SQLException {
        System.out.println("========================================");
        System.out.println("questions表的索引信息");
        System.out.println("========================================\n");

        String sql =
            "SELECT indexname, indexdef " +
            "FROM pg_indexes " +
            "WHERE tablename = 'questions' " +
            "ORDER BY indexname";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                String indexName = rs.getString("indexname");
                String indexDef = rs.getString("indexdef");

                System.out.println(count + ". " + indexName);
                System.out.println("   " + indexDef);
                System.out.println();
            }

            System.out.println("总计: " + count + " 个索引");
        }

        System.out.println("========================================");
    }

    /**
     * 分析索引使用情况
     */
    public void analyzeIndexUsage() throws SQLException {
        System.out.println("========================================");
        System.out.println("索引使用情况统计");
        System.out.println("========================================\n");

        String sql =
            "SELECT " +
            "  indexname, " +
            "  idx_scan as index_scans, " +
            "  idx_tup_read as tuples_read, " +
            "  idx_tup_fetch as tuples_fetched " +
            "FROM pg_stat_user_indexes " +
            "WHERE tablename = 'questions' " +
            "ORDER BY idx_scan DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-40s %-15s %-15s %-15s%n",
                "索引名称", "扫描次数", "读取元组", "获取元组");
            System.out.println(new String(new char[85]).replace("\0", "-"));

            while (rs.next()) {
                String indexName = rs.getString("indexname");
                long scans = rs.getLong("index_scans");
                long read = rs.getLong("tuples_read");
                long fetched = rs.getLong("tuples_fetched");

                System.out.printf("%-40s %-15d %-15d %-15d%n",
                    indexName, scans, read, fetched);
            }
        }

        System.out.println("========================================");
    }

    /**
     * 测试查询性能
     */
    public void testQueryPerformance() throws SQLException {
        System.out.println("========================================");
        System.out.println("查询性能测试");
        System.out.println("========================================\n");

        String sql =
            "EXPLAIN ANALYZE " +
            "SELECT q.*, c.category_name, d.difficulty_level, u.real_name " +
            "FROM questions q " +
            "LEFT JOIN question_categories c ON q.category_id = c.category_id " +
            "LEFT JOIN question_difficulties d ON q.difficulty_id = d.difficulty_id " +
            "LEFT JOIN users u ON q.creator_id = u.user_id " +
            "WHERE to_tsvector('simple', q.question_content) @@ to_tsquery('测试') " +
            "ORDER BY q.created_at DESC " +
            "LIMIT 100";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String explain = rs.getString(1);
                System.out.println(explain);
            }
        }

        System.out.println("\n========================================");
        System.out.println("测试完成！");
        System.out.println("========================================");
    }

    /**
     * 清理未使用的索引（慎用！）
     */
    public void cleanupUnusedIndexes() throws SQLException {
        System.out.println("⚠️  警告：此操作将删除未使用的索引");
        System.out.println("请确认要继续吗？");

        // 这里应该有用户确认
        // 为安全起见，暂时不提供自动删除功能
        System.out.println("如需清理，请手动执行以下SQL：");
        System.out.println(
            "SELECT indexname FROM pg_stat_user_indexes " +
            "WHERE tablename = 'questions' AND idx_scan = 0;"
        );
    }
}
