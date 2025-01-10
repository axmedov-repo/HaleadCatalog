package com.halead.catalog.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.halead.catalog.app.App
import com.halead.catalog.data.models.OverlayMaterial
import com.halead.catalog.utils.getBitmapFromUri

@Composable
fun ImageSelector(
    materials: Map<String, Int>,
    imageBmp: ImageBitmap?,
    selectedMaterial: Int?,
    overlays: List<OverlayMaterial>,
    modifier: Modifier = Modifier,
    showImagePicker: Boolean = false,
    changeImagePickerVisibility: (Boolean) -> Unit,
    onOverlayAdded: (OverlayMaterial) -> Unit,
    onImageSelected: (Bitmap?) -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var launchersResult by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(launchersResult) {
        if (launchersResult != null) {
            if (overlays.isNotEmpty()) {
                showConfirmationDialog = true
            } else {
                onImageSelected(launchersResult)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        launchersResult = bitmap
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        launchersResult = getBitmapFromUri(App.instance, uri)
    }

    Box(
        modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (imageBmp == null) {
                Button(shape = RoundedCornerShape(8.dp), onClick = {
                    changeImagePickerVisibility(true)
                }) {
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

        ImagePickerDialog(
            showDialog = showImagePicker,
            onDismiss = {
                changeImagePickerVisibility(false)
            },
            onCameraClick = {
                changeImagePickerVisibility(false)
                cameraLauncher.launch() // Open Camera
            },
            onGalleryClick = {
                changeImagePickerVisibility(false)
                galleryLauncher.launch("image/*") // Open Gallery
            }
        )

        ConfirmationDialog(
            showDialog = showConfirmationDialog,
            title = "Upload New Image?",
            message = "Are you sure you want to upload a new image?\nAnyway, your current work will be saved in the history for future reference.",
            dismissButtonEnabled = true,
            onDismiss = { showConfirmationDialog = false },
            onConfirm = {
                launchersResult?.let {
                    onImageSelected(it)
                }
                showConfirmationDialog = false
            }
        )
    }
}
