package com.example.gazoapp.logic

import com.example.gazoapp.model.CargaCombustible
import com.example.gazoapp.model.RegistroPrecio
import com.example.gazoapp.model.TipoCombustible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class HistorialPreciosTest {

    private val dia1 = LocalDateTime.of(2026, 1, 1, 10, 0)
    private val dia2 = LocalDateTime.of(2026, 1, 5, 10, 0)
    private val dia3 = LocalDateTime.of(2026, 1, 10, 10, 0)

    private fun registro(id: Int, precio: Double, fecha: LocalDateTime) =
        RegistroPrecio(id, gasolineraId = 1, tipoCombustible = TipoCombustible.REGULAR, precio = precio, fecha = fecha)

    @Test
    fun `historialOrdenado ordena por fecha ascendente sin importar el orden de entrada`() {
        val registros = listOf(registro(1, 23.50, dia3), registro(2, 23.20, dia1), registro(3, 23.70, dia2))

        val ordenado = HistorialPrecios.historialOrdenado(registros)

        assertEquals(listOf(dia1, dia2, dia3), ordenado.map { it.fecha })
    }

    @Test
    fun `tendencia es null cuando hay menos de dos registros`() {
        assertNull(HistorialPrecios.tendencia(emptyList()))
        assertNull(HistorialPrecios.tendencia(listOf(registro(1, 23.50, dia1))))
    }

    @Test
    fun `tendencia detecta subida respecto al registro anterior`() {
        val registros = listOf(registro(1, 23.20, dia1), registro(2, 23.70, dia2))
        assertEquals(TendenciaPrecio.SUBE, HistorialPrecios.tendencia(registros))
    }

    @Test
    fun `tendencia detecta bajada respecto al registro anterior`() {
        val registros = listOf(registro(1, 23.70, dia1), registro(2, 23.20, dia2))
        assertEquals(TendenciaPrecio.BAJA, HistorialPrecios.tendencia(registros))
    }

    @Test
    fun `tendencia detecta sin cambio cuando el precio se repite`() {
        val registros = listOf(registro(1, 23.20, dia1), registro(2, 23.20, dia2))
        assertEquals(TendenciaPrecio.SIN_CAMBIO, HistorialPrecios.tendencia(registros))
    }

    @Test
    fun `tendencia usa los dos registros mas recientes, no los primeros`() {
        // dia1 -> dia2 sube, pero dia2 -> dia3 baja: debe reportar BAJA.
        val registros = listOf(registro(1, 23.20, dia1), registro(2, 23.70, dia2), registro(3, 23.40, dia3))
        assertEquals(TendenciaPrecio.BAJA, HistorialPrecios.tendencia(registros))
    }

    @Test
    fun `costoTotalCargas suma el costo de todas las cargas`() {
        val cargas = listOf(
            CargaCombustible(1, gasolineraId = 1, tipoCombustible = TipoCombustible.REGULAR, litros = 20.0, precioPorLitro = 23.20, fecha = dia1),
            CargaCombustible(2, gasolineraId = 2, tipoCombustible = TipoCombustible.DIESEL, litros = 10.0, precioPorLitro = 24.00, fecha = dia2)
        )

        assertEquals(704.0, HistorialPrecios.costoTotalCargas(cargas), 0.001)
    }

    @Test
    fun `costoTotalCargasDeEstacion filtra por gasolineraId`() {
        val cargas = listOf(
            CargaCombustible(1, gasolineraId = 1, tipoCombustible = TipoCombustible.REGULAR, litros = 20.0, precioPorLitro = 23.20, fecha = dia1),
            CargaCombustible(2, gasolineraId = 2, tipoCombustible = TipoCombustible.DIESEL, litros = 10.0, precioPorLitro = 24.00, fecha = dia2)
        )

        assertEquals(464.0, HistorialPrecios.costoTotalCargasDeEstacion(cargas, 1), 0.001)
        assertEquals(0.0, HistorialPrecios.costoTotalCargasDeEstacion(cargas, 99), 0.001)
    }
}
