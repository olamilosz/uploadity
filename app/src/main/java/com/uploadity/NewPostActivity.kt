package com.uploadity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinApiServiceBuilder
import com.uploadity.api.linkedin.datamodels.Content
import com.uploadity.api.linkedin.datamodels.CreatePostParams
import com.uploadity.api.linkedin.datamodels.Distribution
import com.uploadity.api.linkedin.datamodels.Media
import com.uploadity.api.tumblr.TumblrApiInterface
import com.uploadity.api.tumblr.TumblrApiServiceBuilder
import com.uploadity.api.tumblr.datamodels.TumblrCreatePostParams
import com.uploadity.api.twitter.TwitterApiInterface
import com.uploadity.api.twitter.TwitterApiServiceBuilder
import com.uploadity.api.twitter.datamodels.CreateTwitterPostParams
import com.uploadity.api.twitter.datamodels.MediaObject
import com.uploadity.api.twitter.tools.TwitterApiTools
import com.uploadity.database.AppDatabase
import com.uploadity.database.AppDatabaseRepository
import com.uploadity.database.accounts.Account
import com.uploadity.database.blogs.Blog
import com.uploadity.database.postaccount.PostAccount
import com.uploadity.database.posts.Post
import com.uploadity.databinding.ActivityNewPostBinding
import com.uploadity.tools.SocialMediaPlatforms
import com.uploadity.tools.UserDataStore
import com.uploadity.ui.uicomponents.PostAccountItemListAdapter
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class NewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPostBinding
    private lateinit var appDao: AppDatabase
    private lateinit var mediaUri: Uri
    private lateinit var post: Post
    private lateinit var appDatabase: AppDatabaseRepository
    private var accountsChipMap = mutableMapOf<Int, Account>()
    private var blogChipMap = mutableMapOf<Int, Blog>()
    private var checkedChipIds = mutableListOf<Int>()
    private var isPicture = true
    private var isMediaSelected = false
    private var isInEditMode = false
    private var wasMediaChanged = false

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDao = AppDatabase.getInstance(this)
        appDatabase = (application as UploadityApplication).repository
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        val supportActionBar = supportActionBar
        toolbar.title = ""
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowHomeEnabled(true)
        }

        if (intent != null && intent.extras != null) {
            toolbar.title = "Edit Post"
            isInEditMode = true
            
            binding.cancelButton.visibility = View.GONE
            binding.deleteButton.visibility = View.VISIBLE

            loadPost(intent.extras!!.getInt("post_id"))

        } else {
            toolbar.title = "Create New Post"
            isInEditMode = false
            post = Post(0, "", "", "", true, false, "")
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

            if (isMediaSelected) {
                finish()
            }
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.deleteButton.setOnClickListener {
            val builder: AlertDialog.Builder = this.let {
                AlertDialog.Builder(it)
            }

            builder.setMessage("Czy na pewno chcesz usunąć post?")
            builder.apply {
                setPositiveButton("Usuń") { _, _ ->
                    val post = appDao.postDao().getPost(intent.extras!!.getInt("post_id"))
                    if (post != null) {
                        deletePost(post)
                    }

                    finish()
                }

                setNegativeButton("Nie") { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
        }

        binding.publishButton.setOnClickListener {
            if (isMediaSelected) {
                Log.d("publish", "publish button clicked")
                initializeUpload()

            } else {
                Snackbar.make(binding.root, "Wybierz zdjęcie", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.publishTumblrButton.setOnClickListener {
            if (isMediaSelected) {
                Log.d("publish", "publish tumblr button clicked")
                //publishTumblrPost()

            } else {
                Snackbar.make(binding.root, "Select media", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.publishTwitterButton.setOnClickListener {
            if (isMediaSelected) {
                Log.d("publish", "twitter publish button clicked")
                publishTwitterPost()

            } else {
                Snackbar.make(binding.root, "Select media", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.publishPostButton.setOnClickListener {
            if (isMediaSelected) {
                Log.d("publish", "publish button clicked")
                publishToSelectedAccounts()

            } else {
                Snackbar.make(binding.root, "Select media", Snackbar.LENGTH_SHORT).show()
            }
        }

        val descriptionCharacterCountTextView = binding.descriptionCharacterCount
        descriptionCharacterCountTextView.text = "0 / 280"

        val descriptionEditText = binding.descriptionEditText

        descriptionEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    descriptionCharacterCountTextView.text = "${p0.count()} / 280"
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        val accountChipGroup = binding.accountChipGroup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!post.isPublished) {
                addAccountChips(accountChipGroup)
            }
        }

        accountChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.d("CHIP GROUP", "checked Ids $checkedIds")
            checkedChipIds = checkedIds
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun publishToSelectedAccounts() {
        Log.d("PUBLISH TO ALL", "")

        if (checkedChipIds.isNotEmpty()) {
            savePost()
            post.isPublished = true
            appDao.postDao().update(post)

            for (accountChip in accountsChipMap) {
                if (checkedChipIds.contains(accountChip.key)) {
                    val account = accountChip.value

                    when (account.socialMediaServiceName) {
                        SocialMediaPlatforms.LINKEDIN.platformName -> {
                            Log.d("LINKEDIN ACCOUNT", account.name.toString())

                            initializeUpload()
                        }

                        SocialMediaPlatforms.TWITTER.platformName -> {
                            Log.d("TWITTER ACCOUNT", account.name.toString())

                            publishTwitterPost()
                        }
                    }
                }
            }

            for (blogChip in blogChipMap) {
                if (checkedChipIds.contains(blogChip.key)) {
                    val blog = blogChip.value
                    Log.d("BLOG", blog.name)

                    publishTumblrPost(blog.id)
                }
            }

            finish()

        } else {
            Snackbar.make(binding.root, "Wybierz przynajmniej jedno konto", Snackbar.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addAccountChips(chipGroup: ChipGroup) {
        val accountList = appDao.accountDao().getAllAccounts()
        val noAccountsConnectedView = binding.noAccountsConnected

        if (accountList.isNotEmpty()) {
            val context = this
            chipGroup.visibility = View.VISIBLE
            noAccountsConnectedView.visibility = View.GONE

            for (account in accountList) {
                if (account.socialMediaServiceName == SocialMediaPlatforms.TUMBLR.platformName) {
                    //TODO: iteracja po blogach i osobne chipy
                    val blogList = appDao.blogDao().getBlogsByAccountId(account.id)

                    if (blogList.isNotEmpty()) {
                        for (blog in blogList) {
                            val blogChip = layoutInflater.inflate(R.layout.account_chip, chipGroup, false) as Chip

                            blogChip.apply {
                                text = blog.name
                                isCheckable = true
                                isCheckedIconVisible = false
                                isChecked = false
                                chipIcon = AppCompatResources.getDrawable(context, R.drawable.ic_tumblr)
                            }

                            chipGroup.addView(blogChip)
                            blogChipMap[blogChip.id] = blog
                            Log.d("CHIP CREATE", "BLOG id ${blogChip.id} NAME ${blogChip.text}")
                        }
                    }

                } else {
                    val accountChip = layoutInflater.inflate(R.layout.account_chip, chipGroup, false) as Chip

                    accountChip.apply {
                        text = account.name
                        isCheckable = true
                        isCheckedIconVisible = false
                        isChecked = false
                    }

                    when (account.socialMediaServiceName) {
                        SocialMediaPlatforms.LINKEDIN.platformName -> {
                            accountChip.chipIcon = AppCompatResources.getDrawable(context, R.drawable.ic_linkedin)
                        }

                        SocialMediaPlatforms.TWITTER.platformName -> {
                            accountChip.chipIcon = AppCompatResources.getDrawable(context, R.drawable.ic_twitter)
                        }
                    }

                    chipGroup.addView(accountChip)
                    accountsChipMap[accountChip.id] = account
                    Log.d("CHIP CREATE", "id ${accountChip.id} NAME ${accountChip.text}")
                }
            }

        } else {
            chipGroup.visibility = View.GONE
            noAccountsConnectedView.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun publishTwitterPost() {
        val parameterMap = mutableMapOf<String, String>()
        var accessToken = ""
        var accessTokenSecret = ""

        runBlocking {
            accessToken = getStringPreference(getString(R.string.twitter_access_token_key))
        }

        runBlocking {
            accessTokenSecret = getStringPreference(getString(R.string.twitter_access_token_secret_key))
        }

        if (accessToken.isNotEmpty() && accessTokenSecret.isNotEmpty()) {
            parameterMap["oauth_token"] = accessToken
            parameterMap["media_category"] = "tweet_image"

            Log.d("publishTwitterPost", "parameterMap: $parameterMap")

            val authorizationHeader = TwitterApiTools().generateAuthorizationHeader(
                "https://upload.twitter.com/1.1/media/upload.json",
                parameterMap, accessTokenSecret)

            val file = createImageFile()
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("media", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
                .build()
            val request = Request.Builder()
                .url("https://upload.twitter.com/1.1/media/upload.json?media_category=tweet_image")
                .post(requestBody)
                .addHeader("Authorization", authorizationHeader)
                .build()

            client.newCall(request).enqueue(object: okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("twitter image failure", e.message.toString())
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body.string()
                    Log.d("twitter upload", "response $response")
                    Log.d("twitter upload", "response $body")

                    if (response.code == 200) {
                        try {
                            val jsonObject = JSONObject(body)
                            val mediaId = jsonObject.getString("media_id_string")

                            if (mediaId.isNotEmpty()) {
                                Log.d("media ID", "media ID: $mediaId")
                                createTwitterPost(mediaId, accessToken, accessTokenSecret)
                            }

                        } catch (e: JSONException) {
                            Log.e("JSONException", "uploadingMedia ${e.message}")
                        }
                    }
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTwitterPost(mediaId: String, accessToken: String, accessTokenSecret: String) {
        val twitterApi = TwitterApiServiceBuilder.buildService(TwitterApiInterface::class.java)
        val parameterMap = mutableMapOf<String, String>()
        parameterMap["oauth_token"] = accessToken
        val authorizationHeader = TwitterApiTools().generateAuthorizationHeader(
            "https://api.twitter.com/2/tweets",
            parameterMap,
            accessTokenSecret
        )

        val twitterAccount = appDao.accountDao()
            .getAccountBySocialMediaName(SocialMediaPlatforms.TWITTER.platformName)

        twitterApi.createTwitterPost(
            authorizationHeader,
            CreateTwitterPostParams(
                binding.descriptionEditText.text.toString(),
                MediaObject(arrayOf(mediaId))
            )

        ).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()

                if (responseBody != null) {
                    Log.d("twitter post response", "response ${responseBody.string()}")
                }

                if (twitterAccount != null) {
                    //post account success
                    appDao.postAccountDao().insert(
                        PostAccount(
                            post.id,
                            twitterAccount.id,
                            -1,
                            true,
                            null,
                            SocialMediaPlatforms.TWITTER.platformName,
                            twitterAccount.name ?: ""
                        )
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("twitter post failure", t.message.toString())

                if (twitterAccount != null) {
                    //post account failure
                    appDao.postAccountDao().insert(
                        PostAccount(
                            post.id,
                            twitterAccount.id,
                            -1,
                            false,
                            null,
                            SocialMediaPlatforms.TWITTER.platformName,
                            twitterAccount.name ?: ""
                        )
                    )
                }
            }
        })
    }

    private fun deletePost(post: Post) {
        val postId = post.id
        val imageFile = File(applicationContext.filesDir, "post_$postId.png")

        if (imageFile.exists()) {
            imageFile.delete()
        }

        appDao.postAccountDao().deletePostAccountsByPostId(postId)
        appDao.postDao().delete(post)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun publishTumblrPost(blogId: Int) {
        var accessToken = ""

        runBlocking {
            accessToken = getStringPreference(getString(R.string.tumblr_access_token_key))
        }

        val blog = appDao.blogDao().getBlogById(blogId)

        if (blog != null && accessToken.isNotEmpty()) {
            val tumblrApi = TumblrApiServiceBuilder.buildService(TumblrApiInterface::class.java)

            //image to base64
            if (Build.VERSION.SDK_INT > 28) {
                val source = ImageDecoder.createSource(contentResolver, mediaUri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val byteArray = baos.toByteArray()
                val string = Base64.encodeToString(byteArray, Base64.DEFAULT)

                //Log.d("image BASE64", string)
/*
                val body = JsonObject()
                //body.add("initializeUploadRequest", owner)
                val mediaType = "application/json".toMediaType()*/


                tumblrApi.createPost(
                    authorization = "Bearer $accessToken",
                    blogIdentifier = blog.uuid,
                    requestBody = TumblrCreatePostParams(
                        "photo",
                        binding.descriptionEditText.text.toString(),
                        string
                    )
                ).enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        //Log.d("tumblr image code", response.code().toString())
                        //Log.d("tumblr image msg", response.message())

                        //post account success
                        appDao.postAccountDao().insert(
                            PostAccount(
                                post.id,
                                blog.accountId,
                                blogId,
                                true,
                                null,
                                SocialMediaPlatforms.TUMBLR.platformName,
                                blog.name
                            )
                        )
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("tumblr image failure", t.message.toString())

                        //post account failure
                        appDao.postAccountDao().insert(
                            PostAccount(
                                post.id,
                                blog.accountId,
                                blogId,
                                false,
                                null,
                                SocialMediaPlatforms.TUMBLR.platformName,
                                blog.name
                            )
                        )
                    }
                })

            } else {
                //TODO: zrobić wersję dla niższego SDK
            }
        }
    }

    private fun initializeUpload() {
        var accessToken = ""
        var userId = ""

        runBlocking {
            accessToken = getStringPreference(getString(R.string.linkedin_access_token_key))
        }

        runBlocking {
            userId = getStringPreference(getString(R.string.linkedin_id_key))
        }

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

            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    val responseModel = response.body()?.string()
                    val jsonObject = JSONObject(responseModel ?: "")
                    val uploadUrl = JSONObject(jsonObject["value"].toString())["uploadUrl"].toString()
                    val imageId = JSONObject(jsonObject["value"].toString())["image"].toString()
                    Log.d("initializeUpload", "onResponse value: $uploadUrl")

                    if (responseModel != null && mediaUri.toString().isNotEmpty()) {
                        uploadPicture(uploadUrl, imageId)

                    } else {
                        Log.e("initializeImageUpload", "Couldn't extract uploadUrl value")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("initializeUpload", "onFailure: ${t.message}")
                }
            })
        }
    }

    private fun uploadPicture(uploadUrl: String, imageId: String) {
        val client = OkHttpClient()
        val file = createImageFile()
        var accessToken = ""

        runBlocking {
            accessToken = getStringPreference(getString(R.string.linkedin_access_token_key))
        }

        val request = Request.Builder()
            .header("Authorization", "Bearer $accessToken")
            .url(uploadUrl)
            .put(file.asRequestBody("image/png".toMediaTypeOrNull()))
            .build()

        Log.e("uploadPicture request", request.body.toString())
        Log.e("uploadPicture request", request.method)
        Log.e("uploadPicture request", request.url.toString())
        Log.e("uploadPicture request", request.headers.name(0))
        Log.e("uploadPicture request", request.header("Authorization").toString())

        client.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("uploadPicture onFailure", e.message.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d("uploadPicture onResponse", "code ${response.code}")
                Log.d("uploadPicture onResponse", "headers ${response.headers}")

                publishPost(imageId, accessToken)
            }
        })
    }

    private fun publishPost(imageId: String, accessToken: String) {
        var userId = ""
        val description = binding.descriptionEditText.text.toString()
        val title = binding.titleEditText.toString()
        val linkedinApi = LinkedinApiServiceBuilder.buildService(LinkedinApiInterface::class.java)
        val linkedinAccount = appDao.accountDao().getAccountBySocialMediaName(SocialMediaPlatforms.LINKEDIN.platformName)

        runBlocking {
            userId = getStringPreference(getString(R.string.linkedin_id_key))
        }

        if (userId != "") {
            linkedinApi.createPost(
                accessToken,
                CreatePostParams(
                    "urn:li:person:$userId",
                    description,
                    "PUBLIC",
                    distribution = Distribution(
                        "MAIN_FEED",
                        emptyArray(),
                        emptyArray()
                    ),
                    content = Content(
                        media = Media(
                            title,
                            imageId
                        )
                    ),
                    "PUBLISHED",
                    false
                )
            ).enqueue(object: Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d("publishPost onResponse", "code ${response.code()}")
                    Log.d("publishPost onResponse", "headers ${response.headers()}")

                    post.isPublished = true
                    appDao.postDao().update(post)

                    if (linkedinAccount != null) {
                        //post account success
                        appDao.postAccountDao().insert(
                            PostAccount(
                                post.id,
                                linkedinAccount.id,
                                -1,
                                true,
                                null,
                                SocialMediaPlatforms.LINKEDIN.platformName,
                                linkedinAccount.name ?: ""
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("publishPost onFailure", t.message.toString())
                    Log.e("publishPost onFailure", t.cause.toString())

                    if (linkedinAccount != null) {
                        //post account failure
                        appDao.postAccountDao().insert(
                            PostAccount(
                                post.id,
                                linkedinAccount.id,
                                -1,
                                false,
                                null,
                                SocialMediaPlatforms.LINKEDIN.platformName,
                                linkedinAccount.name ?: ""
                            )
                        )
                    }
                }
            })
        }
    }

    private fun loadPost(postId: Int) {
        post = appDao.postDao().getPost(postId)!!

        if (post.title!!.isNotEmpty()) {
            binding.titleEditText.setText(post.title, TextView.BufferType.EDITABLE)
            binding.titleText.text = post.title
        }

        if (post.description!!.isNotEmpty()) {
            binding.descriptionEditText.setText(post.description, TextView.BufferType.EDITABLE)
            binding.descriptionText.text = post.description
        }

        if (post.mediaUri!!.isNotEmpty()) {
            isMediaSelected = true
            mediaUri = Uri.parse(post.mediaUri?: "")

            if (post.isPicture) {
                val imageView = binding.imageView
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(Uri.parse(post.mediaUri))
            }
        }

        if (post.isPublished) {
            binding.publishPostButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
            binding.saveButton.visibility = View.GONE
            binding.deleteButton.visibility = View.VISIBLE

            binding.accountsLabelText.visibility = View.GONE
            binding.accountChipGroup.visibility = View.GONE
            binding.noAccountsConnected.visibility = View.GONE
            binding.chooseMediaButton.visibility = View.GONE

            binding.titleEditText.visibility = View.GONE
            binding.descriptionEditText.visibility = View.GONE
            binding.titleText.visibility = View.VISIBLE
            binding.descriptionText.visibility = View.VISIBLE
            binding.descriptionCharacterCount.visibility = View.GONE

            //postaccounts
            val postAccountList = appDao.postAccountDao().getPostAccountByPostId(postId)
            Log.d("postAccountList", postAccountList.toString())

            if (postAccountList.isNotEmpty()) {
                binding.publishedSection.visibility = View.VISIBLE
                val postAccountsRecyclerView = binding.postAccountListPublished
                val postAccountsListAdapter = PostAccountItemListAdapter()

                postAccountsListAdapter.submitList(postAccountList)
                postAccountsRecyclerView.adapter = postAccountsListAdapter
                postAccountsRecyclerView.layoutManager = LinearLayoutManager(this)
                postAccountsRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL).apply {
                    AppCompatResources.getDrawable(this@NewPostActivity, R.drawable.margin_vertical_20dp)
                        ?.let { this.setDrawable(it) }
                })
            }
        }
    }

    private fun createImageFile(): File {
        val file = File(applicationContext.filesDir, "post_${post.id}.png")

        if (isMediaSelected && wasMediaChanged) {
            if (!file.exists()) {
                file.createNewFile()

                if (Build.VERSION.SDK_INT > 28) {
                    val source = ImageDecoder.createSource(contentResolver, mediaUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    Log.d(
                        "createImageFile FILE",
                        "update post, mediachanged true file name: ${file.name} path: ${file.absolutePath}"
                    )

                } else {
                    //TODO: zrobić wersję dla niższego SDK
                }

                val mediaUri = file.toURI().toString()
                post.mediaUri = mediaUri
            }
        }

        return file
    }

    private fun savePost() {
        val title = binding.titleEditText
        val description = binding.descriptionEditText

        if (isInEditMode) {
            if (isMediaSelected) {
                if (wasMediaChanged) {
                    val file = File(applicationContext.filesDir, "post_${post.id}.png")

                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    if (Build.VERSION.SDK_INT > 28) {
                        val source = ImageDecoder.createSource(contentResolver, mediaUri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        val fileOutputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                        fileOutputStream.flush()
                        fileOutputStream.close()

                        Log.d("savePost FILE",
                            "update post, mediachanged true file name: ${file.name} path: ${file.absolutePath}")

                    } else {
                        //TODO: zrobić wersję dla niższego SDK
                    }

                    val mediaUri = file.toURI().toString()
                    post.mediaUri = mediaUri
                }

                post.description = description.text.toString()
                post.title = title.text.toString()
                post.isPicture = isPicture

                appDao.postDao().update(post)
            }

        } else {
            if (isMediaSelected) {
                val newPost = Post(
                    0, title.text.toString(),
                    description.text.toString(), "", isPicture,
                    false, ""
                )

                val postId = appDao.postDao().insertAndGetId(newPost)
                val file = File(applicationContext.filesDir, "post_$postId.png")

                if (!file.exists()) {
                    file.createNewFile()
                }

                if (Build.VERSION.SDK_INT > 28) {
                    //TODO: posprzatac i umozliwic dla wszystkich platform, wyobrabnic do image tools
                    val source = ImageDecoder.createSource(contentResolver, mediaUri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    Log.d("savePost FILE",
                        "file name: ${file.name} path: ${file.absolutePath}")
                }

                val mediaUri = file.toURI().toString()
                newPost.mediaUri = mediaUri
                newPost.id = postId.toInt()

                post = newPost
                appDao.postDao().update(newPost)
                Log.d("save post", "Post id ${post.id} media uri ${post.mediaUri} ismediaselsected $isMediaSelected")

            } else {
                Snackbar.make(binding.root, "Wybierz zdjęcie", Snackbar.LENGTH_SHORT).show()

                /*appDao.postDao().insert(
                    Post(
                        0, title.text.toString(),
                        description.text.toString(), "", isPicture,
                        false, ""
                    )
                )

                Log.d("save post", "Post id ${post.id} media uri empty ismediaselsected $isMediaSelected")*/
            }
        }
    }

    private fun chooseMedia(uri: Uri) {
        val imageView = binding.imageView
        val videoView = binding.videoView
        mediaUri = uri
        wasMediaChanged = true

        if (uri.path!!.contains("video")) {
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
            isPicture = true
            isMediaSelected = true
            videoView.visibility = View.GONE
            imageView.setImageURI(uri)
            imageView.visibility = View.VISIBLE
        }
    }

    private suspend fun getStringPreference(key: String): String {
        val value = UserDataStore(applicationContext).getStringPreference(key)

        Log.d("getStringPreference", "key: $key value: $value")

        return value
    }
}