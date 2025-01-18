package com.halead.catalog.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.halead.catalog.ui.theme.SelectedItemColor

@Composable
fun FunctionsMenu(
    modifier: Modifier = Modifier,
    canUndo: Boolean,
    canRedo: Boolean,
    baseImage: ImageBitmap?,
    isOverlaysEmpty: Boolean,
    isPolygonPointsEmpty: Boolean,
    isOverlaySelected: Boolean,
    selectedCursor: CursorData?,
    onFunctionClicked: (FunctionData) -> Unit,
    onCursorClicked: (CursorData) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.LightGray),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(functionsList) { functionData ->
                FunctionItem(
                    data = functionData,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    isOverlaysEmpty = isOverlaysEmpty,
                    isPolygonPointsEmpty = isPolygonPointsEmpty,
                    isOverlaySelected = isOverlaySelected,
                    baseImage = baseImage,
                    onFunctionClicked = { onFunctionClicked(functionData) }
                )
            }
            item {
                VerticalDivider(
                    Modifier.height(40.dp),
                    thickness = 2.dp,
                    color = Color.Gray
                )
            }
            items(cursorTypesList) { cursorData ->
                CursorItem(
                    data = cursorData,
                    selectedData = selectedCursor,
                    baseImage = baseImage,
                    onCursorClicked = { onCursorClicked(cursorData) }
                )
            }
        }
    }
}

@Composable
fun FunctionItem(
    data: FunctionData,
    canUndo: Boolean,
    canRedo: Boolean,
    isOverlaysEmpty: Boolean,
    isPolygonPointsEmpty: Boolean,
    isOverlaySelected: Boolean,
    baseImage: ImageBitmap?,
    onFunctionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val disabled by remember(
        data.type, canUndo, canRedo, isOverlaysEmpty, isPolygonPointsEmpty, isOverlaySelected, baseImage
    ) {
        derivedStateOf {
            when (data.type) {
                FunctionsEnum.REDO -> !canRedo
                FunctionsEnum.UNDO -> !canUndo
                FunctionsEnum.REPLACE_IMAGE -> baseImage == null
                FunctionsEnum.ADD_LAYER -> isOverlaysEmpty
                FunctionsEnum.CLEAR_LAYERS -> isPolygonPointsEmpty
                else -> false
            }
        }
    }

    val alpha = remember(disabled) { if (disabled) 0.4f else 1.0f }

    Image(
        modifier = modifier
            .size(50.dp)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .background(Color.Gray)
            .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = !disabled) { onFunctionClicked() }
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img),
        alpha = alpha
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
    val background by remember(selectedData?.type, data.type, baseImage) {
        derivedStateOf {
            if (selectedData?.type == data.type && baseImage != null) SelectedItemColor else Color.Gray
        }
    }

    val enabled by remember(baseImage) { derivedStateOf { baseImage != null } }
    val alpha = remember(enabled) { if (enabled) 1.0f else 0.4f }

    Image(
        modifier = modifier
            .size(50.dp)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .background(background)
            .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onCursorClicked(data) }
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img),
        alpha = alpha
    )
}
