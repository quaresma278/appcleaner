package com.temizlepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.temizlepro.data.HomeViewModel
import com.temizlepro.data.ScanViewModel
import com.temizlepro.data.SettingsViewModel
import com.temizlepro.data.ViewModelFactory
import com.temizlepro.ui.TemizleProNavHost
import com.temizlepro.ui.theme.TemizleProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemizleProApp()
        }
    }
}

@Composable
fun TemizleProApp() {
    val factory = ViewModelFactory(androidx.compose.ui.platform.LocalContext.current.applicationContext)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val scanViewModel: ScanViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    TemizleProTheme(darkTheme = isSystemInDarkTheme()) {
        TemizleProNavHost(homeViewModel, scanViewModel, settingsViewModel)
    }
}
