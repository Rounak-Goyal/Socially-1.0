package com.example.socially.daos

import com.example.socially.models.Post
import com.example.socially.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {

    private val db = FirebaseFirestore.getInstance()
    val postCollection = db.collection("posts")
    private val auth = Firebase.auth

    fun addPost(text: String,image: String){
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid     //if null op. app =>crashes
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!   //if null op. app =>crashes

            val currentTime = System.currentTimeMillis()
            val post = Post(text,image,user,currentTime)
            postCollection.document().set(post)
        }
    }

    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollection.document(postId).get()
    }

    fun updateLikes(postId:String){
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid                                 //if null op. app =>crashes
            val post = getPostById(postId).await().toObject(Post::class.java)!!         //if null op. app =>crashes
            val isLiked = post.likedBy.contains(currentUserId)

            if(isLiked){
                post.likedBy.remove(currentUserId)
            }else{
                post.likedBy.add(currentUserId)
            }
            postCollection.document(postId).set(post)
        }
    }
}