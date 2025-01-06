package com.halead.catalog

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.halead.catalog.data.materials
import com.halead.catalog.utils.getAspectRatioFromResource

@Composable
fun MaterialsMenu(
    selectedMaterial: Int,
    orientation: Int,
    onMaterialSelected: (Int) -> Unit
) {
    val materialsList by remember { mutableStateOf(materials.values) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Materials", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)

        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(materialsList.toList()) { material ->
                        MenuItem(material, selectedMaterial, onMaterialSelected)
                    }
                }
            }

            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(materialsList.toList()) { material ->
                        MenuItem(material, selectedMaterial, onMaterialSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    material: Int,
    selectedMaterial: Int,
    onMaterialSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val aspectRatio = getAspectRatioFromResource(material)

    Box(modifier = modifier
        .aspectRatio(aspectRatio)
        .border(
            BorderStroke(
                6.dp,
                if (selectedMaterial == material) Color.Green else Color.White
            )
        )
        .padding(6.dp)
        .clickable { onMaterialSelected(material) }
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            painter = painterResource(material)
        )
    }
}
