package com.temizlepro.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.temizlepro.R
import com.temizlepro.data.CleanCategory
import com.temizlepro.data.CleanItem
import com.temizlepro.data.ScanViewModel
import com.temizlepro.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(scanViewModel: ScanViewModel) {
    val context = LocalContext.current
    val results by scanViewModel.results.collectAsState()
    val selectedIds by scanViewModel.selectedIds.collectAsState()
    val deleteStatus by scanViewModel.deleteStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleteStatus) {
        deleteStatus?.let { result ->
            val message = if (result.failedCount == 0) {
                context.getString(R.string.delete_success)
            } else {
                context.getString(R.string.delete_failed)
            }
            snackbarHostState.showSnackbar(message)
            scanViewModel.clearDeleteStatus()
        }
    }

    val grouped = results.groupBy { it.category }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.results_title), style = MaterialTheme.typography.headlineMedium)

            if (results.isEmpty()) {
                Text(text = stringResource(R.string.no_results))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { scanViewModel.selectAll(results.map { it.id }.toSet()) }) {
                        Text(text = stringResource(R.string.select_all))
                    }
                    Button(onClick = { showDialog = true }, enabled = selectedIds.isNotEmpty()) {
                        Text(text = stringResource(R.string.delete_selected))
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    grouped.forEach { (category, items) ->
                        item {
                            CategoryHeader(
                                category = category,
                                onOpenSettings = if (category == CleanCategory.CACHE) {
                                    {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                } else null
                            )
                        }
                        items(items) { item ->
                            ResultItem(
                                item = item,
                                checked = selectedIds.contains(item.id),
                                onCheckedChange = { scanViewModel.toggleSelection(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(R.string.delete_confirm_title)) },
            text = { Text(text = stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    scanViewModel.deleteSelected()
                }) {
                    Text(text = stringResource(R.string.delete_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.delete_cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryHeader(category: CleanCategory, onOpenSettings: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = categoryLabel(category), style = MaterialTheme.typography.titleMedium)
        if (onOpenSettings != null) {
            TextButton(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.action_open_settings))
            }
        }
    }
}

@Composable
private fun ResultItem(item: CleanItem, checked: Boolean, onCheckedChange: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = { onCheckedChange() })
            Spacer(modifier = Modifier.size(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(R.string.item_size, FormatUtils.formatBytes(item.sizeBytes)))
                val modified = FormatUtils.formatDate(item.lastModified)
                Text(text = stringResource(R.string.item_last_modified, modified))
                item.path?.let {
                    Text(text = stringResource(R.string.item_path, it))
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: CleanCategory): String {
    return when (category) {
        CleanCategory.DOWNLOADS -> stringResource(R.string.category_downloads)
        CleanCategory.CACHE -> stringResource(R.string.category_cache)
        CleanCategory.LARGE_FILES -> stringResource(R.string.category_large_files)
        CleanCategory.DUPLICATES -> stringResource(R.string.category_duplicates)
        CleanCategory.TEMP -> stringResource(R.string.category_temp)
    }
}
