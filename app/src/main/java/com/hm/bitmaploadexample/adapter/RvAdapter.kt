package com.hm.bitmaploadexample.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.hm.bitmaploadexample.R
import com.hm.bitmaploadexample.activity.sourcecode.MyCustomViewTarget

/**
 * Created by p_dmweidu on 2023/11/4
 * Desc: 测试Glide在RecyclerView中加载图片，是怎么取消的从而避免现图片错位的问题
 *
 */

class RvAdapter(private val mData: List<String>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_load_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.iv_load_image)
        }

        fun bind(item: String?) {
            item?.let {
                glideIntoTarget(imageView.context, item, imageView)
            }

        }
        private fun glideIntoTarget(context: Context, url: String, imageView: ImageView) {
            Glide.with(context)
                .load(url)
                .error(R.mipmap.ic_launcher)
                .into(object : MyCustomViewTarget<ImageView, Drawable?>(imageView) {

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        imageView.setImageDrawable(errorDrawable)
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
}