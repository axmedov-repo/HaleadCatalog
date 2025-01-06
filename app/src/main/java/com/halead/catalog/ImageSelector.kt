package com.halead.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

@Composable
fun ImageSelector(
    imageBmp: ImageBitmap?,
    selectedMaterial: Int,
    onSelectImage: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageBmp == null) {
            Button(onClick = { onSelectImage() }) {
                Text("Browse or Capture Image")
            }
        } else {
            ImageEditor(
                imageBitmap = imageBmp,
                selectedMaterial = selectedMaterial
            )
        }
    }
}
