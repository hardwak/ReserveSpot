package com.pwr_zpi.reservespotapp

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed


val restaurantCuisineOptions =
    listOf("Polska", "Włoska", "Amerykańska", "Wegańska", "Ukraińska", "Japońska", "Inna")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRestaurantScreen(navController: NavHostController) {

    // TODO: In the future it need to be loaded from DB
    var restaurantName by remember { mutableStateOf("Your restaurants name") }
    var address by remember { mutableStateOf("Restaurants address") }
    var description by remember { mutableStateOf("Description (ex. Italian cuisine, open...)") }
    var openingHours by remember { mutableStateOf("Mon. - Fri: 12:00-22:00\nSat - Sun 10:00 - 22:00") }


    var selectedCuisine by remember { mutableStateOf(restaurantCuisineOptions[0]) }
    var isCuisineMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit restaurant data") },
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Basic information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Name, Address, description
            EditDetailTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                label = "Restaurant name",
                icon = Icons.Default.Restaurant
            )
            EditDetailTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address",
                icon = Icons.Default.LocationOn
            )
            EditDetailTextField(
                value = description,
                onValueChange = { description = it },
                label = "Short description",
                icon = Icons.Default.Info
            )

            // Openm hours
            OutlinedTextField(
                value = openingHours,
                onValueChange = { openingHours = it },
                label = { Text("Open hours") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Photos and categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Cuisine options
            ExposedDropdownMenuBox(
                expanded = isCuisineMenuExpanded,
                onExpandedChange = { isCuisineMenuExpanded = !isCuisineMenuExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = selectedCuisine,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cuisine type") },
                    leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCuisineMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isCuisineMenuExpanded,
                    onDismissRequest = { isCuisineMenuExpanded = false }
                ) {
                    restaurantCuisineOptions.forEach { cuisine ->
                        DropdownMenuItem(
                            text = { Text(cuisine) },
                            onClick = {
                                selectedCuisine = cuisine
                                isCuisineMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Buttons for logo and photos change
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Logic for logo change */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Change logo")
                }
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    onClick = { /* TODO: Logic for changing photos */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Add photos")
                }
            }


            Spacer(Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    // TODO: Logic of saving restaurant data to DB
                    Toast.makeText(context, "Changes saved!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack() // back to account screen
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text("Save changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


