package com.halead.catalog.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

fun rotatePoint(point: Offset, angleDegrees: Float, pivot: Offset): Offset {
    val angleRad = Math.toRadians(angleDegrees.toDouble()).toFloat()
    val cosTheta = cos(angleRad)
    val sinTheta = sin(angleRad)

    val translatedX = point.x - pivot.x
    val translatedY = point.y - pivot.y

    val rotatedX = translatedX * cosTheta - translatedY * sinTheta
    val rotatedY = translatedX * sinTheta + translatedY * cosTheta

    return Offset(rotatedX + pivot.x, rotatedY + pivot.y)
}

fun rotatePoints(points: List<Offset>, angleDegrees: Float, pivot: Offset): List<Offset> {
    return points.map { rotatePoint(it, angleDegrees, pivot) }
}
