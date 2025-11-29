package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pwr_zpi.reservespotapp.ui.theme.RSRed

@Composable
fun OwnerReservationCard(
    reservation: OwnerReservationDto,
    onCancel: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Reservation #${reservation.id}", fontWeight = FontWeight.Bold)
                Text(reservation.reservationDatetime.replace("T", " "), color = RSRed, fontWeight = FontWeight.Bold)
            }
            Text("Table ID: ${reservation.tableId}")
            Text("Time: ${reservation.durationMinutes} min")
            val statusColor = if (reservation.status == "CONFIRMED") Color(0xFF4CAF50) else Color.Gray
            Text("Status: ${reservation.status}", color = statusColor, fontWeight = FontWeight.SemiBold) // Zmiana

            Button(
                onClick = { onCancel(reservation.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}