package com.halead.catalog.utils


import androidx.compose.ui.geometry.Offset

// Function to calculate the cross product of two vectors (2D)
fun crossProduct(o: Offset, a: Offset, b: Offset): Float {
    return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
}

// Function to check if a point is inside the clipping polygon
fun isInside(p: Offset, a: Offset, b: Offset): Boolean {
    return crossProduct(a, b, p) >= 0
}

// Function to clip the first polygon with the second polygon using the Sutherland-Hodgman algorithm
fun clipPolygon(subjectPolygon: List<Offset>, clipPolygon: List<Offset>): List<Offset> {
    var outputList = subjectPolygon

    for (i in clipPolygon.indices) {
        val clipEdgeStart = clipPolygon[i]
        val clipEdgeEnd = clipPolygon[(i + 1) % clipPolygon.size]

        val inputList = outputList
        outputList = mutableListOf()

        var previousVertex = inputList.last()

        for (currentVertex in inputList) {
            if (isInside(currentVertex, clipEdgeStart, clipEdgeEnd)) {
                if (!isInside(previousVertex, clipEdgeStart, clipEdgeEnd)) {
                    val intersection = getIntersection(previousVertex, currentVertex, clipEdgeStart, clipEdgeEnd)
                    outputList.add(intersection)
                }
                outputList.add(currentVertex)
            } else if (isInside(previousVertex, clipEdgeStart, clipEdgeEnd)) {
                val intersection = getIntersection(previousVertex, currentVertex, clipEdgeStart, clipEdgeEnd)
                outputList.add(intersection)
            }
            previousVertex = currentVertex
        }
    }

    return outputList
}

// Function to calculate the intersection point of two lines
fun getIntersection(p1: Offset, p2: Offset, q1: Offset, q2: Offset): Offset {
    val a1 = p2.y - p1.y
    val b1 = p1.x - p2.x
    val c1 = a1 * p1.x + b1 * p1.y

    val a2 = q2.y - q1.y
    val b2 = q1.x - q2.x
    val c2 = a2 * q1.x + b2 * q1.y

    val det = a1 * b2 - a2 * b1
    if (det == 0f) {
        return Offset(0f, 0f) // Parallel lines, no intersection
    } else {
        val x = (b2 * c1 - b1 * c2) / det
        val y = (a1 * c2 - a2 * c1) / det
        return Offset(x, y)
    }
}
