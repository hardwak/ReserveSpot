package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


suspend fun fetchRestaurants(
    context: Context,
    searchCriteria: RestaurantSearchDto
): List<RestaurantDto> {
    return withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getBackendToken()

            if (token.isNullOrEmpty()) {
                Log.e("fetchRestaurants", "No authentication token found")
                return@withContext emptyList()
            }


            val response = RetrofitClient.restaurantApi.searchRestaurants(
                "Bearer $token",
                searchCriteria
            )

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("fetchRestaurants", "Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("fetchRestaurants", "Exception while fetching restaurants", e)
            emptyList()
        }
    }
}

suspend fun fetchAvailableTags(context: Context): List<TagDto> {
    return withContext(Dispatchers.IO) {
        try {
            val token =
                DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
            val response = RetrofitClient.restaurantApi.getAvailableTags("Bearer $token")
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            Log.e("ChooseRestaurant", "Error getting tags", e)
            emptyList()
        }
    }
}

suspend fun fetchAvailableCities(context: Context): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getBackendToken() ?: return@withContext emptyList()


            val response = RetrofitClient.restaurantApi.getAllRestaurants("Bearer $token")

            if (response.isSuccessful) {

                response.body()
                    ?.map { it.city }
                    ?.distinct()      // Distinct to prevent the duplication
                    ?: emptyList()
            } else {
                Log.e("fetchCities", "Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("fetchCities", "Exception while fetching cities", e)
            emptyList()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseRestaurantScreen(navController: NavHostController) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isGeminiSearchVisible by remember { mutableStateOf(false) }
    // field for putting prompt in it
    var geminiPrompt by remember { mutableStateOf(TextFieldValue("")) }


    var isLoading by remember { mutableStateOf(false) }
    var restaurants by remember { mutableStateOf<List<RestaurantDto>>(emptyList()) } // List of API results
    var availableCuisines by remember { mutableStateOf<List<TagDto>>(emptyList()) } // Contains ID and names
    var availableCities by remember { mutableStateOf<List<String>>(emptyList()) } // Contains city names
    var isFiltersLoading by remember { mutableStateOf(true) }

//    filter states
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedCity by remember { mutableStateOf("New York") }
    var selectedCuisines by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedRatingRange by remember { mutableStateOf(1.0f..5.0f) }

    LaunchedEffect(Unit) {
        isFiltersLoading = true
        val fetchedCuisines = fetchAvailableTags(context)
        val fetchedCities = fetchAvailableCities(context)

        availableCuisines = fetchedCuisines
        availableCities = fetchedCities

        if (fetchedCities.isNotEmpty()) {
            selectedCity = fetchedCities.first()
        }
        isFiltersLoading = false
    }

    LaunchedEffect(
        searchQuery.text,
        selectedCity,
        selectedCuisines,
        selectedRatingRange,
        isFiltersLoading
    ) {
        if (isFiltersLoading) return@LaunchedEffect

        delay(300)
        isLoading = true

        // Mapowanie wybranych nazw kuchni na ich ID (konieczne dla API)
        val selectedTagIds = availableCuisines
            .filter { selectedCuisines.contains(it.name) }
            .map { it.id }
            .toSet()


        val searchCriteria = RestaurantSearchDto(
            query = searchQuery.text.ifBlank { null },
            city = selectedCity,
            tagIds = if (selectedTagIds.isEmpty()) null else selectedTagIds,
            minRating = selectedRatingRange.start.toDouble(),
            maxRating = selectedRatingRange.endInclusive.toDouble()
        )

        restaurants = fetchRestaurants(context, searchCriteria)
        isLoading = false
    }

    fun searchWithGemini(prompt: String) {
        println("Gemini Search initiated with prompt: $prompt")

        isGeminiSearchVisible = false
//      clearing field
        geminiPrompt = TextFieldValue("")
    }

    Scaffold(

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)


        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search restaurants") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.Gray,
                        unfocusedLabelColor = Color.Gray
                    )
                )


                Spacer(modifier = Modifier.width(8.dp))


                IconButton(
                    onClick = { showFilterSheet = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filters"
                    )
                }

            }

            Button(
                onClick = {
                    isGeminiSearchVisible = !isGeminiSearchVisible
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y=(-20).dp)
                    .padding(bottom = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RSRed
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Search",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isGeminiSearchVisible) "Hide AI prompt" else "Search with Gemini AI")
            }




            if (isGeminiSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    // alignment of elements
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = geminiPrompt,
                        onValueChange = { geminiPrompt = it },
                        label = { Text("Describe what you are looking for...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = Color.DarkGray,
                            unfocusedLabelColor = Color.DarkGray
                        )
                    )

                    Button(
                        onClick = {
                            searchWithGemini(geminiPrompt.text)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RSRed
                        ),

                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Send")
                    }
                }
            }

            if (isFiltersLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RSRed)
                }

                return@Column
            }


            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RSRed)
                }
            } else if (restaurants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No restaurants meeting this criteria.")
                }
            } else {
                LazyColumn {
                    items(restaurants) { restaurant ->

                        RestaurantInfoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .clickable {

//                                    navController.navigate("restaurantDetails/${restaurant.id}/${restaurant.averageRating}")
                                    val restaurantIdToNavigate = restaurant.id
                                    navController.navigate("restaurantDetails/$restaurantIdToNavigate")
                                },
                            info = restaurant
                        )
                    }
                }
            }
        }


        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun FilterBottomSheetContent(
            selectedCity: String,
            selectedCuisines: Set<String>,
            selectedRatingRange: ClosedFloatingPointRange<Float>,
            onCityChange: (String) -> Unit,
            onCuisineToggle: (String) -> Unit,
            onRatingChange: (ClosedFloatingPointRange<Float>) -> Unit,
            onApply: () -> Unit,
            onResetFilters: () -> Unit,
            availableCities: List<String>,
            availableCuisines: List<String>
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedButton(
                    onClick = onResetFilters, //calling reset function
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RSRed),
                    border = BorderStroke(1.dp, RSRed)
                ) {
                    Text("Reset filters")
                }

                Spacer(Modifier.height(15.dp))

                // City filter
                Text("Choose a city", style = MaterialTheme.typography.titleMedium)
                availableCities.forEach { city ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCityChange(city) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (city == selectedCity),
                            onClick = { onCityChange(city) },
                            colors = RadioButtonDefaults.colors(selectedColor = RSRed)
                        )
                        Spacer(Modifier.padding(start = 15.dp))
                        Text(city)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Cuisine filter
                Text("Choose filter", style = MaterialTheme.typography.titleMedium)
                availableCuisines.forEach { cuisine ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCuisineToggle(cuisine) }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedCuisines.contains(cuisine),
                            onCheckedChange = { onCuisineToggle(cuisine) },
                            colors = CheckboxDefaults.colors(checkedColor = RSRed)
                        )
                        Spacer(Modifier.padding(start = 8.dp))
                        Text(cuisine)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Rating filter
                Text(
                    "Rating: od ${
                        String.format("%.1f", selectedRatingRange.start)
                    } do ${String.format("%.1f", selectedRatingRange.endInclusive)}",
                    style = MaterialTheme.typography.titleMedium
                )
                RangeSlider(
                    value = selectedRatingRange,
                    onValueChange = onRatingChange, // passing new value
                    valueRange = 1.0f..5.0f,
                    steps = 8, // filter steps (5-1) / 0.5 = 8
                    colors = SliderDefaults.colors(
                        thumbColor = RSRed,
                        activeTrackColor = RSRed
                    )
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RSRed)


                ) {
                    Text("Apply filters")
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState
            ) {
                // Passing states and functions to filters panel
                FilterBottomSheetContent(
                    selectedCity = selectedCity,
                    selectedCuisines = selectedCuisines,
                    selectedRatingRange = selectedRatingRange,
                    onCityChange = { selectedCity = it },
                    onCuisineToggle = { cuisine ->
                        // Checkbox logic
                        selectedCuisines = if (selectedCuisines.contains(cuisine)) {
                            selectedCuisines - cuisine
                        } else {
                            selectedCuisines + cuisine
                        }
                    },
                    onRatingChange = { selectedRatingRange = it },

                    onResetFilters = {
                        selectedCity = "New York"
                        selectedCuisines = emptySet()
                        selectedRatingRange = 1.0f..5.0f
                    },
                    onApply = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showFilterSheet = false
                            }
                        }
                    },
                    availableCities = availableCities,
                    availableCuisines = availableCuisines.map { it.name }
                )
            }
        }
    }


}




