package com.pwr_zpi.reservespotapp

import androidx.compose.ui.semantics.Role
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDateTime
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import retrofit2.http.PUT
import java.time.format.DateTimeFormatter
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.AuthProvider
import java.util.concurrent.TimeUnit


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

data class AvailableReservationSlotDto(
    val tableId: Long,
    val tableCapacity: Int,
    val start: String, // LocalDateTime as String (ISO)
    val end: String,
    val locationInRestaurant: String?
)

data class CreateReservationDto(
    val tableId: Long,
    val reservationDatetime: String, // LocalDateTime in ISO-8601
    val durationMinutes: Int
)

data class ReviewDto(
    val id: Long?,
    val userId: Long?,
    val restaurantId: Long?,
    val phoneNumber: String?,
    val rating: Int?,
    val comment: String?,
    val pic: String?,
    val createdAt: LocalDateTime?,
    val pictureIds: Set<Long>?
)

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

data class RestaurantSearchDto(
    val query: String?,
    val city: String,
    val tagIds: Set<Long>?,
    val minRating: Double?,
    val maxRating: Double?
)

data class TagDto(
    val id: Long,
    val name: String
)

data class UserSummaryDto(
    val id: Long,
    val name: String

)

data class ReviewWithUser(
    val review: ReviewDto,
    val userName: String
)

data class PictureDto(
    val id: Long?,
    val url: String?,
    val uploadedAt: LocalDateTime?,
    val description: String?,
    val restaurantIds: Set<Long> = emptySet(),
    val reviewIds: Set<Long> = emptySet()
)

data class CreateReviewDto(
    val restaurantId: Long,
    val reservationId: Long? = null,
    val phoneNumber: String? = null,
    val rating: Int,
    val comment: String?,
    val pic: String?
)

data class UpdateReviewDto(
    val phoneNumber: String? = null,
    val rating: Int,
    val comment: String?,
    val pic: String?
)

data class ReviewEligibilityResponse(
    val canReview: Boolean,
    val message: String?
)

data class AiSearchRequest(
    val query: String
)

data class AiAnalysisDto(
    val id: Long,
    val restaurantId: Long,
    val summaryText: String?,
    val sentimentScore: Double?,
    val lastUpdated: LocalDateTime?
)

data class UserDto(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val role: Role? = null,
    val oauthProviderId: String? = null,
    val provider: AuthProvider? = null,
    val pictureId: Long? = null,
    val restaurants: List<RestaurantSummary>? = null,
    val reservationIds: List<Long>? = null,
    val reviewIds: List<Long>? = null
) {
    data class RestaurantSummary(
        val id: Long? = null,
        val name: String? = null
    )
}

data class UpdateProfileDto(
    val name: String,
    val email: String,
    val phoneNumber: String?
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


interface ReservationApi {
    @GET("/api/reservations/me/upcoming")
    suspend fun getMyUpcomingReservations(@Header("Authorization") token: String): Response<List<ReservationDto>>

    @DELETE("/api/reservations/{id}")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("id") reservationId: Long
    ): Response<Unit>

    @GET("/api/reservations/availability")
    suspend fun getAvailability(
        @Header("Authorization") token: String,
        @Query("restaurantId") restaurantId: Long,
        @Query("date") date: String, // yyyy-MM-dd
        @Query("durationMinutes") durationMinutes: Int
    ): Response<List<AvailableReservationSlotDto>>

    @POST("/api/reservations")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body createDto: CreateReservationDto
    ): Response<ReservationDto>
}

interface RestaurantApi {
    @GET("/api/")
    suspend fun getMyFavourites(@Header("Authorization") token: String): Response<List<RestaurantDto>>

    @GET("/api/restaurants/recommendations")
    suspend fun getRecommendations(@Header("Authorization") token: String): Response<List<RestaurantDto>>

    //    IT WORKS
    @POST("/api/restaurants/search")
    suspend fun searchRestaurants(
        @Header("Authorization") token: String,
        @Body searchDto: RestaurantSearchDto
    ): Response<List<RestaurantDto>>

    @POST("/api/restaurants/search/ai")
    suspend fun searchRestaurantsAi(
        @Header("Authorization") token: String,
        @Body request: AiSearchRequest
    ): Response<List<RestaurantDto>>

    @GET("/api/tags")
    suspend fun getAvailableTags(@Header("Authorization") token: String): Response<List<TagDto>>

    @GET("/api/restaurants")
    suspend fun getAllRestaurants(@Header("Authorization") token: String): Response<List<RestaurantDto>>

    @GET("/api/restaurants/{id}")
    suspend fun getRestaurantDetails(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<RestaurantDto>
}

interface ReviewsApi {

    @GET("/api/reviews/restaurant/{restaurantId}")
    suspend fun getReviews(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Long
    ): Response<List<ReviewDto>>

