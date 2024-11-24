package com.dicoding.storyapp.view.activities.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.storyapp.data.repository.AuthRepository

class UploadViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun getToken(): LiveData<String> {
        return authRepository.getToken().asLiveData()
    }
}