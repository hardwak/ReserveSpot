package com.pwr_zpi.reservespotapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "MainMenu"
            ) {
                composable("MainMenu") { MainMenuScreen(navController)}
                composable("ChooseRestaurant") { ChooseRestaurantScreen(navController) }


            }
        }
    }
}




// Remind the structure

//ReserveSpotAppTheme {
//    Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
//        Greeting(
//            name = "Android",
//            modifier = Modifier.padding(innerPadding)
//        )
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ReserveSpotAppTheme {
//        Greeting("Android")
//    }
//}