package com.uploadity

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.ActivityAccountBinding

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
            binding.email.text = account.email

            when (account.socialMediaServiceName) {
                "linkedin" -> {
                    binding.account.text = "Linkedin"
                }
            }
        }

        val deleteButton = binding.deleteButton
        deleteButton.setOnClickListener {
            appDao.accountDao().delete(account)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}