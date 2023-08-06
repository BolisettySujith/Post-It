package com.example.socialapp.repository

import com.example.socialapp.daos.PostDao
import com.google.firebase.firestore.Query

class PostRepository {
    private val postDao = PostDao()

    fun getPostsQuery(): Query {
        return postDao.getPostsQuery()
    }

    fun addPost(text: String, postImageUrl: String) {
        postDao.addPost(text, postImageUrl)
    }

    fun updateLikes(postId: String) {
        postDao.updateLikes(postId)
    }
}