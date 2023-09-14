package com.uploadity

import android.content.Context
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
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinApiServiceBuilder
import com.uploadity.database.AppDatabase
import com.uploadity.database.posts.Post
import com.uploadity.databinding.ActivityNewPostBinding
import com.uploadity.tools.UserDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

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
            isInEditMode = false
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

        binding.publishButton.setOnClickListener {
            Log.d("publish", "publish button clicked")
            initializeUpload()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initializeUpload() {
        //TODO datastore get accesstoken i id
        var accessToken = ""
        var userId = ""

        runBlocking {
            accessToken = getStringPreference(getString(R.string.linkedin_access_token_key))
        }

        runBlocking {
            userId = getStringPreference(getString(R.string.linkedin_id_key))
        }

        //val sharedPreferences = this.getPreferences(Context.MODE_PRIVATE) ?: return
        //val accessToken = sharedPreferences.getString(getString(R.string.linkedin_access_token_key), "")
        //val userId = sharedPreferences.getString(getString(R.string.linkedin_id_key), "")
        Log.d("initializeUpload", "accessToken: $accessToken userId: $userId")

        if (accessToken != "") {
            val linkedinApi = LinkedinApiServiceBuilder.buildService(LinkedinApiInterface::class.java)
            val owner = JsonObject()
            val body = JsonObject()
            owner.addProperty("owner", "urn:li:person:$userId")
            body.add("initializeUploadRequest", owner)
            val mediaType = "application/json".toMediaType()

            linkedinApi.initializeImageUpload(
                "Bearer $accessToken",
                body.toString().toRequestBody(mediaType)
            ).enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: retrofit2.Response<ResponseBody>
                ) {
                    Log.d("initializeUpload", "onResponse: ${response.body().toString()}")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("initializeUpload", "onFailure: ${t.message}")
                }
            })
        }
    }

    private fun loadPost(postId: Int) {
        post = appDao.postDao().getPost(postId)!!

        if (post.title!!.isNotEmpty()) {
            binding.titleEditText.setText(post.title, TextView.BufferType.EDITABLE)
        }

        if (post.description!!.isNotEmpty()) {
            binding.descriptionEditText.setText(post.description, TextView.BufferType.EDITABLE)
        }

        if (post.mediaUri!!.isNotEmpty()) {
            isMediaSelected = true

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

        //TODO: to jest funkcja load post a nie publish - do uporządkowania

        if (!post.isPublished) {
            binding.publishButton.visibility = View.VISIBLE
            binding.publishButton.setOnClickListener {
                //TODO OPUBLIKOWAĆ POSTA!!!!!!!!!!!!!!! póki co tylko linkedin jeśli jest połączony
                //TODO jeśli mamy access token i jeśli publikacja posta jest ok to wtedy dopiero update posta dao


                post.isPublished = true
                appDao.postDao().update(post)
                finish()
            }
        }
    }

    private fun savePost() {
        val title = binding.titleEditText
        val description = binding.descriptionEditText

        if (isInEditMode) {
            appDao.postDao().update(
                Post(
                    post.id, title.text.toString(),
                    description.text.toString(), mediaUri.toString(), isPicture,
                    false, ""
                )
            )

        } else {
            if (isMediaSelected) {
                appDao.postDao().insert(
                    Post(
                        0, title.text.toString(),
                        description.text.toString(), mediaUri.toString(), isPicture,
                        false, ""
                    )
                )
            } else {
                appDao.postDao().insert(
                    Post(
                        0, title.text.toString(),
                        description.text.toString(), "", isPicture,
                        false, ""
                    )
                )
            }
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
        }
    }

    private suspend fun getStringPreference(key: String): String {
        val value = UserDataStore(applicationContext).getStringPreference(key)

        Log.d("getStringPreference", "key: $key value: $value")

        return value
    }
}