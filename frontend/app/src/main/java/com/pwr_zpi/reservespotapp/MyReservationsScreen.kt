package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReservationsScreen(navController: NavHostController) {

    val context = LocalContext.current

    var upcoming by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
    var finished by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }
    var canceled by remember { mutableStateOf<List<ReservationDto>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var selectedTab by remember { mutableIntStateOf(0) }   // 0 = upcoming, 1 = finished, 2 = canceled

    val tabTitles = listOf("Upcoming", "Finished", "Canceled")

    LaunchedEffect(refreshTrigger) {
        isLoading = true

        val all = fetchReservations(context)
        upcoming = all.filter { it.status == ReservationStatus.CONFIRMED || it.status == ReservationStatus.PENDING }
        finished = all.filter { it.status == ReservationStatus.COMPLETED }
        canceled = all.filter { it.status == ReservationStatus.CANCELLED }

        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = RSRed
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    selectedContentColor = RSRed,
                    unselectedContentColor = Color.Gray,
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val list = when (selectedTab) {
                0 -> upcoming
                1 -> finished
                2 -> canceled
                else -> emptyList()
            }

            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reservations here.")
                }
            } else {
                LazyColumn {
                    items(list) { reservation ->
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
                                        "restaurantDetails/${reservation.restaurantId}"
                                    )
                                },
                            showCancelButton = (selectedTab == 0)
                        )
                    }
                }
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
                .getMyReservationsHistory("Bearer $token")

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
