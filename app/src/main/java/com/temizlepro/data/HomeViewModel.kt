package com.temizlepro.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(private val storageRepository: StorageRepository) : ViewModel() {
    private val _storageSummary = MutableStateFlow(storageRepository.getStorageSummary())
    val storageSummary: StateFlow<StorageSummary> = _storageSummary

    fun refresh() {
        _storageSummary.value = storageRepository.getStorageSummary()
    }
}
