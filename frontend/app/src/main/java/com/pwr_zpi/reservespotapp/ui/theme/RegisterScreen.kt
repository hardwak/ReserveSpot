package com.pwr_zpi.reservespotapp.ui.theme

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.IOException
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.pwr_zpi.reservespotapp.ClientRegisterRequest
import com.pwr_zpi.reservespotapp.R
import com.pwr_zpi.reservespotapp.RetrofitClient
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.handleGoogleSignInResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    var noData by remember { mutableStateOf(false) }
    var incorrectlyRepeatedPassword by remember { mutableStateOf(false) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val BUTTON_HEIGHT = 70.dp

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val dataStoreManager = remember { DataStoreManager(context) }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("622858688727-cfpvp191t9den54uiht2islfvosns021.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val coroutineScope = rememberCoroutineScope()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            handleGoogleSignInResult(
                result = result,
                dataStoreManager = dataStoreManager,
                onError = { message ->
                    errorMessage = message
                    showErrorDialog = true
                },
                onSuccess = { role ->
                    when (role) {
                        "CLIENT" -> navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                        "RESTAURANT" -> navController.navigate("restauranthome") {
                            popUpTo("login") { inclusive = true }
                        }
                        "ADMIN" -> {
                            errorMessage = "Admins cannot log in via this app."
                            showErrorDialog = true
                        }
                    }
                }
            )
        }
    }



    Column (

    ) {
        Text(
            text = "Register",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        TextField(
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = { Text("Repeat Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (noData) {
            Text(
                text = "Please fill in all the fields.",
                color = RSRed,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }

        if (incorrectlyRepeatedPassword) {
            Text(
                text = "Password repeated incorrectly.",
                color = RSRed,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }

//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .padding(horizontal = 16.dp, vertical = 8.dp)
//        ) {
//            Checkbox(
//                checked = rememberMe,
//                onCheckedChange = { rememberMe = it }
//            )
//            Text(
//                text = "Remember Me",
//                fontSize = 16.sp
//            )
//        }


        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    name.isBlank() || email.isBlank() || password.isBlank() -> {
                        noData = true
                        incorrectlyRepeatedPassword = false
                    }
                    password != repeatPassword -> {
                        incorrectlyRepeatedPassword = true
                        noData = false
                    }
                    else -> {
                        noData = false
                        incorrectlyRepeatedPassword = false
                        sendRegisterRequest(
                            name = name,
                            email = email,
                            password = password,
                            onError = { message ->
                                errorMessage = message
                                showErrorDialog = true
                            },
                            onSuccess = {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) {
            Text(
                text = "Sign Up",
                color = Color.White,
            )
        }

        Button(
            onClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    signInLauncher.launch(signInIntent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_2x),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign in with Google",
                color = Color.Black
            )
        }

        Text(
            text = "Already have an account?",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Button(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) {
            Text(
                text = "Log In",
                color = Color.White,
            )
        }

    }

    if (showErrorDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
                }
            },
            title = { Text("Registration Error") },
            text = { Text(errorMessage) }
        )
    }

}


fun sendRegisterRequest(
    name: String,
    email: String,
    password: String,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    coroutineScope.launch {
        try {
            val response = RetrofitClient.authApi.register(
                ClientRegisterRequest(name = name, email = email, password = password)
            )

            if (response.isSuccessful) {
                Log.d("Register", "Registration successful: ${response.body()?.message}")

                // After successful registration, automatically navigate to login screen
                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorBody != null) {
                        JSONObject(errorBody).getString("message")
                    } else {
                        "Unknown error"
                    }
                } catch (e: Exception) {
                    Log.e("Register", "Failed to parse error: ${e.message}")
                    "Unknown error"
                }

                Log.e("Register", "Registration failed: $errorMessage")
                onError(errorMessage)

            }

        } catch (e: IOException) {
            Log.e("Register", "Network error: ${e.message}")
        } catch (e: HttpException) {
            Log.e("Register", "HTTP error: ${e.message()}")
        } catch (e: Exception) {
            Log.e("Register", "Unexpected error: ${e.message}")
        }
    }
}

