package com.dicoding.storyapp.view.activities.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.view.activities.ViewModelFactory
import com.dicoding.storyapp.view.activities.maps.MapsActivity
import com.dicoding.storyapp.view.activities.story.UploadStory
import com.dicoding.storyapp.view.activities.welcome.Welcome
import com.dicoding.storyapp.view.adapter.StoryAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(application)
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (user.isLogin != true) {
                startActivity(Intent(this, Welcome::class.java))
                finish()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.storyResponse.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        val swipeRefresh = binding.swipeRefresh
        swipeRefresh.setOnRefreshListener {
            fetchStories()
            swipeRefresh.isRefreshing = false
        }

        viewModel.getUsername().observe(this) { username ->
            binding.topAppbar.title = getString(R.string.title_welcome_user, username)
        }

        binding.topAppbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.maps -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }

                R.id.language -> {
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }

                R.id.logout -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.logout_confirmation))
                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModel.logout()
                            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Welcome::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }.show()
                    true
                }
                else -> false
            }
        }

        binding.addStoryBtn.setOnClickListener {
            startActivity(Intent(this, UploadStory::class.java))
        }

        viewModel.getLoginToken().observe(this) { token ->
            if (token.isNotEmpty()) {
                fetchStories()
            }
        }
    }

    private fun fetchStories() {
        val storyAdapter = StoryAdapter()
        binding.rvStories.apply {
            adapter = storyAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        binding.progressIndicator.visibility = View.VISIBLE
        viewModel.getListStories()
        viewModel.storyResult.observe(this) { result ->
            viewModel.getLoginToken()
            when (result) {
                is Result.Loading -> {}

                is Result.Success -> {
                    val stories = result.data
                    storyAdapter.submitList(stories)
                }

                is Result.Error -> {}
            }
        }
    }
}