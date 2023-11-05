package com.uploadity.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.uploadity.AccountActivity
import com.uploadity.R
import com.uploadity.UploadityApplication
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.FragmentHomeBinding
import com.uploadity.tools.SocialMediaPlatforms
import com.uploadity.tools.UserDataStore
import com.uploadity.ui.uicomponents.AccountItemListAdapter
import com.uploadity.viewmodels.MainViewModel
import com.uploadity.viewmodels.MainViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val connectButton = binding.button

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                (requireActivity().application as UploadityApplication).repository,
                UserDataStore(requireContext()))
        )[MainViewModel::class.java]

        connectButton.setOnClickListener {
            val intent = mainViewModel.createAuthorizeLinkedinIntent()
            startActivity(intent)
        }

        val connectTumblrButton = binding.buttonConnectTumblr
        connectTumblrButton.setOnClickListener {
            val intent = mainViewModel.createAuthorizeTumblrIntent()
            startActivity(intent)
        }

        val connectTwitterButton = binding.buttonConnectTwitter
        connectTwitterButton.setOnClickListener {
            mainViewModel.requestTwitterToken()
        }

        mainViewModel.getTwitterAuthorizationIntent().observe(viewLifecycleOwner) {
            startActivity(it)
        }

        val recyclerView = binding.accountList
        val accountItemListAdapter = AccountItemListAdapter()

        recyclerView.adapter = accountItemListAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                ?.let { this.setDrawable(it) }
        })

        val accountButtonsSection = binding.accountButtonsSection
        val accountListSection = binding.accountListSection

        mainViewModel.getAllAccounts().observe(viewLifecycleOwner) { accounts ->
            accounts.let { accountItemListAdapter.submitList(it) }

            when (accounts.size) {
                0 -> {
                    accountButtonsSection.visibility = View.VISIBLE
                    accountListSection.visibility = View.GONE
                    setButtonsVisibility(accounts)
                }

                3 -> {
                    accountButtonsSection.visibility = View.GONE
                    accountListSection.visibility = View.VISIBLE
                }

                else -> {
                    accountButtonsSection.visibility = View.VISIBLE
                    accountListSection.visibility = View.VISIBLE
                    setButtonsVisibility(accounts)
                }
            }
        }

        accountItemListAdapter.setOnClickListener(object : AccountItemListAdapter.OnClickListener {
            override fun onClick(position: Int, account: Account) {
                Log.d("account item click", "ON CLICK: id ${account.id}")

                val intent = Intent(context, AccountActivity::class.java)
                intent.putExtra("account_id", account.id)
                startActivity(intent)
            }
        })

        return root
    }

    private fun setButtonsVisibility(accounts: List<Account>) {
        binding.button.visibility = View.VISIBLE
        binding.buttonConnectTumblr.visibility = View.VISIBLE
        binding.buttonConnectTwitter.visibility = View.VISIBLE

        for (account in accounts) {
            when (account.socialMediaServiceName) {
                SocialMediaPlatforms.LINKEDIN.platformName -> binding.button.visibility = View.GONE
                SocialMediaPlatforms.TUMBLR.platformName -> binding.buttonConnectTumblr.visibility = View.GONE
                SocialMediaPlatforms.TWITTER.platformName -> binding.buttonConnectTwitter.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
