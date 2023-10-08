package com.uploadity.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.uploadity.AccountActivity
import com.uploadity.BuildConfig
import com.uploadity.R
import com.uploadity.api.linkedin.LinkedinApiTools
import com.uploadity.api.tumblr.tools.TumblrApiTools
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.FragmentHomeBinding
import com.uploadity.ui.uicomponents.AccountItemListAdapter
import com.uploadity.viewmodels.AccountViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val viewModel: AccountViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var accountItemListAdapter: AccountItemListAdapter
    private lateinit var appDao: AppDatabase
    private lateinit var mCredential: GoogleAccountCredential

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val connectButton = binding.button
        val accountListView = binding.accountList

        connectButton.setOnClickListener {
            val clientId = BuildConfig.LINKEDIN_CLIENT_ID
            val authorizationUrl = LinkedinApiTools().generateAuthorizationUrl(clientId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
            startActivity(intent)
        }

        val connectTumblrButton = binding.buttonConnectTumblr
        connectTumblrButton.setOnClickListener {
            val clientId = BuildConfig.TUMBLR_CLIENT_ID
            val authorizationUrl = TumblrApiTools().generateAuthorizationUrl(clientId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
            startActivity(intent)
        }

        appDao = context?.let { AppDatabase.getInstance(it) }!!
        val accountList = appDao.accountDao().getAllAccounts()
        Log.d("Home Fragment", "Account list: ${accountList.toString()}")

        recyclerView = accountListView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        accountItemListAdapter = AccountItemListAdapter(accountList)
        accountItemListAdapter.setOnClickListener(object : AccountItemListAdapter.OnClickListener {
            override fun onClick(position: Int, account: Account) {
                Log.d("account item click", "ON CLICK: id ${account.id}")

                val intent = Intent(context, AccountActivity::class.java)
                intent.putExtra("account_id", account.id)
                startActivity(intent)
            }
        })

        recyclerView.adapter = accountItemListAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                ?.let { this.setDrawable(it) }
        })

        val youtubeTestButton = binding.youtubeTest
        youtubeTestButton.setOnClickListener {
            Log.d("click", "youtube test button")
            //getResultsFromApi()
        }

        val scopes = mutableListOf<String>(YouTubeScopes.YOUTUBE_UPLOAD)
        mCredential = GoogleAccountCredential.usingOAuth2(activity, scopes)
            .setBackOff(ExponentialBackOff())

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}