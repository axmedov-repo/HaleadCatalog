package com.halead.catalog.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halead.catalog.R
import com.halead.catalog.ui.theme.HaleadCatalogTheme
import kotlinx.coroutines.launch

@Composable
fun GraphicsLayerScreen() {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var scaleX by remember {
        mutableFloatStateOf(1f)
    }
    var scaleY by remember {
        mutableFloatStateOf(1f)
    }
    var translateX by remember {
        mutableFloatStateOf(0f)
    }
    var translateY by remember {
        mutableFloatStateOf(0f)
    }
    var transformOrigin by remember {
        mutableFloatStateOf(0f)
    }
    var rotationX by remember {
        mutableFloatStateOf(0f)
    }
    var rotationY by remember {
        mutableFloatStateOf(0f)
    }
    var rotationZ by remember {
        mutableFloatStateOf(0f)
    }
    var alpha by remember {
        mutableFloatStateOf(1f)
    }
    var clip by remember {
        mutableStateOf(false)
    }
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {


            TransformationSlider(
                label = "scaleX", value = scaleX, onValueChange = {
                    scaleX = it
                }, valueRange = 0f..2f
            )


            TransformationSlider(
                label = "scaleY", value = scaleY, onValueChange = {
                    scaleY = it
                }, valueRange = 0f..2f
            )


            TransformationSlider(
                label = "translateX",
                value = translateX,
                onValueChange = {
                    translateX = it
                },
            )


            TransformationSlider(
                label = "translateY",
                value = translateY,
                onValueChange = {
                    translateY = it
                },
            )


            TransformationSlider(
                label = "transformOrigin",
                value = transformOrigin,
                onValueChange = {
                    transformOrigin = it
                },
            )



            TransformationSlider(
                label = "rotationX", value = rotationX, onValueChange = {
                    rotationX = it
                }, valueRange = 0f..360f
            )


            TransformationSlider(
                label = "rotationY", value = rotationY, onValueChange = {
                    rotationY = it
                }, valueRange = 0f..360f
            )


            TransformationSlider(
                label = "rotationZ", value = rotationZ, onValueChange = {
                    rotationZ = it
                }, valueRange = 0f..360f
            )

            TransformationSlider(
                label = "alpha", value = alpha,
                onValueChange = {
                    alpha = it
                },
            )


            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    clip = !clip
                }) {
                    Text("Clip")
                }

                Button(onClick = {
                    coroutineScope.launch {
                        val bitmap = graphicsLayer
                            .toImageBitmap()
                            .asAndroidBitmap()
                        shareBitmap(bitmap, context)
                    }
                }) {
                    Text("Save")
                }
            }
        }

        Column(
            Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, Color.Black)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
                .clickable {
                    coroutineScope.launch {
                        val bitmap = graphicsLayer
                            .toImageBitmap()
                            .asAndroidBitmap()
                        shareBitmap(bitmap, context)
                    }
                }

            ) {
                Surface {
                    Image(
                        painter = painterResource(id = R.drawable.material1),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .graphicsLayer {
                                this.scaleX = scaleX
                                this.scaleY = scaleY
                                this.translationX = (100 * translateX).dp.toPx()
                                this.translationY = (100 * translateY).dp.toPx()
                                this.transformOrigin = TransformOrigin(transformOrigin, transformOrigin)
                                this.rotationX = rotationX
                                this.rotationY = rotationY
                                this.rotationZ = rotationZ
                                this.alpha = alpha
                                this.clip = clip
                                this.ambientShadowColor = Color.Black
                                this.spotShadowColor = Color.Black
                                this.shape = CircleShape
                            },
                        contentScale = ContentScale.Fit
                    )
//                    Text(
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .blendMode(BlendMode.Difference),
//                        text = "Graphics Layer",
//                        color = Color.White,
//                        textAlign = TextAlign.Center,
//                        style = MaterialTheme.typography.displayLarge
//                    )

                }

            }
        }
    }
}

@Composable
fun TransformationSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {

    Column {
        Text(text = label)
        Slider(
            value = value, onValueChange = onValueChange, valueRange = valueRange
        )
    }
}


private fun Modifier.blendMode(blendMode: BlendMode): Modifier {
    return this.drawWithCache {
        val graphicsLayer = obtainGraphicsLayer()
        graphicsLayer.apply {
            record {
                drawContent()
            }
            this.blendMode = blendMode
        }
        onDrawWithContent {
            drawLayer(graphicsLayer)
        }
    }
}


private fun Modifier.colorFilter(colorFilter: ColorFilter): Modifier {
    return this.drawWithCache {
        val graphicsLayer = obtainGraphicsLayer()
        graphicsLayer.apply {
            record {
                drawContent()
            }
            this.colorFilter = colorFilter
        }
        onDrawWithContent {
            drawLayer(graphicsLayer)
        }
    }
}


@Preview(device = Devices.PIXEL_TABLET)
@Composable
fun GraphicsLayerScreenPreview() {
    HaleadCatalogTheme() {
        Surface {
            GraphicsLayerScreen()
        }
    }
}
