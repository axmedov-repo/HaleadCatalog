package com.halead.catalog.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun getPolygonsHull(polygons: List<List<Offset>>, n: Int = 4): List<Offset> {
    val allPoints = polygons.flatten()
    val initialHull = findConvexHull(allPoints)
    return findPolygonHull(initialHull, n)
}

fun getPolygonHull(polygons: List<Offset>, n: Int = 4): List<Offset> {
    val initialHull = findConvexHull(polygons)
    return findPolygonHull(initialHull, n)
}

private fun findConvexHull(points: List<Offset>): List<Offset> {
    if (points.size < 3) return points

    val start = points.minWith(compareBy({ it.y }, { it.x }))
    val sortedPoints = points.filter { it != start }.sortedWith(compareBy(
        { point ->
            val dx = point.x - start.x
            val dy = point.y - start.y
            Math.atan2(dy.toDouble(), dx.toDouble())
        },
        { point ->
            val dx = point.x - start.x
            val dy = point.y - start.y
            dx * dx + dy * dy
        }
    ))

    val stack = mutableListOf(start)
    for (point in sortedPoints) {
        while (stack.size >= 2 && !isCounterClockwise(
                stack[stack.size - 2],
                stack[stack.size - 1],
                point
            )
        ) {
            stack.removeAt(stack.size - 1)
        }
        stack.add(point)
    }

    return stack
}

private fun isCounterClockwise(p1: Offset, p2: Offset, p3: Offset): Boolean {
    val crossProduct = (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x)
    return crossProduct > 0
}

private fun findPolygonHull(hullOffsets: List<Offset>, numberOfSides: Int = 4): List<Offset> {
    // Handle special cases
    when (hullOffsets.size) {
        0, 1 -> return hullOffsets
        2 -> return expandLineToQuad(hullOffsets[0], hullOffsets[1])
        3 -> return expandTriangleToQuad(hullOffsets)
    }

    var currentHull = hullOffsets
    while (currentHull.size > numberOfSides) {
        var bestCandidate: Pair<List<Offset>, Double>? = null

        for (edgeIdx1 in currentHull.indices) {
            val edgeIdx2 = (edgeIdx1 + 1) % currentHull.size
            val adjIdx1 = (edgeIdx1 - 1 + currentHull.size) % currentHull.size
            val adjIdx2 = (edgeIdx1 + 2) % currentHull.size

            val edgePt1 = currentHull[edgeIdx1]
            val edgePt2 = currentHull[edgeIdx2]
            val adjPt1 = currentHull[adjIdx1]
            val adjPt2 = currentHull[adjIdx2]

            val intersection = lineIntersectionBorder(adjPt1, edgePt1, edgePt2, adjPt2) ?: continue
            val area = triangleAreaBorder(edgePt1, intersection, edgePt2)

            if (bestCandidate != null && bestCandidate.second < area) continue

            val betterHull = currentHull.toMutableList()
            betterHull[edgeIdx1] = intersection
            betterHull.removeAt(edgeIdx2)
            bestCandidate = Pair(betterHull, area)
        }

        bestCandidate?.let {
            currentHull = it.first.toMutableList()
        } ?: break  // If we can't find a valid candidate, break instead of throwing exception
    }

    return currentHull
}

private fun expandTriangleToQuad(triangle: List<Offset>): List<Offset> {
    // Find the longest edge
    val edges = listOf(
        Pair(0, 1),
        Pair(1, 2),
        Pair(2, 0)
    )

    // Calculate edge lengths and find longest
    val edgeLengths = edges.map { (i, j) ->
        Triple(i, j, distance(triangle[i], triangle[j]))
    }
    val longestEdge = edgeLengths.maxBy { it.third }

    // Find the point not on the longest edge
    val oppositePointIndex = (0..2).first { it != longestEdge.first && it != longestEdge.second }
    val oppositePoint = triangle[oppositePointIndex]

    // Get points of the longest edge
    val edgePoint1 = triangle[longestEdge.first]
    val edgePoint2 = triangle[longestEdge.second]

    // Calculate vector from longest edge to opposite point
    val edgeVector = Offset(
        edgePoint2.x - edgePoint1.x,
        edgePoint2.y - edgePoint1.y
    )

    // Calculate midpoint of longest edge
    val midpoint = Offset(
        (edgePoint1.x + edgePoint2.x) / 2,
        (edgePoint1.y + edgePoint2.y) / 2
    )

    // Calculate vector from midpoint to opposite point
    val toOppositeVector = Offset(
        oppositePoint.x - midpoint.x,
        oppositePoint.y - midpoint.y
    )

    // Calculate the length of this vector
    val oppositeLength = sqrt(toOppositeVector.x * toOppositeVector.x + toOppositeVector.y * toOppositeVector.y)

    // Create a point on the opposite side with the same distance
    val fourthPoint = Offset(
        midpoint.x - (toOppositeVector.x / oppositeLength) * oppositeLength * 1.5f,
        midpoint.y - (toOppositeVector.y / oppositeLength) * oppositeLength * 1.5f
    )

    // Create result in correct order
    return sortPointsClockwise(listOf(
        edgePoint1,
        edgePoint2,
        oppositePoint,
        fourthPoint
    ))
}

private fun distance(p1: Offset, p2: Offset): Float {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    return sqrt(dx * dx + dy * dy)
}

private fun sortPointsClockwise(points: List<Offset>): List<Offset> {
    // Calculate centroid
    val centroid = Offset(
        points.sumOf { it.x.toDouble() }.toFloat() / points.size,
        points.sumOf { it.y.toDouble() }.toFloat() / points.size
    )

    // Sort points by their angle from centroid
    return points.sortedBy { point ->
        -kotlin.math.atan2(
            (point.y - centroid.y).toDouble(),
            (point.x - centroid.x).toDouble()
        )
    }
}

private fun expandLineToQuad(p1: Offset, p2: Offset): List<Offset> {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val length = sqrt(dx * dx + dy * dy)
    val perpX = -dy / length * 10  // Perpendicular vector, scaled
    val perpY = dx / length * 10

    return listOf(
        p1,
        Offset(p1.x + perpX, p1.y + perpY),
        p2,
        Offset(p2.x + perpX, p2.y + perpY)
    )
}

private fun lineIntersectionBorder(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Offset? {
    val denom = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
    if (denom.toDouble() == 0.0) return null

    val x = ((p1.x * p2.y - p1.y * p2.x) * (p3.x - p4.x) - (p1.x - p2.x) * (p3.x * p4.y - p3.y * p4.x)) / denom
    val y = ((p1.x * p2.y - p1.y * p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x * p4.y - p3.y * p4.x)) / denom

    return Offset(x, y)
}

private fun triangleAreaBorder(p1: Offset, p2: Offset, p3: Offset): Double {
    return abs((p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)) / 2.0)
}