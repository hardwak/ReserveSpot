package com.pwr_zpi.reservespotapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
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


suspend fun fetchMyAccountDetails(context: Context): AccountUserDto? = withContext(Dispatchers.IO) {
    try {
        val token = DataStoreManager(context).getBackendToken()
        if (token == null) {
            Log.e("Account", "Brak tokena w DataStore!")
            return@withContext null
        }

        val response = RetrofitClient.userApi.getMyDetails("Bearer $token")

        if (response.isSuccessful) {
            response.body()
        } else {
            Log.e("Account", "Błąd API: Kod ${response.code()} - ${response.message()}")
            Log.e("Account", "Treść błędu: ${response.errorBody()?.string()}")
            null
        }
    } catch (e: Exception) {
        Log.e("Account", "Wyjątek podczas pobierania danych konta", e)
        null
    }
}

@Composable
fun AccountScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    var userData by remember { mutableStateOf<AccountUserDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(navController.currentBackStackEntry) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
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

                    Text(
                        text = "Hi, ${userData?.name ?: "User"}!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userData?.email ?: "",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }


                    if (!userData?.phoneNumber.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = userData?.phoneNumber ?: "",
                                fontSize = 15.sp,
                                color = Color.Gray
                            )
                        }
                    }
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
            text = "Change password",
            onClick = { navController.navigate("changePassword") }
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