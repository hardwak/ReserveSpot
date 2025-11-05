package com.pwr_zpi.reservespotapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class GoogleTokenRequest(
    val googleToken: String
)

data class AuthResponse(
    val token: String
)

data class ErrorResponse(
    val message: String
)

interface AuthApi {
    @POST("/api/auth/google")
    suspend fun googleLogin(@Body request: GoogleTokenRequest): Response<AuthResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}