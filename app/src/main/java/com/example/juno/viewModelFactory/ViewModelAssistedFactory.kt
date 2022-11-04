package com.example.juno.viewModelFactory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

interface ViewModelAssistedFactory<T : ViewModel> {
    fun create(handle: SavedStateHandle): T
}