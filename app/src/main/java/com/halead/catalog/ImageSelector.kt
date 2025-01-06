package com.halead.catalog

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.halead.catalog.utils.getBitmapFromUri

@Composable
fun ImageSelector(
    selectedMaterial: Int,
) {
    val context = LocalContext.current
    var imageBmp by remember { mutableStateOf<ImageBitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageBmp = getBitmapFromUri(context, uri)?.asImageBitmap()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageBmp == null) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Browse or Capture Image")
            }
        } else {
            ImageEditor(
                imageBitmap = imageBmp!!,
                selectedMaterial = selectedMaterial
            )
        }
    }
}
