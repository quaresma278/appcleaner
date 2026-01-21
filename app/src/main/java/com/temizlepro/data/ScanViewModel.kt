package com.temizlepro.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val scanRepository: ScanRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    private val _scanState = MutableStateFlow<ScanState?>(null)
    val scanState: StateFlow<ScanState?> = _scanState.asStateFlow()

    private val _results = MutableStateFlow<List<CleanItem>>(emptyList())
    val results: StateFlow<List<CleanItem>> = _results.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _deleteStatus = MutableStateFlow<DeleteResult?>(null)
    val deleteStatus: StateFlow<DeleteResult?> = _deleteStatus.asStateFlow()

    val appCacheBytes: Long
        get() = storageRepository.getAppCacheBytes()

    fun startScan(treeUri: android.net.Uri, includeDuplicates: Boolean, largeThresholdBytes: Long) {
        viewModelScope.launch {
            try {
                scanRepository.scan(treeUri, includeDuplicates, largeThresholdBytes)
                    .collect { state ->
                        _scanState.value = state
                        if (state is ScanState.Completed) {
                            _results.value = state.result.items
                        }
                    }
            } catch (ex: Exception) {
                _scanState.value = ScanState.Error(com.temizlepro.R.string.scan_error)
            }
        }
    }

    fun toggleSelection(itemId: String) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _selectedIds.value = current
    }

    fun selectAll(ids: Set<String>) {
        _selectedIds.value = ids
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val selectedItems = _results.value.filter { _selectedIds.value.contains(it.id) }
            val result = scanRepository.deleteItems(selectedItems)
            _deleteStatus.value = result
            _results.value = _results.value.filterNot { _selectedIds.value.contains(it.id) }
            clearSelection()
        }
    }

    fun clearDeleteStatus() {
        _deleteStatus.value = null
    }
}
