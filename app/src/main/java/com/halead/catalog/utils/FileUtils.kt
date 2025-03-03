package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

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