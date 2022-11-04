package com.example.juno.home

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.juno.R
import com.example.juno.databinding.ActivityMainBinding
import com.example.juno.viewModelFactory.GenericSavedStateViewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
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
        private val TAG = "Main"
    }
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    private var imageURI: Uri? = null


    private lateinit var btc:Button
    private lateinit var eth:Button
    private lateinit var binding:ActivityMainBinding

    //QRcode

    private var barcodeOptions:BarcodeScannerOptions? = null
    private var barcodeScanner: BarcodeScanner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        btc = binding.buttonBtc
        eth = binding.buttonEth

        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        buttonsSetup()
        viewModelSetup()
    }



    private fun buttonsSetup(){
        barcodeOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        barcodeScanner = BarcodeScanning.getClient(barcodeOptions!!)

        btc.setOnClickListener(this)
        eth.setOnClickListener(this)
    }
    private fun viewModelSetup(){
        mainViewModel.codeBTC.observe(this) {
            if (it?.isNotEmpty() == true) {
                binding.textViewResult.text = it
            }
        }
    }
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
            val data = result.data
        }else{
            showToast("Cancelled...")
        }
    }

    private fun checkStoragePermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED)
    }
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE)
    }

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

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE)
    }

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
                        pickImageCamera()
                    } else {
                    showToast("Camera & Storage permission required")
                    }
                }
            }
        }
    }

    private fun detectResultFromImage(){
        try{
            val inputImage = InputImage.fromFilePath(this,imageURI!!)
            val barcodeResult = barcodeScanner!!.process(inputImage)
                .addOnSuccessListener {barcodes ->
                    extractQRcodeInfo(barcodes)
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
            binding.textViewResult.text = rawValue
            mainViewModel.setBTC(rawValue)
        }
    }

    override fun onClick(v: View?) {
        when(v){
            btc ->{
                if(checkCameraPermission()){
                    pickImageCamera()
                }else{
                    requestCameraPermission()
                }
            }
            eth ->{
                if(checkCameraPermission()){
                    pickImageGallery()
                }else{
                    requestCameraPermission()
                }
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}