package com.temizlepro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.temizlepro.R
import com.temizlepro.data.HomeViewModel
import com.temizlepro.util.FormatUtils

@Composable
fun HomeScreen(homeViewModel: HomeViewModel, onQuickScan: () -> Unit) {
    val summary by homeViewModel.storageSummary.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.dashboard_tip), style = MaterialTheme.typography.bodyMedium)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.storage_summary), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = stringResource(R.string.storage_used))
                        Text(text = FormatUtils.formatBytes(summary.usedBytes))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = stringResource(R.string.storage_free))
                        Text(text = FormatUtils.formatBytes(summary.freeBytes))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.storage_total) + ": " + FormatUtils.formatBytes(summary.totalBytes))
            }
        }

        Button(onClick = onQuickScan, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.quick_scan))
        }

        CategoryCard(title = stringResource(R.string.category_downloads))
        CategoryCard(title = stringResource(R.string.category_cache))
        CategoryCard(title = stringResource(R.string.category_large_files))
        CategoryCard(title = stringResource(R.string.category_duplicates))
        CategoryCard(title = stringResource(R.string.category_temp))
    }
}

@Composable
private fun CategoryCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(R.string.category_count, 0))
        }
    }
}
