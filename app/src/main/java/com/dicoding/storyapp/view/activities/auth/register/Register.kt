package com.dicoding.storyapp.view.activities.auth.register

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
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.repository.Result
import com.dicoding.storyapp.databinding.ActivityRegisterBinding
import com.dicoding.storyapp.view.activities.ViewModelFactory
import com.dicoding.storyapp.view.activities.auth.login.Login
import com.dicoding.storyapp.view.customview.Button
import com.dicoding.storyapp.view.customview.EmailEditText
import com.dicoding.storyapp.view.customview.PasswordEditText

class Register : AppCompatActivity() {
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                binding.progressIndicator.visibility = View.GONE
            }
        }

        emailEditText = binding.edRegisterEmail
        passwordEditText = binding.edRegisterPassword
        registerButton = binding.btnRegister

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
        binding.edRegisterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
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
                if (s.toString().isEmpty()) {
                    binding.edRegisterName.error = getString(R.string.empty_name_warning)
                } else {
                    binding.edRegisterName.error = null
                    setMyButtonEnable()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    binding.edRegisterName.error = getString(R.string.empty_name_warning)
                } else {
                    binding.edRegisterName.error = null
                    setMyButtonEnable()
                }
            }
        })

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

        registerButton.setOnClickListener {
            binding.progressIndicator.visibility = View.VISIBLE
            val name = binding.edRegisterName.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.register(name, email, password)

            viewModel.registerResult.observe(this) { result ->
                binding.progressIndicator.visibility = View.VISIBLE
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    }

                    is Result.Error -> {
                        binding.progressIndicator.visibility = View.GONE
                        viewModel.isError.observe(this) { errorMessage ->
                            if (errorMessage != null) {
                                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    else -> Toast.makeText(this, getString(R.string.signup_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnLoginHere.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setMyButtonEnable() {
        val name = binding.edRegisterName.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        val isNameValid = name.isNotEmpty()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotEmpty()
        val isPasswordValid = password.length >= 8 && password.isNotEmpty()

        registerButton.isEnabled = isNameValid && isEmailValid && isPasswordValid
    }
}