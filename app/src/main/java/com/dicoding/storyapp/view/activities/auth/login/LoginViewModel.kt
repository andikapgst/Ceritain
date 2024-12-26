package com.dicoding.storyapp.view.activities.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.data.response.LoginResponse
import com.dicoding.storyapp.data.utils.LoginIdlingResource
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

class LoginViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val idlingResource = LoginIdlingResource()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResponse = MutableLiveData<String?>()
    val loginResponse: LiveData<String?> = _loginResponse

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(email: String, password: String) {
        _isLoading.value = true
        _loginResponse.value = null
        LoginIdlingResource().setIdleState(false)
        viewModelScope.launch {
            try {
                val response = storyRepository.postLogin(email, password)
                when {
                    response.loginResult != null -> {
                        saveSession(
                            UserModel(
                                response.loginResult.name.toString(),
                                response.loginResult.token.toString(),
                                true
                            )
                        )
                        _isLoading.value = false
                        _loginResult.value = Result.Success(response)
                        _loginResponse.value = response.message
                    }
                    response.error == true -> {
                        _isLoading.value = false
                        _loginResult.value = Result.Error(response.message.toString())
                        _loginResponse.value = response.message
                    }
                }
            } catch (e: Exception) {
                handleLoginError(e)
            }
            finally {
                LoginIdlingResource().setIdleState(true)
            }
        }
    }

    private fun handleLoginError(e: Exception) {
        _isLoading.value = false
        val errorMessage = when (e) {
            is HttpException -> {
                val errorBody = Gson().fromJson(e.response()?.errorBody()?.string(), ErrorResponse::class.java)
                errorBody?.message ?: e.message()
            }
            is SocketTimeoutException -> e.message
            else -> e.message
        }
        _loginResponse.value = errorMessage.toString()
        _loginResult.value = Result.Error(errorMessage.toString())
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            storyRepository.saveSession(user)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return storyRepository.getLoginState().asLiveData()
    }

    fun getIdlingResource(): LoginIdlingResource {
        return idlingResource
    }
}