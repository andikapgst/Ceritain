package com.dicoding.storyapp.view.activities.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.repository.AuthRepository
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.data.response.ErrorResponse
import com.dicoding.storyapp.data.response.LoginResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResponse = MutableLiveData<String?>()
    val loginResponse: LiveData<String?> = _loginResponse

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(email: String, password: String) {
        _isLoading.value = true
        _loginResponse.value = null
        viewModelScope.launch {
            try {
                val response = authRepository.postLogin(email, password)
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
                }
            } catch (e: HttpException) {
                val errorBody = Gson().fromJson(e.response()?.errorBody()?.string(), ErrorResponse::class.java)
                val errorMessage = errorBody?.message ?: e.message()
                _isLoading.value = false
                _loginResponse.value = errorMessage.toString()
                _loginResult.value = Result.Error(errorMessage)
            } catch (e: SocketTimeoutException) {
                _isLoading.value = false
                _loginResponse.value = e.message.toString()
                _loginResult.value = Result.Error(e.message.toString())
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            authRepository.saveSession(user)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return authRepository.getLoginState().asLiveData()
    }
}