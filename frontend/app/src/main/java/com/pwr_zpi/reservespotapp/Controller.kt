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

    val hideTopBar = currentRoute in listOf("") // List of screens where top bar should be hidden
    val hideBottomBar =
        currentRoute in listOf("login",) // List of screens where NavBar should be hidden

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                NavigationBar(
                    containerColor = RSRed
                ) {
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
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { MainMenuScreen(navController) }
            composable("restaurants") { ChooseRestaurantScreen(navController) }
            composable("restaurantDetails/{restaurantName}/{rating}") { backStackEntry ->
                val name =
                    backStackEntry.arguments?.getString("restaurantName") ?: "Unknown Restaurant"
                val rating = backStackEntry.arguments?.getFloat("rating") ?: 0.0f

                RestaurantDetailsScreen(navController, name, rating)

            }
            composable("reservation/{restaurantName}") { backStackEntry ->
                val name =
                    backStackEntry.arguments?.getString("restaurantName") ?: "Unknown Restaurant"
                ReservationScreen(navController, name)
            }
            composable(
                route = "reservationSummary/{restaurantName}?date={date}&time={time}&guests={guests}&duration={duration}&type={type}&location={location}",
                arguments = listOf(
                    navArgument("restaurantName") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType; defaultValue = "" },
                    navArgument("time") { type = NavType.StringType; defaultValue = "" },
                    navArgument("guests") { type = NavType.StringType; defaultValue = "" },
                    navArgument("duration") { type = NavType.StringType; defaultValue = "" },
                    navArgument("type") { type = NavType.StringType; defaultValue = "" },
                    navArgument("location") { type = NavType.StringType; defaultValue = "Any" }
                )
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("restaurantName") ?: "Unknown"
                ReservationSummaryScreen(
                    navController = navController,
                    restaurantName = name,
                    date = backStackEntry.arguments?.getString("date") ?: "No data",
                    time = backStackEntry.arguments?.getString("time") ?: "No hours",
                    guests = backStackEntry.arguments?.getString("guests") ?: "0",
                    duration = backStackEntry.arguments?.getString("duration") ?: "No duration",
                    type = backStackEntry.arguments?.getString("type") ?: "No type of reservation",
                    location = backStackEntry.arguments?.getString("location") ?: "Any"
                )
            }
        }
//            composable("login") { LoginScreen(navController) }
//            composable("reservations") { ReservationsScreen(navController) }
//            composable("favourites") { FavouritesScreen(navController) }
//            composable("account") { AccountScreen(navController) } // TODO Screens


        }
}

