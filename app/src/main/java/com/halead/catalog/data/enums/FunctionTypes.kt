package com.halead.catalog.data.enums

import com.halead.catalog.R

enum class FunctionsEnum {
    REPLACE_IMAGE, UNDO, REDO
}

data class FunctionData(
    val img: Int,
    val text: String,
    val type: FunctionsEnum
)

val functionsList = listOf(
    FunctionData(
        img = R.drawable.ic_change,
        text = "Replace",
        type = FunctionsEnum.REPLACE_IMAGE
    ),
    FunctionData(
        img = R.drawable.ic_undo,
        text = "Undo",
        type = FunctionsEnum.UNDO
    ),
    FunctionData(
        img = R.drawable.ic_redo,
        text = "Redo",
        type = FunctionsEnum.REDO
    )
)