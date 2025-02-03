package com.halead.catalog.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

fun isPointInPolygon(point: Offset, polygon: List<Offset>, excluding: List<List<Offset>> = emptyList()): Boolean {
    excluding.forEach {
        if (isPointInPolygon(point, it)) {
            return false
        }
    }

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

/**
 * Check if two polygons intersect
 *
 * @param polygon1 First polygon as a list of Offset points
 * @param polygon2 Second polygon as a list of Offset points
 * @return Boolean indicating whether the polygons intersect
 */
fun doPolygonsIntersect(
    polygonToRemove: List<Offset>,
    polygon2: List<Offset>,
    excluding: List<List<Offset>> = emptyList()
): Boolean {
//    if (excluding.isNotEmpty()) {
//        var count = 0
//        for (point in polygonToRemove) {
//            if (excluding.any { !isPointInPolygon(point, it) } && isPointInPolygon(point, polygon2)) {
//                return true
//            }
//            count++
//        }
//        if (count == polygonToRemove.size) return false
//    }

    if (excluding.isNotEmpty()) {
        for (point in polygonToRemove) {
            val isInsideAnyExclusion = excluding.any { isPointInPolygon(point, it) }
            val isInsidePolygon2 = isPointInPolygon(point, polygon2)

            if (!isInsideAnyExclusion && isInsidePolygon2) {
                return true // Intersection detected
            }
        }
    }

    // Quick bounding box check for early rejection
    val polygonToRemoveBounds = getBoundingBox(polygonToRemove)
    val polygon2Bounds = getBoundingBox(polygon2)

    if (!doBoundingBoxesOverlap(polygonToRemoveBounds, polygon2Bounds)) {
        return false
    }

    // Check if any point of polygon2 is inside polygonToRemove
    for (point in polygon2) {
        if (isPointInPolygon(point, polygonToRemove)) {
            return true
        }
    }

    // Check if any point of polygonToRemove is inside polygon2
    for (point in polygonToRemove) {
        if (isPointInPolygon(point, polygon2)) {
            return true
        }
    }

    // Check for edge intersections
    return doEdgesIntersect(polygonToRemove, polygon2)
}

/**
 * Check if two line segments intersect
 *
 * @param line1Start First point of first line segment
 * @param line1End Second point of first line segment
 * @param line2Start First point of second line segment
 * @param line2End Second point of second line segment
 * @return Boolean indicating whether the line segments intersect
 */
fun doLineSegmentsIntersect(
    line1Start: Offset,
    line1End: Offset,
    line2Start: Offset,
    line2End: Offset
): Boolean {
    // Calculate orientation of triplets
    val o1 = orientation(line1Start, line1End, line2Start)
    val o2 = orientation(line1Start, line1End, line2End)
    val o3 = orientation(line2Start, line2End, line1Start)
    val o4 = orientation(line2Start, line2End, line1End)

    // General case
    if (o1 != o2 && o3 != o4) return true

    // Special Cases (collinear points)
    if (o1 == 0 && onSegment(line1Start, line2Start, line1End)) return true
    if (o2 == 0 && onSegment(line1Start, line2End, line1End)) return true
    if (o3 == 0 && onSegment(line2Start, line1Start, line2End)) return true
    if (o4 == 0 && onSegment(line2Start, line1End, line2End)) return true

    return false
}

/**
 * Check if edges of two polygons intersect
 */
private fun doEdgesIntersect(
    polygon1: List<Offset>,
    polygon2: List<Offset>
): Boolean {
    // Check every edge of polygon1 against every edge of polygon2
    for (i in polygon1.indices) {
        val line1Start = polygon1[i]
        val line1End = polygon1[(i + 1) % polygon1.size]

        for (j in polygon2.indices) {
            val line2Start = polygon2[j]
            val line2End = polygon2[(j + 1) % polygon2.size]

            if (doLineSegmentsIntersect(line1Start, line1End, line2Start, line2End)) {
                return true
            }
        }
    }

    return false
}

/**
 * Compute orientation of three points
 * 0: Collinear
 * 1: Clockwise
 * 2: Counterclockwise
 */
private fun orientation(
    p: Offset,
    q: Offset,
    r: Offset
): Int {
    val val1 = (q.y - p.y) * (r.x - q.x)
    val val2 = (q.x - p.x) * (r.y - q.y)

    return when {
        val1 == val2 -> 0  // Collinear
        val1 < val2 -> 1   // Clockwise
        else -> 2          // Counterclockwise
    }
}

/**
 * Check if point q lies on line segment pr
 */
private fun onSegment(
    p: Offset,
    q: Offset,
    r: Offset
): Boolean {
    return (q.x <= maxOf(p.x, r.x) && q.x >= minOf(p.x, r.x) &&
            q.y <= maxOf(p.y, r.y) && q.y >= minOf(p.y, r.y))
}

/**
 * Get bounding box for a polygon
 */
private fun getBoundingBox(polygon: List<Offset>): Rect {
    val minX = polygon.minOf { it.x }
    val minY = polygon.minOf { it.y }
    val maxX = polygon.maxOf { it.x }
    val maxY = polygon.maxOf { it.y }

    return Rect(minX, minY, maxX, maxY)
}

/**
 * Check if two bounding boxes overlap
 */
private fun doBoundingBoxesOverlap(
    rect1: Rect,
    rect2: Rect
): Boolean {
    return !(rect1.right < rect2.left ||
            rect1.left > rect2.right ||
            rect1.bottom < rect2.top ||
            rect1.top > rect2.bottom)
}
