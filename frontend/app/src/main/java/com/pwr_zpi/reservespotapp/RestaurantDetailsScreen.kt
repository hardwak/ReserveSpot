package com.pwr_zpi.reservespotapp


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
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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


data class RestaurantDetails(
    val name: String,
    val address: String,
    val description: String = "Description placeholder: Italian cuisine, open 12:00-22:00",
//    need to change to real imgUrl from backend
    val imageUrl: Int = R.drawable.food_placeholder
)


// added lazy so Random would be initialized only once
val randomValues by lazy { Random.nextInt(4, 5) }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    navController: NavHostController,
//    getting restaurants name
    restaurantName: String,
    rating : Float
) {
    // temp data to show
    val details = remember {
        RestaurantDetails(
            name = restaurantName,
            address = "Wita Stwosza 56/57, 50-149 Wrocław, Polska",
            description = "Włoska kuchnia \nGodziny otwarcia:\nPon. - Pt: 12:00-22:00\nSob - Nd 10:00 - 22:00",
            // TODO: Use R.drawable.food_placeholder or implement loading images
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Photos", "Reviews")

//    states for visiting statistics
    var showOccupancySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

//    reviews code
    var reviews by remember { mutableStateOf(dummyReviews) }

    val addOrUpdateReview: (Review) -> Unit = { newReview ->
        reviews = if (reviews.any { it.isCurrentUser }) {
            // review editing
            reviews.map { if (it.isCurrentUser) newReview.copy(isCurrentUser = true) else it }
        } else {
            // Adding new review
            listOf(newReview.copy(isCurrentUser = true)) + reviews.filter { !it.isCurrentUser }
        }
    }

    val deleteReview: () -> Unit = {
        // deleting your own review
        reviews = reviews.filter { !it.isCurrentUser }
    }

//    review window
    var isReviewFormVisible by remember { mutableStateOf(false) }

    Scaffold(
//           Book now button
        bottomBar = {
            Button(
                onClick = { navController.navigate("reservation/${details.name}") },
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
            // Main photo as sample app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.food_placeholder),
                    contentDescription = details.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Back button
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

                //            placeholder for restaurants logo
                Box(
                    modifier = Modifier
//                    positions on the bottom and center of Box with photo
                        .align(Alignment.BottomCenter)
//                    offest 40 dp so it would not go to white panel
                        .offset(y = (40).dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(2.dp, RSRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pizza_restaurant_logo_small),
                        contentDescription = "Restaurant Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

            }



            // Info panel
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 295.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
// restaurants info modifiers
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
//                        Spacer to make sure that info starts below logo
                        Spacer(modifier = Modifier.height(48.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.Start

                        ) {

                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {


                                Text(
                                    text = details.name,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold

                                )

                                Spacer(Modifier.width(8.dp))


                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFC107), // Złoty
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
                                text = details.address,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
//                            opening hours
                            Text(
                                text = details.description,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TextButton(
                                onClick = { showOccupancySheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text("See popular times", color = RSRed, fontWeight = FontWeight.Bold)
                            }

                        }
                    }
                }
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
                                color = RSRed // setting color as red
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


// Elements in chosen card
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
        }
    }
    if (showOccupancySheet) {
        ModalBottomSheet(
            onDismissRequest = { showOccupancySheet = false },
            sheetState = sheetState,
            dragHandle = {
                DragHandle(
                    color = RSRed
                )
            }
        ) {
            // Panels filling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Popular Times (Statistics)",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Placeholder should change it to diagram or list/table if we have time
                Text("Monday: 18:00 - 20:00 (Very Busy)")
                Text("Tuesday: 19:00 - 21:00 (Busy)")
                Text("Wednesday: Usually not busy")
                Text("Thursday: 18:00 - 20:00 (Busy)")
                Text("Friday: 17:00 - 22:00 (Very Busy)")
                Text("Saturday: 12:00 - 22:00 (Very Busy)")
                Text("Sunday: 12:00 - 16:00 (Busy)")

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showOccupancySheet = false
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}




