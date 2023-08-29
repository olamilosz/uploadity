package com.uploadity.ui.uicomponents

import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.uploadity.R
import com.uploadity.database.Post
import java.io.File
import kotlin.coroutines.coroutineContext

class PostItemListAdapter(private val dataSet: List<Post>) :
    RecyclerView.Adapter<PostItemListAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item_list_adapter, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = dataSet[position]
        holder.textView.text = post.title

        if (post.isPicture) {
            holder.imageView.setImageURI(Uri.parse(post.mediaUri))
        }

        holder.rowItem.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, post)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, post: Post)
    }
}