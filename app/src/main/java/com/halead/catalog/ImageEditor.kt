package com.halead.catalog

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.halead.catalog.data.OverlayMaterial
import com.halead.catalog.data.materials
import com.halead.catalog.utils.drawMaterialOnPolygon
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.getRegionSize

@Composable
fun ImageEditor(
    imageBitmap: ImageBitmap,
    selectedMaterial: Int
) {
    val context = LocalContext.current
    val overlays = remember { mutableStateListOf<OverlayMaterial>() }

    val selectedMaterialBmp by remember(selectedMaterial) {
        mutableStateOf<ImageBitmap?>(
            if (materials.containsValue(selectedMaterial)) {
                getBitmapFromResource(context, selectedMaterial)?.asImageBitmap()
            } else {
                null
            }
        )
    }

    var isMaterialApplied by remember { mutableStateOf(false) }
    var polygonPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var isDrawing by remember { mutableStateOf(false) }
    val aspectRatio by remember(imageBitmap) {
        derivedStateOf { imageBitmap.width.toFloat() / imageBitmap.height.toFloat() }
    }

    var appliedMaterialsCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(appliedMaterialsCount) {
        Log.d("REGION", "polygonPoints.isNotEmpty()=${polygonPoints.isNotEmpty()}")
        if (polygonPoints.isNotEmpty() && selectedMaterialBmp != null) {
//                        polygonPoints = emptyList()
            isDrawing = false
            Log.d("REGION", "LaunchedEffect 1:\nselectedMaterialBmp=$selectedMaterialBmp\npolygonPoints=$polygonPoints")

//            val updatedImageBitmap = applyMaterialToPolygon(
//                bitmap = imageBitmap.asAndroidBitmap(),
//                polygonPoints = polygonPoints,
//                materialBitmap = selectedMaterialBmp?.asAndroidBitmap()
//            ).asImageBitmap()

            // TODO: I need to make Overlay myself
            val appliedMaterialBitmap = drawMaterialOnPolygon(
                imageBitmap.asAndroidBitmap(),
                selectedMaterialBmp!!.asAndroidBitmap(),
                polygonPoints
            )

            val offsetOfOverlay = findMinOffset(polygonPoints)
            Log.d("REGION", "offsetOfOverlay=$offsetOfOverlay")

            overlays.add(
                OverlayMaterial(
                    materialBitmap = appliedMaterialBitmap,
                    regionPoints = polygonPoints,
                    position = offsetOfOverlay
                )
            )

            polygonPoints = emptyList()
            isMaterialApplied = true
            Log.d("REGION", "LaunchedEffect 2")

//            if (updatedImageBitmap != imageBitmap) {
//                Log.d("REGION", "LaunchedEffect 2")
//                isMaterialApplied = true
//                imageBitmap = updatedImageBitmap
//            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (materials.containsValue(selectedMaterial)) {
            Image(
                painter = painterResource(selectedMaterial),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(160.dp, 80.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Base Image
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Draw overlays
                Canvas(modifier = Modifier.fillMaxSize()) {
                    overlays.forEach { overlay ->
//                        withTransform({
//                            translate(overlay.position.x, overlay.position.y)
////                        rotate(overlay.rotation)
////                        scale(overlay.scale, overlay.scale)
//                        }) {
//                            Log.d("REGION", "Drawing Overlay materialBitmap=${overlay.materialBitmap}")
//                        }
                        Log.d("REGION", "Drawing Overlay materialBitmap=${overlay.materialBitmap.asImageBitmap()}")
                        drawImage(overlay.materialBitmap.asImageBitmap())
                    }
                }
//                if (overlays.isNotEmpty()) {
//                    val overlay = overlays.first()
//                    Log.d("REGION", "Drawing 2 Overlay materialBitmap=${overlay.materialBitmap.asImageBitmap()}")
//                    val size = getRegionSize(overlay.regionPoints)
//                    Image(
//                        bitmap = overlay.materialBitmap.asImageBitmap(),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(
//                                size.width.dp,
//                                size.height.dp
//                            )
//                            .background(Color.Red)
//                    )
//                }

                // Add gesture handling for overlays
                overlays.forEachIndexed { index, overlay ->
                    val size = getRegionSize(overlay.regionPoints)
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(overlay.position.x.toInt(), overlay.position.y.toInt()) }
                            .size(
                                size.width.dp,
                                size.height.dp
                            ) // Example size, adjust based on overlay size
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotation ->
                                    overlays[index] = overlay.copy(
                                        position = overlay.position + Offset(pan.x, pan.y),
//                                    scale = (overlay.scale * zoom).coerceIn(0.5f, 3f),
//                                    rotation = overlay.rotation + rotation
                                    )
                                }
                            }
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Draw Region
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                polygonPoints = polygonPoints + offset
                                isDrawing = true
                            }
                        )
                    }
                ) {
                    // Draw the image
//            drawIntoCanvas { canvas ->
//                val nativeCanvas = canvas.nativeCanvas
//                val scaledBitmap = imageBitmap.asAndroidBitmap()
//                nativeCanvas.drawBitmap(
//                    scaledBitmap,
//                    null,
//                    android.graphics.RectF(0f, 0f, size.width, size.height),
//                    null
//                )
//            }

                    // Draw dots and polygon lines if material is not applied
                    if (polygonPoints.isNotEmpty()) {
                        // Draw dots at each selected point
                        polygonPoints.forEach { point ->
                            drawCircle(
                                color = Color.Red,
                                center = point,
                                radius = 6f
                            )
                        }

                        // Draw lines between the points
                        for (i in 1 until polygonPoints.size) {
                            drawLine(
                                color = Color.Green,
                                start = polygonPoints[i - 1],
                                end = polygonPoints[i],
                                strokeWidth = 3f
                            )
                        }

                        // Close the polygon
                        if (isDrawing && polygonPoints.size > 1) {
                            drawLine(
                                color = Color.Green.copy(0.5f),
                                start = polygonPoints.last(),
                                end = polygonPoints.first(),
                                strokeWidth = 3f
                            )
                        }
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                enabled = selectedMaterialBmp != null,
                modifier = Modifier
                    .padding(4.dp),
                onClick = {
                    appliedMaterialsCount++
                }
            ) {
                Text("Apply material")
            }
        }
    }
}
