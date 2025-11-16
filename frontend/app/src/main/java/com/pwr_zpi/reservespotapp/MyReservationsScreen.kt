package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager

@Composable
fun ReservationsScreen(navController: NavHostController) {

    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }

    var reservations by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reservations = fetchReservations(dataStoreManager)
        loaded = true
    }

    if (!loaded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Text(
                text = "Loading...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    } else {
        LazyColumn {
            items(reservations) { reservation ->
                ReservationInfoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(onClick = { navController.navigate("restaurantDetails/${reservation.restaurantName}/${reservation.restaurantRating}") }),
                    info = reservation
                )
            }
        }
    }
}

suspend fun fetchReservations(
    dataStoreManager: DataStoreManager
): List<ReservationDto> {

    val token = dataStoreManager.getBackendToken() ?: return emptyList()

    val response = RetrofitClient.reservationApi.getMyUpcomingReservations(
        "Bearer $token"
    )

    if (!response.isSuccessful) {
        return emptyList() // or throw error
    }

    val dtoList = response.body() ?: emptyList()

    return dtoList.map { dto ->
        ReservationDto(
            restaurantName = dto.restaurantName,
            dateTime = dto.dateTime,
            numOfPeople = dto.numOfPeople,
            durationH = dto.durationH,
            restaurantRating = dto.restaurantRating
        )
    }
}
