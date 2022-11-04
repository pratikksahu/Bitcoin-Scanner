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


    fun setBTC(str:String?){
        _codeBTC.value = str
    }
}

class MainViewModelFactory @Inject constructor(
) : ViewModelAssistedFactory<MainViewModel> {
      override fun create(handle: SavedStateHandle): MainViewModel {
        return MainViewModel(handle)
    }
}