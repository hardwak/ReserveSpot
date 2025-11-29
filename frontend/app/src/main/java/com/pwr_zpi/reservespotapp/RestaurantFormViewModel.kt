package com.pwr_zpi.reservespotapp

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class RestaurantFormViewModel : ViewModel() {
    var name = mutableStateOf("")
    var address = mutableStateOf("")
    var city = mutableStateOf("")
    var description = mutableStateOf("")
    var latitude = mutableStateOf("")
    var longitude = mutableStateOf("")

    val openingHours = mutableStateMapOf(
        "monday" to "10:00-22:00",
        "tuesday" to "10:00-22:00",
        "wednesday" to "10:00-22:00",
        "thursday" to "10:00-22:00",
        "friday" to "10:00-23:00",
        "saturday" to "12:00-23:00",
        "sunday" to "12:00-22:00"
    )

    fun updateLocation(lat: Double, lng: Double) {
        latitude.value = lat.toString()
        longitude.value = lng.toString()
    }

    fun clear() {
        name.value = ""
        address.value = ""
        city.value = ""
        description.value = ""
        latitude.value = ""
        longitude.value = ""
    }
}