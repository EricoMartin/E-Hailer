package com.basebox.ehailerrider.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User (
    @PrimaryKey
    val id: Int? = null,
    val username: String,
    val phone: String,
    val address: String,
    val nIN: String,
    val password: String,
    val isDriver: String

)

