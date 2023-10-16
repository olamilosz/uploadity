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
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.NewPostActivity
import com.uploadity.R
import com.uploadity.database.AppDatabase
import com.uploadity.database.posts.Post
import com.uploadity.databinding.FragmentDashboardBinding
import com.uploadity.ui.uicomponents.PostItemListAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var appDao: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        appDao = context?.let { AppDatabase.getInstance(it) }!!

        val publishedPostList = appDao.postDao().getAllPublishedPosts()
        val unpublishedPostList = appDao.postDao().getAllUnpublishedPosts()

        if (publishedPostList.isNotEmpty()) {
            binding.publishedPostsLabel.visibility = View.VISIBLE
            binding.publishedPostsRecyclerView.visibility = View.VISIBLE

            val postItemListAdapter = PostItemListAdapter(publishedPostList)
            val publishedPostsRecyclerView = binding.publishedPostsRecyclerView

            postItemListAdapter.setOnClickListener(object : PostItemListAdapter.OnClickListener {
                override fun onClick(position: Int, post: Post) {
                    //open edit post activity and pass data
                    Log.d("published post item click", "ON CLICK: postition $position ${post.title}")

                    val intent = Intent(context, NewPostActivity::class.java)
                    intent.putExtra("post_id", post.id)
                    startActivity(intent)
                }
            })

            publishedPostsRecyclerView.layoutManager = LinearLayoutManager(activity)
            publishedPostsRecyclerView.adapter = postItemListAdapter
            publishedPostsRecyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                    ?.let { this.setDrawable(it) }
            })
        }

        if (unpublishedPostList.isNotEmpty()) {
            binding.unpublishedPostsLabel.visibility = View.VISIBLE
            binding.unpublishedPostsRecyclerView.visibility = View.VISIBLE

            val postItemListAdapter = PostItemListAdapter(unpublishedPostList)
            val unpublishedPostsRecyclerView = binding.unpublishedPostsRecyclerView

            postItemListAdapter.setOnClickListener(object : PostItemListAdapter.OnClickListener {
                override fun onClick(position: Int, post: Post) {
                    //open edit post activity and pass data
                    Log.d("unpublished post item click", "ON CLICK: postition $position ${post.title}")

                    val intent = Intent(context, NewPostActivity::class.java)
                    intent.putExtra("post_id", post.id)
                    startActivity(intent)
                }
            })

            unpublishedPostsRecyclerView.layoutManager = LinearLayoutManager(activity)
            unpublishedPostsRecyclerView.adapter = postItemListAdapter
            unpublishedPostsRecyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                    ?.let { this.setDrawable(it) }
            })
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