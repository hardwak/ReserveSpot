//package com.pwr_zpi.reservespotapp
//
//
//import android.app.TimePickerDialog
//import android.content.Context
//import android.net.Uri
//import android.util.Log
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.AddPhotoAlternate
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Event
//import androidx.compose.material.icons.filled.LocationOn
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Divider
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.TabRowDefaults
//import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.MutableLiveData
//import androidx.navigation.NavHostController
//import coil.compose.AsyncImage
//import com.pwr_zpi.reservespotapp.data.DataStoreManager
//import com.pwr_zpi.reservespotapp.ui.theme.RSRed
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.Calendar
//
//
//
//// Main screen for the owner (restaurants list)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OwnerDashboardScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    var restaurants by remember { mutableStateOf<List<OwnerRestaurantDto>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        isLoading = true
//        // TODO: Pobrac prawdziwe ID właściciela z DataStore lub Tokena
//        val ownerId = 1L
//        restaurants = fetchOwnerRestaurants(context, ownerId)
//        isLoading = false
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Your restaurants") },
//                actions = {
//                    IconButton(onClick = { navController.navigate("ownerReservations") }) {
//                        Icon(Icons.Default.Event, contentDescription = "Reservations")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { navController.navigate("ownerEditRestaurant/new") },
//                containerColor = RSRed,
//                contentColor = Color.White
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add")
//            }
//        }
//    ) { padding ->
//        if (isLoading) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator(color = RSRed)
//            }
//        } else {
//            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
//                items(restaurants) { restaurant ->
//                    OwnerRestaurantCard(
//                        restaurant = restaurant,
//                        onClick = { navController.navigate("ownerEditRestaurant/${restaurant.id}") },
//                        onManageTables = { navController.navigate("ownerTables/${restaurant.id}") }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun OwnerRestaurantCard(
//    restaurant: OwnerRestaurantDto,
//    onClick: () -> Unit,
//    onManageTables: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
//            .clickable { onClick() },
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(restaurant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//            Text("${restaurant.address}, ${restaurant.city}", color = Color.Gray)
//            Spacer(Modifier.height(8.dp))
//
//            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                Text("Rating: ${restaurant.averageRating ?: 0.0}", color = RSRed, fontWeight = FontWeight.Bold)
//                Button(
//                    onClick = onManageTables,
//                    colors = ButtonDefaults.buttonColors(containerColor = RSRed),
//                    modifier = Modifier.height(36.dp)
//                ) {
//                    Text("Tables", fontSize = 12.sp)
//                }
//            }
//        }
//    }
//}
//
//// Edition / Adding restaurant screen
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OwnerEditRestaurantScreen(navController: NavHostController, restaurantIdString: String) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val isNew = restaurantIdString == "new"
//
//    // form fields
//    var name by remember { mutableStateOf("") }
//    var address by remember { mutableStateOf("") }
//    var city by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    val openingHours = remember { mutableStateMapOf(
//        "monday" to "10:00-22:00",
//        "tuesday" to "10:00-22:00",
//        "wednesday" to "10:00-22:00",
//        "thursday" to "10:00-22:00",
//        "friday" to "10:00-23:00",
//        "saturday" to "12:00-23:00",
//        "sunday" to "12:00-22:00"
//    ) }
//
//    var latitude by remember { mutableStateOf("") }
//    var longitude by remember { mutableStateOf("") }
//
//    val currentBackStackEntry = navController.currentBackStackEntry
//    val savedStateHandle = currentBackStackEntry?.savedStateHandle
//
//
//    val latLiveData = remember { savedStateHandle?.getLiveData<Double>("picked_lat") ?: MutableLiveData<Double>() }
//    val lngLiveData = remember { savedStateHandle?.getLiveData<Double>("picked_lng") ?: MutableLiveData<Double>() }
//
//    val pickedLat by latLiveData.observeAsState()
//    val pickedLng by lngLiveData.observeAsState()
//
//    LaunchedEffect(pickedLat, pickedLng) {
//        pickedLat?.let { latitude = it.toString() }
//        pickedLng?.let { longitude = it.toString() }
//    }
//
//    // Choosing photo
//    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
//        selectedImageUri = uri
//    }
//
//    // if editing need to fetch restaurants data
//    // TODO LaunchedEffect should download data by ID
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (isNew) "Add restaurant" else "Edit restaurant") },
//                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
//                actions = {
//                    if (!isNew) {
//                        IconButton(onClick = {
//                            scope.launch {
//                                deleteRestaurant(context, restaurantIdString.toLong())
//                                navController.popBackStack()
//                            }
//                        }) {
//                            Icon(Icons.Default.Delete, null, tint = RSRed)
//                        }
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .padding(16.dp)
//                .verticalScroll(rememberScrollState())
//        ) {
////            Basic info
//            Text("Basic information", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
//
//            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
//            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
//            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
//            OutlinedTextField(
//                value = description,
//                onValueChange = { description = it },
//                label = { Text("Description") },
//                modifier = Modifier.fillMaxWidth().height(150.dp),
//                maxLines = 5
//            )
//            Spacer(Modifier.height(24.dp))
//
//            Text("Location on map", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
//
//
//            OutlinedButton(
//                onClick = {
//
//                    val latArg = latitude.toDoubleOrNull() ?: 0.0
//                    val lngArg = longitude.toDoubleOrNull() ?: 0.0
//                    navController.navigate("pickLocation?lat=$latArg&lng=$lngArg")
//                },
//                modifier = Modifier.fillMaxWidth(),
//                border = androidx.compose.foundation.BorderStroke(1.dp, RSRed),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = RSRed)
//            ) {
//                Icon(Icons.Default.LocationOn, contentDescription = null)
//                Spacer(Modifier.width(8.dp))
//                Text("Select location on map")
//            }
//
//            Spacer(Modifier.height(8.dp))
//
//
//            Spacer(Modifier.height(24.dp))
//
//            // Photo section
//            Text("Restaurant photo", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
//
//            if (selectedImageUri != null) {
//                AsyncImage(
//                    model = selectedImageUri,
//                    contentDescription = "Selected Image",
//                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
//                )
//                Spacer(Modifier.height(8.dp))
//            }
//
//            OutlinedButton(
//                onClick = { launcher.launch("image/*") },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
//                Spacer(Modifier.width(8.dp))
//                Text("Select a photo from the gallery")
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            // Opening hours section
//
//            Text("Opening hours", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
//
//            // Hours editor for each day
//            val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
//            days.forEach { day ->
//                OpeningHoursRow(day, openingHours[day] ?: "Closed") { newHours ->
//                    openingHours[day] = newHours
//                }
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            // Save button
//            Button(
//                onClick = {
//                    scope.launch {
//                        // Conversion lat, long to Double
//                        val latVal = latitude.toDoubleOrNull()
//                        val lonVal = longitude.toDoubleOrNull()
//
//                        // Photo upload (TODO: Zaimplementuj upload i pobierz URL)
//                        val imageUrl = selectedImageUri?.toString() // Temp URI as String
//
//                        val restaurant = OwnerRestaurantDto(
//                            id = if (isNew) null else restaurantIdString.toLong(),
//                            ownerId = 1L, // TODO: Get real owner ID
//                            name = name, address = address, city = city,
//                            description = description,
//                            openingHours = openingHours.toMap(), // Conversion to Map
//                            latitude = latVal,
//                            longitude = lonVal,
//                            pic = imageUrl
//                        )
//                        saveRestaurant(context, restaurant, isNew = false)
//                        navController.popBackStack()
//                    }
//                },
//                modifier = Modifier.fillMaxWidth().height(50.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
//            ) {
//                Text("Save")
//            }
//        }
//    }
//}
//
//
//@Composable
//fun OpeningHoursRow(day: String, currentHours: String, onHoursChanged: (String) -> Unit) {
//    val context = LocalContext.current
//
//
//
//    val showTimePicker = { isStart: Boolean ->
//        val calendar = Calendar.getInstance()
//        val hour = calendar.get(Calendar.HOUR_OF_DAY)
//        val minute = calendar.get(Calendar.MINUTE)
//
//        TimePickerDialog(
//            context,
//            { _, selectedHour, selectedMinute ->
//                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
//                val parts = currentHours.split("-")
//                val newTime = if (isStart) {
//                    "$time-${if (parts.size > 1) parts[1] else "22:00"}"
//                } else {
//                    "${if (parts.isNotEmpty()) parts[0] else "10:00"}-$time"
//                }
//                onHoursChanged(newTime)
//            },
//            hour,
//            minute,
//            true // 24h format
//        ).show()
//    }
//
//    Row(
//        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(day.replaceFirstChar { it.uppercase() }, modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold)
//
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            val parts = currentHours.split("-")
//            val start = if(parts.isNotEmpty()) parts[0] else "--:--"
//            val end = if(parts.size > 1) parts[1] else "--:--"
//
//            // Button hour from
//            OutlinedButton(onClick = { showTimePicker(true) }) {
//                Text(start)
//            }
//            Text(" - ", modifier = Modifier.padding(horizontal = 4.dp))
//            // Button hour to
//            OutlinedButton(onClick = { showTimePicker(false) }) {
//                Text(end)
//            }
//        }
//    }
//}
//
//// Managing tables screen
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OwnerTablesScreen(navController: NavHostController, restaurantId: Long) {
//    val context = LocalContext.current
//    var tables by remember { mutableStateOf<List<TableDto>>(emptyList()) }
//    val scope = rememberCoroutineScope()
//
//    // Adding table form
//    var newCapacity by remember { mutableStateOf("") }
//    var newLocation by remember { mutableStateOf("") }
//
//    LaunchedEffect(Unit) {
//        tables = fetchTables(context, restaurantId)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Manage tables") },
//                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
//            )
//        }
//    ) { padding ->
//        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
//            // Adding form
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                OutlinedTextField(value = newCapacity, onValueChange = { newCapacity = it }, label = { Text("Number of people") }, modifier = Modifier.weight(1f))
//                Spacer(Modifier.width(8.dp))
//                OutlinedTextField(value = newLocation, onValueChange = { newLocation = it }, label = { Text("Loacation") }, modifier = Modifier.weight(2f))
//            }
//            Button(
//                onClick = {
//                    scope.launch {
//                        val newTable = TableDto(restaurantId = restaurantId, tableCapacity = newCapacity.toIntOrNull() ?: 2, locationInRestaurant = newLocation)
//                        addTable(context, newTable)
//                        tables = fetchTables(context, restaurantId) // Refresh
//                        newCapacity = ""; newLocation = ""
//                    }
//                },
//                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
//            ) {
//                Text("Add table")
//            }
//
//            Divider(Modifier.padding(vertical = 16.dp))
//
//            // Tables list
//            LazyColumn {
//                items(tables) { table ->
//                    Row(
//                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).padding(16.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column {
//                            Text("ID: ${table.id}", fontWeight = FontWeight.Bold)
//                            Text("Places: ${table.tableCapacity} | ${table.locationInRestaurant}")
//                        }
//                        IconButton(onClick = {
//                            scope.launch {
//                                deleteTable(context, table.id!!)
//                                tables = fetchTables(context, restaurantId)
//                            }
//                        }) {
//                            Icon(Icons.Default.Delete, null, tint = Color.Gray)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//// Reservations screen (Viewing and deleting)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OwnerReservationsScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    var reservations by remember { mutableStateOf<List<OwnerReservationDto>>(emptyList()) }
//    val scope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) {
//        reservations = fetchOwnerReservations(context)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Reservations") },
//                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
//            )
//        }
//    ) { padding ->
//        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
//            items(reservations) { res ->
//                Card(
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(2.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                            Text("Reservation #${res.id}", fontWeight = FontWeight.Bold)
//                            // Data formatting (simple approach)
//                            Text(res.reservationDatetime.replace("T", " "), color = RSRed, fontWeight = FontWeight.Bold)
//                        }
//                        Text("Table ID: ${res.tableId}")
//                        Text("Time: ${res.durationMinutes} min")
//                        Text("Status: ${res.status}")
//
//                        Button(
//                            onClick = {
//                                scope.launch {
//                                    cancelOwnerReservation(context, res.id)
//                                    reservations = fetchOwnerReservations(context) // refresh
//                                }
//                            },
//                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
//                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
//                        ) {
//                            Text("Cancel / Delete")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OwnerRestaurantListScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    val ownerId = 1L // TODO: Zmień na faktyczne ID właściciela pobrane z DataStore lub tokena
//    var restaurants by remember { mutableStateOf<List<OwnerRestaurantDto>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(ownerId) {
//        isLoading = true
////        restaurants = fetchMyRestaurants(context, ownerId)
//        isLoading = false
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("Moje Restauracje", fontWeight = FontWeight.Bold) })
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { /* TODO: Implementuj dodawanie nowej restauracji */ }) {
//                Icon(Icons.Filled.Add, contentDescription = "Dodaj Restaurację")
//            }
//        }
//    ) { paddingValues ->
//        if (isLoading) {
//            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else if (restaurants.isEmpty()) {
//            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                Text("Brak dodanych restauracji.", color = Color.Gray)
//            }
//        } else {
//            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
//                items(restaurants) { restaurant ->
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 8.dp)
//                            .clickable {
//                                navController.navigate("owner/restaurant/${restaurant.id}")
//                            },
//                        shape = RoundedCornerShape(8.dp)
//                    ) {
//                        Column(Modifier.padding(16.dp)) {
//                            Text(restaurant.name, style = MaterialTheme.typography.titleLarge)
//                            Text("Adres: ${restaurant.address}, ${restaurant.city}", color = Color.Gray)
//                            Text("Ocena: ${String.format("%.1f", restaurant.averageRating)}", color = RSRed)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun OwnerRestaurantDetailsScreen(navController: NavHostController, restaurantId: Long) {
//    val context = LocalContext.current
//    var selectedTabIndex by remember { mutableStateOf(0) }
//    val tabs = listOf("Rezerwacje", "Opinie", "Edycja")
//
//
//    var restaurantDetails by remember { mutableStateOf<OwnerRestaurantDto?>(null) }
//    var isLoadingDetails by remember { mutableStateOf(true) }
//
//
//    var reservations by remember { mutableStateOf<List<OwnerReservationDto>>(emptyList()) }
//    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
//
//
//
//    LaunchedEffect(restaurantId) {
//        isLoadingDetails = true
////        restaurantDetails = fetchRestaurantDetailsForOwner(context, restaurantId)
//        isLoadingDetails = false
//    }
//
//
//    LaunchedEffect(restaurantId, selectedTabIndex) {
//        when (selectedTabIndex) {
//            0 -> reservations = fetchOwnerReservations(context)
//            1 -> reviews = fetchOwnerReviews(context, restaurantId)
//        }
//    }
//
//    if (isLoadingDetails) {
//        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
//        return
//    }
//
//    val restaurant = restaurantDetails ?: run {
//        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nie znaleziono restauracji.", color = Color.Red) }
//        return
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(restaurant.name, maxLines = 1) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
//
//
//            TabRow(
//                selectedTabIndex = selectedTabIndex,
//                modifier = Modifier.fillMaxWidth(),
//                containerColor = Color.White,
//                indicator = { tabPositions ->
//                    TabRowDefaults.Indicator(
//                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
//                        color = RSRed
//                    )
//                }
//            ) {
//                tabs.forEachIndexed { index, title ->
//                    Tab(
//                        selected = selectedTabIndex == index,
//                        onClick = { selectedTabIndex = index },
//                        text = { Text(title) },
//                        selectedContentColor = RSRed,
//                        unselectedContentColor = Color.Gray
//                    )
//                }
//            }
//
//            // Zawartość zakładek
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .padding(16.dp)
//            ) {
//                when (selectedTabIndex) {
//                    0 -> ReservationsManagementTab(reservations, restaurantId, context)
//                    1 -> ReviewsViewTab(reviews)
//                    2 -> EditRestaurantTab(restaurant, context)
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ReservationsManagementTab(allReservations: List<OwnerReservationDto>, currentRestaurantId: Long, context: Context) {
//
//    val filteredReservations = allReservations.filter {
//
//        true
//    }
//
//    if (filteredReservations.isEmpty()) {
//        Text("Brak nadchodzących rezerwacji.")
//    } else {
//        LazyColumn {
//            items(filteredReservations) { reservation ->
//                // TODO: Użyj komponentu OwnerReservationCard
//                Text("Rezerwacja ${reservation.id} - Status: ${reservation.status}", modifier = Modifier.padding(vertical = 4.dp))
//
//                Button(onClick = { /* TODO: Implementuj potwierdzenie/anulowanie */ }) { Text("Zarządzaj") }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ReviewsViewTab(reviews: List<ReviewDto>) {
//    if (reviews.isEmpty()) {
//        Text("Brak opinii dla tej restauracji.")
//    } else {
//        LazyColumn {
//            items(reviews) { review ->
//                // TODO: Użyj komponentu ReviewCard
//                Column(Modifier.padding(vertical = 8.dp).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(8.dp)) {
//                    Text("Użytkownik: ${review.userName} (${review.rating} Gwiazdki)", fontWeight = FontWeight.Bold)
//                    Text(review.comment)
//                    Text("Data: ${review.date}", fontSize = 12.sp, color = Color.Gray)
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun EditRestaurantTab(restaurant: OwnerRestaurantDto, context: Context) {
//    val scope = rememberCoroutineScope()
//    val isNew = restaurant.id == null
//
//
//    var name by remember(restaurant.name) { mutableStateOf(restaurant.name) }
//    var address by remember(restaurant.address) { mutableStateOf(restaurant.address) }
//    var city by remember(restaurant.city) { mutableStateOf(restaurant.city) }
//    var description by remember(restaurant.description) { mutableStateOf(restaurant.description) }
//
//
//    val openingHours = remember(restaurant.openingHours) {
//        mutableStateMapOf<String, String>().apply { putAll(restaurant.openingHours) }
//    }
//
//    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
//        Text("Edytuj Dane Restauracji", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
//
//
//        OutlinedTextField(
//            value = name,
//            onValueChange = { name = it },
//            label = { Text("Nazwa Restauracji") },
//            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
//        )
//
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Opis") },
//            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
//            minLines = 3
//        )
//
//        Button(
//            onClick = {
//                // TODO: Użyj OwnerApi.updateRestaurant
//
//                val updatedDto = restaurant.copy(
//                    name = name,
//                    description = description,
//                    address = address
//                )
//
//                Log.d("Edit", "Próba zapisu: $updatedDto")
//            },
//            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
//        ) {
//            Text("Zapisz Zmiany")
//        }
//    }
//}
//
//
//// Helper functions (API CALLS) ---
//
//suspend fun fetchOwnerRestaurants(context: Context, ownerId: Long): List<OwnerRestaurantDto> = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
//        val response = RetrofitClient.ownerApi.getMyRestaurants("Bearer $token", ownerId)
//        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
//    } catch (e: Exception) { Log.e("OwnerScreens", "Błąd fetchOwnerRestaurants", e); emptyList() }
//}
//
//suspend fun saveRestaurant(context: Context, restaurant: OwnerRestaurantDto, isNew: Boolean) = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
//        if (isNew) RetrofitClient.ownerApi.addRestaurant("Bearer $token", restaurant)
//        else RetrofitClient.ownerApi.updateRestaurant("Bearer $token", restaurant.id!!, restaurant)
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error saving", e) }
//}
//
//suspend fun deleteRestaurant(context: Context, id: Long) = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
//        RetrofitClient.ownerApi.deleteRestaurant("Bearer $token", id)
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error deleting", e) }
//}
//
//suspend fun fetchTables(context: Context, restaurantId: Long): List<TableDto> = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
//        val response = RetrofitClient.ownerApi.getTablesByRestaurant("Bearer $token", restaurantId)
//        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error fetchTables", e); emptyList() }
//}
//
//suspend fun addTable(context: Context, table: TableDto) = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
//        RetrofitClient.ownerApi.addTable("Bearer $token", table)
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error addTable", e) }
//}
//
//suspend fun deleteTable(context: Context, id: Long) = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
//        RetrofitClient.ownerApi.deleteTable("Bearer $token", id)
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error deleteTable", e) }
//}
//
//suspend fun fetchOwnerReservations(context: Context): List<OwnerReservationDto> = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
//        val response = RetrofitClient.ownerApi.getOwnerUpcomingReservations("Bearer $token")
//        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error fetchOwnerReservations", e); emptyList() }
//}
//
//suspend fun cancelOwnerReservation(context: Context, id: Long) = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
//        RetrofitClient.ownerApi.cancelReservation("Bearer $token", id)
//    } catch (e: Exception) { Log.e("OwnerScreens", "Error cancelReservation", e) }
//}
//
//suspend fun fetchOwnerReviews(context: Context, restaurantId: Long): List<ReviewDto> = withContext(Dispatchers.IO) {
//    try {
//        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
//        val response = RetrofitClient.ownerApi.getRestaurantReviews("Bearer $token", restaurantId)
//        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
//    } catch (e: Exception) {
//        Log.e("OwnerScreens", "Błąd pobierania opinii: ${e.message}")
//        emptyList()
//    }
//}
//
////suspend fun fetchRestaurantDetailsForOwner(context: Context, restaurantId: Long): OwnerRestaurantDto? = withContext(Dispatchers.IO) {
////    try {
////        val token = DataStoreManager(context).getBackendToken() ?: return@withContext null
////        val response = RetrofitClient.ownerApi.getRestaurantDetailsForOwner("Bearer $token", restaurantId)
////        if (response.isSuccessful) response.body() else null
////    } catch (e: Exception) {
////        Log.e("OwnerScreens", "Błąd pobierania detali restauracji: ${e.message}")
////        null
////    }
////}