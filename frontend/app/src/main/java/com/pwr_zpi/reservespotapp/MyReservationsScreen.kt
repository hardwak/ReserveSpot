package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.time.LocalDateTime

@Composable
fun ReservationsScreen(navController: NavHostController) {

    val reservationList = fetchReservations()

    LazyColumn {
        items(reservationList) { reservation ->
            ResInfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                info = reservation
            )
        }
    }
}

fun fetchReservations(): List<ReservationInfo> {
    // TODO fetch from backend, this is a placeholder
    return listOf(
        ReservationInfo(
            restaurantName = "Burger Hub",
            dateTime = LocalDateTime.of(2025, 12, 5, 15, 30),
            numOfPeople = 6,
            durationH = 2.5f
        ),
        ReservationInfo(
            restaurantName = "Sushi World",
            dateTime = LocalDateTime.of(2025, 12, 12, 19, 0),
            numOfPeople = 2,
            durationH = 1.0f
        )
    )
}