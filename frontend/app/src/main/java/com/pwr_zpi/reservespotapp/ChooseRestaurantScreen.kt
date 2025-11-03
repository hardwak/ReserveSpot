package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
        topBar = {
            TopAppBar(
                title = { Text("Choose a Restaurant") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)



        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search restaurants") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
//                   same red color as in our app logo
//                    searchbar outline color parameters
                    focusedBorderColor = Color(0xFFD73D4A),
                    unfocusedBorderColor = Color.Gray,

//                    label color parameters
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black


                )


            )



            // Restaurant list
            LazyColumn {
                items(filteredRestaurants) { restaurant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            // TODO screen about chosen restaurant after clicking on the field
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = restaurant.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = restaurant.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
