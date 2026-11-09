package com.example.gazoapp.logic

import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.TipoCombustible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CostosTest {

    private fun estacion(
        id: Int,
        precioRegular: Double?,
        distanciaDesvioIdaKm: Double,
        activa: Boolean = true
    ) = Gasolinera(
        id = id,
        nombre = "Estación $id",
        direccion = "Calle $id",
        municipio = "Centro",
        precioRegular = precioRegular,
        distanciaDesvioIdaKm = distanciaDesvioIdaKm,
        activa = activa
    )

    // Caso 12: calcular el importe de una carga con dos decimales.
    @Test
    fun `costoCarga multiplica litros por precio y redondea a dos decimales`() {
        assertEquals(464.0, Costos.costoCarga(20.0, 23.20), 0.001)
        assertEquals(155.67, Costos.costoCarga(10.0, 15.567), 0.001)
    }

    @Test
    fun `distanciaExtraTotal es el desvio de ida multiplicado por dos`() {
        assertEquals(12.0, Costos.distanciaExtraTotal(6.0), 0.001)
    }

    // Caso 14: rechazar rendimiento igual a cero.
    @Test
    fun `litrosDesvio es null cuando el rendimiento no es positivo`() {
        assertNull(Costos.litrosDesvio(12.0, 0.0))
        assertNull(Costos.litrosDesvio(12.0, -5.0))
        assertEquals(0.8, Costos.litrosDesvio(12.0, 15.0)!!, 0.001)
    }

    @Test
    fun `costoTotalEstimado suma carga y costo extra del desvio`() {
        // Estación A del README §1: 20 L de regular a $23.20, desvío de 6 km, rendimiento 15 km-L.
        val total = Costos.costoTotalEstimado(
            litros = 20.0,
            precioPorLitro = 23.20,
            distanciaDesvioIdaKm = 6.0,
            rendimientoKmPorLitro = 15.0
        )
        assertEquals(482.56, total!!, 0.001)
    }

    @Test
    fun `costoTotalEstimado es null cuando el rendimiento no es positivo`() {
        assertNull(Costos.costoTotalEstimado(20.0, 23.20, 6.0, 0.0))
    }

    // Caso 13: comparar estaciones considerando desvío y rendimiento (ejemplo del README §1).
    @Test
    fun `compararEstaciones ordena por costo total, no solo por precio por litro`() {
        val a = estacion(1, precioRegular = 23.20, distanciaDesvioIdaKm = 6.0)
        val b = estacion(2, precioRegular = 23.70, distanciaDesvioIdaKm = 1.0)
        val c = estacion(3, precioRegular = 24.10, distanciaDesvioIdaKm = 2.0)

        val resultados = Costos.compararEstaciones(
            gasolineras = listOf(a, b, c),
            tipo = TipoCombustible.REGULAR,
            litrosNecesarios = 20.0,
            rendimientoKmPorLitro = 15.0
        )

        // B gana en costo total aunque A tenga el precio por litro más bajo: su desvío es mucho mayor.
        assertEquals(listOf(b, a, c), resultados.map { it.gasolinera })
        assertEquals(477.16, resultados.first { it.gasolinera == b }.costoTotal, 0.01)
        assertEquals(482.56, resultados.first { it.gasolinera == a }.costoTotal, 0.01)
    }

    @Test
    fun `compararEstaciones excluye inactivas y sin precio del combustible`() {
        val inactiva = estacion(4, precioRegular = 10.0, distanciaDesvioIdaKm = 1.0, activa = false)
        val sinPrecio = estacion(5, precioRegular = null, distanciaDesvioIdaKm = 1.0)
        val activa = estacion(6, precioRegular = 23.20, distanciaDesvioIdaKm = 1.0)

        val resultados = Costos.compararEstaciones(listOf(inactiva, sinPrecio, activa), TipoCombustible.REGULAR, 20.0, 15.0)
        assertEquals(listOf(activa), resultados.map { it.gasolinera })
    }

    @Test
    fun `compararEstaciones es vacia cuando el rendimiento no es positivo`() {
        val a = estacion(1, precioRegular = 23.20, distanciaDesvioIdaKm = 6.0)
        assertTrue(Costos.compararEstaciones(listOf(a), TipoCombustible.REGULAR, 20.0, 0.0).isEmpty())
    }
}
