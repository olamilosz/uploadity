package com.uploadity.ui.uicomponents

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.R
import com.uploadity.database.blogs.Blog

class TumblrBlogItemListAdapter(private val dataSet: List<Blog>) :
    RecyclerView.Adapter<TumblrBlogItemListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blogName: TextView
        val blogTitle: TextView

        init {
            blogName = view.findViewById(R.id.blog_name)
            blogTitle = view.findViewById(R.id.blog_title)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tumblr_blog_list_adapter, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val blog = dataSet[position]
        holder.blogName.text = blog.name
        holder.blogTitle.text = blog.title
    }
}