    @GET("/api/reviews/can-create")
    suspend fun checkEligibility(
        @Header("Authorization") token: String,
        @Query("restaurantId") restaurantId: Long
    ): Response<ReviewEligibilityResponse>

    @GET("/api/reviews/me")
    suspend fun getMyReviews(
        @Header("Authorization") token: String
    ): Response<List<ReviewDto>>

    @POST("/api/reviews")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Body review: CreateReviewDto
    ): Response<ReviewDto>

    @PUT("/api/reviews/{id}")
    suspend fun updateReview(
        @Header("Authorization") token: String,
        @Path("id") reviewId: Long,
        @Body review: UpdateReviewDto
    ): Response<ReviewDto>

    @DELETE("/api/reviews/{id}")
    suspend fun deleteReview(
        @Header("Authorization") token: String,
        @Path("id") reviewId: Long
    ): Response<Unit>
}


interface UserApi {
    @GET("/api/users/{id}")
    suspend fun getUserDetails(
        @Header("Authorization") token: String,
        @Path("id") userId: Long
    ): Response<UserSummaryDto>

    @GET("/api/users/email/{email}")
    suspend fun getUserByEmail(
        @Header("Authorization") token: String,
        @Path("email") email: String
    ): Response<UserDto>



    @PUT("/api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: UpdateProfileDto
    ): Response<UserDto>
}

interface PicturesApi {
    @GET("/api/pictures")
    suspend fun getPictures(
        @Header("Authorization") token: String,
        @Query("restaurantId") restaurantId: Long
    ): Response<List<PictureDto>>

    @Multipart
    @POST("/api/pictures/file")
    suspend fun uploadPicture(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): Response<PictureDto>
}

interface AiAnalysisApi {
    @GET("/api/ai-analysis/restaurant/{restaurantId}")
    suspend fun getAnalysis(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Long
    ): Response<AiAnalysisDto>

    @POST("/api/ai-analysis/generate/restaurant/{restaurantId}")
    suspend fun generateAnalysis(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Long
    ): Response<AiAnalysisDto>
}


//Restaurant and owners DTOs

data class OwnerRestaurantDto(
    val id: Long? = null,
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
//interface OwnerApi {
//    // Restaurant
//    @GET("/api/restaurants/owner/{ownerId}")
//    suspend fun getMyRestaurants(@Header("Authorization") token: String, @retrofit2.http.Path("ownerId") ownerId: Long): Response<List<OwnerRestaurantDto>>
//
//    @POST("/api/restaurants")
//    suspend fun addRestaurant(@Header("Authorization") token: String, @Body restaurant: OwnerRestaurantDto): Response<OwnerRestaurantDto>
//
//    @retrofit2.http.PUT("/api/restaurants/{id}")
//    suspend fun updateRestaurant(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long, @Body restaurant: OwnerRestaurantDto): Response<OwnerRestaurantDto>
//
//    @retrofit2.http.DELETE("/api/restaurants/{id}")
//    suspend fun deleteRestaurant(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long): Response<Unit>
//
//    @GET("/api/reviews/restaurant/{restaurantId}")
//    suspend fun getRestaurantReviews(@Header("Authorization") token: String, @retrofit2.http.Path("restaurantId") restaurantId: Long): Response<List<ReviewDto>>
//
//    // Tables
//    @GET("/api/tables/restaurant/{restaurantId}") // endpoint for getting tables (nned to check if it ex.)
//    suspend fun getTablesByRestaurant(@Header("Authorization") token: String, @retrofit2.http.Path("restaurantId") restaurantId: Long): Response<List<TableDto>>
//
//    @POST("/api/tables")
//    suspend fun addTable(@Header("Authorization") token: String, @Body table: TableDto): Response<TableDto>
//
//    @retrofit2.http.DELETE("/api/tables/{id}")
//    suspend fun deleteTable(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long): Response<Unit>
//
//    // Reservations
//    @GET("/api/reservations/owner/upcoming")
//    suspend fun getOwnerUpcomingReservations(@Header("Authorization") token: String): Response<List<OwnerReservationDto>>
//
//    @retrofit2.http.DELETE("/api/reservations/{id}")
//    suspend fun cancelReservation(@Header("Authorization") token: String, @retrofit2.http.Path("id") id: Long): Response<Unit>
//}


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val localDateTimeDeserializer: JsonDeserializer<LocalDateTime> =
        JsonDeserializer { json, _, _ ->
            LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, localDateTimeDeserializer)
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
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

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val reviewsApi: ReviewsApi by lazy {
        retrofit.create(ReviewsApi::class.java)
    }

    val picturesApi: PicturesApi by lazy {
        retrofit.create(PicturesApi::class.java)
    }

    val aiAnalysisApi: AiAnalysisApi by lazy {
        retrofit.create(AiAnalysisApi::class.java)
    }
}