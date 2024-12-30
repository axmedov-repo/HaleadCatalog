package com.halead.catalog

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.halead.catalog.data.materials

@Composable
fun MaterialsMenu(
    selectedMaterial: String,
    orientation: Int,
    onMaterialSelected: (String) -> Unit
) {
    val materialsList = materials.keys
    val modifier: Modifier = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Modifier
            .fillMaxHeight()
            .wrapContentWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Materials", style = MaterialTheme.typography.titleMedium)
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LazyColumn {
                items(materialsList.toList()) { material ->
                    MenuItem(material, selectedMaterial, onMaterialSelected)
                }
            }
        } else {
            LazyRow {
                items(materialsList.toList()) { material ->
                    MenuItem(material, selectedMaterial, onMaterialSelected)
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    material: String,
    selectedMaterial: String,
    onMaterialSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    materials[material]?.let {
        Box(modifier = modifier
            .padding(8.dp)
            .size(160.dp, 80.dp)
            .clickable { onMaterialSelected(material) }
            .then(
                if (selectedMaterial == material) {
                    Modifier.border(width = 4.dp, Color.Green)
                } else {
                    Modifier
                }
            )
            .background(Color.Red)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = "",
                painter = painterResource(it)
            )
        }
    }
}