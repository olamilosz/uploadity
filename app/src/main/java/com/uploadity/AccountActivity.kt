package com.uploadity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.ActivityAccountBinding
import com.uploadity.tools.SocialMediaPlatforms
import com.uploadity.tools.UserDataStore
import com.uploadity.ui.uicomponents.TumblrBlogItemListAdapter
import kotlinx.coroutines.runBlocking

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var appDao: AppDatabase
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDao = AppDatabase.getInstance(this)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        val supportActionBar = supportActionBar
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowHomeEnabled(true)
        }

        if (intent != null && intent.extras != null && intent.extras!!.containsKey("account_id")) {
            val accountId = intent.extras!!.getInt("account_id")
            account = appDao.accountDao().getAccount(accountId)!!
            binding.name.text = account.name

            when (account.socialMediaServiceName) {
                SocialMediaPlatforms.LINKEDIN.platformName -> {
                    binding.account.text = SocialMediaPlatforms.LINKEDIN.capitalizedName
                }

                SocialMediaPlatforms.TUMBLR.platformName -> {
                    binding.account.text = SocialMediaPlatforms.TUMBLR.capitalizedName

                    val blogs = appDao.blogDao().getBlogsByAccountId(accountId)

                    if (blogs.isNotEmpty()) {
                        binding.tumblrBlogsSection.visibility = View.VISIBLE
                        val blogItemListAdapter = TumblrBlogItemListAdapter(blogs)
                        val blogItemListRecyclerView = binding.blogRecyclerView
                        blogItemListRecyclerView.adapter = blogItemListAdapter
                        blogItemListRecyclerView.layoutManager = LinearLayoutManager(this)

                        blogItemListRecyclerView.addItemDecoration(
                            DividerItemDecoration(
                                this,
                                LinearLayoutManager.VERTICAL
                            ).apply {

                            AppCompatResources.getDrawable(
                                applicationContext,
                                R.drawable.margin_vertical_20dp
                            ) ?.let { this.setDrawable(it) }
                        })
                    }
                }

                SocialMediaPlatforms.TWITTER.platformName -> {
                    binding.account.text = SocialMediaPlatforms.TWITTER.capitalizedName
                }
            }
        }

        val deleteButton = binding.deleteButton
        deleteButton.setOnClickListener {
            val builder: AlertDialog.Builder = this.let {
                AlertDialog.Builder(it)
            }

            builder.setMessage("Czy na pewno chcesz usunąć to konto?")
            builder.apply {
                setPositiveButton("usuń"
                ) { _, _ ->
                    deleteAccount()
                }

                setNegativeButton("nie"
                ) { dialog, _ ->
                    dialog.cancel()
                }
            }

            builder.create().show()
        }
    }

    private fun deleteAccount() {
        val socialMediaName = account.socialMediaServiceName ?: ""
        val userDataStore = UserDataStore(this)

        if (account.socialMediaServiceName == SocialMediaPlatforms.TUMBLR.platformName) {
            appDao.blogDao().deleteBlogsByAccountId(account.id)
            //appDao.postAccountDao().deletePostAccountsByAccountId(account.id)
        }

        runBlocking {
            userDataStore.deleteStringPreferenceBySocialMediaName(socialMediaName)
        }

        appDao.accountDao().delete(account)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}