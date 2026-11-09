package com.example.gazoapp.model

import java.time.LocalDateTime

/**
 * Vinculada al ID estable de la gasolinera, nunca a su posición en el arreglo.
 */
data class Evaluacion(
    val id: Int,
    val gasolineraId: Int,
    val atencion: Int,
    val limpieza: Int,
    val rapidez: Int,
    val comentario: String? = null,
    val fecha: LocalDateTime
) {
    val calificacion: Double
        get() = (atencion + limpieza + rapidez) / 3.0
}
