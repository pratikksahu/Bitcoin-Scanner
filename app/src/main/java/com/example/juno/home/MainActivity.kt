package com.example.juno.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.juno.R
import com.example.juno.databinding.ActivityMainBinding
import com.example.juno.databinding.CameraPreviewBinding
import com.example.juno.viewModelFactory.GenericSavedStateViewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.Math.*
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    @Inject
    internal lateinit var mainViewModelFactory: MainViewModelFactory

    private val mainViewModel: MainViewModel by viewModels{
        GenericSavedStateViewModelFactory(mainViewModelFactory,this)
    }

    companion object{
        private val CAMERA_REQUEST_CODE = 100
        private val STORAGE_REQUEST_CODE = 101
        private val TYPE_CAMERA = 1
        private val TYPE_GALLERY  = 2
        private val TYPE_BTC = 1
        private val TYPE_ETH  = 2
        private val TAG = "Main"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    private var imageURI: Uri? = null


    private lateinit var btc:Button
    private lateinit var eth:Button
    private lateinit var share:Button
    private var crypto:Int = 2
    private lateinit var binding:ActivityMainBinding
    private lateinit var cameraBinding: CameraPreviewBinding

    //QRcode

    private var barcodeOptions:BarcodeScannerOptions? = null
    private var barcodeScanner: BarcodeScanner? = null

    //----------------------------------//----------------------------------//----------------------------------
    private var previewView: PreviewView? = null
    lateinit var cameraProvider: ProcessCameraProvider
    private var cameraSelector: CameraSelector? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null

//    private val screenAspectRatio: Int
//        get() {
//            // Get screen metrics used to setup camera for full screen resolution
//            val metrics = DisplayMetrics().also { previewView?.display?.getRealMetrics(it) }
//            return aspectRatio(metrics.widthPixels, metrics.heightPixels)
//        }

    private var imageCapture: ImageCapture? = null
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            cameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder()
//                .build()
//                .also { mPreview ->
//
//                    cameraBinding.viewFinder.surfaceProvider
//                }
//                imageCapture = ImageCapture.Builder().build()
//            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try{
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector!!,preview,imageCapture)
//            }catch (e: java.lang.Exception){
//                Log.d(TAG,e.message.toString())
//            }
//        },ContextCompat.getMainExecutor(this))
//    }

   /* private fun bindCameraUseCases() {
        bindPreviewUseCase()
        bindAnalyseUseCase()
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView!!.display.rotation)
            .build()
        previewUseCase!!.setSurfaceProvider(previewView!!.surfaceProvider)

        try {
            cameraProvider!!.bindToLifecycle(
                /* lifecycleOwner= */this,
                cameraSelector!!,
                previewUseCase
            )
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    private fun bindAnalyseUseCase() {
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // BarcodeScannerOptions.Builder()
        //     .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        //     .build();
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView!!.display.rotation)
            .build()

        // Initialize our background executor
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analysisUseCase?.setAnalyzer(
            cameraExecutor,
            ImageAnalysis.Analyzer { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy)
            }
        )

        try {
            cameraProvider!!.bindToLifecycle(
                /* lifecycleOwner= */this,
                cameraSelector!!,
                analysisUseCase
            )
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach {
                    it.rawValue?.let { it1 -> Log.d(TAG, it1) }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.message ?: it.toString())
            }.addOnCompleteListener {
                // When the image is from CameraX analysis use case, must call image.close() on received
                // images when finished using them. Otherwise, new images may not be received or the camera
                // may stall.
                imageProxy.close()
            }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis], [androidx.camera.core.Preview] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }*/
    //----------------------------------//----------------------------------//----------------------------------


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        btc = binding.buttonBtc
        eth = binding.buttonEth
        share = binding.buttonShare

        //Creating permission lists
        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        buttonsSetup()
        viewModelSetup()
    }

    //Function to prompt dialog box
    private fun dialogBox(){
        val dialogBinding = layoutInflater.inflate(R.layout.fragment_dialog,null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)

        myDialog.setCancelable(true)
        val buttonC = dialogBinding.findViewById<Button>(R.id.button_camera)
        val buttonG = dialogBinding.findViewById<Button>(R.id.button_gallery)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        buttonC.setOnClickListener {
            openCameraOrGallery(TYPE_CAMERA)
            myDialog.dismiss()
        }

        buttonG.setOnClickListener {
            openCameraOrGallery(TYPE_GALLERY)
            myDialog.dismiss()
        }
        myDialog.show()

    }

    private fun buttonsSetup(){
        barcodeOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(barcodeOptions!!)

        btc.setOnClickListener(this)
        eth.setOnClickListener(this)
        share.setOnClickListener(this)

    }

    //Observe live data in view model
    private fun viewModelSetup(){
        mainViewModel.codeBTC.observe(this) {
            if (it?.isNotEmpty() == true) {
                mainViewModel.validateBTC()

                binding.textViewValid.visibility = View.VISIBLE
                binding.textViewResult.visibility = View.VISIBLE
                binding.buttonShare.visibility = View.VISIBLE

                binding.textViewResult.setText(it)
            }
        }

        mainViewModel.codeETH.observe(this){
            if (it?.isNotEmpty() == true) {
                mainViewModel.validateETH()

                binding.textViewValid.visibility = View.VISIBLE
                binding.textViewResult.visibility = View.VISIBLE
                binding.buttonShare.visibility = View.VISIBLE

                binding.textViewResult.setText(it)
            }
        }

        mainViewModel.valid.observe(this){
            if(it == true){
                binding.textViewValid.text = "Valid"
                binding.textViewValid.setTextColor(Color.GREEN)
            }else{
                binding.textViewValid.text = "Invalid"
                binding.textViewValid.setTextColor(Color.RED)
            }
        }
    }
    //Intent if gallery is chosen
    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageURI = data?.data
            if(imageURI != null){
                detectResultFromImage()
            }
        }else{
            showToast("Cancelled...")
        }

    }

    //Intent if camera is chosen
    private fun pickImageCamera(){
        val contentValues = ContentValues()

        contentValues.put(MediaStore.Images.Media.TITLE,"Sample Image")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Sample Description")

        imageURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageURI)
        cameraActivityResultLauncher.launch(intent)
    }
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        if(result.resultCode == Activity.RESULT_OK){
            if(imageURI != null){
                detectResultFromImage()
            }
        }else{
            showToast("Cancelled...")
        }
    }

    //Function to check storage permissions
    private fun checkStoragePermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED)
    }
    //Function to request storage permissions
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE)
    }

    //Function to check camera permissions
    private fun checkCameraPermission(): Boolean {

        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) ==
                PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) ==
                        PackageManager.PERMISSION_GRANTED)
    }
    //Function to request storage permissions
    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE)
    }

    //Handler for permissions result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera()
                    } else {
                    showToast("Camera & Storage permission required")
                    }
                }
            }
            STORAGE_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storageAccepted) {
                        pickImageGallery()
                    } else {
                    showToast("Camera & Storage permission required")
                    }
                }
            }
        }
    }


    //Function to extract QR code from the image
    private fun detectResultFromImage(){
        try{
            val inputImage = InputImage.fromFilePath(this,imageURI!!)
            val barcodeResult = barcodeScanner!!.process(inputImage)
                .addOnSuccessListener {barcodes ->
                    if(barcodes.size == 0)
                        showToast("No QR code found. \n Please try again.")
                    else
                    extractQRcodeInfo(barcodes)
                }
                .addOnCompleteListener {
                }
                .addOnFailureListener{
                    showToast("Failed scanning due to ${it.message}")
                }
        }catch (e:java.lang.Exception){
                    showToast("Failed scanning due to ${e.message}")
        }
    }

    private fun extractQRcodeInfo(barcodes : List<Barcode>){

        barcodes.forEach{
            val rawValue = it.rawValue
            when(crypto){
                1 -> {
                    mainViewModel.setBTC(rawValue)
                }
                2 -> {
                    mainViewModel.setETH(rawValue)
                }
            }

        }
    }

    override fun onClick(v: View?) {
        when(v){
            btc ->{
                crypto = TYPE_BTC
//                dialogBox()
                val intent = Intent(this , CameraPreviewActivity::class.java)
                startActivity(intent)
            }
            eth ->{
                crypto = TYPE_ETH
                dialogBox()
            }
            share ->{
                if(binding.textViewValid.text == "Invalid")
                    binding.textViewResult.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake))
                else{
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.type="text/plain"
                    when(crypto){
                        1 -> {
                            shareIntent.putExtra(Intent.EXTRA_TEXT, mainViewModel.codeBTC.value)
                        }
                        2 -> {
                            shareIntent.putExtra(Intent.EXTRA_TEXT, mainViewModel.codeETH.value)
                        }
                    }
                    startActivity(Intent.createChooser(shareIntent,"Share with:"))
                }
            }
        }
    }

    private fun openCameraOrGallery(type:Int){
        when(type){
            TYPE_CAMERA -> {
                if(checkCameraPermission()){
                    pickImageCamera()
                }else{
                    requestCameraPermission()
                }
            }
            TYPE_GALLERY -> {
                if(checkStoragePermission()){
                    pickImageGallery()
                }else{
                    requestStoragePermission()
                }
            }
        }
    }

    private fun reset(){
        crypto = 0
        mainViewModel.setBTC("")
        mainViewModel.setETH("")
        binding.textViewResult.setText("")
        binding.buttonShare.visibility = View.GONE
        binding.textViewResult.visibility = View.GONE
        binding.textViewValid.visibility = View.GONE
    }
    private fun showToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}