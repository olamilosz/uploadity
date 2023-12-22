package com.uploadity.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.uploadity.NewPostActivity
import com.uploadity.R
import com.uploadity.UploadityApplication
import com.uploadity.database.AppDatabase
import com.uploadity.database.posts.Post
import com.uploadity.databinding.FragmentDashboardBinding
import com.uploadity.ui.uicomponents.PostItemListAdapter
import com.uploadity.viewmodels.DashboardViewModel
import com.uploadity.viewmodels.DashboardViewModelFactory

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var appDao: AppDatabase
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(
            this,
            DashboardViewModelFactory((requireActivity().application as UploadityApplication).repository)
        )[DashboardViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        appDao = context?.let { AppDatabase.getInstance(it) }!!

        val postItemListAdapter = PostItemListAdapter()
        val publishedPostsRecyclerView = binding.publishedPostsRecyclerView

        postItemListAdapter.setOnClickListener(object : PostItemListAdapter.OnClickListener {
            override fun onClick(position: Int, post: Post) {
                Log.d("published post item click", "ON CLICK: position $position ${post.title}")

                val intent = Intent(context, NewPostActivity::class.java)
                intent.putExtra("post_id", post.id)
                startActivity(intent)
            }
        })

        publishedPostsRecyclerView.layoutManager = LinearLayoutManager(activity)
        publishedPostsRecyclerView.adapter = postItemListAdapter

        dashboardViewModel.getAllPublishedPosts().observe(viewLifecycleOwner) { posts ->
            posts.let {
                postItemListAdapter.submitList(posts)

                if (posts.isEmpty()) {
                    binding.publishedPostsLabel.visibility = View.GONE

                } else {
                    binding.publishedPostsLabel.visibility = View.VISIBLE
                }
            }
        }

        publishedPostsRecyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                ?.let { this.setDrawable(it) }
        })

        binding.unpublishedPostsLabel.visibility = View.VISIBLE
        binding.unpublishedPostsRecyclerView.visibility = View.VISIBLE

        val unpublishedPostItemListAdapter = PostItemListAdapter()
        val unpublishedPostsRecyclerView = binding.unpublishedPostsRecyclerView

        unpublishedPostItemListAdapter.setOnClickListener(object : PostItemListAdapter.OnClickListener {
            override fun onClick(position: Int, post: Post) {
                Log.d("unpublished post item click", "ON CLICK: postition $position ${post.title}")

                val intent = Intent(context, NewPostActivity::class.java)
                intent.putExtra("post_id", post.id)
                startActivity(intent)
            }
        })

        unpublishedPostsRecyclerView.layoutManager = LinearLayoutManager(activity)
        unpublishedPostsRecyclerView.adapter = unpublishedPostItemListAdapter

        dashboardViewModel.getAllUnpublishedPosts().observe(viewLifecycleOwner) { posts ->
            posts.let {
                unpublishedPostItemListAdapter.submitList(posts)

                if (posts.isEmpty()) {
                    binding.unpublishedPostsLabel.visibility = View.GONE

                } else {
                    binding.unpublishedPostsLabel.visibility = View.VISIBLE
                }
            }
        }

        unpublishedPostsRecyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                ?.let { this.setDrawable(it) }
        })

        dashboardViewModel.getAllPosts().observe(viewLifecycleOwner) { posts ->
            posts.let {
                if (posts.isEmpty()) {
                    binding.noPostsView.visibility = View.VISIBLE

                } else {
                    binding.noPostsView.visibility = View.GONE
                }
            }
        }

        val fab: View = binding.fab
        fab.setOnClickListener {
            val intent = Intent(context, NewPostActivity::class.java)
            this.startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}