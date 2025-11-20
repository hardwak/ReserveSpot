package com.pwr_zpi.reservespotapp


import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.Calendar

// Main screen for the owner (restaurants list)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    var restaurants by remember { mutableStateOf<List<OwnerRestaurantDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Fetching data during start
    LaunchedEffect(Unit) {
        isLoading = true
        // TODO: Pobrac prawdziwe ID właściciela z DataStore lub Tokena
        val ownerId = 1L
        restaurants = fetchOwnerRestaurants(context, ownerId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twoje Restauracje") },
                actions = {
                    IconButton(onClick = { navController.navigate("ownerReservations") }) {
                        Icon(Icons.Default.Event, contentDescription = "Rezerwacje")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("ownerEditRestaurant/new") },
                containerColor = RSRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RSRed)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(restaurants) { restaurant ->
                    OwnerRestaurantCard(
                        restaurant = restaurant,
                        onClick = { navController.navigate("ownerEditRestaurant/${restaurant.id}") },
                        onManageTables = { navController.navigate("ownerTables/${restaurant.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerRestaurantCard(
    restaurant: OwnerRestaurantDto,
    onClick: () -> Unit,
    onManageTables: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(restaurant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("${restaurant.address}, ${restaurant.city}", color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Ocena: ${restaurant.averageRating ?: 0.0}", color = RSRed, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onManageTables,
                    colors = ButtonDefaults.buttonColors(containerColor = RSRed),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Stoliki", fontSize = 12.sp)
                }
            }
        }
    }
}

// Edition / Adding restaurant screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerEditRestaurantScreen(navController: NavHostController, restaurantIdString: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNew = restaurantIdString == "new"

    // form fields
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val openingHours = remember { mutableStateMapOf(
        "monday" to "10:00-22:00",
        "tuesday" to "10:00-22:00",
        "wednesday" to "10:00-22:00",
        "thursday" to "10:00-22:00",
        "friday" to "10:00-23:00",
        "saturday" to "12:00-23:00",
        "sunday" to "12:00-22:00"
    ) }

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle


    val latLiveData = remember { savedStateHandle?.getLiveData<Double>("picked_lat") ?: MutableLiveData<Double>() }
    val lngLiveData = remember { savedStateHandle?.getLiveData<Double>("picked_lng") ?: MutableLiveData<Double>() }

    val pickedLat by latLiveData.observeAsState()
    val pickedLng by lngLiveData.observeAsState()

    LaunchedEffect(pickedLat, pickedLng) {
        pickedLat?.let { latitude = it.toString() }
        pickedLng?.let { longitude = it.toString() }
    }

    // Choosing photo
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    // if editing need to fetch restaurants data
    // TODO LaunchedEffect should download data by ID

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Dodaj Restaurację" else "Edytuj Restaurację") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = {
                            scope.launch {
                                deleteRestaurant(context, restaurantIdString.toLong())
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, null, tint = RSRed)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
//            Basic info
            Text("Dane Podstawowe", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Miasto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )
            Spacer(Modifier.height(24.dp))

            Text("Lokalizacja na Mapie", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Przycisk otwierający mapę
            OutlinedButton(
                onClick = {
                    // Przekazujemy obecne wartości (jeśli istnieją), aby mapa wycentrowała się na nich
                    val latArg = latitude.toDoubleOrNull() ?: 0.0
                    val lngArg = longitude.toDoubleOrNull() ?: 0.0
                    navController.navigate("pickLocation?lat=$latArg&lng=$lngArg")
                },
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, RSRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RSRed)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Wybierz punkt na mapie")
            }

            Spacer(Modifier.height(8.dp))


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = false // Można zmienić na true, jeśli chcesz zabronić ręcznej edycji
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = false
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Sekcja Zdjęcia ---
            Text("Zdjęcie Restauracji", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Wybierz zdjęcie z galerii")
            }

            Spacer(Modifier.height(24.dp))

            // Opening hours section

            Text("Godziny Otwarcia", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Hours editor for each day
            val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
            days.forEach { day ->
                OpeningHoursRow(day, openingHours[day] ?: "Zamknięte") { newHours ->
                    openingHours[day] = newHours
                }
            }

            Spacer(Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        // Conversion lat, long to Double
                        val latVal = latitude.toDoubleOrNull()
                        val lonVal = longitude.toDoubleOrNull()

                        // Photo upload (TODO: Zaimplementuj upload i pobierz URL)
                        val imageUrl = selectedImageUri?.toString() // Temp URI as String

                        val restaurant = OwnerRestaurantDto(
                            id = if (isNew) null else restaurantIdString.toLong(),
                            ownerId = 1L, // TODO: Get real owner ID
                            name = name, address = address, city = city,
                            description = description,
                            openingHours = openingHours.toMap(), // Conversion to Map
                            latitude = latVal,
                            longitude = lonVal,
                            pic = imageUrl
                        )
                        saveRestaurant(context, restaurant, isNew)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text("Zapisz")
            }
        }
    }
}


@Composable
fun OpeningHoursRow(day: String, currentHours: String, onHoursChanged: (String) -> Unit) {
    val context = LocalContext.current

    // Parsowanie obecnych godzin (proste założenie formatu "HH:MM-HH:MM" lub "Zamknięte")
    // Dla uproszczenia otwieramy picker pusty lub z domyślną godziną

    val showTimePicker = { isStart: Boolean ->
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                // Logika aktualizacji stringa (np. "10:00-18:00")
                val parts = currentHours.split("-")
                val newTime = if (isStart) {
                    "$time-${if (parts.size > 1) parts[1] else "22:00"}"
                } else {
                    "${if (parts.isNotEmpty()) parts[0] else "10:00"}-$time"
                }
                onHoursChanged(newTime)
            },
            hour,
            minute,
            true // 24h format
        ).show()
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(day.replaceFirstChar { it.uppercase() }, modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            val parts = currentHours.split("-")
            val start = if(parts.isNotEmpty()) parts[0] else "--:--"
            val end = if(parts.size > 1) parts[1] else "--:--"

            // Przycisk Godzina Od
            OutlinedButton(onClick = { showTimePicker(true) }) {
                Text(start)
            }
            Text(" - ", modifier = Modifier.padding(horizontal = 4.dp))
            // Przycisk Godzina Do
            OutlinedButton(onClick = { showTimePicker(false) }) {
                Text(end)
            }
        }
    }
}

