package com.pwr_zpi.reservespotapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.pwr_zpi.reservespotapp.ui.theme.RSRed

@Composable
fun LocationPickerScreen(navController: NavHostController, initialLat: Double?, initialLng: Double?) {
    // Domyślnie Wrocław (Rynek) jeśli brak danych
    val startPos = LatLng(initialLat ?: 51.110, initialLng ?: 17.032)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPos, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // MAPA GOOGLE
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        )

        // NIERUCHOMA PINEZKA NA ŚRODKU EKRANU
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Center Marker",
            tint = RSRed,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .offset(y = (-24).dp) // Przesunięcie, aby czubek pinezki wskazywał środek
                .zIndex(1f)
        )

        // PANEL Z PRZYCISKIEM ZATWIERDZENIA
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Wyświetlanie aktualnych współrzędnych
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wybrana lokalizacja:", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${cameraPositionState.position.target.latitude}, ${cameraPositionState.position.target.longitude}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    // PRZEKAZANIE DANYCH Z POWROTEM
                    val selectedLocation = cameraPositionState.position.target
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lat", selectedLocation.latitude)
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lng", selectedLocation.longitude)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Zatwierdź lokalizację")
            }
        }
    }
}