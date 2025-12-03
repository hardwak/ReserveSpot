package com.pwr_zpi.reservespotapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SelectableDates
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    navController: NavHostController,
    restaurantId: Long,
    restaurantName: String,
    initialDate: String? = null,
    initialTime: String? = null,
    initialGuests: String? = null,
    initialDuration: String? = null,
    initialLocation: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)


    val parsedDate = remember(initialDate) {
        if (!initialDate.isNullOrEmpty()) {
            try {
                LocalDate.parse(initialDate, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } else {
            LocalDate.now()
        }
    }


    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(parsedDate) }


    var selectedTime by remember { mutableStateOf(initialTime) }

    var selectedGuests by remember { mutableStateOf(initialGuests?.toIntOrNull() ?: 2) }
    var selectedDurationLabel by remember { mutableStateOf(initialDuration ?: "1 hour") }
    var selectedLocation by remember { mutableStateOf(initialLocation ?: "Any") }


    var availableSlots by remember { mutableStateOf<List<AvailableReservationSlotDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }


    val durationMap = mapOf("1 hour" to 60, "1.5 hours" to 90, "2 hours" to 120)

    val guestsOptions = (1..10).map { it.toString() }
    val durationOptions = durationMap.keys.toList()


    val locationOptions = remember(availableSlots) {
        val locations = availableSlots
            .mapNotNull { it.locationInRestaurant }
            .filter { it.isNotBlank() }
            .distinct() // only unique
            .sorted() // sorting alphabetically

        listOf("Any") + locations
    }

    LaunchedEffect(locationOptions) {
        if (selectedLocation !in locationOptions) {
            selectedLocation = locationOptions.firstOrNull() ?: "Any"
        }
    }


    LaunchedEffect(selectedDate, selectedDurationLabel) {
        isLoading = true
        try {
            val token = dataStore.getBackendToken()
            if (token != null) {
                val durationMinutes = durationMap[selectedDurationLabel] ?: 60
                val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val response = RetrofitClient.reservationApi.getAvailability(
                    "Bearer $token",
                    restaurantId,
                    dateStr,
                    durationMinutes
                )

                if (response.isSuccessful) {
                    availableSlots = response.body() ?: emptyList()


                } else {
                    Toast.makeText(context, "Error downloading hour: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Reservation", "Error fetching slots", e)
        } finally {
            isLoading = false
        }
    }


    val filteredSlots = remember(availableSlots, selectedGuests, selectedLocation, selectedDate) {
        availableSlots.filter { slot ->

            val capacityOk = slot.tableCapacity >= selectedGuests


            val locationOk = selectedLocation == "Any" ||
                    (slot.locationInRestaurant?.equals(selectedLocation, ignoreCase = true) == true)

            // Time filter (Backend returns all times, but here we filter to only available in current time)

            val timeOk = if (selectedDate.isEqual(LocalDate.now())) {
                try {
                    val slotTime = LocalDateTime.parse(slot.start).toLocalTime()
                    slotTime.isAfter(LocalTime.now())
                } catch (e: Exception) { true }
            } else {
                true
            }

            capacityOk && locationOk && timeOk
        }
    }


    val timeOptions = remember(filteredSlots) {
        filteredSlots
            .map { extractTime(it.start) }
            .distinct()
            .sorted()
    }

    // Reset selectedTime if it's no longer available after filtering
    LaunchedEffect(timeOptions) {
        if (selectedTime != null && !timeOptions.contains(selectedTime)) {
            selectedTime = null
        }
    }

    val scrollState = rememberScrollState()


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

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReservationTab("Details", true)
                ReservationTab("Summary", false)
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(1.dp, RSRed, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = RSRed, modifier = Modifier.padding(end = 8.dp))
                Text("Table reservation", fontWeight = FontWeight.Bold, color = RSRed)
            }


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {

                FormSectionTitle("Data")
                DateSelector(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    onOpenCalendar = { showDatePicker = true }
                )


                FormSectionTitle("Guests number")
                HorizontalSelector(
                    options = guestsOptions,
                    selectedValue = selectedGuests.toString(),
                    onSelect = { selectedGuests = it.toIntOrNull() ?: 2 }
                )


                FormSectionTitle("Duration")
                HorizontalSelector(
                    options = durationOptions,
                    selectedValue = selectedDurationLabel,
                    onSelect = { selectedDurationLabel = it }
                )


                FormSectionTitle("Table location")
                if (isLoading) {

                }

                HorizontalSelector(
                    options = locationOptions,
                    selectedValue = selectedLocation,
                    onSelect = { selectedLocation = it }
                )

                FormSectionTitle("Available Hours")
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = RSRed)
                } else if (timeOptions.isEmpty()) {
                    Text("No tables available for selected criteria.", color = Color.Gray)
                } else {
                    HorizontalSelector(
                        options = timeOptions,
                        selectedValue = selectedTime ?: "",
                        onSelect = { selectedTime = it }
                    )
                }
            }


            Button(
                onClick = {
                    if (selectedTime == null) {
                        Toast.makeText(context, "Please select a time", Toast.LENGTH_SHORT).show()
                        return@Button
                    }


                    val chosenSlot = filteredSlots.firstOrNull { extractTime(it.start) == selectedTime }

                    if (chosenSlot != null) {
                        val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)


                        val fullIsoDateTime = chosenSlot.start


                        val encodedRestaurantName = URLEncoder.encode(restaurantName, StandardCharsets.UTF_8.toString())
                        val encodedLocation = URLEncoder.encode(selectedLocation, StandardCharsets.UTF_8.toString())

                        val encodedFullDate = URLEncoder.encode(fullIsoDateTime, StandardCharsets.UTF_8.toString())

                        val route = "reservationSummary/$encodedRestaurantName?" +
                                "tableId=${chosenSlot.tableId}&" +
                                "fullDateTime=$encodedFullDate&" +
                                "dateDisplay=$dateString&" +
                                "timeDisplay=$selectedTime&" +
                                "guests=$selectedGuests&" +
                                "durationMinutes=${durationMap[selectedDurationLabel]}&" +
                                "durationDisplay=$selectedDurationLabel&" +
                                "location=$encodedLocation"

                        navController.navigate(route)
                    } else {
                        Toast.makeText(context, "Slot selection error", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed),
                enabled = selectedTime != null && !isLoading
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


fun extractTime(isoDateTime: String): String {
    return try {
        LocalDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        isoDateTime.substringAfter("T").take(5)
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
            Spacer(modifier = Modifier.width(40.dp).height(2.dp).background(Color.Black))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit, onOpenCalendar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
            Text("<- Previous", color = RSRed)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onOpenCalendar() }
                .border(1.dp, RSRed, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = RSRed, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (selectedDate == LocalDate.now()) "Today" else selectedDate.format(DateTimeFormatter.ofPattern("dd MMM")),
                fontWeight = FontWeight.Bold,
                color = RSRed
            )
        }
        TextButton(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
            Text("Next ->", color = RSRed)
        }
    }
}

@Composable
fun HorizontalSelector(options: List<String>, selectedValue: String, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(options) { option ->
            val isSelected = option == selectedValue
            Text(
                option,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(option) }
                    .background(if (isSelected) RSRed else Color.Transparent)
                    .border(1.dp, if (isSelected) RSRed else Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDatePicker(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val initialTimeMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val todayMillis = remember { LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayMillis
            override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
        }
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialTimeMillis, selectableDates = selectableDates)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedLocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onDateSelected(selectedLocalDate)
                }
            }) { Text("OK", color = RSRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    ) { DatePicker(state = datePickerState) }
}