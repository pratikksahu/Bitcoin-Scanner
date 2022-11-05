package com.example.juno.home

import android.os.Bundle
import android.os.PersistableBundle
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.example.juno.R
import com.example.juno.databinding.CameraPreviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraPreviewActivity: AppCompatActivity() {
    private lateinit var binding: CameraPreviewBinding

    lateinit var cameraProvider: ProcessCameraProvider
    private var cameraSelector: CameraSelector? = null
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.camera_preview)
        binding = DataBindingUtil.setContentView(this,R.layout.camera_preview)
        showToast("Hello")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

//        cameraProviderFuture.addListener({
//            // Camera provider is now guaranteed to be available
//            val cameraProvider = cameraProviderFuture.get()
//
//            // Set up the preview use case to display camera preview.
//            val preview = Preview.Builder().build()
//
//            // Set up the capture use case to allow users to take photos.
//            imageCapture = ImageCapture.Builder()
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                .build()
//
//            // Choose the camera by requiring a lens facing
//            val cameraSelector = CameraSelector.Builder()
//                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//                .build()
//
//            // Attach use cases to the camera with the same lifecycle owner
//            val camera = cameraProvider.bindToLifecycle(
//                this as LifecycleOwner, cameraSelector, preview, imageCapture)
//
//            // Connect the preview use case to the previewView
//            preview.setSurfaceProvider(
//                binding.viewFinder.surfaceProvider
//            )
//        }, ContextCompat.getMainExecutor(this))
//        startCamera()
    }

//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            cameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder()
//                .build()
//                .also { mPreview ->
//                    mPreview.setSurfaceProvider(
//                    binding.viewFinder.surfaceProvider
//                    )
//                }
//            imageCapture = ImageCapture.Builder().build()
//            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try{
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector!!,preview,imageCapture)
//            }catch (e: java.lang.Exception){
//                Log.d("TAG",e.message.toString())
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
private fun showToast(message: String){
    Toast.makeText(this,message, Toast.LENGTH_LONG).show()
}
}