package com.example.socially.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.socially.R
import com.example.socially.adapter.IPostAdapter
import com.example.socially.adapter.PostAdapter
import com.example.socially.daos.PostDao
import com.example.socially.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(),IPostAdapter {

    lateinit var fab: FloatingActionButton
    lateinit var postDao: PostDao
    lateinit var adapter: PostAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var toolBar: Toolbar

    //--------------WrapContentLinearLayoutManager created to avoid backtrace error
    class WrapContentLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("TAG", "meet a error in RecyclerView")
            }
        }
    }
    //-------------


    //menu onCreate
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout, menu)
        return true
    }
    //menu option selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                //logOut
                Firebase.auth.signOut()
                val intent = Intent(this,SignInActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(this, "LogOut", Toast.LENGTH_SHORT).show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)

        fab.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
        //set recyclerView
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postsCollections = postDao.postCollection
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions,this)
        recyclerView.adapter = adapter

        //WrapContentLinearLayoutManager instance created
        val wrapContentLinearLayoutManager = WrapContentLinearLayoutManager(this)
        recyclerView.layoutManager = wrapContentLinearLayoutManager
    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


    override fun onLikeClicked(postId: String){
        postDao.updateLikes(postId)
    }
}
