package com.halead.catalog.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halead.catalog.R
import com.halead.catalog.ui.theme.SelectedItemColor

@Composable
fun PerspectiveSwitch(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        modifier = modifier,
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = SelectedItemColor,
            checkedTrackColor = Color.Gray,
            checkedBorderColor = Color.White,
            uncheckedBorderColor = Color.White,
            uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
            uncheckedTrackColor = Color.Gray,
        ),
        thumbContent = {
            Icon(
                painterResource(R.drawable.ic_3d),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

@Preview
@Composable
private fun PerspectiveSwitchPreview() {
    var checked by remember {
        mutableStateOf(false)
    }
    PerspectiveSwitch(
        isChecked = checked,
        onCheckedChange = { checked = it }
    )
}