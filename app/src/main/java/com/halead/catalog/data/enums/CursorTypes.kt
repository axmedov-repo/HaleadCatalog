package com.halead.catalog.data.enums

import com.halead.catalog.R

enum class CursorTypes {
    DRAW_INSERT, DRAW_EXTEND, DRAG_PAN
}

data class CursorData(
    val img: Int,
    val text: String,
    val type: CursorTypes
)

val DefaultCursorData = CursorData(
    img = R.drawable.ic_control_point,
    text = "Draw Insert",
    type = CursorTypes.DRAW_INSERT
)

val cursorTypesList = listOf(
    CursorData(
        img = R.drawable.ic_control_point,
        text = "Draw Insert",
        type = CursorTypes.DRAW_INSERT
    ),
    CursorData(
        img = R.drawable.ic_resize,
        text = "Draw Extend",
        type = CursorTypes.DRAW_EXTEND
    ),
    CursorData(
        img = R.drawable.ic_drag_pan,
        text = "Drag Pan",
        type = CursorTypes.DRAG_PAN
    )
)