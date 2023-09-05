package com.uploadity.ui.uicomponents

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.R
import com.uploadity.database.accounts.Account

class AccountItemListAdapter(private val dataSet: List<Account>) :
    RecyclerView.Adapter<AccountItemListAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private lateinit var context: Context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView
        val rowItem: LinearLayout

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
            rowItem = view.findViewById(R.id.row_item)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item_list_adapter, parent, false)

        context = parent.context

        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = dataSet[position]
        holder.textView.text = account.name

        if (account.socialMediaServiceName == "linkedin") {
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.linkedin_icon))
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