// Managing tables screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerTablesScreen(navController: NavHostController, restaurantId: Long) {
    val context = LocalContext.current
    var tables by remember { mutableStateOf<List<TableDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Adding table form
    var newCapacity by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        tables = fetchTables(context, restaurantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzaj Stolikami") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Adding form
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newCapacity, onValueChange = { newCapacity = it }, label = { Text("Ilość osób") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = newLocation, onValueChange = { newLocation = it }, label = { Text("Lokalizacja") }, modifier = Modifier.weight(2f))
            }
            Button(
                onClick = {
                    scope.launch {
                        val newTable = TableDto(restaurantId = restaurantId, tableCapacity = newCapacity.toIntOrNull() ?: 2, locationInRestaurant = newLocation)
                        addTable(context, newTable)
                        tables = fetchTables(context, restaurantId) // Refresh
                        newCapacity = ""; newLocation = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text("Dodaj stolik")
            }

            Divider(Modifier.padding(vertical = 16.dp))

            // Tables list
            LazyColumn {
                items(tables) { table ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ID: ${table.id}", fontWeight = FontWeight.Bold)
                            Text("Miejsca: ${table.tableCapacity} | ${table.locationInRestaurant}")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                deleteTable(context, table.id!!)
                                tables = fetchTables(context, restaurantId)
                            }
                        }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


// Reservations screen (Viewing and deleting)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReservationsScreen(navController: NavHostController) {
    val context = LocalContext.current
    var reservations by remember { mutableStateOf<List<OwnerReservationDto>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        reservations = fetchOwnerReservations(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rezerwacje") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(reservations) { res ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Rezerwacja #${res.id}", fontWeight = FontWeight.Bold)
                            // Data formatting (simple approach)
                            Text(res.reservationDatetime.replace("T", " "), color = RSRed, fontWeight = FontWeight.Bold)
                        }
                        Text("Stolik ID: ${res.tableId}")
                        Text("Czas: ${res.durationMinutes} min")
                        Text("Status: ${res.status}")

                        Button(
                            onClick = {
                                scope.launch {
                                    cancelOwnerReservation(context, res.id)
                                    reservations = fetchOwnerReservations(context) // refresh
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Anuluj / Usuń")
                        }
                    }
                }
            }
        }
    }
}


// Helper functions (API CALLS) ---

suspend fun fetchOwnerRestaurants(context: Context, ownerId: Long): List<OwnerRestaurantDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.ownerApi.getMyRestaurants("Bearer $token", ownerId)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
}

suspend fun saveRestaurant(context: Context, restaurant: OwnerRestaurantDto, isNew: Boolean) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        if (isNew) RetrofitClient.ownerApi.addRestaurant("Bearer $token", restaurant)
        else RetrofitClient.ownerApi.updateRestaurant("Bearer $token", restaurant.id!!, restaurant)
    } catch (e: Exception) { Log.e("Owner", "Error saving", e) }
}

suspend fun deleteRestaurant(context: Context, id: Long) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.deleteRestaurant("Bearer $token", id)
    } catch (e: Exception) {}
}

suspend fun fetchTables(context: Context, restaurantId: Long): List<TableDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        // TODO Need to check if there is getTablesByRestaurant endpoint and change if needed
        val response = RetrofitClient.ownerApi.getTablesByRestaurant("Bearer $token", restaurantId)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
}

suspend fun addTable(context: Context, table: TableDto) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.addTable("Bearer $token", table)
    } catch (e: Exception) {}
}

suspend fun deleteTable(context: Context, id: Long) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.deleteTable("Bearer $token", id)
    } catch (e: Exception) {}
}

suspend fun fetchOwnerReservations(context: Context): List<OwnerReservationDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.ownerApi.getOwnerUpcomingReservations("Bearer $token")
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
}

suspend fun cancelOwnerReservation(context: Context, id: Long) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.cancelReservation("Bearer $token", id)
    } catch (e: Exception) {}
}