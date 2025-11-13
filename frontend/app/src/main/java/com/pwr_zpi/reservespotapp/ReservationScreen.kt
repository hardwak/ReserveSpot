package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(navController: NavHostController, restaurantName: String) {
    // Reservation form stages
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedGuests by remember { mutableStateOf(2) }
    var selectedDuration by remember { mutableStateOf("1 hour") }
    var selectedType by remember { mutableStateOf("Basic reservation") }

    val guestsOptions = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val durationOptions = listOf("1 hour", "1.5 hours", "2 hours")
    val typeOptions = listOf("Basic reservation", "Business meeting")




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurantName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReservationTab("Details", true)
                ReservationTab("Summary", false)
            }

            // Header table reservation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(1.dp, RSRed, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = RSRed,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "Table reservation",
                    fontWeight = FontWeight.Bold,
                    color = RSRed
                )
            }


            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)) {

                // Data section
                FormSectionTitle("Data")
                DateSelector(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    onOpenCalendar = { showDatePicker = true }
                )

                // Hour section
                FormSectionTitle("Hour")
                Text(
                    "No free timeslots this day",
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Red
                )

                // Guests number section
                FormSectionTitle("Guests number")
                HorizontalSelector(
                    options = guestsOptions.map { it.toString() },
                    selectedValue = selectedGuests.toString(),
                    onSelect = { selectedGuests = it.toIntOrNull() ?: 2 }
                )

                // Reservation duration section
                FormSectionTitle("Duration")
                HorizontalSelector(
                    options = durationOptions,
                    selectedValue = selectedDuration,
                    onSelect = { selectedDuration = it }
                )

                // Type of meeting
                FormSectionTitle("Meeting type")
                HorizontalSelector(
                    options = typeOptions,
                    selectedValue = selectedType,
                    onSelect = { selectedType = it }
                )
            }


            Button(
                onClick = { /* TODO: Add go to summary screen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text("Next", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        }
    }
    if (showDatePicker) {
        ReservationDatePicker(
            initialDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}


@Composable
fun ReservationTab(title: String, isSelected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        if (isSelected) {
            Spacer(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun FormSectionTitle(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onOpenCalendar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onOpenCalendar() }
                .border(1.dp, RSRed, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = RSRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (selectedDate == LocalDate.now()) "Today" else selectedDate.format(
                    DateTimeFormatter.ofPattern("dd MMM")
                ),
                fontWeight = FontWeight.Bold,
                color = RSRed
            )
        }

        TextButton(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
            Text("Next →")
        }
    }
}


@Composable
fun HorizontalSelector(options: List<String>, selectedValue: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedValue
            Text(
                option,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(option) }
                    .background(if (isSelected) RSRed else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) RSRed else Color.LightGray,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun ReservationDatePicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTimeMillis =
        initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialTimeMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedLocalDate)
                    } ?: onDismiss()
                }
            ) {
                Text("OK", color = RSRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}