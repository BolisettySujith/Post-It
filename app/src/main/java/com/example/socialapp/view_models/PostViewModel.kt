package com.example.socialapp.view_models

import androidx.lifecycle.ViewModel
import com.example.socialapp.repository.PostRepository
import com.google.firebase.firestore.Query

class PostViewModel : ViewModel() {
    private val postRepository = PostRepository()

    fun getPostsQuery(): Query {
        return postRepository.getPostsQuery()
    }

    fun addPost(text: String, postImageUrl: String) {
        postRepository.addPost(text, postImageUrl)
    }

    fun updateLikes(postId: String) {
        postRepository.updateLikes(postId)
    }



}