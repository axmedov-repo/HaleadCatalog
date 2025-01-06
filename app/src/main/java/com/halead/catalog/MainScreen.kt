package com.halead.catalog

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.halead.catalog.utils.getBitmapFromUri

@Composable
fun MainScreen() {
    var selectedMaterialResId by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current
    var imageBmp by remember { mutableStateOf<ImageBitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageBmp = getBitmapFromUri(context, uri)?.asImageBitmap()
    }

    when (val orientation = LocalContext.current.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Functions
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .background(Color.LightGray),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Functions",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Button(enabled = imageBmp != null, onClick = {
                            // TODO: Need to ask to clear or save last editions
                            launcher.launch("image/*")
                        }) {
                            Text("Replace Image")
                        }
                    }
                }

                // Image Manipulation Area
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(8f)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    ImageSelector(imageBmp, selectedMaterialResId) {
                        launcher.launch("image/*")
                    }
                }

                // Materials
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .background(Color.White)
                ) {
                    MaterialsMenu(
                        selectedMaterialResId,
                        orientation = orientation,
                        onMaterialSelected = { material -> selectedMaterialResId = material })
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Functions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .background(Color.LightGray),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Functions",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(enabled = imageBmp != null, onClick = {
                            // TODO: Need to ask to clear or save last editions
                            launcher.launch("image/*")
                        }) {
                            Text("Replace Image")
                        }
                    }
                }

                // Image Manipulation Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(8f)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    ImageSelector(imageBmp, selectedMaterialResId) {
                        launcher.launch("image/*")
                    }
                }

                // Materials
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .background(Color.LightGray)
                        .background(Color.White)
                ) {
                    MaterialsMenu(
                        selectedMaterialResId,
                        orientation = orientation,
                        onMaterialSelected = { material -> selectedMaterialResId = material })
                }
            }
        }
    }
}
