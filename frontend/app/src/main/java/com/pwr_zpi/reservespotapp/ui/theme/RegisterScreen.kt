package com.pwr_zpi.reservespotapp.ui.theme

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pwr_zpi.reservespotapp.R

@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    var noData by remember { mutableStateOf(false) }
    var incorrectlyRepeatedPassword by remember { mutableStateOf(false) }

    val BUTTON_HEIGHT = 70.dp


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
                if (name == "" || email == "" || password == "")
                    noData = true
                else if (password != repeatPassword)
                    incorrectlyRepeatedPassword = true
                /* TODO handle register logic here */
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
//                googleSignInClient.signOut().addOnCompleteListener {
//                    val signInIntent = googleSignInClient.signInIntent
//                    signInLauncher.launch(signInIntent)
//                }
                // TODO handle registering via Google
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