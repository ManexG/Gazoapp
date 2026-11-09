package com.example.gazoapp.logic

import com.example.gazoapp.model.CargaCombustible
import com.example.gazoapp.model.RegistroPrecio

enum class TendenciaPrecio { SUBE, BAJA, SIN_CAMBIO }

/**
 * Lógica de las mejoras de innovación "Historial de precios" y "Mis cargas".
 */
object HistorialPrecios {

    fun historialOrdenado(registros: List<RegistroPrecio>): List<RegistroPrecio> =
        registros.sortedBy { it.fecha }

    /** Clasifica la variación entre dos precios consecutivos. */
    fun tendenciaEntre(actual: Double, anterior: Double): TendenciaPrecio = when {
        actual > anterior -> TendenciaPrecio.SUBE
        actual < anterior -> TendenciaPrecio.BAJA
        else -> TendenciaPrecio.SIN_CAMBIO
    }

    /**
     * Compara los dos registros más recientes para clasificar la variación.
     * `null` si hay menos de dos registros, para que la UI muestre "Sin historial".
     */
    fun tendencia(registros: List<RegistroPrecio>): TendenciaPrecio? {
        val ordenado = historialOrdenado(registros)
        if (ordenado.size < 2) return null
        val ultimo = ordenado[ordenado.size - 1]
        val anterior = ordenado[ordenado.size - 2]
        return tendenciaEntre(ultimo.precio, anterior.precio)
    }

    fun costoTotalCargas(cargas: List<CargaCombustible>): Double =
        cargas.sumOf { it.costoTotal }

    fun costoTotalCargasDeEstacion(cargas: List<CargaCombustible>, gasolineraId: Int): Double =
        costoTotalCargas(cargas.filter { it.gasolineraId == gasolineraId })
}
