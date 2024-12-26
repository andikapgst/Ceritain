package com.dicoding.storyapp.view.activities.story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.utils.getImageUri
import com.dicoding.storyapp.data.utils.reduceFileImage
import com.dicoding.storyapp.data.utils.uriToFile
import com.dicoding.storyapp.databinding.ActivityUploadStoryBinding
import com.dicoding.storyapp.view.activities.ViewModelFactory
import com.dicoding.storyapp.view.activities.main.MainActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.toString

class UploadStoryActivity : AppCompatActivity() {

    private val viewModel by viewModels<UploadViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityUploadStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isGpsEnabled = false
    private var currentLocation: Location? = null
    private var currentImageUri: Uri? = null
    private var latitude: Float? = null
    private var longitude: Float? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getCurrentLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getCurrentLocation()
                }
                else -> {
                    binding.switchAddLocation.isChecked = false
                }
            }
        }

    private fun cameraPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        if (!cameraPermissionsGranted()) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.switchAddLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!isGpsEnabled) {
                    checkLocationSettings()
                } else {
                    getCurrentLocation()
                }
            } else {
                currentLocation = null
            }
        }
        binding.galleryBtn.setOnClickListener { startGallery() }
        binding.cameraBtn.setOnClickListener { startCamera() }
        binding.postBtn.setOnClickListener { uploadStory() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showToast(getString(R.string.empty_image_warning))
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imgPreview.setImageURI(it)
        }
    }

    private fun checkLocationPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationSettings() {
        @Suppress("DEPRECATION")
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            isGpsEnabled = true
            binding.switchAddLocation.isChecked = true
            getCurrentLocation()
            Log.d(TAG, "GPS is enabled")
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: Exception) {
                    Log.e(TAG, "Error showing location settings dialog", sendEx)
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude.toFloat()
                    longitude = location.longitude.toFloat()
                    Log.d("Location", "Current location: $latitude, $longitude")
                } else {
                    Log.w(TAG, "Location is null")
                    showToast(getString(R.string.location_not_found))
                    binding.switchAddLocation.isChecked = false
                    getCurrentLocation()
                }
            }
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                isGpsEnabled = true
                binding.switchAddLocation.isChecked = true
                getCurrentLocation()
                Log.d(TAG, "User enabled location settings")
            } else {
                isGpsEnabled = false
                binding.switchAddLocation.isChecked = false
                Log.w(TAG, "User didn't enable location settings")
            }
        }
    }

    private fun uploadStory() {
        binding.progressIndicator.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (currentImageUri == null) {
            showToast(getString(R.string.empty_image_warning))
            binding.progressIndicator.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            return
        }

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            val description = binding.edAddDesc.text.toString()
            if (description.isEmpty()) {
                showToast(getString(R.string.empty_description_warning))
                binding.progressIndicator.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                return
            }

            val desc = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            val lat: RequestBody? = latitude?.toString()?.toRequestBody("text/plain".toMediaType())
            val lon: RequestBody? = longitude?.toString()?.toRequestBody("text/plain".toMediaType())

            viewModel.uploadStory(
                multipartBody,
                desc,
                lat,
                lon
            )
            viewModel.uploadResponse.observe(this) { message ->
                if (message != null) {
                    showToast(message)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }

            viewModel.isSuccess.observe(this) { isSuccess ->
                if (isSuccess) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }

            viewModel.isLoading.observe(this) { isLoading ->
                binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 100
        private const val TAG = "UploadStoryActivity"
    }
}