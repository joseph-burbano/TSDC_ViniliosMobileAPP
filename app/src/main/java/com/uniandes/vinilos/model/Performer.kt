package com.uniandes.vinilos.model

data class Performer(
    val id: Int,
    val name: String,
    val image: String,
    val description: String,
    val birthDate: String? = null,
    val creationDate: String? = null,
    val albums: List<Album> = emptyList(),
    val type: String? = null
) {
    /** Devuelve true si es músico, usando el campo type cuando está disponible
     * y cayendo de vuelta a la heurística birthDate!=null cuando no lo está. */
    val isMusician: Boolean get() = type == "musician" || (type == null && birthDate != null)
}
