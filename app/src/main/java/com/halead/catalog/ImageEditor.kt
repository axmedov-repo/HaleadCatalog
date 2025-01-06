package com.halead.catalog

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.halead.catalog.data.OverlayMaterial
import com.halead.catalog.data.materials
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.getRegionSize
import com.halead.catalog.utils.resizeBitmap

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

            val appliedMaterialBitmap = getClippedMaterial(
                materialBitmap = resizeBitmap(selectedMaterialBmp!!.asAndroidBitmap(), 1024, 1024),
                regionPoints = polygonPoints
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
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Base Image
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.wrapContentSize().background(Color.Red)
            )

            Box(
                modifier = Modifier.matchParentSize()
            ) {
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
                        Log.d("REGION", "Overlay pos=${overlay.position}")

                        drawImage(overlay.materialBitmap.asImageBitmap(), topLeft = overlay.position)
                    }
                }

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
                modifier = Modifier.matchParentSize()
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
