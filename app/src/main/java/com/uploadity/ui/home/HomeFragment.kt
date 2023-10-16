package com.uploadity.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.AccountActivity
import com.uploadity.BuildConfig
import com.uploadity.R
import com.uploadity.UploadityApplication
import com.uploadity.api.linkedin.LinkedinApiTools
import com.uploadity.api.tumblr.tools.TumblrApiTools
import com.uploadity.api.twitter.TwitterApiInterface
import com.uploadity.api.twitter.TwitterApiServiceBuilder
import com.uploadity.api.twitter.tools.TwitterApiTools
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.FragmentHomeBinding
import com.uploadity.ui.uicomponents.AccountItemListAdapter
import com.uploadity.viewmodels.AccountViewModel
import com.uploadity.viewmodels.AccountViewModelFactory
import kotlinx.coroutines.flow.collect
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory((requireActivity().application as UploadityApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val connectButton = binding.button

        connectButton.setOnClickListener {
            val clientId = BuildConfig.LINKEDIN_CLIENT_ID
            val authorizationUrl = LinkedinApiTools().generateAuthorizationUrl(clientId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
            //val intent = LinkedinApiTools().connectLinkedin()
            startActivity(intent)
        }

        val connectTumblrButton = binding.buttonConnectTumblr
        connectTumblrButton.setOnClickListener {
            val clientId = BuildConfig.TUMBLR_CLIENT_ID
            val authorizationUrl = TumblrApiTools().generateAuthorizationUrl(clientId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
            startActivity(intent)
        }

        val connectTwitterButton = binding.buttonConnectTwitter
        connectTwitterButton.setOnClickListener {
            requestTwitterToken()
        }

        val recyclerView = binding.accountList
        val accountItemListAdapter = AccountItemListAdapter()

        recyclerView.adapter = accountItemListAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                ?.let { this.setDrawable(it) }
        })

        accountViewModel.getAllAccounts().observe(viewLifecycleOwner) { accounts ->
            accounts.let { accountItemListAdapter.submitList(it) }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestTwitterToken() {
        val twitterApi = TwitterApiServiceBuilder.buildService(TwitterApiInterface::class.java)
        val callbackUrl = "https://uploadity.net.pl/twitter"

        val authorizationHeader = TwitterApiTools()
            .generateRequestTokenAuthorizationHeader(callbackUrl)

        Log.d("authorizationHeader", authorizationHeader)

        twitterApi.requestToken(
            authorizationHeader,
            callbackUrl

        ).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                val responseBody = response.body()

                if (responseBody != null) {
                    val responseParameterList = responseBody.string().split("&")
                    val responseParameterMap = mutableMapOf<String, String>()

                    for (parameter in responseParameterList) {
                        responseParameterMap[parameter.substringBefore("=").trim()] =
                            parameter.substringAfter("=").trim()
                    }

                    if (responseParameterMap["oauth_callback_confirmed"] == "true") {
                        val oauthToken = responseParameterMap["oauth_token"] ?: ""

                        if (oauthToken.isNotEmpty()) {
                            authorizeTwitter(oauthToken)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("requestToken", "onFailure code ${t.message}")
            }
        })
    }

    fun authorizeTwitter(oauthToken: String) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://api.twitter.com/oauth/authorize?oauth_token=$oauthToken")
        )

        startActivity(browserIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}