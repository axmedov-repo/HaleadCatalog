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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    confirmButtonText: String = "Yes",
    dismissButtonText: String = "No",
    titleColor: Color = Color.Black,
    messageColor: Color = Color.DarkGray,
    confirmButtonColor: Color = Color.Green,
    dismissButtonColor: Color = Color.LightGray,
    confirmTextColor: Color = Color.White,
    dismissTextColor: Color = Color.Black,
    dismissButtonEnabled: Boolean = false,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 8.dp,
                modifier = modifier.wrapContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        text = title,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        text = message,
                        color = messageColor
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
                                    colors = ButtonDefaults.buttonColors().copy(dismissButtonColor),
                                    modifier = Modifier.width(100.dp),
                                    onClick = { onDismiss() }
                                ) {
                                    Text(
                                        dismissButtonText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = dismissTextColor, fontSize = 16.sp
                                    )
                                }
                            }
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Button(
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors().copy(confirmButtonColor),
                                modifier = Modifier.width(100.dp),
                                onClick = { onConfirm() }
                            ) {
                                Text(
                                    confirmButtonText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = confirmTextColor, fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    device = Devices.PIXEL_TABLET,
    showBackground = true,
)
@Composable
private fun Pre() {
    Box(Modifier.fillMaxSize()) {
        ConfirmationDialog(
            showDialog = true,
            title = "Upload New Image?",
            message = "Are you sure you want to upload a new image?\nAnyway, your current work will be saved in the history for future reference.",
            dismissButtonEnabled = true,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
