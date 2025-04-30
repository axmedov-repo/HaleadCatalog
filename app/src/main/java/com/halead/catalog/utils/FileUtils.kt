package com.halead.catalog.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.halead.catalog.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun saveImageToFile(context: Context, bitmap: Bitmap?, fileName: String): String {
    val fileDir = context.filesDir
    val file = File(fileDir, fileName.ifEmpty { "${bitmap.hashCode()}" })

    FileOutputStream(file).use { fos ->
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    }

    return file.absolutePath
}

fun loadImageFromFile(filePath: String): Bitmap? {
    val file = File(filePath)
    return if (file.exists()) {
        BitmapFactory.decodeFile(file.absolutePath)
    } else {
        null // Return null if the file does not exist
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

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
        timberE("Error saving bitmap: ${e.message}")
        return null
    }
}
