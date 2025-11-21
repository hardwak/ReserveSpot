package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReservationsScreen(navController: NavHostController) {

    val context = LocalContext.current
    var reservations by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        reservations = fetchReservations(context)
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(reservations) { reservation ->
                ReservationInfoCard(
                    info = reservation,
                    onCancel = {
                        refreshTrigger++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            navController.navigate(
                                "restaurantDetails/${reservation.restaurantName}/${reservation.restaurantRating}" // TODO change this to restaurantId
                            )
                        }
                )
            }
        }

    }
}

suspend fun fetchReservations(context: Context): List<ReservationDto> {
    return withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getBackendToken()

            if (token.isNullOrEmpty()) {
                Log.e("fetchReservations", "No authentication token found")
                return@withContext emptyList()
            }

            val response = RetrofitClient.reservationApi
                .getMyUpcomingReservations("Bearer $token")

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("fetchReservations", "Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("fetchReservations", "Exception while fetching reservations", e)
            emptyList()
        }
    }
}
