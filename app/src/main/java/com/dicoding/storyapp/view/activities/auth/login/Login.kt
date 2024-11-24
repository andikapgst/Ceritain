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

class Login : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                binding.progressIndicator.visibility = View.GONE
            }
        }

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
        inactiveButton()
    }

    private fun setupAction() {
        binding.edLoginEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                inactiveButton()
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (!isValidEmail(s.toString())) {
                    binding.ovEmail.error = getString(R.string.invalid_email)
                    inactiveButton()
                } else {
                    binding.ovEmail.error = null
                    activeButton()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (!isValidEmail(s.toString())) {
                    binding.ovEmail.error = getString(R.string.invalid_email)
                    inactiveButton()
                } else {
                    binding.ovEmail.error = null
                    activeButton()
                }
            }
        })

        binding.edLoginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                inactiveButton()
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if ((s.length < 8)) {
                    binding.ovPassword.error = getString(R.string.invalid_password)
                    inactiveButton()
                } else {
                    binding.ovPassword.error = null
                    activeButton()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if ((s.length < 8)) {
                    binding.ovPassword.error = getString(R.string.invalid_password)
                    inactiveButton()
                } else {
                    binding.ovPassword.error = null
                    activeButton()
                }
            }
        })

        binding.btnLogin.setOnClickListener {
            viewModel.isLoading.observe(this) { isLoading ->
                binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()
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

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun activeButton() {
        val email = binding.edLoginEmail.text.toString()
        val password = binding.edLoginPassword.text.toString()
        binding.btnLogin.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && isValidEmail(email) && password.length >= 8
    }

    private fun inactiveButton() {
        binding.btnLogin.isEnabled = false
    }
}