package com.example.gazoapp.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    // Caso 2: rechazar campos obligatorios vacíos.
    @Test
    fun `validarObligatorio rechaza vacio y blanco, acepta texto`() {
        assertNotNull(Validaciones.validarObligatorio("", "El nombre"))
        assertNotNull(Validaciones.validarObligatorio("   ", "El nombre"))
        assertNull(Validaciones.validarObligatorio("Estación A", "El nombre"))
    }

    @Test
    fun `validarAlMenosUnCombustible exige al menos un precio`() {
        assertNotNull(Validaciones.validarAlMenosUnCombustible(null, null, null))
        assertNull(Validaciones.validarAlMenosUnCombustible(23.20, null, null))
    }

    // Caso 3: rechazar precios negativos o cero.
    @Test
    fun `validarPrecio rechaza cero y negativos, acepta null y positivos`() {
        assertNull(Validaciones.validarPrecio(null))
        assertNotNull(Validaciones.validarPrecio(0.0))
        assertNotNull(Validaciones.validarPrecio(-5.0))
        assertNull(Validaciones.validarPrecio(23.20))
    }

    @Test
    fun `validarDistancia acepta cero y positivos, rechaza negativos`() {
        assertNull(Validaciones.validarDistancia(0.0))
        assertNull(Validaciones.validarDistancia(6.0))
        assertNotNull(Validaciones.validarDistancia(-1.0))
    }

    @Test
    fun `validarLitros rechaza cero y negativos`() {
        assertNotNull(Validaciones.validarLitros(0.0))
        assertNotNull(Validaciones.validarLitros(-10.0))
        assertNull(Validaciones.validarLitros(20.0))
    }

    // Caso 14: rechazar rendimiento igual a cero.
    @Test
    fun `validarRendimiento rechaza cero y negativos`() {
        assertNotNull(Validaciones.validarRendimiento(0.0))
        assertNotNull(Validaciones.validarRendimiento(-15.0))
        assertNull(Validaciones.validarRendimiento(15.0))
    }

    @Test
    fun `validarNoNegativo rechaza negativos, acepta cero`() {
        assertNotNull(Validaciones.validarNoNegativo(-1, "La espera"))
        assertNull(Validaciones.validarNoNegativo(0, "La espera"))
    }

    @Test
    fun `validarGasolinera acumula todos los errores de una estacion invalida`() {
        val errores = Validaciones.validarGasolinera(
            nombre = "",
            direccion = "",
            municipio = "",
            precioRegular = null,
            precioPremium = null,
            precioDiesel = -1.0,
            distanciaDesvioIdaKm = -2.0
        )
        // nombre, direccion, municipio, precio diésel negativo, distancia negativa.
        // (el diésel sí está presente -aunque inválido-, así que "al menos un combustible" no suma error aparte).
        assertEquals(5, errores.size)
    }

    @Test
    fun `validarGasolinera reporta sin combustible cuando los tres precios son null`() {
        val errores = Validaciones.validarGasolinera(
            nombre = "Estación A",
            direccion = "Av. Reforma 100",
            municipio = "Centro",
            precioRegular = null,
            precioPremium = null,
            precioDiesel = null,
            distanciaDesvioIdaKm = 6.0
        )
        assertEquals(1, errores.size)
    }

    @Test
    fun `validarGasolinera no reporta errores para una estacion valida`() {
        val errores = Validaciones.validarGasolinera(
            nombre = "Estación A",
            direccion = "Av. Reforma 100",
            municipio = "Centro",
            precioRegular = 23.20,
            precioPremium = null,
            precioDiesel = null,
            distanciaDesvioIdaKm = 6.0
        )
        assertTrue(errores.isEmpty())
    }

    @Test
    fun `validarEvaluacion exige calificaciones entre 1 y 5`() {
        assertEquals(3, Validaciones.validarEvaluacion(0, 6, -1).size)
        assertTrue(Validaciones.validarEvaluacion(5, 4, 3).isEmpty())
    }

    @Test
    fun `validarReporteSaturacion rechaza valores negativos`() {
        assertEquals(2, Validaciones.validarReporteSaturacion(-1, -5).size)
        assertTrue(Validaciones.validarReporteSaturacion(0, 0).isEmpty())
    }

    @Test
    fun `validarCargaCombustible rechaza litros o precio invalidos`() {
        assertEquals(2, Validaciones.validarCargaCombustible(0.0, 0.0).size)
        assertTrue(Validaciones.validarCargaCombustible(20.0, 23.20).isEmpty())
    }
}
