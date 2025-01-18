package com.halead.catalog.utils

import androidx.compose.ui.geometry.Offset

fun isPointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    var isInside = false
    val n = polygon.size
    var j = n - 1 // Previous vertex index (wraps around to last point)

    for (i in polygon.indices) {
        val vertex1 = polygon[i]
        val vertex2 = polygon[j]

        // Check if the point is within the y-range of the edge
        if ((vertex1.y > point.y) != (vertex2.y > point.y)) {
            val intersectionX =
                (vertex2.x - vertex1.x) * (point.y - vertex1.y) / (vertex2.y - vertex1.y) + vertex1.x

            // Toggle isInside if the ray intersects an edge
            if (point.x < intersectionX) {
                isInside = !isInside
            }
        }
        j = i
    }

    return isInside
}
