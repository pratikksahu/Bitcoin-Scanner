package com.example.juno.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.juno.viewModelFactory.ViewModelAssistedFactory
import javax.inject.Inject

class MainViewModel(
    private val handle: SavedStateHandle
) : ViewModel() {

    private val _codeBTC =
        MutableLiveData<String?>()
    val codeBTC: LiveData<String?> =
        _codeBTC

    private val _codeETH =
        MutableLiveData<String?>()
    val codeETH: LiveData<String?> =
        _codeETH


    private val _valid =
        MutableLiveData<Boolean?>()
    val valid: LiveData<Boolean?> =
        _valid

    fun setBTC(str:String?){
        _codeBTC.value = str
    }

    fun setETH(str:String?){
        _codeETH.value = str
    }

    fun validateBTC(){
        val regex = Regex("^1[1-9a-km-zA-HJ-NP-Z]{25,34}$")
        val bol:Boolean = codeBTC.value?.let { regex.matches(it) } == true
        setValid(bol)
    }
    fun validateETH(){
        val regex = Regex("^0x[0-9a-fA-F]+$")
        val bol:Boolean = codeETH.value?.let { regex.matches(it) } == true
        setValid(bol)
    }

    private fun setValid(bol:Boolean?){
        _valid.value = bol
    }
}

class MainViewModelFactory @Inject constructor(
) : ViewModelAssistedFactory<MainViewModel> {
      override fun create(handle: SavedStateHandle): MainViewModel {
        return MainViewModel(handle)
    }
}