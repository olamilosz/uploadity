package com.uploadity.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.AccountActivity
import com.uploadity.BuildConfig
import com.uploadity.R
import com.uploadity.api.linkedin.LinkedinApiTools
import com.uploadity.api.tumblr.tools.TumblrApiTools
import com.uploadity.api.twitter.TwitterApiInterface
import com.uploadity.api.twitter.TwitterApiServiceBuilder
import com.uploadity.api.twitter.datamodels.TwitterRequestTokenResponse
import com.uploadity.api.twitter.tools.TwitterApiTools
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.FragmentHomeBinding
import com.uploadity.ui.uicomponents.AccountItemListAdapter
import com.uploadity.viewmodels.AccountViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val viewModel: AccountViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var accountItemListAdapter: AccountItemListAdapter
    private lateinit var appDao: AppDatabase

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
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

        val connectTwitterButton = binding.buttonConnectTwitter
        connectTwitterButton.setOnClickListener {
            requestTwitterToken()
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

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestTwitterToken() {
        val twitterApi = TwitterApiServiceBuilder.buildService(TwitterApiInterface::class.java)
        val callbackUrl = "https://uploadity.net.pl/twitter"
        val encodedCallbackUrl = URLEncoder.encode(callbackUrl, "UTF-8")
        //val authorizationParameterMap

        val authorizationHeader = TwitterApiTools()
            .generateRequestTokenAuthorizationHeader(encodedCallbackUrl)

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