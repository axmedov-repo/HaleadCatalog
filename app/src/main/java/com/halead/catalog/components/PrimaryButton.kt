package com.halead.catalog.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halead.catalog.ui.theme.AppButtonSize

@Composable
fun PrimaryButton(
    primaryButtonText: String,
    isPrimaryButtonEnabled: Boolean,
    isMaterialsEmpty: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Gray,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(min = AppButtonSize)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        enabled = isPrimaryButtonEnabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = Color.Gray
        )
    ) {
        val textColor = if (isPrimaryButtonEnabled) Color.White else Color.White.copy(alpha = 0.4f)

        Row(
            modifier = Modifier.wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AutoSizeText(
                modifier = Modifier.weight(1f),
                maxTextSize = 14.sp,
                softWrap = false,
                maxLines = 1,
                text = primaryButtonText,
                color = textColor,
                alignment = Alignment.Center
            )
            if (isMaterialsEmpty) {
                Spacer(Modifier.width(4.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}