package com.halead.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.halead.catalog.components.TopBar
import com.halead.catalog.components.TopBarFunctionType
import com.halead.catalog.screens.main.MainScreen
import com.halead.catalog.ui.theme.HaleadCatalogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HaleadCatalogTheme {
                AppScreen()
            }
        }
    }
}

@Preview(device = Devices.PIXEL_TABLET)
@Composable
fun AppScreen() {
    var selectedTopBarFunction by remember { mutableStateOf<TopBarFunctionType?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.LightGray
    ) {
        Column {
//            TopBar {
//                selectedTopBarFunction = if (selectedTopBarFunction != it) it else null
//            }
            MainScreen(selectedTopBarFunction)
        }
    }
}
