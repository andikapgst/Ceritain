package com.dicoding.storyapp.view.activities.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.utils.convertTime
import com.dicoding.storyapp.databinding.ActivityStoryDetailBinding
import com.dicoding.storyapp.view.activities.ViewModelFactory

class StoryDetail : AppCompatActivity() {

    private val viewModel: StoryDetailViewModel by viewModels<StoryDetailViewModel> {
        ViewModelFactory.getInstance(application)
    }
    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra(EXTRA_ID)
        if (id != null) {
            viewModel.getStoryDetail(id)
        } else {
            Toast.makeText(this, "Invalid story ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.toastMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        setupView()
    }

    private fun setupView() {
        viewModel.storyDetail.observe(this) { story ->
            binding.apply {
                tvDetailName.text = intent.getStringExtra(EXTRA_USERNAME)
                tvDetailCreatedAt.text = intent.getStringExtra(EXTRA_CREATED_AT)
                tvDetailCreatedAt.text = convertTime(story?.createdAt.toString())
                tvDetailDescription.text = intent.getStringExtra(EXTRA_DESCRIPTION)
                Glide.with(this@StoryDetail)
                    .load(intent.getStringExtra(EXTRA_IMAGE))
                    .into(ivDetailPhoto)
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_IMAGE = "extra_image"
        const val EXTRA_CREATED_AT = "extra_created_at"
    }
}