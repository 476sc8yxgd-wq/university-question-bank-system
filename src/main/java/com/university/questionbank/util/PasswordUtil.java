package com.university.questionbank.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // 生成密码哈希
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    // 验证密码
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}