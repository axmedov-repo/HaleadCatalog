package com.halead.catalog.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.geometry.Offset

class PerspectiveMaterial(
    private val materialBitmap: Bitmap,
    private val outerPolygon: List<Offset>,
    private val holes: List<List<Offset>> = emptyList()
) {
    private val transformedMaterial: Bitmap
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    // Initial transformation matrix
    private val perspectiveMatrix = Matrix()

    // Scroll state
    private var currentScrollX = 0f
    private var currentScrollY = 0f

    init {
        // Calculate polygon bounds
        val minX = outerPolygon.minOf { it.x }
        val minY = outerPolygon.minOf { it.y }
        val maxX = outerPolygon.maxOf { it.x }
        val maxY = outerPolygon.maxOf { it.y }

        val polygonWidth = (maxX - minX).toInt()
        val polygonHeight = (maxY - minY).toInt()

        // Create transformed material bitmap
        transformedMaterial = Bitmap.createBitmap(polygonWidth, polygonHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(transformedMaterial)

        // Create perspective transformation matrix
        val srcPoints = floatArrayOf(
            0f, 0f,  // Top-left
            materialBitmap.width.toFloat(), 0f,  // Top-right
            materialBitmap.width.toFloat(), materialBitmap.height.toFloat(),  // Bottom-right
            0f, materialBitmap.height.toFloat()  // Bottom-left
        )

        val dstPoints = floatArrayOf(
            outerPolygon[0].x - minX, outerPolygon[0].y - minY,
            outerPolygon[1].x - minX, outerPolygon[1].y - minY,
            outerPolygon[2].x - minX, outerPolygon[2].y - minY,
            outerPolygon[3].x - minX, outerPolygon[3].y - minY
        )

        perspectiveMatrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

        // Create mask path
        val maskPath = Path().apply {
            // Add outer polygon
            moveTo(outerPolygon[0].x - minX, outerPolygon[0].y - minY)
            for (i in 1 until outerPolygon.size) {
                lineTo(outerPolygon[i].x - minX, outerPolygon[i].y - minY)
            }
            close()

            // Add holes using counter-clockwise winding
            holes.forEach { hole ->
                if (hole.isNotEmpty()) {
                    moveTo(hole[0].x - minX, hole[0].y - minY)
                    for (i in hole.size - 1 downTo 1) {
                        lineTo(hole[i].x - minX, hole[i].y - minY)
                    }
                    close()
                }
            }

            fillType = Path.FillType.EVEN_ODD
        }

        // Apply perspective transform to material
        canvas.drawBitmap(materialBitmap, perspectiveMatrix, null)

        // Create a layer to apply the mask
        val layerCanvas = Canvas(transformedMaterial)
        layerCanvas.drawPath(maskPath, paint)
    }

    fun scroll(dx: Float, dy: Float): Bitmap {
        // Create a copy of the transformed material to modify
        val scrolledBitmap = Bitmap.createBitmap(transformedMaterial)
        val canvas = Canvas(scrolledBitmap)

        // Create scroll matrix
        val scrollMatrix = Matrix(perspectiveMatrix)
        scrollMatrix.postTranslate(-dx, -dy)

        // Draw scrolled bitmap
        canvas.drawBitmap(transformedMaterial, scrollMatrix, null)

        // Update scroll state
        currentScrollX += dx
        currentScrollY += dy

        return scrolledBitmap
    }

    // Get current scrolled position
    fun getCurrentScroll() = Offset(currentScrollX, currentScrollY)

    // Cleanup method to free bitmap resources
    fun recycle() {
        transformedMaterial.recycle()
    }
}

// Extension function for convenient creation
fun Bitmap.toPerspectiveMaterial(
    outerPolygon: List<Offset>,
    holes: List<List<Offset>> = emptyList()
) = PerspectiveMaterial(this, outerPolygon, holes)