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

data class ReviewDto(
    val id: Long,
    val userId: Long,
    val userName: String,
    val restaurantId: Long,
    val rating: Int,
    val comment: String,
    val date: String
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
    val id: Long,
    val ownerId: Long,
    val name: String,
    val address: String,
    val city: String,
    val description: String,
    val openingHours: Map<String, String>,
    val averageRating: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val pic: String?,
    val tableIds: Set<Long>,
    val reviewIds: Set<Long>,
    val aiAnalysisIds: Set<Long>,
    val statisticIds: Set<Long>,
    val tagIds: Set<Long>,
    val pictureIds: Set<Long>,
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
    @GET("/api/users/me/favorites")
    suspend fun getMyFavourites(@Header("Authorization") token: String): Response<List<RestaurantDto>>

    @POST("api/users/me/favorites/{restaurantId}")
    suspend fun addFavourite(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Long
    ): Response<Unit>

    @DELETE("api/users/me/favorites/{restaurantId}")
    suspend fun removeFavourite(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Long
    ): Response<Unit>

    @GET("api/users/me/favorites/{restaurantId}/check")
    suspend fun checkFavourite(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Long
    ): Response<Boolean>


    @GET("/api/restaurants/recommendations")
    suspend fun getRecommendations(@Header("Authorization") token: String): Response<List<RestaurantDto>>
}

//Restaurant and owners DTOs

data class OwnerRestaurantDto(
    val id: Long? = null, // Null when adding new
    val ownerId: Long,
    val name: String,
    val address: String,
    val city: String,
    val description: String,
    val openingHours: Map<String, String>,
    val pic: String? = null,
    val averageRating: Double? = 0.0,
    val tableIds: List<Long>? = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null
)

// DTO for table
data class TableDto(
    val id: Long? = null,
    val restaurantId: Long,
    val tableCapacity: Int,
    val locationInRestaurant: String
)

// DTO for reservations (owner view - /api/reservations/owner/upcoming)
data class OwnerReservationDto(
    val id: Long,
    val userId: Long,
    val tableId: Long,
    val reservationDatetime: String, //ISO format
    val durationMinutes: Int,
    val status: String // PENDING, CONFIRMED etc.
)

// API Interface for owner
interface OwnerApi {
    // Restaurant
    @GET("/api/restaurants/owner/{ownerId}")
    suspend fun getMyRestaurants(@Header("Authorization") token: String, @retrofit2.http.Path("ownerId") ownerId: Long): Response<List<OwnerRestaurantDto>>

    @POST("/api/restaurants")
    suspend fun addRestaurant(@Header("Authorization") token: String, @Body restaurant: OwnerRestaurantDto): Response<OwnerRestaurantDto>

    @retrofit2.http.PUT("/api/restaurants/{id}")
    suspend fun updateRestaurant(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long, @Body restaurant: OwnerRestaurantDto): Response<OwnerRestaurantDto>

    @retrofit2.http.DELETE("/api/restaurants/{id}")
    suspend fun deleteRestaurant(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long): Response<Unit>

    @GET("/api/reviews/restaurant/{restaurantId}")
    suspend fun getRestaurantReviews(@Header("Authorization") token: String, @retrofit2.http.Path("restaurantId") restaurantId: Long): Response<List<ReviewDto>>

    // Tables
    @GET("/api/tables/restaurant/{restaurantId}") // endpoint for getting tables (nned to check if it ex.)
    suspend fun getTablesByRestaurant(@Header("Authorization") token: String, @retrofit2.http.Path("restaurantId") restaurantId: Long): Response<List<TableDto>>

    @POST("/api/tables")
    suspend fun addTable(@Header("Authorization") token: String, @Body table: TableDto): Response<TableDto>

    @retrofit2.http.DELETE("/api/tables/{id}")
    suspend fun deleteTable(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long): Response<Unit>

    // Reservations
    @GET("/api/reservations/owner/upcoming")
    suspend fun getOwnerUpcomingReservations(@Header("Authorization") token: String): Response<List<OwnerReservationDto>>

    @retrofit2.http.DELETE("/api/reservations/{id}")
    suspend fun cancelReservation(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long): Response<Unit>
}


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"


//    private val gson = GsonBuilder()
//
//        .registerTypeAdapter(LocalDateTime::class.java, com.google.gson.internal.bind.TypeAdapters.get(LocalDateTime::class.java))
//
//        .create()
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

    val ownerApi: OwnerApi by lazy {
        retrofit.create(OwnerApi::class.java)
    }
}