package com.example.gazoapp.model

import java.time.LocalDateTime

/**
 * Mejora de innovación "Mis cargas": abastecimiento ficticio registrado por el
 * usuario para una estación, con fines de seguimiento personal de gasto.
 */
data class CargaCombustible(
    val id: Int,
    val gasolineraId: Int,
    val tipoCombustible: TipoCombustible,
    val litros: Double,
    val precioPorLitro: Double,
    val fecha: LocalDateTime
) {
    val costoTotal: Double
        get() = litros * precioPorLitro
}
