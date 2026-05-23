package com.uniandes.vinilos.model

data class PerformerPrize(
    val id: Int = 0,
    val premiationDate: String,
    val prize: Prize? = null
)
