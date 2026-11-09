package com.example.gazoapp.model

import java.time.LocalDateTime

/**
 * Reporte manual, no una medición en vivo: la antigüedad de `fechaHora` debe
 * mostrarse siempre junto al nivel para no sugerir un estado "actual".
 */
data class ReporteSaturacion(
    val id: Int,
    val gasolineraId: Int,
    val vehiculosEnEspera: Int,
    val esperaMinutos: Int,
    val fechaHora: LocalDateTime
) {
    val nivel: NivelSaturacion
        get() = NivelSaturacion.desdeVehiculosEnEspera(vehiculosEnEspera)
}
