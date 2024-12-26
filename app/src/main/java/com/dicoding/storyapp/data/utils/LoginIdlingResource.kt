package com.dicoding.storyapp.data.utils

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback

class LoginIdlingResource : IdlingResource {
    private var callback: ResourceCallback? = null
    private var isIdleNow = true

    override fun getName(): String = this.javaClass.name

    override fun isIdleNow(): Boolean = isIdleNow

    override fun registerIdleTransitionCallback(callback: ResourceCallback?) {
        this.callback = callback
    }

    fun setIdleState(isIdle: Boolean) {
        isIdleNow = isIdle
        if (isIdle) {
            callback?.onTransitionToIdle()
        }
    }
}