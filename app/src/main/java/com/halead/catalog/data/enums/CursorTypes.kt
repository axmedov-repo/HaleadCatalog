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

val cursorTypesList = listOf(
    CursorData(
        img = R.drawable.ic_draw,
        text = "Draw",
        type = CursorTypes.DRAW
    ),
    CursorData(
        img = R.drawable.ic_hand,
        text = "Hand",
        type = CursorTypes.HAND
    )
)