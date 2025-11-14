package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ReservationInfo (
    val restaurantName: String,
    val dateTime: LocalDateTime,
    val numOfPeople: Int,
    val durationH: Float
)

@Composable
fun ResInfoCard(
    modifier: Modifier,
    info: ReservationInfo
) {
    val restaurantName = info.restaurantName
    val datetime = info.dateTime
    val numOfPeople = info.numOfPeople
    val durationH = info.durationH

    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = RSRed,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(width = 1.dp,
                color = RSRed,
                shape = RoundedCornerShape(16.dp))

    ) {
        Text(
            text = restaurantName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(bottom = 16.dp)
                .padding(top = 8.dp)
        )

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart)
                .width(150.dp)
        )
        {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    contentDescription = "datetime"
                )
                Text(text = datetime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    contentDescription = "people"
                )
                Text(text = numOfPeople.toString())
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    contentDescription = "duration"
                )
                Text(text = durationH.toString() + "h")
            }
        }


        Button(
            onClick = {
                // TODO cancel reservation
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd)
                .width(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RSRed,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp
            )
        }

    }

}
