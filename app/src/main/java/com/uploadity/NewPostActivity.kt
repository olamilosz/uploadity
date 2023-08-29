package com.uploadity

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.uploadity.database.AppDatabase
import com.uploadity.database.Post
import com.uploadity.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPostBinding
    private lateinit var appDao: AppDatabase
    private lateinit var mediaUri: Uri
    private lateinit var post: Post
    private var isPicture = true
    private var isMediaSelected = false
    private var isInEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDao = AppDatabase.getInstance(this)

        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        toolbar.title = ""
        val supportActionBar = supportActionBar
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowHomeEnabled(true)
        }

        if (intent != null && intent.extras != null) {
            toolbar.title = "Edit Post"
            isInEditMode = true

            if (isInEditMode) {
                binding.cancelButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE

            } else {
                binding.cancelButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE
            }

            loadPost(intent.extras!!.getInt("post_id"))

        } else {
            toolbar.title = "Create New Post"
        }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                chooseMedia(uri)

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        val choosePhotoButton = binding.chooseMediaButton
        choosePhotoButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }

        val saveButton = binding.saveButton
        saveButton.setOnClickListener {
            savePost()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.deleteButton.setOnClickListener {
            val builder: AlertDialog.Builder = this.let {
                AlertDialog.Builder(it)
            }

            builder.setMessage("Are you sure to delete this post?")
            builder.apply {
                setPositiveButton("delete"
                ) { _, _ ->
                    val post = appDao.postDao().getPost(intent.extras!!.getInt("post_id"))
                    if (post != null) {
                        appDao.postDao().delete(post)
                        Snackbar.make(binding.root, "Post successfully deleted", Snackbar.LENGTH_SHORT).show()
                    }

                    finish()
                }

                setNegativeButton("no"
                ) { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadPost(postId: Int) {
        post = appDao.postDao().getPost(postId)!!

        if (post != null) {
            if (post.title!!.isNotEmpty()) {
                binding.titleEditText.setText(post.title, TextView.BufferType.EDITABLE)
            }

            if (post.description!!.isNotEmpty()) {
                binding.descriptionEditText.setText(post.description, TextView.BufferType.EDITABLE)
            }

            if (post.mediaUri!!.isNotEmpty()) {
                if (post.isPicture) {
                    val imageView = binding.imageView
                    imageView.visibility = View.VISIBLE
                    imageView.setImageURI(Uri.parse(post.mediaUri))

                } else {
                    val videoView = binding.videoView
                    videoView.visibility = View.VISIBLE
                    videoView.setVideoURI(Uri.parse(post.mediaUri))

                    val videoControllerView = binding.videoController
                    val videoController = MediaController(this)

                    videoController.setAnchorView(videoControllerView)
                    videoController.setMediaPlayer(videoView)
                    videoView.setMediaController(videoController)
                    videoView.start()
                }
            }
        }
    }

    private fun savePost() {
        val title = binding.titleEditText
        val description = binding.descriptionEditText

        if (isMediaSelected) {
            appDao.postDao().update(
                Post(
                    post.id, title.text.toString(),
                    description.text.toString(), mediaUri.toString(), isPicture
                )
            )

        } else {
            appDao.postDao().insert(
                Post(
                    0, title.text.toString(),
                    description.text.toString(), mediaUri.toString(), isPicture
                )
            )
        }

        finish()
    }


    private fun chooseMedia(uri: Uri) {
        val imageView = binding.imageView
        val videoView = binding.videoView
        mediaUri = uri

        if (uri.path!!.contains("image")) {
            isPicture = true
            isMediaSelected = true
            videoView.visibility = View.GONE
            imageView.setImageURI(uri)
            imageView.visibility = View.VISIBLE

        } else if (uri.path!!.contains("video")) {
            isPicture = false
            isMediaSelected = true
            imageView.visibility = View.GONE
            videoView.setVideoURI(uri)
            videoView.visibility = View.VISIBLE

            val videoControllerView = binding.videoController
            val videoController = MediaController(this)

            videoController.setAnchorView(videoControllerView)
            videoController.setMediaPlayer(videoView)
            videoView.setMediaController(videoController)

            videoController.show()
            videoView.start()

        } else {
            isMediaSelected = false
            Snackbar.make(binding.root, "Invalid media format", Snackbar.LENGTH_SHORT).show()
            //Log.e("chooseMedia", "URI not a video or image: $uri")
        }
    }
}