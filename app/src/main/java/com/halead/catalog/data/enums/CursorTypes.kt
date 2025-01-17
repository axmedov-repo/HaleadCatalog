package com.halead.catalog.data.enums

import com.halead.catalog.R

enum class CursorTypes {
    DRAW, HAND
}

data class CursorData(
    val img: Int,
    val text: String,
    val type: CursorTypes
)

val DefaultCursorData = CursorData(
    img = R.drawable.ic_control_point,
    text = "Draw",
    type = CursorTypes.DRAW
)

val cursorTypesList = listOf(
    CursorData(
        img = R.drawable.ic_control_point,
        text = "Draw",
        type = CursorTypes.DRAW
    ),
    CursorData(
        img = R.drawable.ic_swipe,
        text = "Hand",
        type = CursorTypes.HAND
    )
)