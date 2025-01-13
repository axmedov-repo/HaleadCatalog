package com.halead.catalog.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.halead.catalog.app.App
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.utils.getBitmapFromUri

@Composable
fun ImageSelector(
    materials: Map<String, Int>,
    imageBmp: ImageBitmap?,
    selectedMaterial: Int?,
    overlays: List<OverlayMaterialModel>,
    modifier: Modifier = Modifier,
    showImagePicker: Boolean = false,
    applyMaterialTrigger: Boolean,
    currentCursor: CursorData,
    changeImagePickerVisibility: (Boolean) -> Unit,
    onImageSelected: (Bitmap?) -> Unit
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
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
            .padding(bottom = 16.dp, start = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (imageBmp == null) {
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { changeImagePickerVisibility(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonColor)
                ) {
                    Text("Browse or Capture Image", color = Color.White)
                }
            } else {
                ImageEditor(
                    materials = materials,
                    imageBitmap = imageBmp,
                    selectedMaterial = selectedMaterial,
                    overlays = overlays,
                    currentCursor = currentCursor,
                    applyMaterialTrigger = applyMaterialTrigger
                )
            }
        }

        ImagePickerDialog(showDialog = showImagePicker, onDismiss = {
            changeImagePickerVisibility(false)
        }, onCameraClick = {
            changeImagePickerVisibility(false)
            cameraLauncher.launch() // Open Camera
        }, onGalleryClick = {
            changeImagePickerVisibility(false)
            galleryLauncher.launch("image/*") // Open Gallery
        })

        ConfirmationDialog(showDialog = showConfirmationDialog,
            title = "Upload New Image?",
            message = "All your current drawings will be lost.",
            dismissButtonEnabled = true,
            confirmButtonColor = ButtonColor,
            onDismiss = { showConfirmationDialog = false },
            onConfirm = {
                launchersResult?.let {
                    onImageSelected(it)
                }
                showConfirmationDialog = false
            })
    }
}
