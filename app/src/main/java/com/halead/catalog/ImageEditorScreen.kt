package com.halead.catalog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.halead.catalog.data.materials
import com.halead.catalog.utils.getBitmapFromResource

@Composable
fun ImageEditorScreen() {
    val context = LocalContext.current
    var selectedMaterial by rememberSaveable { mutableStateOf("") }
    val selectedMaterialBitmap = remember(selectedMaterial) {
        materials[selectedMaterial]?.let { getBitmapFromResource(context, it) }
    }
    when (val orientation = context.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    ImageEditor(selectedMaterialBitmap)
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .background(Color.White)
                ) {
                    MaterialsMenu(
                        selectedMaterial,
                        orientation = orientation,
                        onMaterialSelected = { material -> selectedMaterial = material })
                }
            }
        }

        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    ImageEditor(selectedMaterialBitmap)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color.White)
                ) {
                    MaterialsMenu(
                        selectedMaterial,
                        orientation = orientation,
                        onMaterialSelected = { material -> selectedMaterial = material })
                }
            }
        }
    }
}
