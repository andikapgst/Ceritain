package com.dicoding.storyapp.view.activities.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(
    private val storyRepository: StoryRepository
): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerResponse = MutableLiveData<String?>()
    val registerResponse: LiveData<String?> = _registerResponse
    
    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        _registerResponse.value = null
        viewModelScope.launch {
            try {
                val response = storyRepository.postRegister(name, email, password)
                when {
                    response.error != true -> {
                        _registerResult.value = Result.Success(response)
                        _isLoading.value = false
                        _registerResponse.value = response.message
                    }
                }
            } catch (e: HttpException) {
                val errorBody = Gson().fromJson(e.response()?.errorBody()?.string(), ErrorResponse::class.java)
                val errorMessage = errorBody?.message ?: e.message()
                _isLoading.value = false
                _registerResponse.value = errorMessage.toString()
                _registerResult.value = Result.Error(errorMessage)
            }
        }
    }
}