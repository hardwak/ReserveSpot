package com.pwr_zpi.reservespotapp


import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pwr_zpi.reservespotapp.ui.theme.RSRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Controller(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideTopBar = currentRoute in listOf("login", "register")
    val hideBottomBar = currentRoute in listOf(
        "login",
        "register",
        "restaurant_register",
        "ownerDashboard",
        "OwnerRestaurantDetailsScreen"
    )

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_red_outside_small),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(end = 12.dp)
                            )
                            Text(
                                text = "ReserveSpot",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(containerColor = RSRed) {
                    val navBarItems =
                        listOf("home", "restaurants", "reservations", "favourites", "account")
                    navBarItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when (item) {
                                        "home" -> Icons.Default.Home
                                        "restaurants" -> Icons.Default.LocationOn
                                        "reservations" -> Icons.Default.DateRange
                                        "favourites" -> Icons.Default.Favorite
                                        "account" -> Icons.Default.AccountCircle
                                        else -> Icons.Default.Close
                                    },
                                    contentDescription = item
                                )
                            },
                            label = {
                                Text(
                                    text = item.replaceFirstChar { it.uppercase() },
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            },
                            selected = currentRoute == item,
                            onClick = {
                                navController.navigate(item) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RSRed,
                                unselectedIconColor = Color.White,
                                indicatorColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("home") { MainMenuScreen(navController) }
            composable("restaurants") { ChooseRestaurantScreen(navController) }
            composable("reservations") { ReservationsScreen(navController) }
            composable("account") { AccountScreen(navController) }
            composable(
                route = "restaurantDetails/{restaurantId}",
                arguments = listOf(
                    navArgument("restaurantId") { type = NavType.LongType; defaultValue = 0L }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("restaurantId") ?: 0L
                if (id != 0L) {
                    RestaurantDetailsScreen(navController, id)
                }
            }





            composable(
                route = "pickLocation?lat={lat}&lng={lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("lng") { type = NavType.FloatType; defaultValue = 0f }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble()
                val lng = backStackEntry.arguments?.getFloat("lng")?.toDouble()
                val finalLat = if (lat != 0.0) lat else null
                val finalLng = if (lng != 0.0) lng else null

                LocationPickerScreen(navController, finalLat, finalLng)
            }

//            composable("owner") { OwnerRestaurantListScreen(navController) } // Ekran z listą restauracji
            composable(
                route = "owner/restaurant/{restaurantId}",
                arguments = listOf(navArgument("restaurantId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("restaurantId") ?: 0L
                if (id != 0L) {
                    OwnerRestaurantDetailsScreen(navController, id)
                }
            }

            composable(
                route = "reservation/{restaurantId}/{restaurantName}?date={date}&time={time}&guests={guests}&duration={duration}&location={location}",
                arguments = listOf(
                    navArgument("restaurantId") {
                        type = NavType.LongType
                    },
                    navArgument("restaurantName") { type = NavType.StringType },

                    navArgument("date") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("time") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("guests") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("duration") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    },
                    navArgument("location") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("restaurantId") ?: 0L
                val name = backStackEntry.arguments?.getString("restaurantName") ?: "Restaurant"

                val date = backStackEntry.arguments?.getString("date")
                val time = backStackEntry.arguments?.getString("time")
                val guests = backStackEntry.arguments?.getString("guests")
                val duration = backStackEntry.arguments?.getString("duration")
                val location = backStackEntry.arguments?.getString("location")


                ReservationScreen(
                    navController = navController,
                    restaurantId = id,
                    restaurantName = name,
                    initialDate = date,
                    initialTime = time,
                    initialGuests = guests,
                    initialDuration = duration,
                    initialLocation = location
                )
            }

            composable("restaurants") { ChooseRestaurantScreen(navController) }
            composable("account") { AccountScreen(navController) }
            composable("editDetails") { EditDetailsScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("changePassword") { ChangePasswordScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("restaurant_register") { RestaurantRegisterScreen(navController) }
            composable("reservations") { ReservationsScreen(navController) }
            composable("ownerDashboard") { OwnerRestaurantListScreen(navController) }
            composable("ownerAddRestaurant") {
                AddRestaurantScreen(navController)
                composable("ownerEditRestaurant/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: "new"
//                OwnerEditRestaurantScreen(navController, id)
                }
                composable("ownerTables/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
//                OwnerTablesScreen(navController, id)
                }
//             composable("favourites") { FavouritesScreen(navController) }

                composable(
                    route = "reservationSummary/{restaurantName}?tableId={tableId}&fullDateTime={fullDateTime}&durationMinutes={durationMinutes}&dateDisplay={dateDisplay}&timeDisplay={timeDisplay}&guests={guests}&durationDisplay={durationDisplay}&location={location}",
                    arguments = listOf(
                        navArgument("restaurantName") { type = NavType.StringType },
                        // Parametry backendowe
                        navArgument("tableId") { type = NavType.LongType },
                        navArgument("fullDateTime") { type = NavType.StringType }, // ISO String
                        navArgument("durationMinutes") { type = NavType.IntType },
                        // Parametry UI (do wyświetlenia)
                        navArgument("dateDisplay") { type = NavType.StringType; defaultValue = "" },
                        navArgument("timeDisplay") { type = NavType.StringType; defaultValue = "" },
                        navArgument("guests") { type = NavType.StringType; defaultValue = "" },
                        navArgument("durationDisplay") {
                            type = NavType.StringType; defaultValue = ""
                        },
                        navArgument("location") { type = NavType.StringType; defaultValue = "Any" }
                    )
                ) { backStackEntry ->
                    ReservationSummaryScreen(
                        navController = navController,
                        restaurantName = backStackEntry.arguments?.getString("restaurantName")
                            ?: "Unknown",
                        tableId = backStackEntry.arguments?.getLong("tableId") ?: 0L,
                        fullDateTime = backStackEntry.arguments?.getString("fullDateTime") ?: "",
                        durationMinutes = backStackEntry.arguments?.getInt("durationMinutes") ?: 60,
                        dateDisplay = backStackEntry.arguments?.getString("dateDisplay") ?: "",
                        timeDisplay = backStackEntry.arguments?.getString("timeDisplay") ?: "",
                        guests = backStackEntry.arguments?.getString("guests") ?: "2",
                        durationDisplay = backStackEntry.arguments?.getString("durationDisplay")
                            ?: "",
                        location = backStackEntry.arguments?.getString("location") ?: "Any"
                    )
                }


            }
        }
    }
}



