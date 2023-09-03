package com.uploadity.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.uploadity.R
import com.uploadity.api.linkedin.LinkedinApi
import com.uploadity.databinding.FragmentHomeBinding
import com.uploadity.model.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val viewModel: MainViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/

        val connectButton = binding.button

        connectButton.setOnClickListener {
            val clientId = getString(R.string.LINKEDIN_CLIENT_ID)
            val authorizationUrl = LinkedinApi().generateAuthorizationUrl(clientId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
            startActivity(intent)
        }

        viewModel.linkedinCode.observe(viewLifecycleOwner, Observer {
            //got linkedin code
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}