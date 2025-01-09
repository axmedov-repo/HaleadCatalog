package com.halead.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halead.catalog.screens.MainScreen
import com.halead.catalog.ui.theme.HaleadCatalogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HaleadCatalogTheme {
                AppScreen()
            }
        }
    }
}

@Preview(device = Devices.PIXEL_TABLET)
@Composable
fun AppScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.LightGray
    ) {
        Column {
            TopBar()
            MainScreen()
        }
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Menu,
            tint = Color.LightGray,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .clickable { }
        )
        Icon(
            Icons.Rounded.Create,
            tint = Color.LightGray,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .clickable { }
        )
        Icon(
            painter = painterResource(R.drawable.ic_undo),
            tint = Color.LightGray,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .clickable { }
        )
        Icon(
            painter = painterResource(R.drawable.ic_redo),
            tint = Color.LightGray,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .clickable { }
        )
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Rounded.Share,
            tint = Color.LightGray,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .clickable { }
        )
        Icon(
            Icons.Rounded.Settings,
            tint = Color.LightGray,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(6.dp)
                .clickable { }
        )
    }
}