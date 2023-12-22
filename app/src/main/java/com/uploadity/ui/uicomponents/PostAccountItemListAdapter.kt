package com.uploadity.ui.uicomponents

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.R
import com.uploadity.database.postaccount.PostAccount
import com.uploadity.database.posts.Post
import com.uploadity.tools.SocialMediaPlatforms

class PostAccountItemListAdapter: ListAdapter<PostAccount, PostAccountItemListAdapter.ViewHolder>(PostAccountComparator()) {

    private var onClickListener: PostAccountItemListAdapter.OnClickListener? = null
    private var onEditButtonClickListener: PostAccountItemListAdapter.OnEditButtonClickListener? = null
    private lateinit var context: Context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView
        val textView: TextView
        val editButton: ImageButton
        val deleteButton: ImageButton

        init {
            icon = view.findViewById(R.id.icon)
            textView = view.findViewById(R.id.text_view)
            editButton = view.findViewById(R.id.edit_button)
            deleteButton = view.findViewById(R.id.delete_button)
        }
    }

    class PostAccountComparator: DiffUtil.ItemCallback<PostAccount>() {
        override fun areItemsTheSame(oldItem: PostAccount, newItem: PostAccount): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PostAccount, newItem: PostAccount): Boolean {
            return oldItem.accountId == newItem.accountId &&
                    oldItem.postId == newItem.postId &&
                    oldItem.blogId == newItem.blogId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_account_item_list_adapter, parent, false)

        context = parent.context

        return ViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postAccount = getItem(position)
        holder.textView.text = postAccount.name

        holder.icon.setPadding(16, 24, 16, 16)

        when (postAccount.socialMediaPlatformName) {
            SocialMediaPlatforms.LINKEDIN.platformName -> {
                holder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_linkedin))
                holder.icon.setBackgroundColor(ContextCompat.getColor(context, R.color.linkedin_blue))
            }

            SocialMediaPlatforms.TUMBLR.platformName -> {
                holder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_tumblr))
                holder.icon.setBackgroundColor(ContextCompat.getColor(context, R.color.tumblr_background))
            }

            SocialMediaPlatforms.TWITTER.platformName -> {
                holder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_twitter))
                holder.icon.setBackgroundColor(ContextCompat.getColor(context, R.color.twitter_background))
            }
        }

        holder.deleteButton.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, postAccount)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun setOnEditButtonClickListener(onClickListener: OnEditButtonClickListener) {
        this.onEditButtonClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, post: PostAccount)
    }

    interface OnEditButtonClickListener {
        fun onClick(position: Int, post: PostAccount)
    }

}