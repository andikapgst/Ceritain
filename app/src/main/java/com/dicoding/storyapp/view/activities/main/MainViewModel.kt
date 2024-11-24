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
import com.dicoding.storyapp.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

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
        viewModelScope.launch {
            _storyResult.value = Result.Loading
            try {
                val storyResponse = storyRepository.getStories()
                _storyResult.value = Result.Success(storyResponse.listStory)
            } catch (e: Exception) {
                _storyResult.value = Result.Error(e.message.toString())
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