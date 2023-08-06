package com.example.socialapp.repository

import com.example.socialapp.daos.UserDao
import com.example.socialapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class UserRepository {
    private val userDao = UserDao()

    fun addUser(user: User?) {
        userDao.addUser(user)
    }

    fun getUserById(uId: String): Task<DocumentSnapshot> {
        return userDao.getUserById(uId)
    }

}