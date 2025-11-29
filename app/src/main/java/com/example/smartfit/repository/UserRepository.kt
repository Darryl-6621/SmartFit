package com.example.smartfit.repository

import com.example.smartfit.data.room.User
import com.example.smartfit.data.room.UserDao
import kotlinx.coroutines.flow.Flow
import android.util.Log

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Boolean {
        if (userDao.getUserByEmail(user.email) != null) return false
        userDao.insertUser(user)
        return true
    }

    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmailAndPassword(email, password)
        if (user != null) {
            Log.d("UserRepository", "Login successful for user ID: ${user.id}")
        } else {
            Log.e("UserRepository", "Login failed: Invalid credentials for $email")
        }
        return user
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    fun getUserFlow(email: String): Flow<User?> {
        return userDao.getUserFlow(email)
    }
}