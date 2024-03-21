package com.basebox.ehailerrider.data

import com.google.android.gms.maps.model.LatLng

data class Trip(
    val id: Int? = null,
    val start: LatLng,
    val stop: LatLng,
    val driver: String,
    val client: String,
    val completed: Boolean,
    val started: Boolean,
    val  is_canceled: Boolean,
    val driver_arrived: Boolean,
    val user_rating: String? = null,
    val driver_rating: String? = null
)
