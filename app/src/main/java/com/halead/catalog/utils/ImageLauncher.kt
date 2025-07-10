package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberCameraLauncher(
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
fun rememberGalleryLauncher(
    context: Context,
    onResult: (Bitmap?) -> Unit
) = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
    onResult(getBitmapFromUri(context, galleryUri))
}