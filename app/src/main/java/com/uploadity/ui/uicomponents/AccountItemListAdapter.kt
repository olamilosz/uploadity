package com.uploadity.ui.uicomponents

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.R
import com.uploadity.database.accounts.Account

class AccountItemListAdapter : ListAdapter<Account, AccountItemListAdapter.AccountViewHolder>(AccountComparator()) {

    private var onClickListener: OnClickListener? = null
    private lateinit var context: Context

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView
        val rowItem: LinearLayout

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
            rowItem = view.findViewById(R.id.row_item)
        }
    }

    class AccountComparator: DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item_list_adapter, parent, false)

        context = parent.context

        return AccountViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = getItem(position)
        holder.textView.text = account.name
        holder.imageView.setPadding(16, 24, 16, 16)

        when (account.socialMediaServiceName) {
            "linkedin" -> {
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_linkedin))
                holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.linkedin_blue))
            }

            "tumblr" -> {
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_tumblr))
                holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.tumblr_background))
            }

            "twitter" -> {
                holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_twitter))
                holder.imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.twitter_background))
            }
        }

        holder.rowItem.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, account)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, account: Account)
    }

}