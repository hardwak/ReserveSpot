//package com.pwr_zpi.reservespotapp
//
//import android.content.Context
//import android.util.Log
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//import com.pwr_zpi.reservespotapp.data.DataStoreManager
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//@Composable
//fun FavouritesScreen(navController: NavHostController) {
//
//    val context = LocalContext.current
//    var favourites by remember { mutableStateOf<List<RestaurantDto>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        isLoading = true
//        favourites = fetchFavourites(context)
//        isLoading = false
//    }
//
//    if (isLoading) {
//        CircularProgressIndicator()
//    } else {
//        LazyColumn {
//            items(favourites) { reservation ->
//                FavouriteInfoCard(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                        .padding(horizontal = 16.dp, vertical = 8.dp)
//                        .clickable(onClick = { navController.navigate("restaurantDetails/${reservation.restaurantId}/${reservation.rating}") }),
//                    info = reservation
//                )
//            }
//        }
//    }
//}
//
//suspend fun fetchFavourites(context: Context): List<RestaurantDto> {
//    return withContext(Dispatchers.IO) {
//        try {
//            val dataStoreManager = DataStoreManager(context)
//            val token = dataStoreManager.getBackendToken()
//
//            if (token.isNullOrEmpty()) {
//                Log.e("fetchFavourites", "No authentication token found")
//                return@withContext emptyList()
//            }
//
//            val response = RetrofitClient.restaurantApi
//                .getMyFavourites("Bearer $token")
//
//            if (response.isSuccessful) {
//                response.body() ?: emptyList()
//            } else {
//                Log.e("fetchFavourites", "Error: ${response.code()} - ${response.message()}")
//                emptyList()
//            }
//        } catch (e: Exception) {
//            Log.e("fetchFavourites", "Exception while fetching favourite restaurants", e)
//            emptyList()
//        }
//    }
//}
