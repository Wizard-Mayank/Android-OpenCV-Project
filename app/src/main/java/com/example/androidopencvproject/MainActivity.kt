package com.example.androidopencvproject

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidopencvproject.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Set up the ImageAnalysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // This is where the magic happens (our C++ bridge)
            imageAnalyzer.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->

                // Get the raw Y-plane data (grayscale) from the camera
                val yPlane = imageProxy.planes[0]
                val yBuffer = yPlane.buffer
                val yStride = yPlane.rowStride

                val yBytes = ByteArray(yBuffer.remaining())
                yBuffer.get(yBytes)

                // Call our JNI function to process the image
                val processedBytes = processImage(
                    imageProxy.width,
                    imageProxy.height,
                    yBytes,
                    yStride
                )

                // --- Safely display the processed C++ result ---

                // Check if C++ returned valid data
                if (processedBytes != null && processedBytes.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)

                    // Only update if the bitmap was successfully decoded
                    if (bitmap != null) {
                        // Update the ImageView on the UI thread
                        runOnUiThread {
                            binding.processedImageview.setImageBitmap(bitmap)
                        }
                    }
                }

                // Close the imageProxy
                imageProxy.close()
            })


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the ImageAnalysis use case to the camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer
                )

            } catch (exc: Exception) {
                // Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    // This handles the pop-up result (Allow / Deny)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        // private const val TAG = "AndroidOpenCVProject" // Uncomment for logging
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()

        // Load the native library
        init {
            // 1. Load the C++ runtime (which OpenCV depends on)
            System.loadLibrary("c++_shared")

            // 2. Load the main OpenCV library
            System.loadLibrary("opencv_java4")

            // 3. Load your app's library LAST
            System.loadLibrary("androidopencvproject")
        }
    }

    /**
     * A native method that is implemented by the
     * 'androidopencvproject' native library, which is packaged
     * with this application.
     */
    external fun processImage(
        width: Int,
        height: Int,
        yPlane: ByteArray, // We will send the Y-plane (grayscale) data
        yStride: Int
    ): ByteArray // We will get back a processed, JPEG-encoded byte array
}