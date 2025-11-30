package com.pwr_zpi.reservespotapp

import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import com.pwr_zpi.reservespotapp.RetrofitClient // Umożliwia dostęp do RetrofitClient.userApi
import com.pwr_zpi.reservespotapp.AccountUserDto // Umożliwia dostęp do userResponse.body()
import com.pwr_zpi.reservespotapp.RestaurantDto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRestaurantListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dataStore = DataStoreManager(context)

    var restaurants by remember { mutableStateOf<List<RestaurantDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val token = dataStore.getBackendToken()
        if (token != null) {
            val userResponse = RetrofitClient.userApi.getMyDetails("Bearer $token")
            if (userResponse.isSuccessful && userResponse.body() != null) {
                val ownerId = userResponse.body()!!.id!!
                restaurants = fetchOwnerRestaurants(context, ownerId)
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Restaurants", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("ownerAddRestaurant") },
                containerColor = RSRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },

        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(16.dp)
            ) {
                FilledTonalButton(
                    onClick = { navController.navigate("ownerAccount") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit personal info")
                }
            }
        }


    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RSRed)
            }
        } else if (restaurants.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("You don't have any restaurants yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                items(restaurants) { restaurant ->
                    OwnerRestaurantCard(
                        restaurant = restaurant,
                        onClick = { navController.navigate("owner/restaurant/${restaurant.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerRestaurantCard(restaurant: RestaurantDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(restaurant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("${restaurant.address}, ${restaurant.city}", color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = RSRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("${restaurant.averageRating ?: 0.0}", color = RSRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRestaurantDetailsScreen(navController: NavHostController, restaurantId: Long) {
    val context = LocalContext.current

    var restaurant by remember { mutableStateOf<RestaurantDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Edit", "Tables", "Reservations", "Reviews", "Photos")

    LaunchedEffect(restaurantId) {
        isLoading = true
        restaurant = fetchOwnerRestaurantDetails(context, restaurantId)
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RSRed) }
        return
    }

    if (restaurant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error fetching data.") }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(restaurant!!.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = RSRed,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = RSRed
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, color = if (selectedTabIndex == index) RSRed else Color.Gray) },
                        icon = {
                            when(index) {
                                0 -> Icon(Icons.Default.Edit, null)
                                1 -> Icon(Icons.Default.TableRestaurant, null)
                                2 -> Icon(Icons.Default.Event, null)
                                3 -> Icon(Icons.Default.Star, null)
                                4 -> Icon(Icons.Default.PhotoLibrary, null)
                            }
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> EditRestaurantTab(restaurant!!, context) { updated -> restaurant = updated }
                1 -> ManageTablesTab(restaurantId, context)
                2 -> OwnerReservationsTab(context)
                3 -> OwnerReviewsTab(restaurantId, context)
                4 -> OwnerPhotosTab(restaurantId, context)
            }
        }
    }
}

@Composable
fun EditRestaurantTab(restaurant: RestaurantDto, context: Context, onUpdateSuccess: (RestaurantDto) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(restaurant.name) }
    var address by remember { mutableStateOf(restaurant.address) }
    var city by remember { mutableStateOf(restaurant.city) }
    var description by remember { mutableStateOf(restaurant.description) }
    val openingHours = remember { mutableStateMapOf<String, String>().apply { putAll(restaurant.openingHours) } }
    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Basic data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Spacer(Modifier.height(24.dp))
        Text("Opening hours", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        days.forEach { day ->
            OpeningHoursRow(day, openingHours[day] ?: "Closed") { newHours -> openingHours[day] = newHours }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                scope.launch {
                    val gson = Gson()
                    val openingHoursJson = gson.toJson(openingHours.toMap())
                    val updateDto = UpdateRestaurantDto(
                        name = name, address = address, city = city, description = description,
                        openingHours = openingHoursJson, latitude = restaurant.latitude,
                        longitude = restaurant.longitude, pic = restaurant.pic
                    )
                    val updatedRestaurant = updateRestaurant(context, restaurant.id, updateDto)
                    if (updatedRestaurant != null) {
                        onUpdateSuccess(updatedRestaurant)
                        Toast.makeText(context, "Changes saved!", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Save error.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) {
            Text("Save changes")
        }
    }
}

@Composable
fun OpeningHoursRow(day: String, currentHours: String, onHoursChanged: (String) -> Unit) {
    val context = LocalContext.current
    val showTimePicker = { isStart: Boolean ->
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            val parts = currentHours.split("-")
            val newTime = if (isStart) "$time-${if (parts.size > 1) parts[1] else "22:00"}" else "${if (parts.isNotEmpty()) parts[0] else "10:00"}-$time"
            onHoursChanged(newTime)
        }, hour, minute, true).show()
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(day.replaceFirstChar { it.uppercase() }, modifier = Modifier.width(100.dp))
        Row {
            val parts = currentHours.split("-")
            OutlinedButton(onClick = { showTimePicker(true) }, modifier = Modifier.height(35.dp)) { Text(if(parts.isNotEmpty()) parts[0] else "Closed", fontSize = 12.sp) }
            Text("-", modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterVertically))
            OutlinedButton(onClick = { showTimePicker(false) }, modifier = Modifier.height(35.dp)) { Text(if(parts.size > 1) parts[1] else "Closed", fontSize = 12.sp) }
        }
    }
}

