package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRestaurantScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // NOWE: pola lokalizacji
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    // odbiór lokalizacji z LocationPicker
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Double>("picked_lat")
            ?.observeForever { lat ->
                latitude = lat.toString()
            }

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Double>("picked_lng")
            ?.observeForever { lng ->
                longitude = lng.toString()
            }
    }

    val openingHours = remember {
        mutableStateMapOf(
            "monday" to "10:00-22:00",
            "tuesday" to "10:00-22:00",
            "wednesday" to "10:00-22:00",
            "thursday" to "10:00-22:00",
            "friday" to "10:00-23:00",
            "saturday" to "12:00-23:00",
            "sunday" to "12:00-22:00"
        )
    }
    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Restaurant") },
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
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text("Basic Information", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RSRed)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Street and Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.height(24.dp))
            Text("Location", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RSRed)
            Text("Coordinates are needed for map display.", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)


            Spacer(Modifier.height(8.dp))

            // NEW BUTTON – opens map
            Button(
                onClick = {
                    val lat = latitude.toFloatOrNull() ?: 0f
                    val lng = longitude.toFloatOrNull() ?: 0f
                    navController.navigate("pickLocation?lat=$lat&lng=$lng")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text("Select on Map")
            }

            Spacer(Modifier.height(24.dp))
            Text("Opening Hours", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RSRed)
            Spacer(Modifier.height(8.dp))

            days.forEach { day ->
                OpeningHoursRow(day, openingHours[day] ?: "Closed") { newHours ->
                    openingHours[day] = newHours
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isBlank() || address.isBlank() || city.isBlank()) {
                        Toast.makeText(context, "Fill in the name, address, and city", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    scope.launch {
                        val gson = Gson()
                        val openingHoursJson = gson.toJson(openingHours.toMap())

                        val lat = latitude.toDoubleOrNull()
                        val lng = longitude.toDoubleOrNull()

                        val dto = CreateRestaurantDto(
                            name = name,
                            address = address,
                            city = city,
                            description = description,
                            openingHours = openingHoursJson,
                            latitude = lat,
                            longitude = lng
                        )

                        val success = createNewRestaurant(context, dto)
                        isSaving = false

                        if (success) {
                            Toast.makeText(context, "Restaurant created!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Error creating restaurant", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text("Create Restaurant", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Note: You will be able to add photos in the restaurant details after creation.",
                fontSize = 12.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

suspend fun createNewRestaurant(context: Context, dto: CreateRestaurantDto): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val token = DataStoreManager(context).getBackendToken() ?: return@withContext false
            val response = RetrofitClient.ownerApi.createRestaurant("Bearer $token", dto)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AddRestaurant", "Error", e)
            false
        }
    }