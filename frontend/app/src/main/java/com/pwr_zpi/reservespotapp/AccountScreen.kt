package com.pwr_zpi.reservespotapp

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch
import android.util.Base64
import org.json.JSONObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AccountScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    // Stan dla imienia
    var userName: String? by remember { mutableStateOf("User") }

    // Logika pobierania danych
    LaunchedEffect(Unit) {
        val token = dataStore.getBackendToken()
        if (token != null) {
            // 1. Wyciągnij email z tokena
            val email = getEmailFromToken(token)

            if (email != null) {
                try {

                    val response = RetrofitClient.userApi.getUserByEmail("Bearer $token", email)
                    if (response.isSuccessful) {
                        response.body()?.let { userDto ->
                            userName = userDto.name ?: "User"
                        }
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(64.dp),
                tint = RSRed
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Hi, $userName!", // Tutaj wyświetli się imię
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome back!",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        AccountOptionRow(
            icon = Icons.Default.AccountCircle,
            text = "Edit data",
            onClick = {
                navController.navigate("editDetails")
            }
        )

        AccountOptionRow(
            icon = Icons.Default.Settings,
            text = "Settings",
            onClick = { navController.navigate("settings") }
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                scope.launch {
                    // Logika wylogowania
                    dataStore.clearBackendToken()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RSRed),
            border = BorderStroke(1.dp, RSRed)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Log out", fontWeight = FontWeight.Bold)
        }
    }
}

// Funkcja pomocnicza (jeśli nie masz jej w Utils)
fun getEmailFromToken(token: String): String? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val jsonObject = JSONObject(payload)
        // Spring Security domyślnie trzyma email/username w polu "sub"
        jsonObject.optString("email")
    } catch (e: Exception) {
        null
    }
}