package com.dicoding.storyapp

import com.dicoding.storyapp.data.local.database.StoryEntity
import com.dicoding.storyapp.data.response.LoginResponse
import com.dicoding.storyapp.data.response.LoginResult

object DataDummy {

    fun generateDummyStoriesResponse(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..10) {
            val story = StoryEntity(
                i.toString(),
                "photo + $i",
                "created + $i",
                "name + $i",
                "desc + $i",
                i.toDouble(),
                i.toDouble()
            )
            items.add(story)
        }
        return items
    }

    fun generateDummyLoginResponse(): LoginResponse {
        return LoginResponse(
            LoginResult(
                "name",
                "userId",
                "token"
            ),
            false,
            "success"
        )
    }

    fun generateDummyLoginFailedResponse(): LoginResponse {
        return LoginResponse(
            null,
            true,
            "failed"
        )
    }
}