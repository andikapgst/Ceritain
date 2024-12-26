package com.dicoding.storyapp.view.activities.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.view.activities.maps.MapsActivity.StoryLocation
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _location = MutableLiveData<List<StoryLocation>>()
    val location: LiveData<List<StoryLocation>> = _location

    fun getStoryLocation(): LiveData<List<StoryLocation>> {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoriesWithLocation()
                val locations = response.listStory.map { story ->
                    StoryLocation(
                        photoUrl = story.photoUrl ?: "",
                        name = story.name ?: "",
                        description = story.description ?: "",
                        lat = story.lat ?: 0.0f,
                        lon = story.lon ?: 0.0f
                    )
                }
                _location.value = locations
                Log.d("MapsViewModel", "Response: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                Log.e("MapsViewModel", "Error: ${errorResponse.message}")
            }
        }
        return _location
    }
}