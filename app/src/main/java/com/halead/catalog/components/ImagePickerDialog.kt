package com.halead.catalog.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.halead.catalog.R
import com.halead.catalog.ui.theme.ButtonColor

@Preview
@Composable
fun ImagePickerDialog(
    modifier: Modifier = Modifier,
    showDialog: Boolean = true,
    hasCurrentImage: Boolean = false,
    onDismiss: () -> Unit = {},
    onResetCurrentImage :() ->Unit = {},
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
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
                        text = "Upload New Image",
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable { onCameraClick() }
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(72.dp),
                                painter = painterResource(R.drawable.ic_camera),
                                tint = Color.LightGray,
                                contentDescription = null
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Camera", color = Color.Gray, fontSize = 14.sp)
                        }

                        Column(
                            modifier = Modifier
                                .clickable { onGalleryClick() }
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(72.dp),
                                painter = painterResource(R.drawable.ic_image),
                                tint = Color.LightGray,
                                contentDescription = null
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Gallery", color = Color.Gray, fontSize = 14.sp)
                        }
                    }

//                    if(hasCurrentImage){
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Button(
//                            modifier = Modifier
//                                .height(50.dp)
//                                .shadow(4.dp, RoundedCornerShape(8.dp))
//                                .clip(shape = RoundedCornerShape(8.dp))
//                                .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp)),
//                            shape = RoundedCornerShape(8.dp),
//                            onClick = onResetCurrentImage,
//                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
//                        ) {
//                            Text("Reset current image", color = Color.White)
//                        }
//                    }
                }
            }
        }
    }
}
