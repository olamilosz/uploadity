package com.uploadity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uploadity.databinding.ActivityMainBinding
import com.uploadity.tools.UserDataStore
import com.uploadity.viewmodels.MainViewModel
import com.uploadity.viewmodels.MainViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory((application as UploadityApplication).repository, UserDataStore(this))
        )[MainViewModel::class.java]

        if (appLinkData != null) {
            mainViewModel.handleLinkDataFromCallback(appLinkData)
        }
    }
}