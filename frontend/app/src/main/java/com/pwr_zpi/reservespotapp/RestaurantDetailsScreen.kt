package com.pwr_zpi.reservespotapp

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch
import kotlin.random.Random


data class RestaurantDataModel(
    val name: String,
    val address: String,
    val description: String,
    val imageUrl: String? = null
)

sealed class LoadState {
    object Loading : LoadState()
    data class Success(val data: RestaurantDataModel) : LoadState()
    data class Error(val message: String) : LoadState()
}

// added lazy so Random would be initialized only once
val randomValues by lazy { Random.nextInt(4, 5) }


suspend fun fetchRestaurantDetails(context: Context, restaurantId: Long): LoadState = withContext(Dispatchers.IO) {

    try {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext LoadState.Error("Brak tokena")




        kotlinx.coroutines.delay(500)
        LoadState.Success(RestaurantDataModel(
            name = "Testowa Restauracja $restaurantId",
            address = "Pobrany adres",
            description = "Pobrany opis i godziny pracy.",
            imageUrl = null // Użyj prawdziwego URL
        ))

    } catch (e: Exception) {
        Log.e("Details", "Błąd pobierania detali", e)
        LoadState.Error("Nie udało się załadować danych restauracji.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    navController: NavHostController,
    restaurantId: Long,
    rating: Float
) {
    val context = LocalContext.current


    var uiState by remember { mutableStateOf<LoadState>(LoadState.Loading) }


    LaunchedEffect(restaurantId) {
        uiState = LoadState.Loading
        uiState = fetchRestaurantDetails(context, restaurantId)
    }


    val detailsData = when (uiState) {
        is LoadState.Loading -> {
            // Możemy wyświetlić prosty wskaźnik ładowania
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return // Zakończ funkcję, jeśli trwa ładowanie
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

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Photos", "Reviews")


    // states for visiting statistics
    var showOccupancySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()


    var reviews by remember { mutableStateOf(dummyReviews) }

    val addOrUpdateReview: (Review) -> Unit = { newReview ->
        reviews = if (reviews.any { it.isCurrentUser }) {
            reviews.map { if (it.isCurrentUser) newReview.copy(isCurrentUser = true) else it }
        } else {
            listOf(newReview.copy(isCurrentUser = true)) + reviews.filter { !it.isCurrentUser }
        }
    }

    val deleteReview: () -> Unit = {
        reviews = reviews.filter { !it.isCurrentUser }
    }

    // review window
    var isReviewFormVisible by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

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
                                    text = String.format("%.1f", rating),
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

                            0 -> PhotosTabContent()
                            1 -> ReviewsTabContent(
                                reviews = reviews,
                                onAddOrEditReview = addOrUpdateReview,
                                onDeleteReview = deleteReview,
                                isFormVisible = isReviewFormVisible,
                                onToggleForm = { isReviewFormVisible = it }
                            )
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