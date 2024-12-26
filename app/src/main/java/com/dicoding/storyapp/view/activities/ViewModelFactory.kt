package com.dicoding.storyapp.view.activities

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.di.Injection
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.view.activities.auth.login.LoginViewModel
import com.dicoding.storyapp.view.activities.auth.register.RegisterViewModel
import com.dicoding.storyapp.view.activities.detail.StoryDetailViewModel
import com.dicoding.storyapp.view.activities.main.MainViewModel
import com.dicoding.storyapp.view.activities.maps.MapsViewModel
import com.dicoding.storyapp.view.activities.story.UploadViewModel

class ViewModelFactory(
    private val storyRepository: StoryRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(StoryDetailViewModel::class.java) -> {
                StoryDetailViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(UploadViewModel::class.java) -> {
                UploadViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(
                        Injection.provideStoryRepository(context)
                    )
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}