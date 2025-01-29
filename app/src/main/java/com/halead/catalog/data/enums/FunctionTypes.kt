package com.halead.catalog.data.enums

import com.halead.catalog.R

enum class FunctionsEnum {
    RESET_IMAGE, UNDO, REDO, ADD_LAYER, CLEAR_LAYERS, REMOVE_SELECTION, MOVE_TO_BACK, MOVE_TO_FRONT
}

data class FunctionData(
    val img: Int,
    val text: String,
    val type: FunctionsEnum
)

val functionsList = listOf(
    FunctionData(
        img = R.drawable.ic_reset_image,
        text = "Reset Image",
        type = FunctionsEnum.RESET_IMAGE
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
        img = R.drawable.ic_add_layer,
        text = "Add Layer",
        type = FunctionsEnum.ADD_LAYER
    ),
    FunctionData(
        img = R.drawable.ic_clear_layers,
        text = "Clear Layers",
        type = FunctionsEnum.CLEAR_LAYERS
    ),
    FunctionData(
        img = R.drawable.ic_remove_selection,
        text = "Remove Selection",
        type = FunctionsEnum.REMOVE_SELECTION
    ),
    FunctionData(
        img = R.drawable.ic_move_to_back,
        text = "Move To Back",
        type = FunctionsEnum.MOVE_TO_BACK
    ),
    FunctionData(
        img = R.drawable.ic_move_to_front,
        text = "Move To Front",
        type = FunctionsEnum.MOVE_TO_FRONT
    )
)
