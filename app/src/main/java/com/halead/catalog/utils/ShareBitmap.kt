package com.halead.catalog.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareBitmap(bitmap: Bitmap, context: Context) {
    val imageFile = File(context.cacheDir, "shared_image.jpg")
    val fos = FileOutputStream(imageFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    fos.close()

    val uri = FileProvider.getUriForFile(
        context, "${context.applicationContext.packageName}.fileprovider", imageFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "image/jpeg"
    shareIntent.putExtra(
        Intent.EXTRA_STREAM, uri
    )
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    context.startActivity(
        Intent.createChooser(
            shareIntent, "Share Image"
        )
    )
}
