package com.halead.catalog.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.enums.cursorTypesList
import com.halead.catalog.data.enums.functionsList
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.ui.theme.AppButtonSize
import com.halead.catalog.ui.theme.BorderThickness
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.noRippleClickable

@Composable
fun FunctionsMenu(
    modifier: Modifier = Modifier,
    canUndo: Boolean,
    canRedo: Boolean,
    baseImage: ImageBitmap?,
    overlays: List<OverlayMaterialModel>,
    polygonPointsSize: Int,
    selectedOverlay: OverlayMaterialModel?,
    selectedCursor: CursorData?,
    onFunctionClicked: (FunctionData) -> Unit,
    onCursorClicked: (CursorData) -> Unit,
) {
    val functions = remember {
        functionsList.filter { it.type !in listOf(FunctionsEnum.UNDO, FunctionsEnum.REDO) }
    }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            functions,
            key = { it.type }) { functionData ->

            val rememberFunctionClicked = remember(functionData) {
                { onFunctionClicked(functionData) }
            }

            FunctionItem(
                data = functionData,
                canUndo = canUndo,
                canRedo = canRedo,
                overlays = overlays,
                polygonPointsSize = polygonPointsSize,
                selectedOverlay = selectedOverlay,
                baseImage = baseImage,
                onFunctionClicked = rememberFunctionClicked
            )
        }
//        item {
//            VerticalDivider(
//                Modifier.height(AppButtonSize).padding(vertical = 4.dp).clip(RoundedCornerShape(2.dp)),
//                thickness = 2.dp,
//                color = Color.Gray
//            )
//        }
//        items(cursorTypesList, key = { it.type }) { cursorData ->
//            CursorItem(
//                data = cursorData,
//                selectedData = selectedCursor,
//                baseImage = baseImage,
//                onCursorClicked = { onCursorClicked(cursorData) }
//            )
//        }
    }
}

@Composable
fun CursorsMenu(
    modifier: Modifier = Modifier,
    canUndo: Boolean,
    canRedo: Boolean,
    baseImage: ImageBitmap?,
    overlays: List<OverlayMaterialModel>,
    polygonPointsSize: Int,
    selectedOverlay: OverlayMaterialModel?,
    selectedCursor: CursorData?,
    onFunctionClicked: (FunctionData) -> Unit,
    onCursorClicked: (CursorData) -> Unit,
) {
    val functions = remember {
        functionsList.filter { it.type in listOf(FunctionsEnum.UNDO, FunctionsEnum.REDO) }
    }
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 8.dp, top = 8.dp, bottom = 8.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        items(
            functions,
            key = { it.type }) { functionData ->

            val rememberFunctionClicked = remember(functionData) {
                { onFunctionClicked(functionData) }
            }

            FunctionItem(
                data = functionData,
                canUndo = canUndo,
                canRedo = canRedo,
                overlays = overlays,
                polygonPointsSize = polygonPointsSize,
                selectedOverlay = selectedOverlay,
                baseImage = baseImage,
                onFunctionClicked = rememberFunctionClicked
            )
        }
        items(cursorTypesList, key = { it.type }) { cursorData ->
            CursorItem(
                data = cursorData,
                selectedData = selectedCursor,
                baseImage = baseImage,
                onCursorClicked = onCursorClicked
            )
        }

    }
}

@Composable
fun FunctionItem(
    data: FunctionData,
    canUndo: Boolean,
    canRedo: Boolean,
    overlays: List<OverlayMaterialModel>,
    polygonPointsSize: Int,
    selectedOverlay: OverlayMaterialModel?,
    baseImage: ImageBitmap?,
    onFunctionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDisabled by remember(
        data.type, canUndo, canRedo, overlays, polygonPointsSize, selectedOverlay, baseImage
    ) {
        derivedStateOf {
            when (data.type) {
                FunctionsEnum.REDO -> !canRedo
                FunctionsEnum.UNDO -> !canUndo
                FunctionsEnum.ADD_LAYER -> overlays.isEmpty()
                FunctionsEnum.CLEAR_LAYERS -> polygonPointsSize == 0
                FunctionsEnum.REMOVE_SELECTION -> overlays.isEmpty() || polygonPointsSize < 3 || (selectedOverlay != null && selectedOverlay.material != -1)
                FunctionsEnum.MOVE_TO_FRONT -> selectedOverlay == null || overlays.size < 2 || overlays.indexOf(
                    selectedOverlay
                ) == overlays.lastIndex

                FunctionsEnum.MOVE_TO_BACK -> selectedOverlay == null || overlays.size < 2 || overlays.indexOf(
                    selectedOverlay
                ) == 0

                FunctionsEnum.ROTATE_LEFT, FunctionsEnum.ROTATE_RIGHT -> selectedOverlay == null
                else -> baseImage == null
            }
        }
    }

    Image(
        modifier = modifier
            .size(AppButtonSize)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .background(Color.Gray)
            .border(BorderThickness, Color.White, shape = RoundedCornerShape(8.dp))
            .noRippleClickable(enabled = !isDisabled, onClick = onFunctionClicked)
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img),
        alpha = if (isDisabled) 0.4f else 1.0f
    )
}

@Composable
fun CursorItem(
    data: CursorData,
    selectedData: CursorData?,
    baseImage: ImageBitmap?,
    onCursorClicked: (CursorData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled by remember(baseImage) { derivedStateOf { baseImage != null } }

    Image(
        modifier = modifier
            .size(AppButtonSize)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .drawBehind {
                drawRoundRect(
                    if (selectedData?.type == data.type && baseImage != null) SelectedItemColor else Color.Gray
                )
            }
            .border(BorderThickness, Color.White, shape = RoundedCornerShape(8.dp))
            .noRippleClickable(enabled = enabled) { onCursorClicked(data) }
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img),
        alpha = if (enabled) 1.0f else 0.4f
    )
}
