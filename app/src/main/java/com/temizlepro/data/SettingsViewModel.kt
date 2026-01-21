package com.temizlepro.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    val includeDuplicates: StateFlow<Boolean> = settingsRepository.includeDuplicates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val largeThresholdMb: StateFlow<Int> = settingsRepository.largeThresholdMb
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 200)

    fun setIncludeDuplicates(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIncludeDuplicates(value)
        }
    }

    fun setLargeThresholdMb(value: Int) {
        viewModelScope.launch {
            settingsRepository.setLargeThresholdMb(value)
        }
    }
}
