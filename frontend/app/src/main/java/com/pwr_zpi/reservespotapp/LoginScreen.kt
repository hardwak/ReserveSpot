package com.pwr_zpi.reservespotapp

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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loginDataIncorrect by remember { mutableStateOf(false) }

    val BUTTON_HEIGHT = 70.dp

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                // Send token to backend
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.authApi.googleLogin(
                            GoogleTokenRequest(googleToken = idToken)
                        )

                        if (response.isSuccessful && response.code() == 200) {
                            val authResponse = response.body()
                            val backendToken = authResponse?.token

                            // Store the token (SharedPreferences, DataStore, etc.)
                            // TODO: Save backendToken for future API calls
                            Log.d("GoogleSSO", "Backend token: $backendToken")

                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("GoogleSSO", "Backend auth failed: $errorBody")
                            // TODO: Show error to user
                        }
                    } catch (e: Exception) {
                        Log.e("GoogleSSO", "Network error: ${e.message}")
                        // TODO: Show error to user
                    }
                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleSSO", "Sign-in failed: ${e}")
        }
    }


    Column (

    ) {
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        if (loginDataIncorrect) {
            Text(
                text = "Login or password are incorrect!",
                color = RSRed,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                loginDataIncorrect = true
            /* TODO handle login logic here */
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RSRed)
        ) {
            Text(
                text = "Log in",
                color = Color.White,
            )
        }

        Button(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
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
            text = "Don't have an account yet?",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Button(
            onClick = {
                /* TODO handle login logic here */
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

    }
}