package com.halead.catalog.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.halead.catalog.data.entity.OverlayMaterial
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.resizeBitmap

@Composable
fun ImageEditor(
    materials: Map<String, Int>,
    imageBitmap: ImageBitmap,
    selectedMaterial: Int?,
    overlays : List<OverlayMaterial>,
    onOverlayAdded :(OverlayMaterial) -> Unit
) {
    val context = LocalContext.current

    val selectedMaterialBmp by remember(selectedMaterial) {
        mutableStateOf<ImageBitmap?>(
            if (selectedMaterial != null && materials.containsValue(selectedMaterial)) {
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
        if (polygonPoints.isNotEmpty() && selectedMaterialBmp != null) {
            isDrawing = false

            val appliedMaterialBitmap = getClippedMaterial(
                materialBitmap = resizeBitmap(selectedMaterialBmp!!.asAndroidBitmap(), 1024, 1024),
                regionPoints = polygonPoints
            )

            val offsetOfOverlay = findMinOffset(polygonPoints)

            onOverlayAdded(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Base image with aspect ratio scaling
            Box(
                modifier = Modifier
                    .aspectRatio(aspectRatio) // Maintain aspect ratio
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Draw overlays
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    overlays.forEach { overlay ->
                        drawImage(overlay.materialBitmap.asImageBitmap(), topLeft = overlay.position)
                    }
                }

                // Draw Region (polygon points)
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = { offset ->
                                polygonPoints = polygonPoints + offset
                                isDrawing = true
                            })
                        }
                ) {
                    if (polygonPoints.isNotEmpty()) {
                        polygonPoints.forEach { point ->
                            drawCircle(color = Color.Red, center = point, radius = 6f)
                        }

                        for (i in 1 until polygonPoints.size) {
                            drawLine(
                                color = Color.Green,
                                start = polygonPoints[i - 1],
                                end = polygonPoints[i],
                                strokeWidth = 3f
                            )
                        }

                        if (isDrawing && polygonPoints.size > 1) {
                            drawLine(
                                color = Color.Green.copy(alpha = 0.5f),
                                start = polygonPoints.last(),
                                end = polygonPoints.first(),
                                strokeWidth = 3f
                            )
                        }
                    }
                }
            }
        }

        Button(
            shape = RoundedCornerShape(8.dp),
            enabled = selectedMaterialBmp != null,
            onClick = { appliedMaterialsCount++ }
        ) {
            Text("Apply Material")
        }
    }
}
