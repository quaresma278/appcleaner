package com.temizlepro.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.temizlepro.R
import com.temizlepro.data.ScanState
import com.temizlepro.data.ScanViewModel
import com.temizlepro.data.SettingsViewModel

@Composable
fun ScanScreen(
    scanViewModel: ScanViewModel,
    settingsViewModel: SettingsViewModel,
    onScanFinished: () -> Unit
) {
    val context = LocalContext.current
    val includeDuplicates by settingsViewModel.includeDuplicates.collectAsState()
    val thresholdMb by settingsViewModel.largeThresholdMb.collectAsState()
    val scanState by scanViewModel.scanState.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            if (uri == null) {
                message = context.getString(R.string.scan_canceled)
                return@rememberLauncherForActivityResult
            }
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            val thresholdBytes = thresholdMb * 1024L * 1024L
            scanViewModel.startScan(uri, includeDuplicates, thresholdBytes)
        }
    )

    LaunchedEffect(scanState) {
        if (scanState is ScanState.Completed) {
            onScanFinished()
        } else if (scanState is ScanState.Error) {
            message = context.getString((scanState as ScanState.Error).messageRes)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.scan_title), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.scan_instructions))

        Button(onClick = { launcher.launch(null) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.scan_pick_folder))
        }

        if (scanState is ScanState.Scanning) {
            val state = scanState as ScanState.Scanning
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = state.message)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = state.progress / 100f, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        message?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = it)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { message = null }) {
                        Text(text = stringResource(R.string.action_try_again))
                    }
                }
            }
        }
    }
}
