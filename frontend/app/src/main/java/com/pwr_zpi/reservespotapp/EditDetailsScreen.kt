package com.pwr_zpi.reservespotapp


import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.ui.theme.RSRed

//file name for storing data on device
private const val USER_PREFS_NAME = "user_details_prefs"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDetailsScreen(navController: NavHostController) {

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE)
    }

    var name by remember { mutableStateOf(prefs.getString("name", "Jan") ?: "Jan") }
    var surname by remember { mutableStateOf(prefs.getString("surname", "Kowalski") ?: "Kowalski") }
    var email by remember {
        mutableStateOf(
            prefs.getString("email", "jan.kowalski@example.com") ?: "jan.kowalski@example.com"
        )
    }
    var phone by remember { mutableStateOf(prefs.getString("phone", "123456789") ?: "123456789") }

    var isEmailError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhoneValid(phone: String): Boolean {
        return phone.length >= 9 && phone.all { it.isDigit() }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
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
                    "Number must have at least 9 numbers.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )
            }


            Spacer(Modifier.height(24.dp))


            Button(
                onClick = {

                    isEmailError = !isEmailValid(email)
                    isPhoneError = !isPhoneValid(phone)


                    if (!isEmailError && !isPhoneError) {
                        with(prefs.edit()) {
                            putString("name", name)
                            putString("surname", surname)
                            putString("email", email)
                            putString("phone", phone)
                            apply()
                        }
                        Toast.makeText(context, "Data saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter correct data", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RSRed)
            ) {
                Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// EditDetailsTextField function is in CommonUI file