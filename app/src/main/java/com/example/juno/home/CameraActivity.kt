package com.example.juno.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.juno.databinding.ActivityCameraBinding
import com.example.juno.home.cutomView.BarcodeBoxView
import com.example.juno.viewModelFactory.GenericSavedStateViewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

typealias LumaListener = (luma: Double) -> Unit

@AndroidEntryPoint
class CameraActivity:AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "CAMERA_ACTIVITY"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    private var imageCapture: ImageCapture? = null

    lateinit var binding:ActivityCameraBinding
    private lateinit var button_capture:Button
    private lateinit var button_back:Button
    private lateinit var cameraExecutor:ExecutorService
    private lateinit var barcodeBoxView: BarcodeBoxView
    private lateinit var qrCodeAnalyzer: QrCodeAnalyzer

    private var btc_eth_code: String = ""

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                val returnedValue = Intent().apply {
                    putExtra("data", "Goback")
                }
                setResult(Activity.RESULT_OK, returnedValue)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        button_capture = binding.buttonCapture
        button_back = binding.buttonBack
        button_back.setOnClickListener(this)
        button_capture.setOnClickListener(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeBoxView = BarcodeBoxView(this)
        addContentView(barcodeBoxView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        this.onBackPressedDispatcher.addCallback(callback)
        startCamera()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE
        ).build()
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()
            // getClient() creates a new instance of the MLKit barcode scanner with the specified options
            val scanner = BarcodeScanning.getClient(options)

            // setting up the analysis use case
            val analysisUseCase = ImageAnalysis.Builder()
                .build()

            // define the actual functionality of our analysis use case
//            analysisUseCase.setAnalyzer(
//                // newSingleThreadExecutor() will let us perform analysis on a single worker thread
//                Executors.newSingleThreadExecutor()
//            ) { imageProxy ->
//                processImageProxy(scanner, imageProxy)
//            }
            qrCodeAnalyzer = QrCodeAnalyzer(this,
                barcodeBoxView
                ,binding.viewFinder.width.toFloat()
                ,binding.viewFinder.height.toFloat()
            )
            analysisUseCase.setAnalyzer(cameraExecutor,qrCodeAnalyzer)
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,analysisUseCase)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    showToast(msg)
                    Log.d(TAG, msg)
                }
            }
        )
    }
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun  processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {

        imageProxy.image?.let { image ->
            val inputImage =
                InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    val barcode = barcodeList.getOrNull(0)

                    // `rawValue` is the decoded value of the barcode
                    barcode?.rawValue?.let { value ->
                        btc_eth_code = value

                    }
                }
                .addOnFailureListener {
                    // This failure will happen if the barcode scanning model
                    // fails to download from Google Play Services
                    Log.e(TAG, it.message.orEmpty())
                }.addOnCompleteListener {
                    // When the image is from CameraX analysis use case, must
                    // call image.close() on received images when finished
                    // using them. Otherwise, new images may not be received
                    // or the camera may stall.
                    imageProxy.image?.close()
                    imageProxy.close()

                }
        }
    }


    override fun onClick(v: View?) {

        when(v){
            button_capture -> {
                takePhoto()
                val returnedValue = Intent().apply {
                    putExtra("data", qrCodeAnalyzer.btc_eth_value)
                }
                setResult(Activity.RESULT_OK, returnedValue)
                finish()
            }
            button_back ->{
                val returnedValue = Intent().apply {
                    putExtra("data", "Goback")
                }
                setResult(Activity.RESULT_OK, returnedValue)
                finish()
            }
        }
    }
    private fun showToast(msg:String?){
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }
}
