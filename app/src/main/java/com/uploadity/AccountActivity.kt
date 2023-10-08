package com.uploadity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.ActivityAccountBinding
import com.uploadity.ui.uicomponents.TumblrBlogItemListAdapter

class AccountActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAccountBinding
    private lateinit var appDao: AppDatabase
    private lateinit var account: Account

    @SuppressLint("SetTextI18n")
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
            binding.accountId.text = account.accountId
            binding.email.text = account.email

            when (account.socialMediaServiceName) {
                "linkedin" -> {
                    binding.account.text = "Linkedin"
                }
                "tumblr" -> {
                    binding.account.text = "Tumblr"
                    binding.email.visibility = View.GONE
                    binding.emailLabel.visibility = View.GONE

                    val blogs = appDao.blogDao().getBlogsByAccountId(accountId)

                    if (blogs.isNotEmpty()) {
                        Log.d("Blog list", blogs.toString())
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
            }
        }

        val deleteButton = binding.deleteButton
        deleteButton.setOnClickListener {
            val builder: AlertDialog.Builder = this.let {
                AlertDialog.Builder(it)
            }

            builder.setMessage("Are you sure to delete this account and associated blogs?")
            builder.apply {
                setPositiveButton("delete"
                ) { _, _ ->
                    appDao.blogDao().deleteBlogsByAccountId(account.id)
                    appDao.blogDao().deleteBlogsByAccountId(0)
                    appDao.accountDao().delete(account)
                    Snackbar.make(binding.root, "Post successfully deleted", Snackbar.LENGTH_SHORT).show()
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
}