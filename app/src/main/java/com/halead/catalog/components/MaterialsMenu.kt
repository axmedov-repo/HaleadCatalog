package com.halead.catalog.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.halead.catalog.R
import com.halead.catalog.data.models.OverlayMaterial
import com.halead.catalog.ui.theme.BorderThickness
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.getAspectRatioFromResource
import com.halead.catalog.utils.noRippleClickable
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

@Composable
fun MaterialsMenu(
    materials: ImmutableList<Int>,
    selectedMaterial: OverlayMaterial?,
    loadingApplyMaterial: () -> Boolean,
    modifier: Modifier = Modifier,
    onAddMaterial: () -> Unit = {},
    onMaterialSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        items(materials) { material ->
            MenuItem(
                material = material,
                isSelected = selectedMaterial?.resId == material,
                loadingApplyMaterial = loadingApplyMaterial,
                onMaterialSelected = onMaterialSelected
            )
        }
        /*item {
            AddMenuItem { onAddMaterial() }
        }*/
    }
}

@Composable
private fun MenuItem(
    @DrawableRes material: Int,
    isSelected: Boolean,
    loadingApplyMaterial: () -> Boolean,
    onMaterialSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val aspectRatio by remember(material) {
        derivedStateOf {
            getAspectRatioFromResource(material, context)
        }
    }
    val width = 512
    val height = (width / aspectRatio).roundToInt()
    var isLoading by remember(material) {
        mutableStateOf(false)
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(material)
            .size(width, height)
            .build(),
        onState = {
            isLoading = it is AsyncImagePainter.State.Loading
        }
    )

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .border(
                BorderStroke(
                    BorderThickness,
                    if (isSelected) SelectedItemColor else Color.White
                ), RoundedCornerShape(8.dp)
            )
            .noRippleClickable { onMaterialSelected(material) }
            .padding(BorderThickness)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        if (isLoading || (loadingApplyMaterial() && isSelected)) {
            ShimmerBox(modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
private fun AddMenuItem(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(BorderStroke(BorderThickness, Color.White), RoundedCornerShape(8.dp))
            .padding(BorderThickness)
            .background(Color.White.copy(0.2f))
            .height(100.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(36.dp),
            painter = painterResource(R.drawable.ic_add),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = null
        )
    }
}