package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch


data class Restaurant(
    val name: String,
    val address: String,
    val city: String,
    val cuisine: String,
    val rating: Float
)

val allCuisines = listOf("Italian", "Polish", "Ukrainian", "Japanese", "Vegan", "American")
val allCities = listOf("Wrocław", "Warszawa", "Kraków", "Białystok")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseRestaurantScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isGeminiSearchVisible by remember { mutableStateOf(false) }
    // field for putting prompt in it
    var geminiPrompt by remember { mutableStateOf(TextFieldValue("")) }

//    filter states
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

//    saving filter states
    var selectedCity by remember { mutableStateOf("Wrocław") }
    var selectedCuisines by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedRatingRange by remember { mutableStateOf(1.0f..5.0f) }


    fun searchWithGemini(prompt: String) {


        println("Gemini Search initiated with prompt: $prompt")

// hiding field
        isGeminiSearchVisible = false
//      clearing field
        geminiPrompt = TextFieldValue("")
    }


    val restaurants = listOf(
        Restaurant("La Bella Pizza", "ul. Wrocławska 10", "Wrocław", "Italian", 4.5f),
        Restaurant("Sushi Master", "ul. Długa 22", "Wrocław", "Japanese", 1.4f),
        Restaurant("Burger Town", "ul. Słoneczna 5", "Warszawa", "American", 2.1f),
        Restaurant("Green Garden", "ul. Polna 3", "Kraków", "Vegan", 3.8f),
        Restaurant("Puzata Chata", "ul. Ukraińska 24", "Białystok", "Ukrainian", 5.0f),
        Restaurant("Stara Pierogarnia", "ul. Rynek 5", "Wrocław", "Polish", 4.6f)
    )

    val filteredRestaurants = restaurants.filter { restaurant ->
        val matchesSearch = restaurant.name.contains(searchQuery.text, ignoreCase = true)
        val matchesCity = restaurant.city == selectedCity
        val matchesCuisine =
            selectedCuisines.isEmpty() || selectedCuisines.contains(restaurant.cuisine)
        val matchesRating = restaurant.rating in selectedRatingRange

        matchesSearch && matchesCity && matchesCuisine && matchesRating
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
                    .padding(top = 0.dp, bottom = 16.dp),
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
                    .padding(bottom = 8.dp),
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


//                uncomment for color change
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Green
//                )


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


            // Restaurant list
            LazyColumn {
                items(filteredRestaurants) { restaurant ->
                    InfoCard(
                        restaurantName = restaurant.name,
                        rating = restaurant.rating,
                        views = 350,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 8.dp, horizontal = 4.dp) //outside padding

                            .clickable {
                                navController.navigate("restaurantDetails/${restaurant.name}/${restaurant.rating}")
                            }
                    )
                }
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
                    selectedCity = "Wrocław"
                    selectedCuisines = emptySet()
                    selectedRatingRange = 1.0f..5.0f
                },
                onApply = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showFilterSheet = false
                        }
                    }
                }
            )
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
    onResetFilters: () -> Unit
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
        Text("Wybierz miasto", style = MaterialTheme.typography.titleMedium)
        allCities.forEach { city ->
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
        Text("Choose cuisine", style = MaterialTheme.typography.titleMedium)
        allCuisines.forEach { cuisine ->
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
                String.format(
                    "%.1f",
                    selectedRatingRange.start
                )
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


