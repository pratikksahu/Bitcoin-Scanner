package com.example.juno.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.juno.viewModelFactory.ViewModelAssistedFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.jetbrains.annotations.NotNull
import javax.inject.Inject

@ActivityRetainedScoped
class MainViewModel(
    private val handle: SavedStateHandle
) : ViewModel() {
    private val regexBTC = Regex("^(bc1|[13])[a-zA-HJ-NP-Z0-9]{25,39}$")
    private val regexETH = Regex("^(0x)?[0-9A-Fa-f]{40}$")

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
        val bol:Boolean = codeBTC.value?.let { regexBTC.matches(it) } == true
        setValid(bol)
    }
    fun validateETH(){
        val bol:Boolean = codeETH.value?.let { regexETH.matches(it) } == true
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