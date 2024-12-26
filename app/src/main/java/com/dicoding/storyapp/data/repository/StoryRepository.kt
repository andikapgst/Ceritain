package com.dicoding.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.local.database.StoryDatabase
import com.dicoding.storyapp.data.local.database.StoryEntity
import com.dicoding.storyapp.data.local.remote.StoryRemoteMediator
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.response.FileUploadResponse
import com.dicoding.storyapp.data.response.LoginResponse
import com.dicoding.storyapp.data.response.RegisterResponse
import com.dicoding.storyapp.data.response.StoryDetailResponse
import com.dicoding.storyapp.data.response.StoryResponse
import com.dicoding.storyapp.data.retrofit.ApiConfig
import com.dicoding.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
    private val pref: UserPreference
) {

    suspend fun postRegister(name: String, email: String, password: String): RegisterResponse {
        return apiService.postRegister(name, email, password)
    }

    suspend fun postLogin(email: String, password: String): LoginResponse {
        return apiService.postLogin(email, password)
    }

    fun getLoginState(): Flow<UserModel> {
        return pref.getLoginState()
    }

    fun getUsername(): Flow<String> {
        return pref.getUsername()
    }

    fun getToken(): Flow<String> {
        return pref.getLoginToken()
    }

    suspend fun saveSession(user: UserModel) {
        pref.saveSession(user)
    }

    suspend fun logout() {
        pref.logout()
    }

    suspend fun uploadStory(file: MultipartBody.Part, description: RequestBody, lat: RequestBody? = null, lon: RequestBody? = null): FileUploadResponse {
        val token = pref.getLoginState().first().token
        val apiService = ApiConfig.getApiService(token)
        return apiService.uploadStory(file, description, lat, lon)
    }

    suspend fun getStoryDetail(id: String): StoryDetailResponse {
        val token = pref.getLoginState().first().token
        val apiService = ApiConfig.getApiService(token)
        return apiService.getDetailStory(id)
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<StoryEntity>> {
        val token = runBlocking { pref.getLoginState().first().token }
        val apiService = ApiConfig.getApiService(token)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
            //    StoryPagingSource(apiService)
                storyDatabase.storyDao().getAllStories()
            }
        ).liveData
    }

    suspend fun getStoriesWithLocation(): StoryResponse {
        val token = pref.getLoginState().first().token
        val apiService = ApiConfig.getApiService(token)
        return apiService.getStoriesWithLocation()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase,
            pref: UserPreference,
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, storyDatabase, pref)
            }.also { instance = it }
    }
}