package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PhotosTabContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PhotoPlaceholder(Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                PhotoPlaceholder(Modifier.weight(1f))
            }
        }

    }
}


@Composable
fun PhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Propotions 1:1 (square)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.LightGray)
    ) {
        // Need to switch to Image() function
        Text("Photo", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
    }
}
