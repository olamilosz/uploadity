package com.uploadity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.uploadity.api.linkedin.LinkedinApi
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinServiceBuilder
import com.uploadity.api.linkedin.datamodels.LinkedinAccessTokenParams
import com.uploadity.api.linkedin.datamodels.LinkedinAccessTokenResponse
import com.uploadity.databinding.ActivityMainBinding
import com.uploadity.model.MainViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            this.startActivity(intent)
        }

        // handle deep link from linkedin authorize process
        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data

        if (appLinkData != null) {
            when (appLinkData.lastPathSegment) {
                "linkedin" -> {
                    val code = appLinkData.getQueryParameter("code") ?: ""

                    if (code != "") {
                        Log.d("LINKEDIN CODE", code)

                        // JEŚLI OTRZYMALIŚMY KOD ODPALAMY ACCESS TOKEN
                        getAccessToken(
                            code,
                            getString(R.string.LINKEDIN_CLIENT_ID),
                            getString(R.string.LINKEDIN_CLIENT_SECRET)
                        )

                    } else {
                        Snackbar.make(
                            binding.root,
                            "Linkedin cancelled login",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun getAccessToken2(code: String, clientId: String, clientSecret: String) {
        val client = OkHttpClient()
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "client_id=775zx8tdbz37lj&client_secret=UpNygVsjxoYu96iZ&code=AQROlb-e8yWjuh3Qk4FaLirXRFZJ7SrgRtMpjkxIH1eJKQGoJjPdTCBRDsrSAcshX2kquZ6xiaEhXLNPPjZZIBKYuuUNuO9HPmSvHZ1I2d6a7S7ZY2BD9outfvmqSQzYv4ieD8KtIcGMOUepO4EkRmECi3E15bZQuToi-f8w_zlMYlUupRhxyuWQfE19ro4BRVhJh-AtOM198u3Ylrc&grant_type=authorization_code&redirect_uri=https://uploadity.net.pl/linkedin".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://www.linkedin.com/oauth/v2/accessToken")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Cookie", "bcookie=\"v=2&aa885746-b94b-42c6-87ec-e73b4f6c50d1\"; lang=v=2&lang=en-us; li_gc=MTswOzE2OTM2ODc0MzY7MjswMjFW7ajRZ88Z8uA+Qta+jdX4I4tUUlCj47RJaDJBnF1LQg==; lidc=\"b=TB42:s=T:r=T:a=T:p=T:g=4554:u=3:x=1:i=1693687497:t=1693764460:v=2:sig=AQFA3QfZiqm_SzkGleLPPaU2jXJCFqJc\"; bscookie=\"v=1&202309022043563c456ef8-6bf9-4300-8d85-a06d774ab9baAQEBOLm7qvm2DYCcGDMNP6sGVYRShJZZ\"")
            .build()
        val response = client.newCall(request).execute()
    }

    private fun getAccessToken(code: String, clientId: String, clientSecret: String) {
        val linkedinApi = LinkedinServiceBuilder.buildService(LinkedinApiInterface::class.java)
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "grant_type=authorization_code&client_id=775zx8tdbz37lj&client_secret=UpNygVsjxoYu96iZ&&redirect_uri=https://uploadity.net.pl/linkedin&code=$code"

        linkedinApi.getAccessToken(
            body.toRequestBody(mediaType)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Snackbar.make(
                    binding.root,
                    "Linkedin get access token success",
                    Snackbar.LENGTH_SHORT
                ).show()

                Log.d("access token success", response.body().toString())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Snackbar.make(
                    binding.root,
                    "Linkedin get access token fail",
                    Snackbar.LENGTH_SHORT
                ).show()

                Log.e("access token fail", t.message ?: "")
            }

        })
    }
}