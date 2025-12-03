package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun PhotosTabContent(photos: List<PictureDto>, onClickPhoto: (String) -> Unit) {
    if (photos.isEmpty()) {
        Text("No photos available.", color = Color.Gray, modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
            photos.chunked(2).forEach { rowPhotos ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPhotos.forEach { photo ->
                        val rawUrl = photo.url


                        val fixedUrl = if (!rawUrl.isNullOrBlank()) {
                            when {
                                rawUrl.contains("localhost") -> rawUrl.replace("localhost", "10.0.2.2")
                                !rawUrl.startsWith("http") -> "http://10.0.2.2:8080" + if (rawUrl.startsWith("/")) rawUrl else "/$rawUrl"
                                else -> rawUrl
                            }
                        } else {
                            null
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)

                                .clickable {
                                    if (!fixedUrl.isNullOrBlank()) {
                                        onClickPhoto(fixedUrl)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            if (!fixedUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(fixedUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Restaurant Gallery Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),

                                )
                            } else {

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray)
                                ) {
                                    Text("No Photo", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                                }
                            }
                        }
                    }

                    if (rowPhotos.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


