package com.pwr_zpi.reservespotapp

import com.pwr_zpi.reservespotapp.data.DataStoreManager
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavHostController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = DataStoreManager(context)

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    val passwordMask = '●'

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Field for old pass
            OutlinedTextField(
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    errorMessage = ""
                },
                label = { Text("Current password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(mask = passwordMask),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (oldPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Show/Hide password")
                    }
                }
            )

            // Field for new pass
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = ""
                },
                label = { Text("New password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(mask = passwordMask),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Show/Hide password")
                    }
                }
            )


            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }


            Button(
                onClick = {

                    if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                        errorMessage = "All fields are required."
                        return@Button
                    }
                    if (newPassword.length < 8) {
                        errorMessage = "New password must have at least 8 characters."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = ""

                    scope.launch {
                        val token = dataStore.getBackendToken()
                        if (token != null) {
                            try {
                                val requestDto = ChangePasswordDto(
                                    currentPassword = oldPassword,
                                    newPassword = newPassword
                                )

                                val response = RetrofitClient.userApi.changePassword("Bearer $token", requestDto)

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {

                                    val errorBody = response.errorBody()?.string()
                                    val message = try {
                                        JSONObject(errorBody).getString("message")
                                    } catch (e: Exception) {
                                        "Failed to change password."
                                    }


                                    errorMessage = when (response.code()) {
                                        401 -> "Incorrect current password."
                                        400 -> message
                                        else -> "Error: $message"
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network error. Please try again."
                                e.printStackTrace()
                            }
                        } else {
                            errorMessage = "You are not logged in."
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
