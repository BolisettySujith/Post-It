package com.example.socialapp.daos

import com.example.socialapp.models.Post
import com.example.socialapp.models.User
import com.example.socialapp.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {
    private val db = FirebaseFirestore.getInstance()
    private val postCollections = db.collection("posts")
    private val auth = Firebase.auth
    private val userRepository = UserRepository()

    fun getPostsQuery(): Query {
        return postCollections.orderBy("createdAt", Query.Direction.DESCENDING)
    }

    fun addPost(text: String, postImageUrl: String) {
        val currentUserId = auth.currentUser!!.uid
        GlobalScope.launch(Dispatchers.IO) {
            val user = userRepository.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime = System.currentTimeMillis()
            val post = Post(text, user, postImageUrl = postImageUrl, createdAt = currentTime)
            postCollections.document().set(post)
        }
    }

    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollections.document(postId).get()
    }
    fun updateLikes(postId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)
            val isLiked = post!!.likedBy.contains(currentUserId)
             if(isLiked) {
                 post.likedBy.remove(currentUserId)
             } else {
                 post.likedBy.add(currentUserId)
             }
            postCollections.document(postId).set(post)
        }

    }
}