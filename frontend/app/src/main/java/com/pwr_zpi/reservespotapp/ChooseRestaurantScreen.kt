package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed


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


    fun searchWithGemini(prompt: String) {


        println("Gemini Search initiated with prompt: $prompt")

// hiding field
        isGeminiSearchVisible = false
//      clearing field
        geminiPrompt = TextFieldValue("")
    }


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
                    containerColor = RSRed
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 8.dp, horizontal = 4.dp) //outside padding

                            .clickable {
                                navController.navigate("restaurantDetails/${restaurant.name}")
                            },
                        info = RestaurantCardInfo(
                            restaurantName = restaurant.name,
                            rating = 4.6f,
                            views = 350,
                        )
                    )

                }
            }
        }
    }
}


