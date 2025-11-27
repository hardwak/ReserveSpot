package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

// --- FUNKCJA POMOCNICZA DO DEKODOWANIA TOKENA ---
fun getEmailFromToken(token: String): String? {
    try {
        // Token JWT składa się z 3 części oddzielonych kropkami. Druga część to Payload (dane).
        val parts = token.split(".")
        if (parts.size < 2) return null

        // Dekodujemy Base64
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val jsonObject = JSONObject(payload)

        // Próbujemy wyciągnąć email. W Spring Security zazwyczaj jest to "sub" (subject)
        // Ale czasem może być też w polu "email". Sprawdzamy oba.
        return if (jsonObject.has("sub")) {
            jsonObject.getString("sub")
        } else if (jsonObject.has("email")) {
            jsonObject.getString("email")
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("JWT_DECODE", "Błąd dekodowania tokena", e)
        return null
    }
}

// --- ZAKTUALIZOWANA FUNKCJA POBIERANIA DANYCH ---
suspend fun fetchMyAccountDetails(context: Context): AccountUserDto? = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken()
        if (token == null) {
            Log.e("Account", "Brak tokena!")
            return@withContext null
        }

        // 1. Wyciągamy email z tokena
        val email = getEmailFromToken(token)
        Log.d("Account", "Zdekodowany email z tokena: $email")

        if (email == null) {
            Log.e("Account", "Nie udało się wyciągnąć maila z tokena.")
            return@withContext null
        }

        // 2. Pobieramy dane używając endpointu /email/{email}
        val response = RetrofitClient.userApi.getUserByEmail("Bearer $token", email)

        if (response.isSuccessful) {
            response.body()
        } else {
            Log.e("Account", "Błąd API: Kod ${response.code()} - ${response.message()}")
            Log.e("Account", "Treść błędu: ${response.errorBody()?.string()}")
            null
        }
    } catch (e: Exception) {
        Log.e("Account", "Wyjątek sieciowy", e)
        null
    } as Nothing?
}

@Composable
fun AccountScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    // Stan danych użytkownika
    var userData by remember { mutableStateOf<AccountUserDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Pobieranie danych przy starcie
    LaunchedEffect(Unit) {
        isLoading = true
        val user = fetchMyAccountDetails(context)
        if (user != null) {
            userData = user
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RSRed)
            }
        } else {
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
                    // Wyświetlamy dane pobrane z backendu
                    Text(
                        text = "Hi, ${userData?.name ?: "User"}!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userData?.email ?: "Welcome back!",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
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