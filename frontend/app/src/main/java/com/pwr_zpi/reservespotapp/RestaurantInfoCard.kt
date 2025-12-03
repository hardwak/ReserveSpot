package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun RestaurantInfoCard(
    modifier: Modifier = Modifier,
    info: RestaurantDto,
    onRefresh: () -> Unit = {}
) {

    val rawUrl = info.pic ?: ""
    val imageURL = if (rawUrl.isNotBlank()) {
        rawUrl.replace("localhost", "10.0.2.2")
    } else {
        ""
    }

    val restaurantName = info.name
    val rating = info.averageRating
    val views = info.reviewIds.size
    val id = info.id
    val context = LocalContext.current

    var isFavourite by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        checkFavouriteRestaurant(
            id,
            context,
            onSuccess = { isFav ->
                isFavourite = isFav
            }
        )
    }

    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp))

    ) {
        // Background image
        if (imageURL.isBlank()) {
            Image(
                painter = painterResource(id = R.drawable.food_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageURL)
                    .crossfade(true) // smooth fade-in effect
                    .build(),
                placeholder = painterResource(R.drawable.loading_placeholder),
                error = painterResource(R.drawable.food_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Text(
            text = restaurantName,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ){

            Text(
                text = rating.toString(),
                color = Color.Yellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(4.dp)
            )

            Icon(
                imageVector = Icons.Default.Star,
                tint = Color.Yellow,
                modifier = Modifier
                    .size(28.dp),
                contentDescription = "Star"
            )

            Text(
                text = "(" + views.toString() + ")",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(4.dp),
                color = Color.White
            )
        }

        IconButton(
            onClick = {
                if (isFavourite) {
                    removeFavouriteRestaurant(
                        id,
                        context,
                        onSuccess = {
                            isFavourite = false
                            onRefresh()
                        }
                    )
                } else {
                    addFavouriteRestaurant(
                        id,
                        context,
                        onSuccess = {
                            isFavourite = true
                        }
                    )
                }
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd)
                .width(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = RSRed,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                tint = Color.White,
                contentDescription = "favourite"
            )
        }

    }
}

fun checkFavouriteRestaurant(
    id: Long,
    context: Context,
    onSuccess: (Boolean) -> Unit,
) {
    val dataStore = DataStoreManager(context)
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        try {
            val token = dataStore.getBackendToken()
            if (token == null) {
                Log.e("checkFavouriteRestaurant", "No authentication token found")
                return@launch
            }

            val response = RetrofitClient.restaurantApi.checkFavourite(
                token = "Bearer $token",
                restaurantId = id
            )

            if (response.isSuccessful) {
                val isFav = response.body() ?: false
                withContext(Dispatchers.Main) { onSuccess(isFav) }
            } else {
                Log.e("checkFavouriteRestaurant", "${response.body()}")
            }

        } catch (e: Exception) {
            Log.e("checkFavouriteRestaurant", "${e.message}")
        }
    }
}



fun addFavouriteRestaurant(
    id: Long,
    context: Context,
    onSuccess: () -> Unit,
) {
    val dataStore = DataStoreManager(context)
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        try {
            val token = dataStore.getBackendToken()
            if (token == null) {
                Log.e("addFavouriteRestaurant", "No authentication token found")
                return@launch
            }

            val response = RetrofitClient.restaurantApi.addFavourite(
                token = "Bearer $token",
                restaurantId = id
            )

            if (response.isSuccessful) {
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e("addFavouriteRestaurant", "${response.code()}")
                }
            }

        } catch (e: Exception) {
            Log.e("addFavouriteRestaurant", "${e.message}")
        }
    }
}


fun removeFavouriteRestaurant(
    id: Long,
    context: Context,
    onSuccess: () -> Unit,
) {
    val dataStore = DataStoreManager(context)
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        try {
            val token = dataStore.getBackendToken()
            if (token == null) {
                Log.e("removeFavouriteRestaurant", "No authentication token found")
                return@launch
            }

            val response = RetrofitClient.restaurantApi.removeFavourite(
                token = "Bearer $token",
                restaurantId = id
            )

            if (response.isSuccessful) {
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e("removeFavouriteRestaurant", "${response.code()}")
                }
            }

        } catch (e: Exception) {
            Log.e("removeFavouriteRestaurant", "${e.message}")
        }
    }
}


