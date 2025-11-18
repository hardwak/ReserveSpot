package com.pwr_zpi.reservespotapp

import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
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
import com.google.android.gms.common.api.ApiException
import com.pwr_zpi.reservespotapp.data.DataStoreManager
import com.pwr_zpi.reservespotapp.ui.theme.RSRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@Composable
fun LoginScreen(navController: NavHostController) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("622858688727-cfpvp191t9den54uiht2islfvosns021.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dataStoreManager = remember { DataStoreManager(context) }
    // var rememberMe by remember { mutableStateOf(false) }

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
                            showErrorDialog = true
                            errorMessage = "Admins cannot log in via this app."
                        }
                    }
                }
            )
        }
    }


    LaunchedEffect(Unit) {
        val valid = checkBackendToken(dataStoreManager, RetrofitClient.authApi)
        if (valid) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loginDataIncorrect by remember { mutableStateOf(false) }

    val BUTTON_HEIGHT = 70.dp


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
                loginDataIncorrect = sendLoginRequest(
                    email = email,
                    password = password,
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
            text = "Don't have an account yet?",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Button(
            onClick = {
                navController.navigate("register")
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


suspend fun checkBackendToken(
    dataStoreManager: DataStoreManager,
    authApi: AuthApi
): Boolean {
    val token = dataStoreManager.getBackendToken() ?: return false

    return try {
        val response = authApi.validateToken("Bearer $token")
        if (response.isSuccessful) {
            true
        } else {
            dataStoreManager.clearBackendToken()
            false
        }
    } catch (e: Exception) {
        dataStoreManager.clearBackendToken()
        false
    }
}


fun extractRolesFromJwt(token: String): List<String> {
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return emptyList()

        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        val json = JSONObject(payload)

        when {
            json.has("roles") -> {
                val arr = json.getJSONArray("roles")
                List(arr.length()) { arr.getString(it) }
            }
            json.has("role") -> listOf(json.getString("role"))
            json.has("authorities") -> {
                val arr = json.getJSONArray("authorities")
                List(arr.length()) { arr.getString(it) }
            }
            else -> emptyList()
        }
    } catch (e: Exception) {
        Log.e("JWT", "Failed to parse token: ${e.message}")
        emptyList()
    }
}


fun sendLoginRequest(
    email: String,
    password: String,
    dataStoreManager: DataStoreManager,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit  // Pass role info back
): Boolean {
    var failure = false
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    coroutineScope.launch {
        try {
            val response = RetrofitClient.authApi.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val backendToken = response.body()!!.token

                // Save token in DataStore
                dataStoreManager.saveBackendToken(backendToken)

                Log.d("Login", "Login successful, token: $backendToken")

                // Decode role from token
                val roles = extractRolesFromJwt(backendToken)

                Log.d("Login", "Roles: $roles")

                // Navigate based on roles
                CoroutineScope(Dispatchers.Main).launch {
                    if (roles.contains("ROLE_CLIENT") || roles.contains("CLIENT")) {
                        onSuccess("CLIENT")
                    } else if (roles.contains("ROLE_RESTAURANT") || roles.contains("RESTAURANT")) {
                        onSuccess("RESTAURANT")
                    } else if (roles.contains("ROLE_ADMIN") || roles.contains("ADMIN")) {
                        onSuccess("ADMIN")
                    } else {
                        onError("Unknown user role")
                    }
                }

            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Login", "Login failed: $errorBody")
                failure = true
            }

        } catch (e: IOException) {
            Log.e("Login", "Network error: ${e.message}")
            onError(e.message ?: "Unknown error")
        } catch (e: HttpException) {
            Log.e("Login", "HTTP error: ${e.message}")
            onError(e.message ?: "Unknown error")
        } catch (e: Exception) {
            Log.e("Login", "Unexpected error: ${e.message}")
            onError(e.message ?: "Unknown error")
        }
    }
    return failure
}

suspend fun handleGoogleSignInResult(
    result: ActivityResult,
    dataStoreManager: DataStoreManager,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit  // Pass the user role back
) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account.idToken

        if (idToken != null) {
            val response = RetrofitClient.authApi.googleLogin(
                GoogleTokenRequest(googleToken = idToken)
            )

            if (response.isSuccessful && response.code() == 200) {
                val backendToken = response.body()?.token
                if (backendToken != null) {
                    dataStoreManager.saveBackendToken(backendToken)
                    Log.d("GoogleSSO", "Backend token: $backendToken")

                    val roles = extractRolesFromJwt(backendToken)
                    Log.d("GoogleSSO", "Roles: $roles")

                    when {
                        roles.contains("ROLE_CLIENT") || roles.contains("CLIENT") ->
                            onSuccess("CLIENT")
                        roles.contains("ROLE_RESTAURANT") || roles.contains("RESTAURANT") ->
                            onSuccess("RESTAURANT")
                        roles.contains("ROLE_ADMIN") || roles.contains("ADMIN") -> {
                            onError("Admins cannot log in via this app.")
                        }
                        else -> onError("Unknown user role")
                    }
                } else {
                    onError("Missing token in response")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody ?: "{}").getString("message")
                } catch (_: Exception) {
                    "Authentication failed"
                }
                Log.e("GoogleSSO", "Backend auth failed: $errorMessage")
                onError(errorMessage)
            }
        }
    } catch (e: ApiException) {
        Log.e("GoogleSSO", "Sign-in failed: ${e.message}")
        onError(e.message ?: "Unknown error")
    } catch (e: Exception) {
        Log.e("GoogleSSO", "Network error: ${e.message}")
        onError(e.message ?: "Unknown error")
    }
}


