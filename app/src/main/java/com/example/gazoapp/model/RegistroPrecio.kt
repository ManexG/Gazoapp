package com.example.gazoapp.model

import java.time.LocalDateTime

/**
 * Mejora de innovación "Historial de precios": fotografía del precio de un
 * combustible en una estación en un momento dado, para construir tendencias
 * locales (sube/baja) sin depender de fuentes externas.
 */
data class RegistroPrecio(
    val id: Int,
    val gasolineraId: Int,
    val tipoCombustible: TipoCombustible,
    val precio: Double,
    val fecha: LocalDateTime
)
