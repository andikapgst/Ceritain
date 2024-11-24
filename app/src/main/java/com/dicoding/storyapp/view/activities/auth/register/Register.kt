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

class Register : AppCompatActivity() {
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityRegisterBinding

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
        binding.edRegisterEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                if (!isValidEmail(s.toString())) {
                    binding.ovEmail.error = getString(R.string.invalid_email)
                    inactiveButton()
                } else {
                    binding.ovEmail.error = null
                    activeButton()
                }
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

        binding.edRegisterPassword.addTextChangedListener(object : TextWatcher {
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

        binding.btnSignup.setOnClickListener {
            binding.progressIndicator.visibility = View.VISIBLE
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
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

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun activeButton() {
        val email = binding.edRegisterEmail.text.toString()
        val password = binding.edRegisterPassword.text.toString()
        binding.btnSignup.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && isValidEmail(email) && password.length >= 8
    }

    private fun inactiveButton() {
        binding.btnSignup.isEnabled = false
    }
}