package com.dicoding.storyapp.view.activities.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.repository.AuthRepository
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.data.response.ListStoryItem
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _storyResponse = MutableLiveData<String?>()
    val storyResponse: LiveData<String?> = _storyResponse

    private val _storyResult = MutableLiveData<Result<List<ListStoryItem>>>()
    val storyResult: LiveData<Result<List<ListStoryItem>>> = _storyResult

    fun getSession(): LiveData<UserModel> {
        return authRepository.getLoginState().asLiveData()
    }

    fun getLoginToken(): LiveData<String> {
        return authRepository.getToken().asLiveData()
    }

    fun getUsername(): LiveData<String> {
        return authRepository.getUsername().asLiveData()
    }

    fun getListStories() {
        _isLoading.value = true
        viewModelScope.launch {
            _storyResult.value = Result.Loading
            try {
                val storyResponse = storyRepository.getStories()
                _isLoading.value = false
                _storyResult.value = Result.Success(storyResponse.listStory)
                _storyResponse.value = storyResponse.message
            } catch (e: HttpException) {
                val errorBody = Gson().fromJson(e.response()?.errorBody()?.string(), ErrorResponse::class.java)
                val errorMessage = errorBody?.message ?: e.message()
                _isLoading.value = false
                _storyResult.value = Result.Error(errorMessage)
                _storyResponse.value = errorMessage
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val user = getUsername()
                authRepository.logout()
                Log.d("MainViewModel", "User $user successfully logged out")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error logging out user: ${e.message}")
            }
        }
    }
}