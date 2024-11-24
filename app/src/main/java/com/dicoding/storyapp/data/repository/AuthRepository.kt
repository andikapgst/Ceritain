package com.dicoding.storyapp.data.repository

import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.response.LoginResponse
import com.dicoding.storyapp.data.response.RegisterResponse
import com.dicoding.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val apiService: ApiService,
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

    companion object {
        @Volatile
        private var instance: AuthRepository? = null
        fun getInstance(
            apiService: ApiService,
            pref: UserPreference
        ): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(apiService, pref)
            }.also { instance = it }
    }
}