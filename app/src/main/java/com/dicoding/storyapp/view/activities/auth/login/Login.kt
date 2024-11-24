package com.dicoding.storyapp.view.activities.auth.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.view.activities.ViewModelFactory
import com.dicoding.storyapp.view.activities.auth.register.Register
import com.dicoding.storyapp.view.activities.main.MainActivity
import com.dicoding.storyapp.view.customview.Button
import com.dicoding.storyapp.view.customview.EmailEditText
import com.dicoding.storyapp.view.customview.PasswordEditText
import kotlin.text.isNotEmpty

class Login : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                binding.progressIndicator.visibility = View.GONE
            }
        }

        emailEditText = binding.edLoginEmail
        passwordEditText = binding.edLoginPassword
        loginButton = binding.btnLogin

        setMyButtonEnable()
        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                setMyButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                setMyButtonEnable()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        loginButton.setOnClickListener {
            viewModel.isLoading.observe(this) { isLoading ->
                binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.login(email, password)

            viewModel.loginResult.observe(this) { result ->
                binding.progressIndicator.visibility = View.VISIBLE
                when (result) {
                    is Result.Success -> {
                        viewModel.getSession()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                        Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    }

                    is Result.Error -> {
                        binding.progressIndicator.visibility = View.GONE
                        viewModel.isError.observe(this) { errorMessage ->
                            if (errorMessage != null) {
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    else -> Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRegisterHere.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setMyButtonEnable() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotEmpty()
        val isPasswordValid = password.length >= 8 && password.isNotEmpty()

        loginButton.isEnabled = isEmailValid && isPasswordValid // Enable button if both are valid
    }
}