package com.postkeeper.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.postkeeper.app.R
import com.postkeeper.app.data.model.MediaType
import com.postkeeper.app.data.model.Platform
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.databinding.ItemPostBinding

class PostAdapter(
    private val onItemClick: (Post) -> Unit,
    private val onDownloadClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                textViewTitle.text = post.title ?: "Post from ${post.platform.name}"
                textViewAuthor.text = post.author ?: "@unknown"
                
                // Show platform icon
                imageViewPlatform.setImageResource(
                    when (post.platform) {
                        Platform.INSTAGRAM -> R.drawable.ic_instagram
                        Platform.TWITTER -> R.drawable.ic_twitter
                        Platform.UNKNOWN -> R.drawable.ic_unknown
                    }
                )
                
                // Show media type indicator
                imageViewMediaType.setImageResource(
                    when (post.mediaType) {
                        MediaType.VIDEO -> R.drawable.ic_video
                        MediaType.IMAGE -> R.drawable.ic_image
                        MediaType.UNKNOWN -> R.drawable.ic_unknown
                    }
                )
                
                // Load thumbnail or placeholder
                val thumbnailUrl = post.thumbnailUrl ?: post.mediaUrl
                if (thumbnailUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(thumbnailUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(imageViewThumbnail)
                }
                
                // Show download status
                if (post.isDownloaded) {
                    buttonDownload.text = "Downloaded"
                    buttonDownload.isEnabled = false
                } else {
                    buttonDownload.text = "Download"
                    buttonDownload.isEnabled = true
                }
                
                root.setOnClickListener { onItemClick(post) }
                buttonDownload.setOnClickListener { onDownloadClick(post) }
                buttonDelete.setOnClickListener { onDeleteClick(post) }
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
