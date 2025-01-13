package com.halead.catalog.screens.work

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.data.models.WorkModel

@Composable
fun WorkHistoryPanel(
    modifier: Modifier = Modifier,
    workViewModel: WorkViewModel = viewModel<WorkViewModelImpl>(),
    onWorkClick: (WorkModel) -> Unit = {}
) {
    val uiState by workViewModel.uiState.collectAsState()

    Column(
        modifier = modifier.background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "History", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(uiState.works) {
                WorkHistoryItem(
                    work = it,
                    onClick = { onWorkClick(it) }
                )
            }
        }
    }
}

@Composable
fun WorkHistoryItem(
    work: WorkModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val aspectRatio by remember(work.baseImage) {
        derivedStateOf {
            work.baseImage.width.toFloat() / work.baseImage.height.toFloat()
        }
    }

    Box(
        modifier = modifier
            .width(200.dp)
            .aspectRatio(aspectRatio)
            .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(8.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.Red)
            )
            .padding(2.dp)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = work.baseImage.asImageBitmap(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
    }
}
