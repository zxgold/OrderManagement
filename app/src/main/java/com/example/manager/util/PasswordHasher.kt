package com.example.manager.util


object PasswordHasher {
    // 真实项目中，这里应该使用 bcrypt, scrypt, 或 Argon2
    fun hashPassword(password: String): String {
        // 开发初期极度简化：返回明文或简单变换
        // return "hashed_$password" // 示例，绝不能用于生产
        return password // 当前为了方便测试，直接返回
    }

    fun verifyPassword(passwordAttempt: String, storedHash: String): Boolean {
        // 开发初期极度简化：
        // return hashPassword(passwordAttempt) == storedHash
        return passwordAttempt == storedHash // 当前为了方便测试，直接比较
    }
}