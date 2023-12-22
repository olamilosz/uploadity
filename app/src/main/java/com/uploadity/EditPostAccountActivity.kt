package com.uploadity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import com.google.gson.JsonObject
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinApiServiceBuilder
import com.uploadity.database.AppDatabase
import com.uploadity.database.postaccount.PostAccount
import com.uploadity.databinding.ActivityEditPostAccountBinding
import com.uploadity.tools.SocialMediaPlatforms
import com.uploadity.tools.UserDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder
import java.util.Locale

class EditPostAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostAccountBinding
    private lateinit var appDao: AppDatabase
    private var postAccount: PostAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        val supportActionBar = supportActionBar
        toolbar.title = ""
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowHomeEnabled(true)
        }

        appDao = AppDatabase.getInstance(this)
        val postId: Int?
        val accountId: Int?
        val blogId: Int?
        val extras = intent.extras

        if (extras != null) {
            postId = extras.getInt("postId")
            accountId = extras.getInt("accountId")
            blogId = extras.getInt("blogId")
            postAccount = appDao.postAccountDao().getPostAccount(postId, accountId, blogId)
            loadPostAccount(toolbar)

            Log.d("editpostactivity", "postAccount $postAccount post $postId account $accountId blog $blogId")
        }

        val updateButton = binding.updateButton
        updateButton.setOnClickListener {
            updatePostAccount()
        }

        val descriptionCharacterCountTextView = binding.descriptionCharacterCount
        val descriptionEditText = binding.descriptionEditText
        descriptionEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    descriptionCharacterCountTextView.text = "${p0.count()} / 280"

                    if (postAccount != null) {
                        binding.updateButton.isEnabled = postAccount!!.description != p0.toString()
                    }
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun loadPostAccount(toolbar: androidx.appcompat.widget.Toolbar) {
        if (postAccount != null) {
            toolbar.title = "Edytuj publikację ${postAccount!!.socialMediaPlatformName.replaceFirstChar { 
                it.uppercase()
            }}"

            val description = postAccount!!.description
            binding.accountNameText.text = postAccount!!.name
            binding.descriptionEditText.setText(description)
            binding.descriptionCharacterCount.text = "${description.length} / 3000"
        }
    }

    private fun updatePostAccount() {
        if (postAccount != null) {
            when (postAccount!!.socialMediaPlatformName) {
            SocialMediaPlatforms.LINKEDIN.platformName -> {
                val userDataStore = UserDataStore(this)
                var accessToken = ""

                runBlocking {
                    accessToken =
                        userDataStore.getStringPreference(getString(R.string.linkedin_access_token_key))
                }

                if (accessToken.isNotEmpty()) {
                    val linkedinApi = LinkedinApiServiceBuilder.buildService(LinkedinApiInterface::class.java)
                    val commentaryJson = JsonObject()
                    commentaryJson.addProperty("commentary", binding.descriptionEditText.text.toString())

                    val setJson = JsonObject()
                    setJson.add("\$set", commentaryJson)
                    Log.d("JSON LINKEDIN EDIT", setJson.toString())

                    val mediaType = "application/json".toMediaType()
                    val requestBody = setJson.toString().toRequestBody(mediaType)

                    linkedinApi.editPost(
                        "Bearer $accessToken",
                        //postAccount!!.publishedPostId,
                        "urn:li:share:7133196897040654336",
                        requestBody
                    ).enqueue(object : Callback<ResponseBody>{
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {

                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                        }
                    })
                }
            }

            SocialMediaPlatforms.TUMBLR.platformName -> {
                //tumblr i blog
            }
            }
        }
    }
}