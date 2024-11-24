package com.dicoding.storyapp.view.activities.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.Story
import kotlinx.coroutines.launch

class StoryDetailViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _storyDetail = MutableLiveData<Story?>()
    val storyDetail: LiveData<Story?> = _storyDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    fun getStoryDetail(id: String) {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoryDetail(id)
                _storyDetail.value = response.story
            } catch (e: Exception) {
                _toastMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}