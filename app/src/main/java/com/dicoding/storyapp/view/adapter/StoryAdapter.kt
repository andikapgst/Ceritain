package com.dicoding.storyapp.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.local.database.StoryEntity
import com.dicoding.storyapp.databinding.StoryCardBinding
import com.dicoding.storyapp.view.activities.detail.StoryDetail

class StoryAdapter:
    PagingDataAdapter<StoryEntity, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = StoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stories = getItem(position)
        if (stories != null) {
            holder.bind(stories)
        }
    }

    fun getStoryItemAt(position: Int): StoryEntity {
        return getItem(position) as StoryEntity
    }

    class ViewHolder(val binding: StoryCardBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(stories: StoryEntity) {
            Glide.with(binding.root.context)
                .load(stories.photoUrl)
                .into(binding.ivItemPhoto)
            binding.tvItemName.text = stories.name
            binding.tvItemDescription.text = stories.description
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, StoryDetail::class.java)
                intent.apply {
                    putExtra(StoryDetail.EXTRA_ID, stories.id)
                    putExtra(StoryDetail.EXTRA_IMAGE, stories.photoUrl)
                    putExtra(StoryDetail.EXTRA_CREATED_AT, stories.createdAt)
                    putExtra(StoryDetail.EXTRA_USERNAME, stories.name)
                    putExtra(StoryDetail.EXTRA_DESCRIPTION, stories.description)
                }

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.ivItemPhoto, "image"),
                        Pair(binding.tvItemName, "name"),
                        Pair(binding.tvItemDescription, "description")
                    )

                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<StoryEntity> =
            object : DiffUtil.ItemCallback<StoryEntity>() {
                override fun areItemsTheSame(
                    oldItem: StoryEntity,
                    newItem: StoryEntity
                ): Boolean {
                    return oldItem.id == newItem.id
                }
                override fun areContentsTheSame(
                    oldItem: StoryEntity, newItem: StoryEntity
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}