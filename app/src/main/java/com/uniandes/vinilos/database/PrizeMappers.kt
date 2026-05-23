package com.uniandes.vinilos.database

import com.uniandes.vinilos.database.entities.PrizeEntity
import com.uniandes.vinilos.model.Prize

fun PrizeEntity.toPrize() = Prize(
    id = id,
    name = name,
    description = description,
    organization = organization
)

fun Prize.toEntity() = PrizeEntity(
    id = id,
    name = name,
    description = description,
    organization = organization
)
