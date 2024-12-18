package com.dicoding.storyapp.view.activities.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.data.response.ListStoryItem
import com.dicoding.storyapp.data.response.StoryResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun getStoryLocation(): LiveData<List<ListStoryItem>> {
        val locationLiveData = MutableLiveData<List<ListStoryItem>>()
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoriesWithLocation()
                val locations = response.listStory.map { story ->
                    ListStoryItem(
                        story.id,
                        story.name,
                        story.description,
                        story.photoUrl,
                        story.lat.toString(),
                        story.lon.toString()
                    )
                }
                locationLiveData.value = locations
                Log.d("MapsViewModel", "Response: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                Log.e("MapsViewModel", "Error: ${errorResponse.message}")
            }
        }
        return locationLiveData
    }
}