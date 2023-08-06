package com.example.socialapp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.socialapp.databinding.ActivityCreatePostBinding
import com.example.socialapp.view_models.PostViewModel
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCreatePostBinding
//    private  lateinit var postDao : PostDao
    private lateinit var postViewModel: PostViewModel
    private var imageUri : Uri? = null

    var bmp: Bitmap? = null
    var baos: ByteArrayOutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_post)
        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)
        binding.postButton.setOnClickListener {
            val input = binding.postInput.text.toString().trim()
            if(input.isNotEmpty()) {
                if(imageUri != null){
                    uploadImage(input)
                } else {
                    postViewModel.addPost(input, "")
//                    postDao.addPost(input,"")
                    finish()
                }
            }
        }
        binding.postAddImage.setOnClickListener {
            selectImage()
        }

        val textView = TextView(this)
        textView.text = "Create Post"
        textView.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        textView.textSize = 22F
        textView.setTextColor(resources.getColor(R.color.white));
        textView.setTypeface(null, Typeface.BOLD);
        textView.gravity = Gravity.START;

        supportActionBar?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.customView = textView;
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

        startActivityIfNeeded(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            Log.d("Image uri", data?.data!!.toString())
            binding.addPostImage.setImageURI(imageUri)
        }
    }

    private fun uploadImage(inputText: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading post...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
        } catch (e: IOException) {
            e.printStackTrace();
        }

        baos = ByteArrayOutputStream()

        bmp?.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        val fileInBytes = baos!!.toByteArray()

        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
        storageReference.putBytes(fileInBytes).
                addOnSuccessListener {
                    Toast.makeText(this, "Successfully uploaded the image", Toast.LENGTH_SHORT).show()
                    storageReference.downloadUrl.addOnSuccessListener {
                        Log.d("Download Uri", it.toString())
                        postViewModel.addPost(inputText,it.toString())
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        finish()
                    }

                }.
                addOnFailureListener{
                    Toast.makeText(this, "Failure in uploading the image", Toast.LENGTH_SHORT).show()
                    if(progressDialog.isShowing) progressDialog.dismiss()
                }

    }
}