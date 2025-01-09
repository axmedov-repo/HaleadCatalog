package com.halead.catalog.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
    selectedCursor: CursorData? = null,
    onFunctionClicked: (FunctionData) -> Unit,
    onCursorClicked: (CursorData) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        /*        Text(
            text = "Functions",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.titleMedium
        )*/

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(functionsList) { functionData ->
                FunctionItem(
                    data = functionData,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onFunctionClicked = { onFunctionClicked(functionData) }
                )
            }
            item {
                HorizontalDivider(
                    Modifier.width(40.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }
            items(cursorTypesList) { cursorData ->
                CursorItem(
                    data = cursorData,
                    selectedData = selectedCursor,
                    onFunctionClicked = { onCursorClicked(cursorData) }
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
    onFunctionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val disabled by remember(data, canUndo, canRedo) {
        derivedStateOf {
            (!canRedo && data.type == FunctionsEnum.REDO) || (!canUndo && data.type == FunctionsEnum.UNDO)
        }
    }

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
        alpha = if (disabled) 0.4f else 1.0f
    )
}

@Composable
fun CursorItem(
    data: CursorData,
    selectedData: CursorData?,
    onFunctionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .size(50.dp)
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .background(if (selectedData == data) SelectedItemColor else Color.Gray)
            .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp))
            .clickable { onFunctionClicked() }
            .padding(8.dp),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        colorFilter = ColorFilter.tint(Color.White),
        painter = painterResource(data.img)
    )
}
