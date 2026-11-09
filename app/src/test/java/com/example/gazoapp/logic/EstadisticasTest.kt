package com.example.gazoapp.logic

import com.example.gazoapp.model.Evaluacion
import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.TipoCombustible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class EstadisticasTest {

    private val fecha = LocalDateTime.of(2026, 1, 1, 10, 0)

    private fun estacion(
        id: Int,
        precioRegular: Double? = null,
        activa: Boolean = true
    ) = Gasolinera(
        id = id,
        nombre = "Estación $id",
        direccion = "Calle $id",
        municipio = "Centro",
        precioRegular = precioRegular,
        distanciaDesvioIdaKm = 1.0,
        activa = activa
    )

    // Caso 7: mínimo, máximo y promedio con valores conocidos (ejemplo del README §1).
    @Test
    fun `precioMinimo maximo y promedio con valores conocidos`() {
        val a = estacion(1, precioRegular = 23.20)
        val b = estacion(2, precioRegular = 23.70)
        val c = estacion(3, precioRegular = 24.10)
        val gasolineras = listOf(a, b, c)

        assertEquals(23.20, Estadisticas.precioMinimo(gasolineras, TipoCombustible.REGULAR)!!.valor, 0.001)
        assertEquals(listOf(a), Estadisticas.precioMinimo(gasolineras, TipoCombustible.REGULAR)!!.estaciones)
        assertEquals(24.10, Estadisticas.precioMaximo(gasolineras, TipoCombustible.REGULAR)!!.valor, 0.001)
        assertEquals(23.666666, Estadisticas.precioPromedio(gasolineras, TipoCombustible.REGULAR)!!, 0.001)
    }

    // Caso 8: empate de precio mínimo mostrado correctamente.
    @Test
    fun `precioMinimo reporta todas las estaciones empatadas`() {
        val a = estacion(1, precioRegular = 23.20)
        val d = estacion(4, precioRegular = 23.20)
        val c = estacion(3, precioRegular = 24.10)

        val resultado = Estadisticas.precioMinimo(listOf(a, d, c), TipoCombustible.REGULAR)!!
        assertEquals(23.20, resultado.valor, 0.001)
        assertEquals(setOf(a, d), resultado.estaciones.toSet())
    }

    @Test
    fun `precioMinimo excluye inactivas y sin precio del combustible`() {
        val inactiva = estacion(5, precioRegular = 10.0, activa = false)
        val sinPrecio = estacion(6, precioRegular = null)
        val activa = estacion(7, precioRegular = 23.20)

        val resultado = Estadisticas.precioMinimo(listOf(inactiva, sinPrecio, activa), TipoCombustible.REGULAR)!!
        assertEquals(listOf(activa), resultado.estaciones)
    }

    @Test
    fun `precioMinimo es null cuando no hay estaciones elegibles`() {
        assertNull(Estadisticas.precioMinimo(emptyList(), TipoCombustible.REGULAR))
    }

    // Caso 9: registrar varias evaluaciones y verificar el promedio.
    @Test
    fun `promedioEvaluaciones promedia la calificacion de cada evaluacion`() {
        val e1 = Evaluacion(1, gasolineraId = 1, atencion = 5, limpieza = 4, rapidez = 3, fecha = fecha) // calificacion 4.0
        val e2 = Evaluacion(2, gasolineraId = 1, atencion = 5, limpieza = 5, rapidez = 5, fecha = fecha) // calificacion 5.0

        assertEquals(4.5, Estadisticas.promedioEvaluaciones(listOf(e1, e2))!!, 0.001)
    }

    // Caso 10: mostrar "Sin evaluaciones" cuando no existan (nunca cero).
    @Test
    fun `promedioEvaluaciones es null cuando no hay evaluaciones`() {
        assertNull(Estadisticas.promedioEvaluaciones(emptyList()))
    }

    @Test
    fun `promedioEvaluacionesDeEstacion filtra por gasolineraId`() {
        val e1 = Evaluacion(1, gasolineraId = 1, atencion = 5, limpieza = 5, rapidez = 5, fecha = fecha)
        val e2 = Evaluacion(2, gasolineraId = 2, atencion = 1, limpieza = 1, rapidez = 1, fecha = fecha)

        assertEquals(5.0, Estadisticas.promedioEvaluacionesDeEstacion(1, listOf(e1, e2))!!, 0.001)
        assertNull(Estadisticas.promedioEvaluacionesDeEstacion(3, listOf(e1, e2)))
    }

    @Test
    fun `estacionMejorEvaluada elige el mayor promedio respetando el minimo de evaluaciones`() {
        val a = estacion(1, precioRegular = 23.20)
        val b = estacion(2, precioRegular = 23.70)
        val evaluacionesA = listOf(Evaluacion(1, gasolineraId = 1, atencion = 3, limpieza = 3, rapidez = 3, fecha = fecha))
        val evaluacionesB = listOf(
            Evaluacion(2, gasolineraId = 2, atencion = 5, limpieza = 5, rapidez = 5, fecha = fecha),
            Evaluacion(3, gasolineraId = 2, atencion = 4, limpieza = 4, rapidez = 4, fecha = fecha)
        )
        val todas = evaluacionesA + evaluacionesB

        assertEquals(b, Estadisticas.estacionMejorEvaluada(listOf(a, b), todas))
        assertNull(Estadisticas.estacionMejorEvaluada(listOf(a, b), todas, minimoEvaluaciones = 3))
    }
}
