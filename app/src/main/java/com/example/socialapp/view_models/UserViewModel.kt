package com.example.socialapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.socialapp.models.User
import com.example.socialapp.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val _signInResult = MutableLiveData<Boolean>()
    val signInResult: LiveData<Boolean> = _signInResult


    fun init(googleSignInClient: GoogleSignInClient, auth: FirebaseAuth) {
        this.googleSignInClient = googleSignInClient
        this.auth = auth
    }

    fun signInWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val authResult = auth.signInWithCredential(credential).await()
                    val firebaseUser = authResult.user
                    val user = firebaseUser?.let { User(it.uid, firebaseUser.displayName.toString(), firebaseUser.photoUrl.toString()) }
                    addUser(user)
                    _signInResult.postValue(true)
                } catch (e: Exception) {
                    _signInResult.postValue(false)
                }
            }
        } else {
            _signInResult.postValue(false)
        }
    }

    fun addUser(user: User?) {
        userRepository.addUser(user)
    }
}