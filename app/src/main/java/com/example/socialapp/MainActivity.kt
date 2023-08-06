package com.example.socialapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.socialapp.daos.PostDao
import com.example.socialapp.databinding.ActivityMainBinding
import com.example.socialapp.models.Post
import com.example.socialapp.view_models.PostViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), IPostAdapter,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding;
    private lateinit var adapter: PostAdapter
    private lateinit var postDao : PostDao
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private lateinit var postViewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.navView.setNavigationItemSelectedListener(this)

        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(
         toggle
        )
        toggle.syncState()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        val currentUser = auth.currentUser
        Log.d("user", currentUser?.displayName!!)
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val hView: View = navigationView.getHeaderView(0)
        Glide.with(this).load(currentUser.photoUrl).circleCrop().into(hView.findViewById(R.id.nav_profile_image))
        hView.findViewById<TextView>(R.id.nav_profile_name).text = currentUser.displayName
        hView.findViewById<TextView>(R.id.nav_profile_email).text = currentUser.email

        binding.fab.setOnClickListener{
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    logoutDialog()
                    true
                }
                else -> false
            }
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val query = postViewModel.getPostsQuery()
        val options = FirestoreRecyclerOptions.Builder<Post>()
            .setQuery(query, Post::class.java)
            .build()

        adapter = PostAdapter(options, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postViewModel.updateLikes(postId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                logoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutDialog() {
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogStyle)

        dialog.setTitle("Logout !!")
            .setMessage("Do you really want to log out ?")
            .setPositiveButton("YES") { dialog, whichButton ->
                Log.d("Logout", "Logging out")
                logOut()
            }
            .setNegativeButton("NO") { dialog, whichButton ->
                // DO YOUR STAFF
                 dialog.cancel()
            }
        dialog.show()
    }

    private fun logOut() {
        Firebase.auth.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            // After revoking the access, proceed to sign in again
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_logout -> logoutDialog()
            R.id.nav_home -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
            R.id.nav_settings -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}