package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.libraries.places.api.model.Review
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random


//data class RestaurantDataModel(
//    val name: String,
//    val address: String,
//    val description: String,
//    val imageUrl: String? = null
//)

sealed class LoadState {
    object Loading : LoadState()
    data class Success(val data: RestaurantDto) : LoadState()
    data class Error(val message: String) : LoadState()
}



suspend fun fetchRestaurantDetails(context: Context, restaurantId: Long): LoadState = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext LoadState.Error("Brak tokena")

        // Wywołanie endpointu z ID
        val response = RetrofitClient.restaurantApi.getRestaurantDetails("Bearer $token", restaurantId)

        if (response.isSuccessful) {
            val dto = response.body()
            if (dto != null) {
                LoadState.Success(dto) // Sukces zwraca pełny RestaurantDto
            } else {
                LoadState.Error("Puste dane z serwera.")
            }
        } else {
            Log.e("Details", "Błąd serwera: ${response.code()} - ${response.message()}")
            LoadState.Error("Błąd serwera: ${response.code()}")
        }
    } catch (e: Exception) {
        Log.e("Details", "Błąd pobierania detali", e)
        LoadState.Error("Nie udało się połączyć z serwerem.")
    }
}

suspend fun fetchReviews(context: Context, restaurantId: Long): List<ReviewDto> = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()

        // ZMIANA: Używamy RetrofitClient.reviewsApi
        val response = RetrofitClient.reviewsApi.getReviews("Bearer $token", restaurantId)

        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    } catch (e: Exception) {
        Log.e("Details", "Error fetching reviews: ${e.message}")
        emptyList()
    }
}

suspend fun fetchReviewsWithUserNames(context: Context, restaurantId: Long): List<ReviewWithUser> = withContext(Dispatchers.IO) {
    val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
    val reviews = fetchReviews(context, restaurantId) // Ta funkcja już istnieje i pobiera List<ReviewDto>

    if (reviews.isEmpty()) return@withContext emptyList()

    // Używamy async/await do równoległego pobierania detali użytkowników
    reviews.map { review ->
        // Uwaga: To jest bardzo nieefektywne, jeśli recenzji jest dużo!
        // Użytkownik musi mieć ID, inaczej zwracamy domyślną nazwę.
        val userName = if (review.userId != null) {
            try {
                val userResponse = RetrofitClient.userApi.getUserDetails("Bearer $token", review.userId)
                if (userResponse.isSuccessful) {
                    userResponse.body()?.name ?: "Użytkownik #${review.userId}"
                } else {
                    "Użytkownik anonimowy (Błąd ${userResponse.code()})"
                }
            } catch (e: Exception) {
                Log.e("Details", "Błąd pobierania nazwy użytkownika: ${e.message}")
                "Użytkownik anonimowy (Błąd połączenia)"
            }
        } else {
            "Użytkownik"
        }
        ReviewWithUser(review, userName)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    navController: NavHostController,
    restaurantId: Long,
    rating: Double
) {
    val context = LocalContext.current

//    val detailsData =
    var uiState by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    var reviewsWithUser by remember { mutableStateOf(emptyList<ReviewWithUser>()) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Photos", "Reviews")


    // states for visiting statistics
    var showOccupancySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var reviews by remember { mutableStateOf(emptyList<ReviewDto>()) }
    val listState = rememberLazyListState()

    LaunchedEffect(restaurantId) {
        uiState = LoadState.Loading
        uiState = fetchRestaurantDetails(context, restaurantId)
    }

    LaunchedEffect(restaurantId, selectedTabIndex) {
        // Ładuj opinie tylko wtedy, gdy zakładka 'Reviews' jest aktywna (index 1)
        if (selectedTabIndex == 1) {
            reviewsWithUser = fetchReviewsWithUserNames(context, restaurantId)
        }
    }

    val detailsData = when (uiState) {
        is LoadState.Loading -> {
            // Możemy wyświetlić prosty wskaźnik ładowania
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        is LoadState.Error -> {
            // Wyświetlanie komunikatu błędu
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Błąd: ${(uiState as LoadState.Error).message}", color = Color.Red)
            }
            return
        }
        is LoadState.Success -> (uiState as LoadState.Success).data
    }

    val displayRating = detailsData.averageRating?.toFloat() ?: 0.0f



    val showBackButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }


    Scaffold(
        bottomBar = {
            Button(
                onClick = { navController.navigate("reservation/${detailsData.name}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text(
                    "BOOK NOW",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.food_placeholder),
                    contentDescription = detailsData.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )


            }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {


                item {
                    Spacer(modifier = Modifier.height(240.dp))
                }

                //Info panel
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                    ) {
                        // Spacer under logo
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = detailsData.name,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = String.format("%.1f", displayRating),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            Text(
                                text = detailsData.address,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = detailsData.description,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                        }
                    }
                }

                // Sticky Header
                stickyHeader {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                        containerColor = Color.Transparent,

                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = RSRed
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selectedContentColor = RSRed,
                                unselectedContentColor = Color.Gray
                            )


                        }

                    }

                }




                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 500.dp)
                            .background(Color.White)
                    ) {
                        when (selectedTabIndex) {

                            // TODO: Aktualizuj wywołania do używania zaktualizowanych DTOs
                            0 -> Text("Photos tab (Not implemented)")
                            1 -> ReviewsTabContent(reviewsWithUser = reviewsWithUser)

//
                        }
                    }
                }

            }
            AnimatedVisibility(
                visible = showBackButton,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            )
            {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RSRed)

                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
            }

        }
    }
}


@Composable
fun ReviewsTabContent(reviewsWithUser: List<ReviewWithUser>) {
    if (reviewsWithUser.isEmpty()) {
        Text("Brak opinii dla tej restauracji.", color = Color.Gray, modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(reviewsWithUser) { item ->
                val review = item.review
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(item.userName, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))

                            // Ocena (rating)
                            review.rating?.let { rating ->
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                                Text(" ${rating}/5", color = Color.Gray)
                            }
                        }
                        Text(review.comment ?: "Brak komentarza.", modifier = Modifier.padding(top = 4.dp))
                        review.createdAt?.let {
                            Text(it.toString().take(10), fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}