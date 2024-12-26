@file:Suppress("DEPRECATION")

package com.dicoding.storyapp.view.auth

import com.dicoding.storyapp.view.activities.main.MainActivity
import com.dicoding.storyapp.view.activities.auth.login.LoginActivity
import com.dicoding.storyapp.R
import org.hamcrest.CoreMatchers.not
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.dicoding.storyapp.data.utils.LoginIdlingResource
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    var activityRule = ActivityTestRule(LoginActivity::class.java, true, false)

    private lateinit var idlingResource: LoginIdlingResource

    @Before
    fun disableAnimations() {
        val activity = activityRule.activity
        activity.window.setWindowAnimations(0)
    }

    @Before
    fun setUp() {
        // Launch the LoginActivity
        activityRule.launchActivity(Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java))

        // Get the IdlingResource from the ViewModel
        val viewModel = activityRule.activity.viewModel
        idlingResource = viewModel.getIdlingResource()
    }

    @Test
    fun testLoginWithValidCredentials() {
        activityRule.launchActivity(Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java))

        onView(withId(R.id.ed_login_email)).perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.btn_login)).check(matches(isEnabled()))

        onView(withId(R.id.btn_login)).perform(click())

        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun testLoginWithInvalidEmail() {
        activityRule.launchActivity(Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java))

        onView(withId(R.id.ed_login_email)).perform(typeText("invalid-email"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.btn_login)).check(matches(not(isEnabled())))
    }

    @Test
    fun testLoginWithShortPassword() {
        activityRule.launchActivity(Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java))

        onView(withId(R.id.ed_login_email)).perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("short"), closeSoftKeyboard())

        onView(withId(R.id.btn_login)).check(matches(not(isEnabled())))
    }
}