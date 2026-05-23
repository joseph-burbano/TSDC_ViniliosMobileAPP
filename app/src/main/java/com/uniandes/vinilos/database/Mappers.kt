package com.uniandes.vinilos.database

import com.uniandes.vinilos.database.entities.PerformerEntity
import com.uniandes.vinilos.model.Performer

fun PerformerEntity.toPerformer() = Performer(
    id = id,
    name = name,
    image = image,
    description = description,
    birthDate = birthDate,
    creationDate = creationDate,
    albums = albums,
    type = type
)

fun Performer.toEntity() = PerformerEntity(
    id = id,
    name = name,
    image = image,
    description = description,
    birthDate = birthDate,
    creationDate = creationDate,
    albums = albums,
    type = type
)
