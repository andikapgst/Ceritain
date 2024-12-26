package com.dicoding.storyapp.view.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.dicoding.storyapp.DataDummy
import com.dicoding.storyapp.MainDispatcherRule
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.getOrAwaitValue
import com.dicoding.storyapp.view.activities.auth.login.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Test
    fun `when Login Success Should Save Token and Set Login Status True`() = runTest {
        val dummyLoginResponse = DataDummy.generateDummyLoginResponse()
        val expectedLoginResult = Result.Success(dummyLoginResponse)
        expectedLoginResult.data
        val email = "alfredmoore@example.com"
        val password = "Password Test"
        val userModel = UserModel(
            dummyLoginResponse.loginResult?.name ?: "",
            dummyLoginResponse.loginResult?.token ?: "",
            true
        )

        `when`(storyRepository.postLogin(email, password)).thenReturn(dummyLoginResponse)
        `when`(storyRepository.saveSession(userModel)).thenReturn(Unit)

        val loginViewModel = LoginViewModel(storyRepository)
        loginViewModel.login(email, password)

        Mockito.verify(storyRepository).postLogin(email, password)
        Mockito.verify(storyRepository).saveSession(userModel)

        val actualLoginResult = loginViewModel.loginResult.getOrAwaitValue()
        assertNotNull(actualLoginResult)
        assertTrue(actualLoginResult is Result.Success)
        assertEquals(dummyLoginResponse, (actualLoginResult as Result.Success).data)
    }

    @Test
    fun `when Login Failed Should Not Save Token and Set Login Status False`() = runTest {
        val data = DataDummy.generateDummyLoginFailedResponse()
        val expectedLoginResult = Result.Error(data.message.toString())
        val email = "james.francis.byrnes@example-pet-store.com"
        val password = "PasswordTest"

        `when`(storyRepository.postLogin(email, password)).thenReturn(data)

        val loginViewModel = LoginViewModel(storyRepository)
        loginViewModel.login(email, password)

        Mockito.verify(storyRepository).postLogin(email, password)

        val actualLoginResult = loginViewModel.loginResult.getOrAwaitValue()
        assertNotNull(actualLoginResult)
        assertTrue(actualLoginResult is Result.Error)
        assertEquals(expectedLoginResult.error, (actualLoginResult as Result.Error).error)
    }

    @Test
    fun `when Save Session Should Call Repository Save Session`() = runTest {
        val userModel = UserModel(
            "User Test",
            "token",
            true
        )
        `when`(storyRepository.saveSession(userModel)).thenReturn(Unit)

        val loginViewModel = LoginViewModel(storyRepository)
        loginViewModel.saveSession(userModel)

        Mockito.verify(storyRepository).saveSession(userModel)
    }

    @Test
    fun `when Get Session Should Return User Model`() = runTest {
        val userModel = UserModel(
            "User Test",
            "token",
            true
        )
        val expectedUserModel = MutableLiveData<UserModel>()
        expectedUserModel.value = userModel
        `when`(storyRepository.getLoginState()).thenReturn(expectedUserModel.asFlow())

        val loginViewModel = LoginViewModel(storyRepository)
        val actualUserModel = loginViewModel.getSession().getOrAwaitValue()

        Mockito.verify(storyRepository).getLoginState()
        assertNotNull(actualUserModel)
        assertEquals(userModel, actualUserModel)
    }
}