package com.example.gazoapp.logic

import com.example.gazoapp.model.Evaluacion
import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.ReporteSaturacion
import com.example.gazoapp.model.TipoCombustible

enum class CriterioRecomendacion {
    MENOR_PRECIO,
    MENOR_COSTO_ESTIMADO,
    MEJOR_ATENCION,
    MENOR_ESPERA
}

/** Estación sugerida junto con el texto que explica por qué (RF-11). */
data class Recomendacion(
    val gasolinera: Gasolinera,
    val explicacion: String
)

/**
 * Genera una recomendación explicable según uno de los 4 criterios de RF-11.
 * Devuelve `null` cuando no hay datos suficientes para decidir (caso de
 * prueba 17), para que la UI muestre "datos insuficientes".
 */
object Recomendador {

    fun recomendar(
        criterio: CriterioRecomendacion,
        gasolineras: List<Gasolinera>,
        evaluaciones: List<Evaluacion> = emptyList(),
        reportes: List<ReporteSaturacion> = emptyList(),
        tipo: TipoCombustible? = null,
        litrosNecesarios: Double? = null,
        rendimientoKmPorLitro: Double? = null
    ): Recomendacion? = when (criterio) {
        CriterioRecomendacion.MENOR_PRECIO -> recomendarPorMenorPrecio(gasolineras, tipo)
        CriterioRecomendacion.MENOR_COSTO_ESTIMADO ->
            recomendarPorMenorCosto(gasolineras, tipo, litrosNecesarios, rendimientoKmPorLitro)
        CriterioRecomendacion.MEJOR_ATENCION -> recomendarPorMejorAtencion(gasolineras, evaluaciones)
        CriterioRecomendacion.MENOR_ESPERA -> recomendarPorMenorEspera(gasolineras, reportes)
    }

    private fun recomendarPorMenorPrecio(gasolineras: List<Gasolinera>, tipo: TipoCombustible?): Recomendacion? {
        if (tipo == null) return null
        val resultado = Estadisticas.precioMinimo(gasolineras, tipo) ?: return null
        val estacion = resultado.estaciones.first()
        val empate = if (resultado.estaciones.size > 1) {
            " (empatada con ${resultado.estaciones.size - 1} estación(es) más)"
        } else ""
        return Recomendacion(
            estacion,
            "Precio más bajo de ${tipo.etiqueta}: $%.2f por litro%s.".format(resultado.valor, empate)
        )
    }

    private fun recomendarPorMenorCosto(
        gasolineras: List<Gasolinera>,
        tipo: TipoCombustible?,
        litrosNecesarios: Double?,
        rendimientoKmPorLitro: Double?
    ): Recomendacion? {
        if (tipo == null || litrosNecesarios == null || rendimientoKmPorLitro == null) return null
        val mejor = Costos.compararEstaciones(gasolineras, tipo, litrosNecesarios, rendimientoKmPorLitro)
            .firstOrNull() ?: return null
        return Recomendacion(
            mejor.gasolinera,
            "Menor costo total estimado para ${tipo.etiqueta}: $%.2f (carga + desvío).".format(mejor.costoTotal)
        )
    }

    private fun recomendarPorMejorAtencion(gasolineras: List<Gasolinera>, evaluaciones: List<Evaluacion>): Recomendacion? {
        val estacion = Estadisticas.estacionMejorEvaluada(gasolineras, evaluaciones) ?: return null
        val promedio = Estadisticas.promedioEvaluacionesDeEstacion(estacion.id, evaluaciones) ?: return null
        return Recomendacion(estacion, "Mejor atención evaluada: promedio de %.2f sobre 5.".format(promedio))
    }

    private fun recomendarPorMenorEspera(gasolineras: List<Gasolinera>, reportes: List<ReporteSaturacion>): Recomendacion? {
        val ultimoReportePorEstacion = reportes.groupBy { it.gasolineraId }
            .mapValues { (_, reportesDeEstacion) -> reportesDeEstacion.maxBy { it.fechaHora } }
        val mejor = gasolineras.filter { it.activa }
            .mapNotNull { gasolinera -> ultimoReportePorEstacion[gasolinera.id]?.let { gasolinera to it } }
            .minByOrNull { it.second.esperaMinutos } ?: return null
        return Recomendacion(
            mejor.first,
            "Menor espera reportada: ${mejor.second.esperaMinutos} min (según el reporte más reciente)."
        )
    }
}
