package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoCard(
//    deleted fixed height and width because modifier should control that
    modifier: Modifier = Modifier,
    // image: ? = null, TODO pass an image to the method
    restaurantName: String,
    rating: Float,
    views: Int

    ) {
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp))

    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.food_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Text(
            text = restaurantName,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ){

            Text(
                text = rating.toString(),
                color = Color.Yellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(4.dp)
            )

            Icon(
                imageVector = Icons.Default.Star,
                tint = Color.Yellow,
                modifier = Modifier
                    .size(28.dp),
                contentDescription = "Star"
            )

            Text(
                text = "(" + views.toString() + ")",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(4.dp),
                color = Color.White
            )
        }
    }
}
