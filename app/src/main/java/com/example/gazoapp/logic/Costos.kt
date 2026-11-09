package com.example.gazoapp.logic

import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.TipoCombustible
import kotlin.math.round

/** Estación evaluada por la calculadora de desvíos, ordenable por costo total (RF-10). */
data class ResultadoComparacion(
    val gasolinera: Gasolinera,
    val costoCarga: Double,
    val costoExtraDesvio: Double,
    val costoTotal: Double
)

/**
 * Fórmulas de costo de carga y de desvío (RF-09, RF-10), según el README §8:
 * costo de carga = litros × precio; distancia extra = desvío de ida × 2;
 * litros de desvío = distancia extra / rendimiento; costo total = carga + extra.
 */
object Costos {

    private fun redondear2(valor: Double): Double = round(valor * 100.0) / 100.0

    fun costoCarga(litros: Double, precioPorLitro: Double): Double =
        redondear2(litros * precioPorLitro)

    fun distanciaExtraTotal(distanciaDesvioIdaKm: Double): Double =
        distanciaDesvioIdaKm * 2.0

    /** `null` si el rendimiento es inválido (≤ 0), para evitar una división entre cero. */
    fun litrosDesvio(distanciaExtraTotal: Double, rendimientoKmPorLitro: Double): Double? {
        if (rendimientoKmPorLitro <= 0.0) return null
        return distanciaExtraTotal / rendimientoKmPorLitro
    }

    fun costoExtraDesvio(litrosDesvio: Double, precioPorLitro: Double): Double =
        redondear2(litrosDesvio * precioPorLitro)

    /** Costo total estimado (carga + desvío) para una sola estación. `null` si el rendimiento es inválido. */
    fun costoTotalEstimado(
        litros: Double,
        precioPorLitro: Double,
        distanciaDesvioIdaKm: Double,
        rendimientoKmPorLitro: Double
    ): Double? {
        val litrosDesvio = litrosDesvio(distanciaExtraTotal(distanciaDesvioIdaKm), rendimientoKmPorLitro) ?: return null
        return redondear2(costoCarga(litros, precioPorLitro) + costoExtraDesvio(litrosDesvio, precioPorLitro))
    }

    /**
     * Compara estaciones activas que ofrecen [tipo], ordenadas por costo total
     * ascendente (RF-10). Excluye inactivas y sin precio del combustible (regla 5);
     * devuelve lista vacía si el rendimiento es inválido.
     */
    fun compararEstaciones(
        gasolineras: List<Gasolinera>,
        tipo: TipoCombustible,
        litrosNecesarios: Double,
        rendimientoKmPorLitro: Double
    ): List<ResultadoComparacion> {
        if (rendimientoKmPorLitro <= 0.0) return emptyList()
        return gasolineras.filter { it.activa }
            .mapNotNull { gasolinera ->
                val precio = gasolinera.precioPorTipo(tipo) ?: return@mapNotNull null
                val costoCarga = costoCarga(litrosNecesarios, precio)
                val litrosDesvio = litrosDesvio(distanciaExtraTotal(gasolinera.distanciaDesvioIdaKm), rendimientoKmPorLitro)
                    ?: return@mapNotNull null
                val costoExtra = costoExtraDesvio(litrosDesvio, precio)
                ResultadoComparacion(gasolinera, costoCarga, costoExtra, redondear2(costoCarga + costoExtra))
            }
            .sortedBy { it.costoTotal }
    }
}
