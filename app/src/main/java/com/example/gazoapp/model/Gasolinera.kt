package com.example.gazoapp.model

/**
 * `distanciaDesvioIdaKm` es una referencia declarada por quien registra la estación,
 * no una distancia geográfica calculada (regla de negocio: sin GPS en esta etapa).
 */
data class Gasolinera(
    val id: Int,
    var nombre: String,
    var marca: String = "",
    var direccion: String,
    var municipio: String,
    var precioRegular: Double? = null,
    var precioPremium: Double? = null,
    var precioDiesel: Double? = null,
    var distanciaDesvioIdaKm: Double,
    var activa: Boolean = true
) {
    fun precioPorTipo(tipo: TipoCombustible): Double? = when (tipo) {
        TipoCombustible.REGULAR -> precioRegular
        TipoCombustible.PREMIUM -> precioPremium
        TipoCombustible.DIESEL -> precioDiesel
    }
}
