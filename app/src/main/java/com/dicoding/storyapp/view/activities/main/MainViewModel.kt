package com.dicoding.storyapp.view.activities.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.local.database.StoryEntity
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _storyResult = MutableLiveData<Result<List<ListStoryItem>>>()
    val storyResult: LiveData<Result<List<ListStoryItem>>> = _storyResult

    fun getSession(): LiveData<UserModel> {
        return storyRepository.getLoginState().asLiveData()
    }

    fun getLoginToken(): LiveData<String> {
        return storyRepository.getToken().asLiveData()
    }

    fun getUsername(): LiveData<String> {
        return storyRepository.getUsername().asLiveData()
    }

    fun getStories(): LiveData<PagingData<StoryEntity>> {
        return storyRepository.getStories().cachedIn(viewModelScope)
    }

    /*fun getListStories() {
        _isLoading.value = true
        viewModelScope.launch {
            _storyResult.value = Result.Loading
            try {
                val storyResponse = storyRepository.getStories()
                _isLoading.value = false
                _storyResult.value = Result.Success(storyResponse.listStory)
            } catch (e: HttpException) {
                val errorBody = Gson().fromJson(e.response()?.errorBody()?.string(), ErrorResponse::class.java)
                val errorMessage = errorBody?.message ?: e.message()
                _isLoading.value = false
                _storyResult.value = Result.Error(errorMessage)
            }
        }
    }*/

    fun logout() {
        viewModelScope.launch {
            try {
                storyRepository.logout()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error logging out user: ${e.message}")
            }
        }
    }
}