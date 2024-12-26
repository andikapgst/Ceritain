package com.dicoding.storyapp.data.di

import android.content.Context
import com.dicoding.storyapp.data.local.database.StoryDatabase
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val database = StoryDatabase.getDatabase(context)
        val user = runBlocking { pref.getLoginState().first() }
        val apiService = ApiConfig.getApiService(user.token)
        return StoryRepository.getInstance(apiService, database, pref)
    }
}