@Composable
fun ManageTablesTab(restaurantId: Long, context: Context) {
    var tables by remember { mutableStateOf<List<RestaurantTableDto>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var newCapacity by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("") }

    LaunchedEffect(restaurantId) { tables = fetchTables(context, restaurantId) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add table", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            OutlinedTextField(value = newCapacity, onValueChange = { newCapacity = it }, label = { Text("Capacity") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value = newLocation, onValueChange = { newLocation = it }, label = { Text("Location") }, modifier = Modifier.weight(2f))
        }
        Button(
            onClick = {
                scope.launch {
                    val dto = CreateRestaurantTableDto(restaurantId, tables.size + 1, newCapacity.toIntOrNull() ?: 2, newLocation)
                    addTable(context, dto)
                    tables = fetchTables(context, restaurantId)
                    newCapacity = ""; newLocation = ""
                    Toast.makeText(context, "Table added!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) { Text("Add table") }
        Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
        Text("Your tables", fontWeight = FontWeight.Bold)
        LazyColumn {
            items(tables) { table ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("ID: ${table.id} | Nr: ${table.tableNumber ?: "?"}"); Text("Capacity: ${table.capacity} | ${table.locationInRestaurant}") }
                    IconButton(onClick = { scope.launch { deleteTable(context, table.id); tables = fetchTables(context, restaurantId) } }) { Icon(Icons.Default.Delete, "Delete", tint = Color.Gray) }
                }
            }
        }
    }
}

@Composable
fun OwnerReservationsTab(context: Context) {
    var reservations by remember { mutableStateOf<List<OwnerReservationDto>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { reservations = fetchOwnerReservations(context); isLoading = false }

//    LaunchedEffect(restaurantId) {
//        isLoading = true
//        // Używamy zaktualizowanej funkcji fetchOwnerReservations
//        reservations = fetchOwnerReservations(context, restaurantId)
//        isLoading = false
//    }

    if (isLoading) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (reservations.isEmpty()) {
        Text("No upcoming reservations.", modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(reservations) { res ->
                OwnerReservationCard(reservation = res, onCancel = { id -> scope.launch { cancelOwnerReservation(context, id); reservations = fetchOwnerReservations(context); Toast.makeText(context, "Canceled.", Toast.LENGTH_SHORT).show() } })
            }
        }
    }
}

@Composable
fun OwnerReviewsTab(restaurantId: Long, context: Context) {
    var reviews by remember { mutableStateOf<List<ReviewDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(restaurantId) {
        isLoading = true
        reviews = fetchRestaurantReviews(context, restaurantId)
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RSRed) }
    } else if (reviews.isEmpty()) {
        Text("This restaurant has no reviews yet.", modifier = Modifier.padding(16.dp), color = Color.Gray)
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(reviews) { review ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = RSRed, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${review.rating ?: 0}/5",
                                fontWeight = FontWeight.Bold,
                                color = RSRed,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = review.createdAt?.toString()?.take(10) ?: "",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (!review.comment.isNullOrEmpty()) {
                            Text(text = review.comment, fontSize = 14.sp)
                        } else {
                            Text(text = "No comment", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerPhotosTab(restaurantId: Long, context: Context) {
    var pictures by remember { mutableStateOf<List<PictureDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun refreshPictures() {
        scope.launch {
            isLoading = true
            pictures = fetchRestaurantPictures(context, restaurantId)
            isLoading = false
        }
    }

    LaunchedEffect(restaurantId) {
        refreshPictures()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isUploading = true
                val success = uploadRestaurantPicture(context, uri)
                if (success) {
                    Toast.makeText(context, "Photo added! (Refresh or check if assigned)", Toast.LENGTH_SHORT).show()
                    refreshPictures()
                } else {
                    Toast.makeText(context, "Error sending photo.", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Uploading...")
            } else {
                Icon(Icons.Default.AddPhotoAlternate, null)
                Spacer(Modifier.width(8.dp))
                Text("Add photo from gallery")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = RSRed) }
        } else if (pictures.isEmpty()) {
            Text("No photos in the gallery for this restaurant.", color = Color.Gray)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pictures) { pic ->
                    val imageUrl = pic.url?.let {
                        if (it.startsWith("http")) it else "${RetrofitClient.BASE_URL}$it"
                    }

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Restaurant photo",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

suspend fun fetchOwnerRestaurants(context: Context, ownerId: Long): List<RestaurantDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.ownerApi.getMyRestaurants("Bearer $token", ownerId)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) {
        Log.e("API", "Błąd pobierania restauracji", e)
        emptyList()
    }
}



suspend fun fetchOwnerRestaurantDetails(context: Context, id: Long): RestaurantDto? = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext null
        val response = RetrofitClient.restaurantApi.getRestaurantDetails("Bearer $token", id)
        if (response.isSuccessful) response.body() else null
    } catch (e: Exception) { null }
}

suspend fun updateRestaurant(context: Context, id: Long, dto: UpdateRestaurantDto): RestaurantDto? = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext null
        val response = RetrofitClient.ownerApi.updateRestaurant("Bearer $token", id, dto)
        if (response.isSuccessful) response.body() else null
    } catch (e: Exception) { null }
}

suspend fun fetchTables(context: Context, restaurantId: Long): List<RestaurantTableDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.ownerApi.getTablesByRestaurant("Bearer $token", restaurantId)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
}

suspend fun addTable(context: Context, dto: CreateRestaurantTableDto) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.addTable("Bearer $token", dto)
    } catch (e: Exception) { Log.e("API", "Błąd dodawania stolika", e) }
}

