package com.halead.catalog.data.enums

import com.halead.catalog.R

enum class FunctionsEnum {
    REPLACE_IMAGE, UNDO, REDO, ADD_LAYER, CLEAR_LAYERS
}

data class FunctionData(
    val img: Int,
    val text: String,
    val type: FunctionsEnum
)

val functionsList = listOf(
    FunctionData(
        img = R.drawable.ic_reset_image,
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
    ),
    FunctionData(
        img = R.drawable.ic_layer_add,
        text = "Add Layer",
        type = FunctionsEnum.ADD_LAYER
    ),
    FunctionData(
        img = R.drawable.ic_layers_clear,
        text = "Clear Layers",
        type = FunctionsEnum.CLEAR_LAYERS
    )
)