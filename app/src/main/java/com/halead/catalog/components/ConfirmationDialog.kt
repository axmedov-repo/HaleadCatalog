package com.halead.catalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class ConfirmationDialogColors(
    val titleColor: Color,
    val messageColor: Color,
    val confirmButtonColor: Color,
    val dismissButtonColor: Color,
    val confirmTextColor: Color,
    val dismissTextColor: Color
)

object ConfirmationDialogDefaults {
    @Composable
    fun colors(
        titleColor: Color = Color.Black,
        messageColor: Color = Color.DarkGray,
        confirmButtonColor: Color = Color.Green,
        dismissButtonColor: Color = Color.LightGray,
        confirmTextColor: Color = Color.White,
        dismissTextColor: Color = Color.Black
    ) = ConfirmationDialogColors(
        titleColor = titleColor,
        messageColor = messageColor,
        confirmButtonColor = confirmButtonColor,
        dismissButtonColor = dismissButtonColor,
        confirmTextColor = confirmTextColor,
        dismissTextColor = dismissTextColor
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    confirmButtonText: String = "Yes",
    dismissButtonText: String = "No",
    dismissButtonEnabled: Boolean = false,
    colors: ConfirmationDialogColors = ConfirmationDialogDefaults.colors(),
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    text = title,
                    color = colors.titleColor,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    text = message,
                    color = colors.messageColor
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dismissButtonEnabled) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Button(
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors()
                                    .copy(colors.dismissButtonColor),
                                modifier = Modifier.width(100.dp),
                                onClick = { onDismiss() }
                            ) {
                                Text(
                                    dismissButtonText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.dismissTextColor, fontSize = 16.sp
                                )
                            }
                        }
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Button(
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors().copy(colors.confirmButtonColor),
                            modifier = Modifier.width(100.dp),
                            onClick = onConfirm
                        ) {
                            Text(
                                confirmButtonText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = colors.confirmTextColor, fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    Box(Modifier.fillMaxSize()) {
        ConfirmationDialog(
            title = "Sample Title",
            message = "Sample Message",
            dismissButtonEnabled = true,
            onConfirm = {}
        )
    }
}