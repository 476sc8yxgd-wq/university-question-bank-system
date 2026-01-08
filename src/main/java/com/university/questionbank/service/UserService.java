package com.university.questionbank.service;

import com.university.questionbank.dao.RoleDAO;
import com.university.questionbank.dao.UserDAO;
import com.university.questionbank.dao.QuestionDAO;
import com.university.questionbank.dao.DAOFactory;
import com.university.questionbank.model.Role;
import com.university.questionbank.model.User;
import com.university.questionbank.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO = DAOFactory.createUserDAO();
    private RoleDAO roleDAO = DAOFactory.createRoleDAO();
    private QuestionDAO questionDAO = DAOFactory.createQuestionDAO();

    // 用户登录认证
    public User login(String username, String password) throws SQLException {
        User user;

        // 如果是 REST API 模式，使用专门的 login 方法
        if (userDAO instanceof com.university.questionbank.dao.UserRestDAO) {
            com.university.questionbank.dao.UserRestDAO restDAO = (com.university.questionbank.dao.UserRestDAO) userDAO;
            user = restDAO.login(username, password);
        } else {
            // JDBC 模式的登录逻辑
            user = userDAO.getUserByUsername(username);
            if (user == null) {
                throw new SQLException("用户名不存在");
            }

            if (user.getStatus() == 0) {
                throw new SQLException("用户已被禁用");
            }

            // 验证密码
            if (!PasswordUtil.checkPassword(password, user.getPassword())) {
                throw new SQLException("密码错误");
            }
        }

        if (user == null) {
            throw new SQLException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new SQLException("用户已被禁用");
        }

        // 对于 REST API 模式，需要单独查询 Role 对象
        if (user.getRole() == null && user instanceof com.university.questionbank.model.User) {
            try {
                Role role = roleDAO.getRoleById(user.getRoleId());
                user.setRole(role);
            } catch (Exception e) {
                // 如果查询 Role 失败，创建一个默认的 Role
                System.out.println("警告：无法查询用户角色，使用默认角色");
                Role defaultRole = new Role();
                defaultRole.setRoleId(user.getRoleId());
                defaultRole.setRoleName("未知角色");
                user.setRole(defaultRole);
            }
        }

        return user;
    }

    // 根据用户ID获取用户
    public User getUserById(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }

    // 获取所有用户
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    // 添加用户
    public void addUser(User user) throws SQLException {
        // 检查用户名是否已存在
        User existingUser = userDAO.getUserByUsername(user.getUsername());
        if (existingUser != null) {
            throw new SQLException("用户名已存在");
        }
        
        // 对密码进行加密
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        
        userDAO.addUser(user);
    }

    // 更新用户
    public void updateUser(User user) throws SQLException {
        // 如果密码不为空，则加密后更新
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 检查是否为新密码（不是加密后的密码）
            if (!user.getPassword().startsWith("$2a$")) {
                user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            }
        }
        
        userDAO.updateUser(user);
    }

    // 删除用户
    public void deleteUser(int userId) throws SQLException {
        userDAO.deleteUser(userId);
    }

    // 检查用户是否有关联的题目
    public boolean userHasQuestions(int userId) throws SQLException {
        List<?> questions = questionDAO.searchQuestions(null, null, null, null);
        for (Object q : questions) {
            if (q instanceof com.university.questionbank.model.Question) {
                com.university.questionbank.model.Question question = (com.university.questionbank.model.Question) q;
                if (question.getCreatorId() == userId) {
                    return true;
                }
            }
        }
        return false;
    }

    // 删除用户创建的所有题目
    public void deleteUserQuestions(int userId) throws SQLException {
        List<?> questions = questionDAO.searchQuestions(null, null, null, null);
        for (Object q : questions) {
            if (q instanceof com.university.questionbank.model.Question) {
                com.university.questionbank.model.Question question = (com.university.questionbank.model.Question) q;
                if (question.getCreatorId() == userId) {
                    questionDAO.deleteQuestion(question.getQuestionId());
                }
            }
        }
    }

    // 启用/禁用用户
    public void updateUserStatus(int userId, int status) throws SQLException {
        userDAO.updateUserStatus(userId, status);
    }
}