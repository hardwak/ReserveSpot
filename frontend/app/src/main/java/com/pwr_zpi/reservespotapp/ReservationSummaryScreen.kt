package com.pwr_zpi.reservespotapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationSummaryScreen(
    navController: NavHostController,
    restaurantName: String,
    tableId: Long,
    fullDateTime: String,
    durationMinutes: Int,
    dateDisplay: String,
    timeDisplay: String,
    guests: String,
    durationDisplay: String,
    location: String
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val decodedRestaurantName = remember(restaurantName) {
        try {
            URLDecoder.decode(restaurantName, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            restaurantName.replace("+", " ")
        }
    }

    val decodedLocation = remember(location) {
        try {
            URLDecoder.decode(location, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            location.replace("+", " ")
        }
    }

    val decodedFullDateTime = remember(fullDateTime) {
        try {
            URLDecoder.decode(fullDateTime, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            fullDateTime
        }
    }


    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }


    val formattedDateDisplay = try {
        LocalDate.parse(dateDisplay, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    } catch (e: Exception) {
        dateDisplay
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(decodedRestaurantName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Powrót")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReservationTab("Details", false)
                ReservationTab("Summary", true)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    "Confirm reservation",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                SummaryDetail("Date and time:", "$formattedDateDisplay at $timeDisplay")
                SummaryDetail("Guests number:", guests)
                SummaryDetail("Duration:", durationDisplay)
                SummaryDetail("Table location", decodedLocation)
                Spacer(modifier = Modifier.height(40.dp))

                OutlinedButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RSRed),
                    border = BorderStroke(1.dp, RSRed),
                    enabled = !isSubmitting
                ) {
                    Text("Change reservation data", fontSize = 16.sp)
                }
            }

            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val token = DataStoreManager(context).getBackendToken()

                            if (token != null) {

                                Log.d("ReservationSummary", "Wysyłam: TableId=$tableId, Date=$decodedFullDateTime")

                                val createDto = CreateReservationDto(
                                    tableId = tableId,
                                    reservationDatetime = decodedFullDateTime,
                                    durationMinutes = durationMinutes
                                )

                                val response = RetrofitClient.reservationApi.createReservation("Bearer $token", createDto)

                                if (response.isSuccessful) {
                                    showConfirmationDialog = true
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    Log.e("ReservationSummary", "Error Code: ${response.code()}")
                                    Log.e("ReservationSummary", "Error Body: $errorBody")
                                    Toast.makeText(context, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ReservationSummary", "EXCEPTION", e)
                            Toast.makeText(context, "Connection error", Toast.LENGTH_LONG).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed),

                enabled = !isSubmitting && tableId > 0
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "CONFIRM RESERVATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showConfirmationDialog) {
        val navigateBackToDetails: () -> Unit = {
            showConfirmationDialog = false

            navController.popBackStack()
            navController.popBackStack()
        }

        AlertDialog(
            onDismissRequest = navigateBackToDetails,
            title = { Text("Reservation confirmed!", color = Color.Black) },
            text = {
                Text("Your table reservation in $decodedRestaurantName has been placed.", color = Color.Black)
            },
            confirmButton = {
                TextButton(onClick = navigateBackToDetails) {
                    Text("OK", color = Color.Black)
                }
            }
        )
    }
}

@Composable
fun SummaryDetail(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider()
}