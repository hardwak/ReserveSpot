package com.pwr_zpi.reservespotapp

import  androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlin.random.Random

data class RestaurantDetails(
    val name: String,
    val address: String,
    val description: String = "Description placeholder: Italian cuisine, open 12:00-22:00",
//    need to change to real imgUrl from backend
    val imageUrl: Int = R.drawable.food_placeholder
)
data class Review(
    val id: Int,
    val author: String,
    val rating: Float,
    val text: String,
    val isCurrentUser: Boolean = false // flag for identifying user
)

const val CURRENT_USER_ID = 999

//dummy reviews
val dummyReviews = listOf(
    Review(1, "Wojciech Humeniuk", 4.5f, "Jedzenie i dodatkowo czas oczekiwania na zamówienie skrócił się znacznie. Polecam!", isCurrentUser = false),
    Review(2, "Karolina Drabent", 5.0f, "Rewelacja! Świetne nowe miejsce na mapie Wrocławia. Znakomite jedzenie, warte każdej ceny.", isCurrentUser = false),
    Review(3, "Andrzej Sparzak", 4.0f, "Dobra.", isCurrentUser = false),
    Review(CURRENT_USER_ID, "Ja (Current User)", 5.0f, "Bardzo dobre jedzenie, super wystrój i miła obsługa. Polecam spróbować ich pizzę!", isCurrentUser = true)
)

// added lazy so Random would be initialized only once
val randomValues by lazy {Random.nextInt(4, 5)}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    navController: NavHostController,
//    getting restaurants name
    restaurantName: String
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
                onClick = { /* TODO: Przekierowanie do ekranu rezerwacji */ },
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

            }

//            placeholder for restaurants logo
            Box(
                modifier = Modifier
//                    positions on the bottom and center of Box with photo
                    .align(Alignment.BottomCenter)
//                    offest 40 dp so it would not go to white panel
                    .offset(y = (-80).dp)
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

            // Info panel
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 260.dp)
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
                        Spacer(modifier = Modifier.height(0.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.Start

                        ) {
                            Text(
                                // Big restaurant name
                                text = details.name,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            // Address/info
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
                        }
                    }
                }
                    stickyHeader {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White),
                            containerColor = Color.Transparent
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
}

@Composable
fun GeminiSummaryCard(summary: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // placeholder for Gemini icon
                Icon(Icons.Default.Star, contentDescription = "AI Summary", tint = Color(0xFF00796B))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI Review Summary (Gemini)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00796B)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(summary)
        }
    }
}

@Composable
fun ReviewsTabContent(
    reviews: List<Review>,
    onAddOrEditReview: (Review) -> Unit,
    onDeleteReview: () -> Unit,
    isFormVisible: Boolean,
    onToggleForm: (Boolean) -> Unit
) {
    val currentUserReview = reviews.firstOrNull { it.isCurrentUser }
    val geminiSummary = "Na podstawie ${reviews.size} opinii, sztuczna inteligencja stwierdza, że restauracja ma bardzo dobrą obsługę i smaczne dania główne, szczególnie kuchni włoskiej. Klienci polecają pizzę i makarony, ale niektórzy zwracają uwagę na długi czas oczekiwania w godzinach szczytu. Ogólna ocena to 4.7/5.0."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {

        GeminiSummaryCard(summary = geminiSummary)


        Button(
            onClick = {
                onToggleForm(!isFormVisible)

                val newReviewText = if (currentUserReview == null) "Moja nowa recenzja! Pięć gwiazdek!" else "Moja edytowana recenzja, zmieniam tekst."
                val newReview = Review(
                    id = CURRENT_USER_ID,
                    author = "Ja (Current User)",
                    rating = 5.0f,
                    text = newReviewText,
                    isCurrentUser = true
                )
                onAddOrEditReview(newReview)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) {
            Text(if (currentUserReview == null) "Dodaj swoją opinię" else "Edytuj swoją opinię")
        }

        if (isFormVisible) {
            ReviewForm(
                currentReview = currentUserReview,
                onSave = { text, rating ->
                    val newReview = Review(
                        id = CURRENT_USER_ID,
                        author = currentUserReview?.author ?: "Ja",
                        rating = rating,
                        text = text,
                        isCurrentUser = true
                    )
                    onAddOrEditReview(newReview)
                    onToggleForm(false)
                },
                onCancel = { onToggleForm(false) }
            )
        }


        // review list
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
//           First is our review (sorting)
            val sortedReviews = reviews.sortedByDescending { it.isCurrentUser }

            sortedReviews.forEach { review ->
                ReviewItem(
                    review = review,

                    onDelete = { if (review.isCurrentUser) onDeleteReview() },
//                    id = 999
                )
            }
        }
    }
}

@Composable
fun ReviewItem(
    review: Review,
    onDelete: (Review) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.author, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                // delete button only for users own review
                if (review.isCurrentUser) {
                    IconButton(
                        onClick = { onDelete(review) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Usuń opinię",
                            tint = RSRed
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // showing rating
                Text( " ${review.rating}/5.0", color = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating Star",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(review.text, color = Color.DarkGray)
        }
    }
}

@Composable
fun PhotosTabContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PhotoPlaceholder(Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                PhotoPlaceholder(Modifier.weight(1f))
            }
        }

    }
}


@Composable
fun PhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Propotions 1:1 (square)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.LightGray)
    ) {
        // Need to switch to Image() function
        Text("Photo", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
    }
}

@Composable
fun ReviewsTabContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        repeat(10) { index ->
            ReviewItem(index + 1)
        }
    }
}

@Composable
fun ReviewItem(id: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User #$id", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simulation of reviews rating
                Text(" $randomValues.0/5.0", color = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "To jest przykładowa recenzja. Obsługa była miła, jedzenie smaczne. [Recenzja $id]",
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ReviewForm(
    currentReview: Review?, // Jeśli null, dodajemy nową; jeśli nie, edytujemy
    onSave: (String, Float) -> Unit,
    onCancel: () -> Unit
) {
    var reviewText by remember { mutableStateOf(currentReview?.text ?: "") }
    var rating by remember { mutableStateOf(currentReview?.rating ?: 5.0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)) // Jasnożółte tło
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (currentReview == null) "Dodaj nową opinię" else "Edytuj opinię",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(8.dp))

            // Prosty TextField na treść opinii (używam bazowego TextField dla uproszczenia)
            TextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Twoja opinia...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
            )
            Spacer(Modifier.height(8.dp))

            // Placeholder dla wyboru oceny
            Text("Ocena: ${rating.toInt()} / 5", color = Color.Gray)
            // TODO: Zastąpić suwakiem lub wyborem gwiazdek

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Przycisk Anuluj
                TextButton(onClick = onCancel) {
                    Text("Anuluj", color = Color.Gray)
                }
                Spacer(Modifier.width(8.dp))
                // Przycisk Zapisz
                Button(
                    onClick = { onSave(reviewText, rating) },
                    enabled = reviewText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Zapisz")
                }
            }
        }
    }
}