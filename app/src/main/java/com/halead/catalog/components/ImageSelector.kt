package com.halead.catalog.components

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.halead.catalog.BuildConfig
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.ImageSelectingPurpose
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.utils.canMakeClosedShape
import com.halead.catalog.utils.createImageFile
import com.halead.catalog.utils.getBitmapFromUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileNamePrefix: String = "IMG"): Uri? {
    try {
        // Create timestamp for unique filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${fileNamePrefix}_$timestamp.bmp"

        // For API 29 and above (Android 10+), use MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/bmp")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/HaleadCatalog")
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            return uri
        } else {
            // For older Android versions
            val imagesDir = context.getExternalFilesDir("Pictures/HaleadCatalog")
                ?: context.filesDir
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val file = File(imagesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }

            // Create content URI using FileProvider
            return FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )
        }
    } catch (e: Exception) {
        Log.e("ImageSaver", "Error saving bitmap: ${e.message}")
        return null
    }
}

@Composable
fun ImageSelector(
    imageBmp: ImageBitmap?,
    purpose: ImageSelectingPurpose,
    overlays: List<OverlayMaterialModel>,
    polygonPoints: List<Offset>,
    modifier: Modifier = Modifier,
    showImagePicker: Boolean = false,
    currentCursor: CursorData,
    changeImagePickerVisibility: (Boolean) -> Unit,
    onMainUiEvent: (MainUiEvent) -> Unit
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var launchersResult by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

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

//    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
//        launchersResult = bitmap
//    }

    // Store the imageUri in rememberSaveable to preserve it across recompositions
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Track camera permission state for better UX
    var hasCameraPermission by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Camera launcher using TakePicture contract
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                try {
                    imageUri?.let { uri ->
                        Log.d("ImageSelector", "Camera capture successful, processing URI: $uri")
                        val bitmap = getBitmapFromUri(context, uri)

                        if (bitmap != null) {
                            Log.d("ImageSelector", "Successfully converted URI to bitmap")
                            launchersResult = bitmap

                            // Save as bitmap if needed
//                            try {
//                                val outputDir = context.getExternalFilesDir("Pictures") ?: context.filesDir
//                                val outputFile = File(outputDir, "capture_${System.currentTimeMillis()}.bmp")
//
//                                FileOutputStream(outputFile).use { out ->
//                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//                                    out.flush()
//                                }
//
//                                Log.d("ImageSelector", "Saved bitmap to: ${outputFile.absolutePath}")
//                            } catch (e: Exception) {
//                                Log.e("ImageSelector", "Failed to save bitmap: ${e.message}")
//                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val outputDir = context.getExternalFilesDir("Pictures") ?: context.filesDir
                                    val outputFile = File(outputDir, "capture_${System.currentTimeMillis()}.bmp")

                                    // Use a buffered output stream for better performance
                                    BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
                                        // Note: You're saving as PNG (not BMP despite the file extension)
                                        // PNG compression can be slow for large bitmaps
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                        // No need to call flush() when using .use() as it handles closing properly
                                    }
                                } catch (e: IOException) {
                                    Log.e("BitmapSave", "Error saving bitmap", e)
                                }
                            }
                        } else {
                            Log.e("ImageSelector", "Failed to get bitmap from URI: $uri")
                            Toast.makeText(context, "Failed to process captured image", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Log.e("ImageSelector", "Camera returned success but URI is null")
                        Toast.makeText(context, "Failed to capture image: missing URI", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ImageSelector", "Exception processing camera result: ${e.message}", e)
                    Toast.makeText(context, "Error processing captured image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("ImageSelector", "Camera capture cancelled or failed")
                Toast.makeText(context, "Image capture cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Function to safely create file and URI for camera
    fun launchCamera(context: Context) {
        try {
            Log.d("ImageSelector", "Preparing to launch camera")

            // Create a temporary file to store the camera output
            val photoFile = context.createImageFile()
            Log.d("ImageSelector", "Created temp file: ${photoFile.absolutePath}")

            // Get URI from file
            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider",
                photoFile
            )

            Log.d("ImageSelector", "Created URI for camera: $uri")
            imageUri = uri

            // Launch camera with this URI
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("ImageSelector", "Error launching camera: ${e.message}", e)
            Toast.makeText(
                context,
                "Could not open camera: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                Log.d("ImageSelector", "Camera permission granted")
                Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
                launchCamera(context)
            } else {
                Log.d("ImageSelector", "Camera permission denied")
                Toast.makeText(context, "Camera permission required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
        launchersResult = getBitmapFromUri(context, galleryUri)
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
                    imageBitmap = imageBmp,
                    overlays = overlays,
                    currentCursor = currentCursor,
                    polygonPoints = polygonPoints,
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

        ConfirmationDialog(showDialog = showConfirmationDialog,
            title = "Upload New Image?",
            message = "All your current drawings will be lost.",
            dismissButtonEnabled = true,
            confirmButtonColor = ButtonColor,
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
            })
    }
}
