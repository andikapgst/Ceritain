package com.dicoding.storyapp.data.repository

import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.response.StoryDetailResponse
import com.dicoding.storyapp.data.response.StoryResponse
import com.dicoding.storyapp.data.retrofit.ApiConfig
import com.dicoding.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.first

class StoryRepository(
    private var apiService: ApiService,
    private val pref: UserPreference
) {
    suspend fun getStoryDetail(id: String): StoryDetailResponse {
        val token = pref.getLoginState().first().token
        apiService = ApiConfig.getApiService(token)
        return apiService.getDetailStory(id)
    }

    suspend fun getStories(): StoryResponse {
        val token = pref.getLoginState().first().token
        apiService = ApiConfig.getApiService(token)
        return apiService.getStories()
    }

    suspend fun getStoriesWithLocation(): StoryResponse {
        val token = pref.getLoginState().first().token
        apiService = ApiConfig.getApiService(token)
        return apiService.getStoriesWithLocation()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            pref: UserPreference
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, pref)
            }.also { instance = it }
    }
}