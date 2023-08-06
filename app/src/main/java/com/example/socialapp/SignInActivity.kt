package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.socialapp.databinding.ActivitySignInBinding
import com.example.socialapp.models.User
import com.example.socialapp.view_models.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN: Int =  123
    }

    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.init(googleSignInClient, auth)

        binding.signInButton.setOnClickListener{
            signIn()
        }

        userViewModel.signInResult.observe(this, Observer { success ->
            if (success) {
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                finish()
            } else {
                binding.createAccountText.visibility = View.VISIBLE
                binding.signInButton.visibility = View.VISIBLE
                binding.progressbar.visibility = View.GONE
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Companion.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Request Code", requestCode.toString())
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d("Task data", data.toString())
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("Firebase", "FirebaseAuth with google id:"+account.id)
            userViewModel.signInWithGoogle(account)
            binding.signInButton.visibility = View.GONE
            binding.createAccountText.visibility = View.GONE
            binding.progressbar.visibility = View.VISIBLE
        } catch (e: ApiException) {
            Log.d("API Exception", e.message.toString())
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser!= null) {

            val user = User(firebaseUser.uid, firebaseUser.displayName.toString(), firebaseUser.photoUrl.toString())

            userViewModel.addUser(user)
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            binding.signInButton.visibility = View.VISIBLE
            binding.createAccountText.visibility = View.VISIBLE
            binding.progressbar.visibility = View.GONE
        }
    }

}