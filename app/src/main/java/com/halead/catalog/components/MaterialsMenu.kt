package com.halead.catalog.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.halead.catalog.utils.getAspectRatioFromResource

@Composable
fun MaterialsMenu(
    materials: Map<String, Int>,
    selectedMaterial: Int?,
    modifier: Modifier = Modifier,
    onMaterialSelected: (Int) -> Unit
) {
    val materialsList by remember { mutableStateOf(materials.values) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
//        Text(text = "Materials", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(materialsList.toList()) { material ->
                MenuItem(material, selectedMaterial, onMaterialSelected)
            }
        }
    }
}

@Composable
fun MenuItem(
    material: Int,
    selectedMaterial: Int?,
    onMaterialSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val aspectRatio = getAspectRatioFromResource(material)

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .border(
                BorderStroke(
                    2.dp,
                    if (selectedMaterial == material) Color.Green else Color.White
                ), RoundedCornerShape(8.dp)
            )
            .clickable(
                onClick = { onMaterialSelected(material) },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.Red)
            )
            .padding(2.dp)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            painter = painterResource(material)
        )
    }
}
