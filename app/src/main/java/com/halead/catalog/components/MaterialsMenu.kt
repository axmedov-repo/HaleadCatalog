package com.halead.catalog.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.halead.catalog.R
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.getAspectRatioFromResource
import com.halead.catalog.utils.noRippleClickable

@Composable
fun MaterialsMenu(
    materials: List<Int>,
    selectedMaterial: Int?,
    loadingApplyMaterial: Boolean,
    modifier: Modifier = Modifier,
    onAddMaterial: () -> Unit = {},
    onMaterialSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        items(materials) { material ->
            MenuItem(material, selectedMaterial, loadingApplyMaterial, onMaterialSelected)
        }
        /*item {
            AddMenuItem { onAddMaterial() }
        }*/
    }
}

@Composable
fun MenuItem(
    material: Int,
    selectedMaterial: Int?,
    loadingApplyMaterial: Boolean,
    onMaterialSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val aspectRatio by remember {
        derivedStateOf {
            getAspectRatioFromResource(material, context)
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .border(
                BorderStroke(
                    4.dp,
                    if (selectedMaterial == material) SelectedItemColor else Color.White
                ), RoundedCornerShape(8.dp)
            )
            .noRippleClickable { onMaterialSelected(material) }
            .padding(4.dp)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(material),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        if (loadingApplyMaterial && selectedMaterial == material) {
            ShimmerEffect(
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.LightGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

@Composable
private fun AddMenuItem(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(BorderStroke(4.dp, Color.White), RoundedCornerShape(8.dp))
            .padding(4.dp)
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