package com.pwr_zpi.reservespotapp

import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

sealed class LoadState {
    object Loading : LoadState()
    data class Success(val data: RestaurantDto) : LoadState()
    data class Error(val message: String) : LoadState()
}


suspend fun checkReviewEligibility(context: Context, restaurantId: Long): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val token = DataStoreManager(context).getBackendToken() ?: return@withContext false
            val response = RetrofitClient.reviewsApi.checkEligibility("Bearer $token", restaurantId)
            if (response.isSuccessful) {
                response.body()?.canReview ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


suspend fun fetchRestaurantDetails(context: Context, restaurantId: Long): LoadState =
    withContext(Dispatchers.IO) {
        try {
            val token = DataStoreManager(context).getBackendToken()
            if (token.isNullOrBlank()) {
                return@withContext LoadState.Error("Authorization error: No token")
            }
            val response =
                RetrofitClient.restaurantApi.getRestaurantDetails("Bearer $token", restaurantId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) LoadState.Success(dto) else LoadState.Error("Empty data from server.")
            } else {
                LoadState.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            LoadState.Error("Error connecting with server.")
        }
    }

suspend fun fetchMyReviewForRestaurant(context: Context, restaurantId: Long): ReviewDto? =
    withContext(Dispatchers.IO) {
        try {
            val token = DataStoreManager(context).getBackendToken() ?: return@withContext null
            val response = RetrofitClient.reviewsApi.getMyReviews("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.find { it.restaurantId == restaurantId }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

suspend fun fetchReviews(context: Context, restaurantId: Long): List<ReviewDto> =
    withContext(Dispatchers.IO) {
        try {
            val token =
                DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
            val response = RetrofitClient.reviewsApi.getReviews("Bearer $token", restaurantId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

suspend fun fetchAiAnalysis(context: Context, restaurantId: Long): AiAnalysisDto? =
    withContext(Dispatchers.IO) {
        try {
            val token = DataStoreManager(context).getBackendToken() ?: return@withContext null

            val response = RetrofitClient.aiAnalysisApi.getAnalysis("Bearer $token", restaurantId)

            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()
            } else if (response.code() == 404) {
                Log.d("AI_ANALYSIS", "Analysis not found, generating new one...")
                val genResponse =
                    RetrofitClient.aiAnalysisApi.generateAnalysis("Bearer $token", restaurantId)
                if (genResponse.isSuccessful) {
                    return@withContext genResponse.body()
                }
            }
            null
        } catch (e: Exception) {
            Log.e("AI_ANALYSIS", "Error fetching analysis", e)
            null
        }
    }

suspend fun fetchReviewsWithUserNames(context: Context, restaurantId: Long): List<ReviewWithUser> =
    withContext(Dispatchers.IO) {
        val token = DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
        val reviews = fetchReviews(context, restaurantId)

        if (reviews.isEmpty()) return@withContext emptyList()

        reviews.map { review ->
            val userName = if (review.userId != null) {
                try {
                    val userResponse =
                        RetrofitClient.userApi.getUserDetails("Bearer $token", review.userId)
                    if (userResponse.isSuccessful) {
                        userResponse.body()?.name ?: "User #${review.userId}"
                    } else {
                        "Anonymous"
                    }
                } catch (e: Exception) {
                    "User"
                }
            } else {
                "User"
            }
            ReviewWithUser(review, userName)
        }
    }


suspend fun fetchPhotos(context: Context, restaurantId: Long): List<PictureDto> =
    withContext(Dispatchers.IO) {
        try {
            val token =
                DataStoreManager(context).getBackendToken() ?: return@withContext emptyList()
            val response = RetrofitClient.picturesApi.getPictures("Bearer $token", restaurantId)
            if (response.isSuccessful) {
                val allPhotos = response.body() ?: emptyList()
                allPhotos.filter { it.restaurantIds.contains(restaurantId) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

suspend fun uploadImageToBackend(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val token = DataStoreManager(context).getBackendToken() ?: return@withContext null
            val imagePart = prepareImagePart(context, uri) ?: return@withContext null
            val response = RetrofitClient.picturesApi.uploadPicture("Bearer $token", imagePart)
            if (response.isSuccessful) response.body()?.url else null
        } catch (e: Exception) {
            Log.e("Upload", "Upload exception", e)
            null
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    navController: NavHostController,
    restaurantId: Long,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var uiState by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    var reviewsWithUser by remember { mutableStateOf(emptyList<ReviewWithUser>()) }
    var photos by remember { mutableStateOf(emptyList<PictureDto>()) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Photos", "Reviews")

    var aiAnalysis by remember { mutableStateOf<AiAnalysisDto?>(null) }
    var myReview by remember { mutableStateOf<ReviewDto?>(null) }
    var canCreateReview by remember { mutableStateOf(false) }
    var isReviewDialogVisible by remember { mutableStateOf(false) }


    var showOccupancySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    fun refreshReviews() {
        scope.launch {
            reviewsWithUser = fetchReviewsWithUserNames(context, restaurantId)
            myReview = fetchMyReviewForRestaurant(context, restaurantId)
            canCreateReview = checkReviewEligibility(context, restaurantId)
            photos = fetchPhotos(context, restaurantId)

            if (reviewsWithUser.isNotEmpty()) {
                aiAnalysis = fetchAiAnalysis(context, restaurantId)
            }
        }
    }

    LaunchedEffect(restaurantId) {
        uiState = LoadState.Loading
        uiState = fetchRestaurantDetails(context, restaurantId)

        val result = fetchRestaurantDetails(context, restaurantId)
        uiState = result
        if (result is LoadState.Success) {
            refreshReviews()
        }
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1 && reviewsWithUser.isNotEmpty()) {
            aiAnalysis = fetchAiAnalysis(context, restaurantId)
        }
    }

//    LaunchedEffect(restaurantId, selectedTabIndex) {
//        if (selectedTabIndex == 1) {
//
//            if (reviewsWithUser.isNotEmpty()) {
//                aiAnalysis = fetchAiAnalysis(context, restaurantId)
//            }
//        }
//        if (selectedTabIndex == 0) {
//            photos = fetchPhotos(context, restaurantId)
//        }
//    }


//    LaunchedEffect(restaurantId) {
//        uiState = LoadState.Loading
//        uiState = fetchRestaurantDetails(context, restaurantId)
//
//        launch { myReview = fetchMyReviewForRestaurant(context, restaurantId) }
//        launch { canCreateReview = checkReviewEligibility(context, restaurantId) }
//    }
//
//    LaunchedEffect(restaurantId, selectedTabIndex) {
//        if (selectedTabIndex == 1) {
//            reviewsWithUser = fetchReviewsWithUserNames(context, restaurantId)
//            aiAnalysis = fetchAiAnalysis(context, restaurantId)
//        }
//
//        if (selectedTabIndex == 0) {
//            photos = fetchPhotos(context, restaurantId)
//        }
//    }







    if (isReviewDialogVisible) {
        AddEditReviewDialog(
            restaurantId = restaurantId,
            existingReview = myReview,
            onDismiss = { isReviewDialogVisible = false },
            onSuccess = {
                isReviewDialogVisible = false
                refreshReviews()
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun deleteMyReview() {
        val reviewId = myReview?.id ?: return
        scope.launch(Dispatchers.IO) {
            val token = DataStoreManager(context).getBackendToken()
            if (token != null) {
                val response = RetrofitClient.reviewsApi.deleteReview("Bearer $token", reviewId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
                        refreshReviews()
                    } else {
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val detailsData = when (uiState) {
        is LoadState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return
        }

        is LoadState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Error: ${(uiState as LoadState.Error).message}", color = Color.Red) }
            return
        }

        is LoadState.Success -> (uiState as LoadState.Success).data
    }

    val displayRating = detailsData.averageRating?.toFloat() ?: 0.0f
    val showBackButton by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { navController.navigate("reservation/$restaurantId/${detailsData.name}") },
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
        },

//        CHECKING canCreateReview
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                if (myReview == null && canCreateReview) {
//                if (myReview == null) {
                    FloatingActionButton(
                        onClick = { isReviewDialogVisible = true },
                        containerColor = RSRed,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Review")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .offset(y = (-40).dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)) {
                val imageUrl = detailsData.pic
                if (imageUrl.isNullOrBlank()) {
                    Image(
                        painter = painterResource(id = R.drawable.food_placeholder),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imageUrl)
                            .crossfade(true).build(),
                        placeholder = painterResource(id = R.drawable.food_placeholder),
                        error = painterResource(id = R.drawable.loading_placeholder),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 240.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(Color.White)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .align(Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = detailsData.name,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Opening hours:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                val orderedDaysMap = listOf(
                                    "monday" to "Monday",
                                    "tuesday" to "Tuesday",
                                    "wednesday" to "Wednesday",
                                    "thursday" to "Thursday",
                                    "friday" to "Friday",
                                    "saturday" to "Saturday",
                                    "sunday" to "Sunday"
                                )
                                Column(modifier = Modifier.padding(start = 4.dp)) {
                                    orderedDaysMap.forEach { (backendKey, englishName) ->
                                        val hours = detailsData.openingHours[backendKey] ?: "Closed"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = englishName,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = hours,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .padding(top = 40.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(RSRed)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
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
                        containerColor = Color.Transparent,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = RSRed
                            )
                        }) {
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
                            .heightIn(min = 1.dp)
                            .background(Color.White)
                    ) {
                        when (selectedTabIndex) {
                            0 -> PhotosTabContent(photos = photos)
                            1 -> ReviewsTabContent(
                                reviewsWithUser = reviewsWithUser,
                                myReviewId = myReview?.id,
                                aiAnalysis = aiAnalysis,
                                onEditClick = { isReviewDialogVisible = true },
                                onDeleteClick = { deleteMyReview() }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ReviewsTabContent(
    reviewsWithUser: List<ReviewWithUser>,
    myReviewId: Long?,
    aiAnalysis: AiAnalysisDto?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {


    Column(modifier = Modifier.padding(top = 8.dp)) {

        if (aiAnalysis != null) {
            AiSummaryCard(analysis = aiAnalysis)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (reviewsWithUser.isEmpty()) {
            Text(
                "No reviews for this restaurant.",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {

            reviewsWithUser.forEach { item ->
                val review = item.review
                val isMyReview = review.id == myReviewId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMyReview) Color(
                            0xFFFFF8F8
                        ) else Color.White
                    ),
                    border = if (isMyReview) BorderStroke(1.dp, RSRed) else null
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isMyReview) "You" else item.userName,
                                fontWeight = FontWeight.Bold,
                                color = if (isMyReview) RSRed else Color.Black
                            )
                            Spacer(Modifier.width(8.dp))

                            review.rating?.let { rating ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(" ${rating}/5", color = Color.Gray)
                            }

                            Spacer(Modifier.weight(1f))

                            if (isMyReview) {
                                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.Gray
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = onDeleteClick,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }

                            }
                        }

                        Text(
                            review.comment ?: "No comment.",
                            modifier = Modifier.padding(top = 4.dp)
                        )


                        if (!review.pic.isNullOrBlank()) {
                            val rawUrl = review.pic

                            val finalImageUrl = when {
                                rawUrl.contains("localhost") -> rawUrl.replace(
                                    "localhost",
                                    "10.0.2.2"
                                )
                                !rawUrl.startsWith("http") -> "http://10.0.2.2:8080" + rawUrl
                                else -> rawUrl
                            }

                            Log.d("DEBUG_IMAGE", "Original: $rawUrl -> Final: $finalImageUrl")

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(finalImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Review photo",
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.food_placeholder),
                                error = painterResource(id = R.drawable.food_placeholder)
                            )
                        }

                        review.createdAt?.let {
                            Text(
                                it.toString().take(10),
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun StarRatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}


@Composable
fun PhotosTabContent(photos: List<PictureDto>) {
    if (photos.isEmpty()) {
        Text("No photos available.", color = Color.Gray, modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
            photos.chunked(2).forEach { rowPhotos ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPhotos.forEach { photo ->
                        val url = photo.url
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            if (url != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(url)
                                        .crossfade(true).build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    placeholder = painterResource(id = R.drawable.food_placeholder),
                                    error = painterResource(id = R.drawable.food_placeholder)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.food_placeholder),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    if (rowPhotos.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun AiSummaryCard(analysis: AiAnalysisDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F7FF),
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, Color(0xFF2196F3))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Summary",
                    tint = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini Review Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = analysis.summaryText ?: "Analysis available, but text is missing.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            analysis.sentimentScore?.let { score ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sentiment Score: ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(text = String.format("%.2f", score), fontSize = 12.sp)
                }
            }
        }
    }
}


@Composable
fun AddEditReviewDialog(
    restaurantId: Long,
    existingReview: ReviewDto?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rating by remember { mutableStateOf(existingReview?.rating ?: 5) }
    var comment by remember { mutableStateOf(existingReview?.comment ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPicUrl by remember { mutableStateOf(existingReview?.pic) }

    var isSubmitting by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                existingPicUrl = null
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (existingReview == null) "Add Review" else "Edit Review") },
        text = {
            Column {
                Text("Rating:")
                StarRatingBar(rating = rating, onRatingChanged = { rating = it })

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Select Photo from Gallery")
                }

                if (selectedImageUri != null) {
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Red
                            )
                        }
                    }
                } else if (!existingPicUrl.isNullOrBlank()) {
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(existingPicUrl).build(),
                            contentDescription = "Existing Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { existingPicUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch(Dispatchers.IO) {
                        val token = DataStoreManager(context).getBackendToken()
                        if (token != null) {
                            var finalPicUrl: String? = existingPicUrl

                            if (selectedImageUri != null) {
                                val uploadedUrl =
                                    uploadImageToBackend(context, selectedImageUri!!)
                                if (uploadedUrl != null) {
                                    finalPicUrl = uploadedUrl
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Failed to upload image",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isSubmitting = false
                                    }
                                    return@launch
                                }
                            }

                            val response = if (existingReview == null) {
                                val createDto = CreateReviewDto(
                                    restaurantId = restaurantId,
                                    rating = rating,
                                    comment = comment,
                                    pic = finalPicUrl
                                )
                                RetrofitClient.reviewsApi.createReview(
                                    "Bearer $token",
                                    createDto
                                )
                            } else {
                                val updateDto = UpdateReviewDto(
                                    rating = rating,
                                    comment = comment,
                                    pic = finalPicUrl
                                )
                                RetrofitClient.reviewsApi.updateReview(
                                    "Bearer $token",
                                    existingReview.id!!,
                                    updateDto
                                )
                            }

                            withContext(Dispatchers.Main) {
                                isSubmitting = false
                                if (response.isSuccessful) {
                                    onSuccess()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                if (isSubmitting) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
                else Text(if (existingReview == null) "Submit" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = Color.Gray
                )
            }
        }
    )
}