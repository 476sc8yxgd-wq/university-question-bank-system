package com.university.questionbank.dao;

import com.university.questionbank.dao.impl.UserDAOImpl;
import com.university.questionbank.dao.impl.QuestionDAOImpl;
import com.university.questionbank.dao.impl.RoleDAOImpl;
import com.university.questionbank.dao.impl.QuestionCategoryDAOImpl;
import com.university.questionbank.dao.impl.QuestionDifficultyDAOImpl;
import java.util.logging.Logger;

/**
 * DAO 工厂类
 * 根据配置创建不同的 DAO 实现（JDBC 或 REST API）
 */
public class DAOFactory {
    private static final Logger logger = Logger.getLogger(DAOFactory.class.getName());

    // 数据访问模式：REST_API 或 JDBC
    private static DataAccessMode mode = DataAccessMode.JDBC;

    public enum DataAccessMode {
        REST_API,
        JDBC
    }

    /**
     * 设置数据访问模式
     */
    public static void setMode(DataAccessMode mode) {
        DAOFactory.mode = mode;
        logger.info("数据访问模式设置为: " + mode);
    }

    /**
     * 获取当前数据访问模式
     */
    public static DataAccessMode getMode() {
        return mode;
    }

    /**
     * 检查是否使用 REST API 模式
     */
    public static boolean isRestAPIMode() {
        return mode == DataAccessMode.REST_API;
    }

    /**
     * 创建用户 DAO
     */
    public static UserDAO createUserDAO() {
        if (mode == DataAccessMode.REST_API) {
            logger.info("创建 UserRestDAO");
            return new UserRestDAO();
        } else {
            logger.info("创建 JDBC UserDAO");
            return new UserDAOImpl();
        }
    }

    /**
     * 创建题目 DAO
     */
    public static QuestionDAO createQuestionDAO() {
        if (mode == DataAccessMode.REST_API) {
            logger.info("创建 QuestionRestDAO");
            return new QuestionRestDAO();
        } else {
            logger.info("创建 JDBC QuestionDAO");
            return new QuestionDAOImpl();
        }
    }

    /**
     * 创建角色 DAO
     */
    public static RoleDAO createRoleDAO() {
        if (mode == DataAccessMode.REST_API) {
            logger.info("创建 RoleRestDAO");
            return new RoleRestDAO();
        } else {
            logger.info("创建 JDBC RoleDAO");
            return new RoleDAOImpl();
        }
    }

    /**
     * 创建题目分类 DAO
     */
    public static QuestionCategoryDAO createQuestionCategoryDAO() {
        if (mode == DataAccessMode.REST_API) {
            logger.info("创建 QuestionCategoryRestDAO");
            return new QuestionCategoryRestDAO();
        } else {
            logger.info("创建 JDBC QuestionCategoryDAO");
            return new QuestionCategoryDAOImpl();
        }
    }

    /**
     * 创建题目难度 DAO
     */
    public static QuestionDifficultyDAO createQuestionDifficultyDAO() {
        if (mode == DataAccessMode.REST_API) {
            logger.info("创建 QuestionDifficultyRestDAO");
            return new QuestionDifficultyRestDAO();
        } else {
            logger.info("创建 JDBC QuestionDifficultyDAO");
            return new QuestionDifficultyDAOImpl();
        }
    }
}
