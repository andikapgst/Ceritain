package com.dicoding.storyapp.view.activities.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.FileUploadResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class UploadViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _uploadResponse = MutableLiveData<String?>()
    val uploadResponse: LiveData<String?> = _uploadResponse

    fun uploadStory(file: MultipartBody.Part, description: RequestBody, lat: RequestBody? = null, lon: RequestBody? = null) {
        _isLoading.value = true
        _uploadResponse.value = null
        viewModelScope.launch {
            try {
                val response = storyRepository.uploadStory(file, description, lat, lon)
                _isSuccess.value = true
                _isLoading.value = false
                _uploadResponse.value = response.message
                Log.d("UploadViewModel", "uploadStory: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, FileUploadResponse::class.java)
                _isSuccess.value = false
                _uploadResponse.value = errorResponse.message
                _isLoading.value = false
                Log.e("UploadViewModel", "uploadStory: ${errorResponse.message}")
            }
        }
    }
}