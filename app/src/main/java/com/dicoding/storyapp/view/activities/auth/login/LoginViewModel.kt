package com.dicoding.storyapp.view.activities.auth.login

import android.util.Log
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

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<String?>()
    val isError: LiveData<String?> = _isError

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(email: String, password: String) {
        _isLoading.value = true
        _isError.value = null
        viewModelScope.launch {
            try {
                val loginResponse = authRepository.postLogin(email, password)
                when {
                    loginResponse.loginResult != null -> {
                        saveSession(
                            UserModel(
                                loginResponse.loginResult.name.toString(),
                                loginResponse.loginResult.token.toString(),
                                true
                            )
                        )
                        _isSuccess.value = true
                        _loginResult.value = Result.Success(loginResponse)
                        _isError.value = null
                    }
                    loginResponse.message != null -> {
                        throw Exception(loginResponse.message)
                    }
                }
            } catch (e: HttpException) {
                val errorBody = Gson().fromJson(e.response()?.errorBody()?.string(), ErrorResponse::class.java)
                val errorMessage = errorBody?.message ?: e.message()
                _isError.value = errorMessage.toString()
                _isSuccess.value = false
                _loginResult.value = Result.Error(errorMessage)
            } finally {
                _isLoading.value = false
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