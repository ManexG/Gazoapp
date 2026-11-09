package com.example.gazoapp.logic

import com.example.gazoapp.model.Evaluacion
import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.TipoCombustible

/** Resultado de un extremo (mínimo o máximo) que puede tener varias estaciones empatadas (regla 8). */
data class ResultadoExtremo(
    val valor: Double,
    val estaciones: List<Gasolinera>
)

/**
 * Cálculos de mínimos, máximos, promedios de precio y promedios de evaluación
 * (RF-06, RF-07, RF-12). Solo se consideran estaciones activas con precio
 * válido para el combustible pedido (regla 5).
 */
object Estadisticas {

    private fun preciosValidos(gasolineras: List<Gasolinera>, tipo: TipoCombustible): List<Pair<Gasolinera, Double>> =
        gasolineras.filter { it.activa }.mapNotNull { gasolinera ->
            gasolinera.precioPorTipo(tipo)?.let { precio -> gasolinera to precio }
        }

    fun precioMinimo(gasolineras: List<Gasolinera>, tipo: TipoCombustible): ResultadoExtremo? {
        val candidatos = preciosValidos(gasolineras, tipo)
        if (candidatos.isEmpty()) return null
        val valorMinimo = candidatos.minOf { it.second }
        return ResultadoExtremo(valorMinimo, candidatos.filter { it.second == valorMinimo }.map { it.first })
    }

    fun precioMaximo(gasolineras: List<Gasolinera>, tipo: TipoCombustible): ResultadoExtremo? {
        val candidatos = preciosValidos(gasolineras, tipo)
        if (candidatos.isEmpty()) return null
        val valorMaximo = candidatos.maxOf { it.second }
        return ResultadoExtremo(valorMaximo, candidatos.filter { it.second == valorMaximo }.map { it.first })
    }

    fun precioPromedio(gasolineras: List<Gasolinera>, tipo: TipoCombustible): Double? {
        val candidatos = preciosValidos(gasolineras, tipo)
        if (candidatos.isEmpty()) return null
        return candidatos.sumOf { it.second } / candidatos.size
    }

    /**
     * Promedio general de un conjunto de evaluaciones: primero se calcula la
     * calificación de cada evaluación y luego se promedian (regla del README
     * §8). `null` si no hay evaluaciones; nunca se devuelve cero.
     */
    fun promedioEvaluaciones(evaluaciones: List<Evaluacion>): Double? {
        if (evaluaciones.isEmpty()) return null
        return evaluaciones.sumOf { it.calificacion } / evaluaciones.size
    }

    fun promedioEvaluacionesDeEstacion(gasolineraId: Int, evaluaciones: List<Evaluacion>): Double? =
        promedioEvaluaciones(evaluaciones.filter { it.gasolineraId == gasolineraId })

    /** Estación activa con mejor promedio de evaluación, exigiendo al menos [minimoEvaluaciones] (RF-12). */
    fun estacionMejorEvaluada(
        gasolineras: List<Gasolinera>,
        evaluaciones: List<Evaluacion>,
        minimoEvaluaciones: Int = 1
    ): Gasolinera? {
        val candidatos = gasolineras.filter { it.activa }.mapNotNull { gasolinera ->
            val evaluacionesDeEstacion = evaluaciones.filter { it.gasolineraId == gasolinera.id }
            if (evaluacionesDeEstacion.size < minimoEvaluaciones) return@mapNotNull null
            val promedio = promedioEvaluaciones(evaluacionesDeEstacion) ?: return@mapNotNull null
            gasolinera to promedio
        }
        return candidatos.maxByOrNull { it.second }?.first
    }
}
