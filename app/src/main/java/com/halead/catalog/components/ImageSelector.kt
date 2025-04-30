package com.halead.catalog.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.halead.catalog.BuildConfig
import com.halead.catalog.R
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.ImageSelectingPurpose
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.AppButtonSize
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.utils.canMakeClosedShape
import com.halead.catalog.utils.createImageFile
import com.halead.catalog.utils.getBitmapFromUri
import com.halead.catalog.utils.showToast
import com.halead.catalog.utils.timber

@Composable
fun ImageSelector(
    imageBmp: ImageBitmap?,
    purpose: ImageSelectingPurpose,
    overlays: List<OverlayMaterialModel>,
    polygonPoints: List<Offset>,
    modifier: Modifier = Modifier,
    showImagePicker: Boolean = false,
    currentCursor: CursorData,
    editorSize: IntSize,
    changeImagePickerVisibility: (Boolean) -> Unit,
    onMainUiEvent: (MainUiEvent) -> Unit
) {
    val context = LocalContext.current
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var hasCameraPermission by rememberSaveable { mutableStateOf(false) }
    var launchersResult by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(launchersResult, purpose) {
        if (launchersResult != null) {
            when (purpose) {
                ImageSelectingPurpose.EDITING_IMAGE -> {
                    if (polygonPoints.canMakeClosedShape() || overlays.isNotEmpty()) {
                        showConfirmationDialog = true
                    } else {
                        onMainUiEvent(MainUiEvent.SelectImage(launchersResult))
                    }
                }

                ImageSelectingPurpose.ADD_MATERIAL -> {
                    onMainUiEvent(MainUiEvent.AddMaterial(launchersResult))
                }
            }
        }
    }

    val cameraLauncher = rememberCameraLauncher(imageUri, context) { launchersResult = it }
    val galleryLauncher = rememberGalleryLauncher(context) { launchersResult = it }

    // Function to safely create file and URI for camera
    fun launchCamera(context: Context) {
        try {
            val photoFile = context.createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider",
                photoFile
            )
            imageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            showToast(context, "Could not open camera: ${e.message}")
        }
    }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                launchCamera(context)
            } else {
                showToast(context, "Camera permission required to take photos")
            }
        }
    )

    Box(
        modifier
            .fillMaxSize()
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
                BrowseOrCaptureImageButton { changeImagePickerVisibility(true) }
            } else {
                ImageEditor(
                    imageBitmap = imageBmp,
                    overlays = overlays,
                    currentCursor = currentCursor,
                    polygonPoints = polygonPoints,
                    editorSize = editorSize,
                    onMainUiEvent = onMainUiEvent
                )
            }
        }

        ImagePickerDialog(
            showDialog = showImagePicker,
            hasCurrentImage = imageBmp != null,
            onResetCurrentImage = {
                launchersResult = imageBmp?.asAndroidBitmap()?.let { Bitmap.createBitmap(it) }
                changeImagePickerVisibility(false)
            },
            onDismiss = {
                changeImagePickerVisibility(false)
            },
            onCameraClick = {
                // Check and request camera permission if needed
                if (hasCameraPermission) {
                    launchCamera(context)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
                changeImagePickerVisibility(false)
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*") // Open Gallery
                changeImagePickerVisibility(false)
            }
        )

        if (showConfirmationDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.upload_new_image_question),
                message = stringResource(R.string.current_drawing_will_be_lost),
                dismissButtonEnabled = true,
                colors = ConfirmationDialogDefaults.colors(confirmButtonColor = ButtonColor),
                onDismiss = { showConfirmationDialog = false },
                onConfirm = {
                    launchersResult?.let {
                        when (purpose) {
                            ImageSelectingPurpose.EDITING_IMAGE -> {
                                onMainUiEvent(MainUiEvent.SelectImage(launchersResult))
                            }

                            ImageSelectingPurpose.ADD_MATERIAL -> {
                                onMainUiEvent(MainUiEvent.AddMaterial(launchersResult))
                            }
                        }
                    }
                    showConfirmationDialog = false
                }
            )
        }
    }
}

@Composable
private fun BrowseOrCaptureImageButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .height(AppButtonSize)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp))
            .padding(2.dp),
        shape = RoundedCornerShape(6.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor)
    ) {
        Text(
            text = stringResource(R.string.browse_or_capture_image),
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun rememberCameraLauncher(
    imageUri: Uri?,
    context: Context,
    onResult: (Bitmap) -> Unit
) =
    /*    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            launchersResult = bitmap
        }*/
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                try {
                    imageUri?.let { uri ->
                        timber("ImageSelector", "Camera capture successful, processing URI: $uri")
                        val bitmap = getBitmapFromUri(context, uri)

                        if (bitmap != null) {
                            timber("ImageSelector", "Successfully converted URI to bitmap")
                            onResult(bitmap)

                            // Save bitmap if need
                            /*CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val outputDir =
                                        context.getExternalFilesDir("Pictures") ?: context.filesDir
                                    val outputFile =
                                        File(outputDir, "capture_${System.currentTimeMillis()}.bmp")

                                    BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)

                                    }
                                } catch (e: IOException) {
                                    timberE("Error saving bitmap")
                                }
                            }*/
                        } else {
                            showToast(context, "Failed to process captured image")
                        }
                    } ?: run {
                        showToast(context, "Failed to capture image: missing URI")
                    }
                } catch (e: Exception) {
                    showToast(context, "Error processing captured image")
                }
            } else {
                showToast(context, "Image capture cancelled")
            }
        }
    )

@Composable
private fun rememberGalleryLauncher(
    context: Context,
    onResult: (Bitmap?) -> Unit
) = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
    onResult(getBitmapFromUri(context, galleryUri))
}