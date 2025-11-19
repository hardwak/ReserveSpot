package com.pwr_zpi.reservespotapp

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    var openingHours by remember { mutableStateOf("") }

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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Miasto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = openingHours, onValueChange = { openingHours = it }, label = { Text("Godziny (np. 10-22)") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val restaurant = OwnerRestaurantDto(
                            id = if (isNew) null else restaurantIdString.toLong(),
                            ownerId = 1L, // TODO: Get real owner ID
                            name = name, address = address, city = city,
                            description = description, openingHours = openingHours
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
            // Formularz dodawania
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
                        tables = fetchTables(context, restaurantId) // Odśwież
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