package com.dicoding.storyapp.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.dicoding.storyapp.view.adapter.StoryAdapter

class StackWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext, StoryAdapter())
    }
}