suspend fun deleteTable(context: Context, id: Long) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.deleteTable("Bearer $token", id)
    } catch (e: Exception) { Log.e("API", "Błąd usuwania stolika", e) }
}

suspend fun fetchOwnerReservations(context: Context): List<OwnerReservationDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.ownerApi.getOwnerUpcomingReservations("Bearer $token")
//        val response = RetrofitClient.ownerApi.getOwnerUpcomingReservationsByRestaurant("Bearer $token", restaurantId)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
}

suspend fun cancelOwnerReservation(context: Context, id: Long) = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext
        RetrofitClient.ownerApi.cancelReservation("Bearer $token", id)
    } catch (e: Exception) { Log.e("API", "Błąd anulowania", e) }
}

suspend fun fetchRestaurantReviews(context: Context, restaurantId: Long): List<ReviewDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.reviewsApi.getReviews("Bearer $token", restaurantId)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) {
        Log.e("API", "Błąd pobierania opinii", e)
        emptyList()
    }
}

suspend fun fetchRestaurantPictures(context: Context, restaurantId: Long): List<PictureDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val response = RetrofitClient.picturesApi.getPictures("Bearer $token", restaurantId)

        if (response.isSuccessful) {
            val allPictures = response.body() ?: emptyList()
            allPictures.filter { it.restaurantIds.contains(restaurantId) }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("API", "Error downloading photos", e)
        emptyList()
    }
}

suspend fun uploadRestaurantPicture(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext false
        val file = File(context.cacheDir, "upload_image.jpg")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = RetrofitClient.picturesApi.uploadPicture("Bearer $token", body)
        response.isSuccessful
    } catch (e: Exception) {
        Log.e("API", "Upload error", e)
        false
    }
}