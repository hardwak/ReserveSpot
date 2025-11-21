package com.pwr_zpi.reservespotapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class GoogleTokenRequest(
    val googleToken: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String
)

data class ErrorResponse(
    val message: String
)

data class ClientRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "CLIENT"
)

data class RestaurantRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "RESTAURANT"
)

data class RegisterResponse(
    val message: String
)

data class MyUpcomingReservationsResponse(
    val reservations: List<ReservationDto>
)

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

data class ReservationDto(
    val id: Long,
    val userId: Long,
    val tableId: Long,
    val reservationDatetime: String,   // LocalDateTime → String in JSON
    val durationMinutes: Int,
    val status: ReservationStatus,
    val restaurantId: Long,
    val restaurantName: String,
    val numOfPeople: Int,
    val restaurantRating: Double
)

interface AuthApi {
    @POST("/api/auth/google")
    suspend fun googleLogin(@Body request: GoogleTokenRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/api/auth/current")
    suspend fun validateToken(@Header("Authorization") token: String): Response<Unit>

    @POST("/api/auth/register")
    suspend fun register(@Body request: ClientRegisterRequest): Response<RegisterResponse>

    @POST("/api/auth/register")
    suspend fun restaurantRegister(@Body request: RestaurantRegisterRequest): Response<RegisterResponse>

}

data class RestaurantDto(
    val restaurantName: String,
    val imageURL: String? = null,
    val rating: Float,
    val views: Int
)

interface ReservationApi {
    @GET("/api/reservations/me/upcoming")
    suspend fun getMyUpcomingReservations(@Header("Authorization") token: String): Response<List<ReservationDto>>

    @DELETE("api/reservations/{id}")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("id") reservationId: Long
    ): Response<Unit>
}

interface RestaurantApi {
    @GET("/api/") // TODO set favourites endpoint
    suspend fun getMyFavourites(@Header("Authorization") token: String): Response<List<RestaurantDto>>
}


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val reservationApi: ReservationApi by lazy {
        retrofit.create(ReservationApi::class.java)
    }

    val restaurantApi: RestaurantApi by lazy {
        retrofit.create(RestaurantApi::class.java)
    }
}