package com.pwr_zpi.reservespotapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//data class ReservationDto (
//    val restaurantName: String,
//    val dateTime: LocalDateTime,
//    val numOfPeople: Int,
//    val durationM: Float,
//    val restaurantRating: Float
//)

@Composable
fun ReservationInfoCard(
    modifier: Modifier,
    onCancel: () -> Unit,
    info: ReservationDto
) {
    val id = info.id
    val restaurantName = info.restaurantName
    val datetime = LocalDateTime.parse(info.reservationDatetime)
    val numOfPeople = info.numOfPeople
    val durationM = info.durationMinutes

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = RSRed,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(width = 1.dp,
                color = RSRed,
                shape = RoundedCornerShape(16.dp))

    ) {
        Text(
            text = restaurantName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(bottom = 16.dp)
                .padding(top = 8.dp)
        )

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart)
                .width(150.dp)
        )
        {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    contentDescription = "datetime"
                )
                Text(text = datetime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    contentDescription = "people"
                )
                Text(text = numOfPeople.toString())
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    contentDescription = "duration"
                )
                Text(text = durationM.toString() + "min")
            }
        }


        Button(
            onClick = {
                showDialog = true
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd)
                .width(150.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RSRed,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Cancel reservation?") },
                text = { Text("Are you sure you want to cancel your reservation?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            cancelReservation(
                                id = id,
                                context = context,
                                onSuccess = { onCancel() },
                                onError = {
                                    Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

    }

}

fun cancelReservation(
    id: Long,
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val dataStore = DataStoreManager(context)
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        try {
            val token = dataStore.getBackendToken()
            if (token == null) {
                withContext(Dispatchers.Main) {
                    onError("User not logged in")
                }
                return@launch
            }

            val response = RetrofitClient.reservationApi.cancelReservation(
                token = "Bearer $token",
                reservationId = id
            )

            if (response.isSuccessful) {
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Failed: ${response.code()}")
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

