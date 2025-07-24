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
import com.halead.catalog.data.models.OverlayData
import com.halead.catalog.ui.theme.AppButtonSize
import com.halead.catalog.ui.theme.BorderThickness
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.noRippleClickable
import kotlinx.collections.immutable.ImmutableList

@Composable
fun FunctionsMenu(
    modifier: Modifier = Modifier,
    canUndo: Boolean,
    canRedo: Boolean,
    baseImage: ImageBitmap?,
    overlays: ImmutableList<OverlayData>,
    polygonPointsSize: () -> Int,
    selectedOverlay: OverlayData?,
    onFunctionClicked: (FunctionData) -> Unit,
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
            items = functions,
            key = { it.type }
        ) { functionData ->

            val rememberFunctionClicked = remember(functionData) {
                { onFunctionClicked(functionData) }
            }

            val isEnabled by remember(
                functionData.type, baseImage, canUndo, canRedo, overlays, polygonPointsSize(), selectedOverlay
            ) {
                derivedStateOf {
                    !when (functionData.type) {
                        FunctionsEnum.REDO -> !canRedo
                        FunctionsEnum.UNDO -> !canUndo
                        FunctionsEnum.ADD_LAYER -> overlays.isEmpty()
                        FunctionsEnum.CLEAR_LAYERS -> polygonPointsSize() == 0
                        FunctionsEnum.REMOVE_SELECTION -> overlays.isEmpty() || polygonPointsSize() < 3 || selectedOverlay?.material != null
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

            FunctionItem(
                data = functionData,
                enabled = isEnabled,
                onFunctionClicked = rememberFunctionClicked
            )
        }
    }
}

@Composable
fun CursorsMenu(
    modifier: Modifier = Modifier,
    canUndo: Boolean,
    canRedo: Boolean,
    overlays: ImmutableList<OverlayData>,
    polygonPointsSize: () -> Int,
    selectedOverlay: OverlayData?,
    baseImage: ImageBitmap?,
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
            items = functions,
            key = { it.type }
        ) { functionData ->

            val rememberFunctionClicked = remember(functionData) {
                { onFunctionClicked(functionData) }
            }

            val isEnabled by remember(
                functionData.type, baseImage, canUndo, canRedo, overlays, polygonPointsSize(), selectedOverlay
            ) {
                derivedStateOf {
                    !when (functionData.type) {
                        FunctionsEnum.REDO -> !canRedo
                        FunctionsEnum.UNDO -> !canUndo
                        FunctionsEnum.ADD_LAYER -> overlays.isEmpty()
                        FunctionsEnum.CLEAR_LAYERS -> polygonPointsSize() == 0
                        FunctionsEnum.REMOVE_SELECTION -> overlays.isEmpty() || polygonPointsSize() < 3 || selectedOverlay?.material != null
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

            FunctionItem(
                data = functionData,
                enabled = isEnabled,
                onFunctionClicked = rememberFunctionClicked
            )
        }
        items(cursorTypesList, key = { it.type }) { cursorData ->
            CursorItem(
                data = cursorData,
                selectedData = selectedCursor,
                enabled = baseImage != null,
                onCursorClicked = onCursorClicked
            )
        }

    }
}

@Composable
fun FunctionItem(
    data: FunctionData,
    enabled: Boolean,
    onFunctionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .size(AppButtonSize)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .background(Color.Gray)
            .border(BorderThickness, Color.White, shape = RoundedCornerShape(8.dp))
            .noRippleClickable(enabled = enabled, onClick = onFunctionClicked)
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img),
        alpha = if (enabled) 1f else 0.4f
    )
}

@Composable
fun CursorItem(
    data: CursorData,
    selectedData: CursorData?,
    enabled: Boolean,
    onCursorClicked: (CursorData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .size(AppButtonSize)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .drawBehind {
                drawRoundRect(
                    if (selectedData?.type == data.type && enabled) SelectedItemColor else Color.Gray
                )
            }
            .border(BorderThickness, Color.White, shape = RoundedCornerShape(8.dp))
            .noRippleClickable(enabled = enabled) { onCursorClicked(data) }
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img),
        alpha = if (enabled) 1f else 0.4f
    )
}
