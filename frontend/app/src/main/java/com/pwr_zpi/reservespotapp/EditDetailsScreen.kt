package com.pwr_zpi.reservespotapp

import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDetailsScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)


    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }


    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var isEmailError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhoneValid(phone: String): Boolean {
        return phone.length >= 9
    }

    // --- POBIERANIE DANYCH ---
    LaunchedEffect(Unit) {
        val token = dataStore.getBackendToken()
        if (token != null) {
            try {
                // 1. Dekodujemy email z tokena (funkcja pomocnicza na dole pliku)
                val emailFromToken = getEmailFromTokenForEdit(token)

                if (emailFromToken != null) {
                    // 2. Pobieramy dane używając endpointu /email/{email}
                    val response = RetrofitClient.userApi.getUserByEmail("Bearer $token", emailFromToken)

                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            // Rozdzielamy "Imie Nazwisko" na dwa pola
                            val fullName = user.name ?: ""
                            val parts = fullName.trim().split(" ", limit = 2)

                            name = parts.getOrElse(0) { "" }
                            surname = parts.getOrElse(1) { "" }
                            email = user.email ?: ""
                            phone = user.phoneNumber ?: ""
                        }
                    } else {
                        Log.e("EditDetails", "Error fetching user: ${response.code()}")
                    }
                } else {
                    Log.e("EditDetails", "Could not extract email from token")
                }
            } catch (e: Exception) {
                Log.e("EditDetails", "Exception", e)
                Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your data") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RSRed)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Personal data",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                EditDetailTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    icon = Icons.Default.Person
                )
                EditDetailTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = "Surname",
                    icon = Icons.Default.Person
                )
                EditDetailTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailError = false
                    },
                    label = "Email",
                    icon = Icons.Default.Email,
                    isError = isEmailError
                )
                if (isEmailError) {
                    Text(
                        "Enter correct email address.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
                    )
                }

                EditDetailTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        isPhoneError = false
                    },
                    label = "Phone",
                    icon = Icons.Default.Phone,
                    isError = isPhoneError
                )
                if (isPhoneError) {
                    Text(
                        "Number must have at least 9 characters.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // --- ZAPISYWANIE DANYCH ---
                Button(
                    onClick = {
                        isEmailError = !isEmailValid(email)
                        isPhoneError = !isPhoneValid(phone)

                        if (!isEmailError && !isPhoneError) {
                            isSaving = true
                            scope.launch {
                                val token = dataStore.getBackendToken()
                                if (token != null) {
                                    // Łączymy imię i nazwisko z powrotem w jeden ciąg
                                    val fullNameToSend = "$name $surname".trim()

                                    val updateDto = UpdateProfileDto(
                                        name = fullNameToSend,
                                        email = email,
                                        phoneNumber = phone
                                    )

                                    try {
                                        // Tutaj używamy PUT /api/users/me (który masz na backendzie)
                                        val response = RetrofitClient.userApi.updateProfile("Bearer $token", updateDto)

                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Data saved!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                                            Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isSaving = false
                            }
                        } else {
                            Toast.makeText(context, "Please enter correct data", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RSRed),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Funkcja pomocnicza do dekodowania tokena (kopia tej z AccountScreen, aby plik był niezależny)
fun getEmailFromTokenForEdit(token: String): String? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val jsonObject = JSONObject(payload)
        var email = jsonObject.optString("sub")
        if (email.isNullOrEmpty()) {
            email = jsonObject.optString("email")
        }
        email.ifEmpty { null }
    } catch (e: Exception) {
        null
    }
}