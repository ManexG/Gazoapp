package com.example.gazoapp.logic

import com.example.gazoapp.model.Evaluacion
import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.ReporteSaturacion
import com.example.gazoapp.model.TipoCombustible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class RecomendadorTest {

    private val fecha = LocalDateTime.of(2026, 1, 1, 10, 0)

    private fun estacion(id: Int, precioRegular: Double?, distanciaDesvioIdaKm: Double = 1.0) = Gasolinera(
        id = id,
        nombre = "Estación $id",
        direccion = "Calle $id",
        municipio = "Centro",
        precioRegular = precioRegular,
        distanciaDesvioIdaKm = distanciaDesvioIdaKm
    )

    @Test
    fun `recomienda la estacion de menor precio`() {
        val a = estacion(1, precioRegular = 23.20)
        val b = estacion(2, precioRegular = 23.70)

        val recomendacion = Recomendador.recomendar(
            criterio = CriterioRecomendacion.MENOR_PRECIO,
            gasolineras = listOf(a, b),
            tipo = TipoCombustible.REGULAR
        )

        assertEquals(a, recomendacion!!.gasolinera)
    }

    @Test
    fun `menor precio sin tipo de combustible es datos insuficientes`() {
        val a = estacion(1, precioRegular = 23.20)
        assertNull(Recomendador.recomendar(CriterioRecomendacion.MENOR_PRECIO, listOf(a), tipo = null))
    }

    @Test
    fun `recomienda la estacion de menor costo total estimado`() {
        // Ejemplo del README §1: B gana en costo total pese a no tener el precio más bajo.
        val a = estacion(1, precioRegular = 23.20, distanciaDesvioIdaKm = 6.0)
        val b = estacion(2, precioRegular = 23.70, distanciaDesvioIdaKm = 1.0)
        val c = estacion(3, precioRegular = 24.10, distanciaDesvioIdaKm = 2.0)

        val recomendacion = Recomendador.recomendar(
            criterio = CriterioRecomendacion.MENOR_COSTO_ESTIMADO,
            gasolineras = listOf(a, b, c),
            tipo = TipoCombustible.REGULAR,
            litrosNecesarios = 20.0,
            rendimientoKmPorLitro = 15.0
        )

        assertEquals(b, recomendacion!!.gasolinera)
    }

    @Test
    fun `menor costo estimado sin litros o rendimiento es datos insuficientes`() {
        val a = estacion(1, precioRegular = 23.20)
        assertNull(
            Recomendador.recomendar(
                CriterioRecomendacion.MENOR_COSTO_ESTIMADO,
                listOf(a),
                tipo = TipoCombustible.REGULAR,
                litrosNecesarios = null,
                rendimientoKmPorLitro = 15.0
            )
        )
    }

    @Test
    fun `recomienda la estacion con mejor atencion evaluada`() {
        val a = estacion(1, precioRegular = 23.20)
        val b = estacion(2, precioRegular = 23.70)
        val evaluaciones = listOf(
            Evaluacion(1, gasolineraId = 1, atencion = 3, limpieza = 3, rapidez = 3, fecha = fecha),
            Evaluacion(2, gasolineraId = 2, atencion = 5, limpieza = 5, rapidez = 5, fecha = fecha)
        )

        val recomendacion = Recomendador.recomendar(
            criterio = CriterioRecomendacion.MEJOR_ATENCION,
            gasolineras = listOf(a, b),
            evaluaciones = evaluaciones
        )

        assertEquals(b, recomendacion!!.gasolinera)
    }

    // Caso 17: solicitar recomendación de atención sin datos suficientes.
    @Test
    fun `mejor atencion sin evaluaciones es datos insuficientes`() {
        val a = estacion(1, precioRegular = 23.20)
        assertNull(Recomendador.recomendar(CriterioRecomendacion.MEJOR_ATENCION, listOf(a), evaluaciones = emptyList()))
    }

    @Test
    fun `recomienda la estacion con menor espera segun el reporte mas reciente`() {
        val a = estacion(1, precioRegular = 23.20)
        val b = estacion(2, precioRegular = 23.70)
        val reportes = listOf(
            ReporteSaturacion(1, gasolineraId = 1, vehiculosEnEspera = 7, esperaMinutos = 15, fechaHora = fecha),
            ReporteSaturacion(2, gasolineraId = 2, vehiculosEnEspera = 1, esperaMinutos = 3, fechaHora = fecha)
        )

        val recomendacion = Recomendador.recomendar(
            criterio = CriterioRecomendacion.MENOR_ESPERA,
            gasolineras = listOf(a, b),
            reportes = reportes
        )

        assertEquals(b, recomendacion!!.gasolinera)
    }

    // Caso 17: solicitar recomendación de espera sin datos suficientes.
    @Test
    fun `menor espera sin reportes es datos insuficientes`() {
        val a = estacion(1, precioRegular = 23.20)
        assertNull(Recomendador.recomendar(CriterioRecomendacion.MENOR_ESPERA, listOf(a), reportes = emptyList()))
    }
}
