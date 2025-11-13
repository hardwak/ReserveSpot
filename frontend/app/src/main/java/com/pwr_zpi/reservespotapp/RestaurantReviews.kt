package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pwr_zpi.reservespotapp.ui.theme.RSRed


data class Review(
    val id: Int,
    val author: String,
    val rating: Float,
    val text: String,
    val isCurrentUser: Boolean = false // flag for identifying user
)

const val CURRENT_USER_ID = 999

val dummyReviews = listOf(
    Review(
        1,
        "Wojciech Humeniuk",
        4.5f,
        "Jedzenie i dodatkowo czas oczekiwania na zamówienie skrócił się znacznie. Polecam!",
        isCurrentUser = false
    ),
    Review(
        2,
        "Karolina Drabent",
        5.0f,
        "Rewelacja! Świetne nowe miejsce na mapie Wrocławia. Znakomite jedzenie, warte każdej ceny.",
        isCurrentUser = false
    ),
    Review(3, "Andrzej Sparzak", 4.0f, "Dobra.", isCurrentUser = false),
    Review(
        CURRENT_USER_ID,
        "Ja (Current User)",
        5.0f,
        "Bardzo dobre jedzenie, super wystrój i miła obsługa. Polecam spróbować ich pizzę!",
        isCurrentUser = true
    )
)


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
                Icon(
                    Icons.Default.Star,
                    contentDescription = "AI Summary",
                    tint = Color(0xFF00796B)
                )
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
    val geminiSummary =
        "Na podstawie ${reviews.size} opinii, sztuczna inteligencja stwierdza, że restauracja ma bardzo dobrą obsługę i smaczne dania główne, szczególnie kuchni włoskiej. Klienci polecają pizzę i makarony, ale niektórzy zwracają uwagę na długi czas oczekiwania w godzinach szczytu. Ogólna ocena to 4.7/5.0."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {

        GeminiSummaryCard(summary = geminiSummary)


        Button(
            onClick = {
                onToggleForm(!isFormVisible)

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) {
            Text(if (currentUserReview == null) "Add a review" else "Edit review")
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
        Column(modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)) {
//           First is our review (sorting)
            val sortedReviews = reviews.sortedByDescending { it.isCurrentUser }

            sortedReviews.forEach { review ->
                ReviewItem(
                    review = review,

                    onDelete = { if (review.isCurrentUser) onDeleteReview() },

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
                            contentDescription = "Delete review",
                            tint = RSRed
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // showing rating
                Text(" ${review.rating}/5.0", color = Color.Gray)
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
fun ReviewForm(
    currentReview: Review?,
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
                text = if (currentReview == null) "Add new review" else "Edit review",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your rating: ", color = Color.Gray)
                Spacer(Modifier.width(8.dp))

                RatingBar(
                    currentRating = rating,
                    onRatingChange = { rating = it }
                )
            }
            Spacer(Modifier.height(8.dp))

            TextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Your review...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )
            Spacer(Modifier.height(8.dp))


            Text("Ocena: ${rating.toInt()} / 5", color = Color.Gray)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { onSave(reviewText, rating) },
                    enabled = reviewText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    currentRating: Float,
    onRatingChange: (Float) -> Unit,
    maxRating: Int = 5
) {
    Row {
//        for every star
        repeat(maxRating) { index ->
            val starIndex = index + 1
            IconButton(
                onClick = { onRatingChange(starIndex.toFloat()) },
                modifier = Modifier.size(32.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating $starIndex",
                    tint = if (starIndex <= currentRating.toInt()) Color(0xFFFFC107) else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
