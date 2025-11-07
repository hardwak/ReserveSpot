package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


data class Restaurant(
    val name: String,
    val address: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseRestaurantScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isGeminiSearchVisible by remember { mutableStateOf(false) }
    // field for putting prompt in it
    var geminiPrompt by remember { mutableStateOf(TextFieldValue("")) }


    val restaurants = listOf(
        Restaurant("La Bella Pizza", "ul. Wrocławska 10"),
        Restaurant("Sushi Master", "ul. Długa 22"),
        Restaurant("Burger Town", "ul. Słoneczna 5"),
        Restaurant("Green Garden", "ul. Polna 3"),
        Restaurant("Puzata Chata", "ul. Ukraińska 24")
    )


    val filteredRestaurants = restaurants.filter {
        it.name.contains(searchQuery.text, ignoreCase = true)
    }

    Scaffold(

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(padding)
                .padding(horizontal = 16.dp)


        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search restaurants") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
//                   same red color as in our app logo
//                    searchbar outline color parameters
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,

//                    label color parameters
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray


                )
            )

            Button(
                onClick = {
                    isGeminiSearchVisible = !isGeminiSearchVisible
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD73D4A)
                )
            )
            {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Search",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isGeminiSearchVisible) "Hide AI prompt" else "Search with Gemini AI")
            }
            if (isGeminiSearchVisible) {
                OutlinedTextField(
                    value = geminiPrompt,
                    onValueChange = { geminiPrompt = it },
                    label = { Text("Describe what you are looking for (e.g., 'Good ramen in Wrocław')") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.DarkGray
                    )
                )
            }


            // Restaurant list
            LazyColumn {
                items(filteredRestaurants) { restaurant ->
                    InfoCard(
                        restaurantName = restaurant.name,
                        rating = 4.6f,
                        views = 350,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 8.dp, horizontal = 4.dp) //outside padding

                            .clickable {
                                // TODO more info about restaurant screen
                            }
                    )
                }
            }
        }
    }
}

