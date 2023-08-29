package com.uploadity.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.NewPostActivity
import com.uploadity.R
import com.uploadity.database.AppDatabase
import com.uploadity.database.Post
import com.uploadity.databinding.FragmentDashboardBinding
import com.uploadity.ui.uicomponents.PostItemListAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var postItemListAdapter: PostItemListAdapter
    private lateinit var appDao: AppDatabase
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this)[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        appDao = context?.let { AppDatabase.getInstance(it) }!!
        val postList = appDao.postDao().getAll()
        postItemListAdapter = PostItemListAdapter(postList)

        postItemListAdapter.setOnClickListener(object : PostItemListAdapter.OnClickListener {
            override fun onClick(position: Int, post: Post) {
                //open edit post activity and pass data
                Log.d("post item click", "ON CLICK: postition $position ${post.title}")

                val intent = Intent(context, NewPostActivity::class.java)
                intent.putExtra("post_id", post.id)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        })

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = postItemListAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
            AppCompatResources.getDrawable(requireContext(), R.drawable.margin_vertical_20dp)
                ?.let { this.setDrawable(it) }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}