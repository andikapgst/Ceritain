package com.dicoding.storyapp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.dicoding.storyapp.R

/**
 * Implementation of App Widget functionality.
 */
class StackWidget : AppWidgetProvider() {

    companion object {
        private const val TOAST_ACTION = "com.dicoding.storyapp.TOAST_ACTION"
        const val EXTRA_ITEM = "com.dicoding.storyapp.EXTRA_ITEM"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            // Create a RemoteViews object for the widget layout
            val views = RemoteViews(context.packageName, R.layout.stack_widget)

            // Set up the RemoteViews adapter using a StackWidgetService subclass
            val serviceIntent = Intent(context, StackWidgetService::class.java)
            @Suppress("DEPRECATION")
            views.setRemoteAdapter(R.id.story_widget_item_root, serviceIntent)

            // Update the app widget with the RemoteViews object
            appWidgetManager.updateAppWidget(appWidgetId, views)
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.stack_widget)
        @Suppress("DEPRECATION")
        views.setRemoteAdapter(R.id.story_widget_item_root, Intent(context, StackWidgetService::class.java))
        views.setEmptyView(R.id.story_widget_item_root, R.id.tv_empty_feed)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null) {
            if (intent.action == TOAST_ACTION) {
                val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
                Toast.makeText(context, "Touched view $viewIndex", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/*internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.stack_widget)
    views.setTextViewText(R.id.story_widget_item_root, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}*/