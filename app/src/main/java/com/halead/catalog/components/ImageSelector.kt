package com.halead.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.halead.catalog.data.entity.OverlayMaterial

@Composable
fun ImageSelector(
    materials: Map<String, Int>,
    imageBmp: ImageBitmap?,
    selectedMaterial: Int?,
    modifier: Modifier = Modifier,
    overlays: List<OverlayMaterial>,
    onOverlayAdded: (OverlayMaterial) -> Unit,
    onSelectImage: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageBmp == null) {
            Button(shape = RoundedCornerShape(8.dp), onClick = { onSelectImage() }) {
                Text("Browse or Capture Image")
            }
        } else {
            ImageEditor(
                materials = materials,
                imageBitmap = imageBmp,
                selectedMaterial = selectedMaterial,
                overlays = overlays,
                onOverlayAdded = onOverlayAdded
            )
        }
    }
}
