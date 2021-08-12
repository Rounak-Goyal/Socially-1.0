package com.example.socially.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.socially.R
import com.example.socially.daos.PostDao
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    lateinit var postButton: Button
    lateinit var postInput: EditText
    private lateinit var postDao: PostDao
    lateinit var toolBar: Toolbar
    lateinit var chooseImageButton : Button
    lateinit var uploadButton: Button

    companion object{
        private val PICK_IMAGE_REQUEST = 71
        private var filePath: Uri? = null
        private var firebaseStore: FirebaseStorage? = null
        private var storageReference: StorageReference? = null
        private var postImageUri: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        //
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)
        //
        postButton = findViewById(R.id.postButton)
        postInput = findViewById(R.id.postInput)
        chooseImageButton = findViewById(R.id.btn_choose_image)
        uploadButton = findViewById(R.id.btn_upload_image)


        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        chooseImageButton.setOnClickListener {
            launchGallery()
        }
        uploadButton.setOnClickListener {
            if(filePath!=null) {
                uploadImage(filePath)
            }
        }


        postDao = PostDao()

        postButton.setOnClickListener {
            val input = postInput.text.toString().trim()
            val imageUri = postImageUri.toString()

            if (input.isNotEmpty()){
                postDao.addPost(input,imageUri) //pass text,image
                finish()
            }else{
                finish()
                Toast.makeText(this, "Post is discarded!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                uploadImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Get the Uri of data
            filePath = data.data
            uploadImage(filePath)
        }
    }


    private fun uploadImage(fileUri: Uri?) {
        if (fileUri != null) {
            val fileName = UUID.randomUUID().toString() +".jpg"

            //val database = FirebaseDatabase.getInstance()//not required
            val refStorage = storageReference!!.child("images/$fileName")

            refStorage.putFile(fileUri)
                .addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            postImageUri = it.toString()

                        }
                    })

                .addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })

        }
        else{
            Toast.makeText(this, "some error occurred!", Toast.LENGTH_SHORT).show()
        }


    }


}