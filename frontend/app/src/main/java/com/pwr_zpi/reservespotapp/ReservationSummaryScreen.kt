package com.pwr_zpi.reservespotapp

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationSummaryScreen(
    navController: NavHostController,
    restaurantName: String,
    date: String,
    time: String,
    guests: String,
    duration: String,
    type: String,
    location: String

) {

    val formattedDate = try {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    } catch (e: Exception) {
        date
    }

    var showConfirmationDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurantName) },
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
            // Details and summary
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReservationTab("Details", false)
                ReservationTab("Summary", true)
            }

            // Header and info in summary
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

                SummaryDetail("Date and time:", "$formattedDate o $time")
                SummaryDetail("Guests number:", guests)
                SummaryDetail("Duration:", duration)
                SummaryDetail("Meeting type:", type)
                SummaryDetail("Table location", location)

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RSRed),
                    border = BorderStroke(1.dp, RSRed)
                ) {
                    Text("Change reservation data", fontSize = 16.sp)
                }
            }

            // Confirm reservation button
            Button(
                // TODO add saving reservation to DB
                onClick = { showConfirmationDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text(
                    "CONFIRM RESERVATION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }

    if (showConfirmationDialog) {


        val navigateBackToDetails: () -> Unit = {
            showConfirmationDialog = false
            navController.popBackStack("restaurantDetails/$restaurantName", inclusive = false)
        }

        AlertDialog(
            onDismissRequest = navigateBackToDetails,

            title = {
                Text("Reservation confirmed!", color = Color.Black)
            },
            text = {
                Text("Your table reservation in $restaurantName has been placed.", color = Color.Black)
            },
            confirmButton = {
                TextButton(
                    onClick = navigateBackToDetails
                ) {
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

