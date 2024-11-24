package com.dicoding.storyapp.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.response.ListStoryItem
import com.dicoding.storyapp.data.utils.convertTime
import com.dicoding.storyapp.view.activities.detail.StoryDetail
import com.dicoding.storyapp.view.activities.main.MainActivity
import com.dicoding.storyapp.view.activities.main.MainViewModel
import com.dicoding.storyapp.view.adapter.StoryAdapter

internal class StackRemoteViewsFactory(
    private val context: Context,
    private val adapter: StoryAdapter
) : RemoteViewsService.RemoteViewsFactory {

    private var stories: Result<List<ListStoryItem>> = Result.Loading

    override fun onCreate() {
        val viewModel = ViewModelProvider(context as MainActivity)[MainViewModel::class.java]
        viewModel.getListStories()
        viewModel.storyResult.observe(context) { newStories ->
            stories = newStories
            onDataSetChanged()
        }
    }

    override fun onDataSetChanged() {
        val remoteViewsList = mutableListOf<RemoteViews>()
        when (stories) {
            is Result.Success -> {
                val stories = (stories as Result.Success).data
                stories.forEach { story ->
                    val remoteViews = RemoteViews(context.packageName, R.layout.stack_widget)
                    remoteViews.setTextViewText(R.id.tv_widget_name, story.name)
                    remoteViews.setTextViewText(
                        R.id.tv_widget_created_at,
                        convertTime(story.createdAt.toString())
                    )
                    remoteViews.setImageViewUri(R.id.iv_widget_photo, Uri.parse(story.photoUrl))
                }
            }

            is Result.Error -> {
                val errorMessage = (stories as Result.Error).error
                Log.e("StackRemoteViewsFactory", "Error: $errorMessage")
            }

            Result.Loading -> {
                Log.d("StackRemoteViewsFactory", "Loading...")
            }
        }
        remoteViewsList.addAll(remoteViewsList)
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        return adapter.itemCount
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.stack_widget)
        val story = adapter.getStoryItemAt(position)

        remoteViews.setTextViewText(R.id.tv_widget_name, story.name)
        remoteViews.setTextViewText(R.id.tv_widget_created_at, convertTime(story.createdAt.toString()))
        remoteViews.setImageViewUri(R.id.iv_widget_photo, Uri.parse(story.photoUrl))

        val fillInIntent = Intent().apply {
            putExtra(StoryDetail.EXTRA_ID, story.id)
            putExtra(StoryDetail.EXTRA_IMAGE, story.photoUrl)
            putExtra(StoryDetail.EXTRA_CREATED_AT, story.createdAt)
            putExtra(StoryDetail.EXTRA_USERNAME, story.name)
            putExtra(StoryDetail.EXTRA_DESCRIPTION, story.description)
        }
        remoteViews.setOnClickFillInIntent(R.id.story_widget_item_root, fillInIntent)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }
}