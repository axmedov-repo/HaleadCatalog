package com.halead.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    var selectedMaterialResId by remember { mutableIntStateOf(-1) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Image Manipulation Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            ImageSelector(selectedMaterialResId)
        }

        // Materials
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .background(Color.LightGray)
                .padding(horizontal = 16.dp)
                .background(Color.White)
        ) {
            MaterialsMenu(
                selectedMaterialResId,
                onMaterialSelected = { material -> selectedMaterialResId = material })
        }
    }
}
