package com.uniandes.vinilos.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prizes")
data class PrizeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val organization: String
)
