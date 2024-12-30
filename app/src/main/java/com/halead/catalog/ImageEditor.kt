package com.halead.catalog

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.halead.catalog.utils.applyMaterialToRectangle
import com.halead.catalog.utils.getBitmapFromUri

@Composable
fun ImageEditor(
    selectedMaterialBitmap: Bitmap?,
) {
    val context = LocalContext.current
    var imageBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isMaterialApplied by remember { mutableStateOf(false) }
//    var updatedBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        uri?.let {
            imageBitmap = getBitmapFromUri(context, it)
        }
    }
    var startPoint by remember { mutableStateOf(Offset(0f, 0f)) }
    var endPoint by remember { mutableStateOf(Offset(50f, 50f)) }

    LaunchedEffect(selectedMaterialBitmap, startPoint, endPoint) {
        if (selectedMaterialBitmap != null && startPoint != endPoint) {
            imageBitmap?.let {
                val updatedMap = applyMaterialToRectangle(
                    bitmap = it, // Load original Bitmap from URI
                    startPoint = startPoint,
                    endPoint = endPoint,
                    materialBitmap = selectedMaterialBitmap // Load material Bitmap
                )

                if (updatedMap != imageBitmap) {
                    isMaterialApplied = true
                    imageBitmap = updatedMap
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageBitmap == null) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Browse or Capture Image")
            }
        } else {
            Box(Modifier.background(Color.Red)) {
                RegionSelector(
                    imageBitmap = imageBitmap!!.asImageBitmap(),
                    isMaterialApplied = isMaterialApplied,
                    onDrawingStarted = { isMaterialApplied = false },
                    onRegionSelected = { newStartPoint, newEndPoint ->
                        startPoint = newStartPoint
                        endPoint = newEndPoint
                    }
                )
            }
        }
    }
}
