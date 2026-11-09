package com.example.gazoapp.data

import com.example.gazoapp.model.CargaCombustible
import com.example.gazoapp.model.Evaluacion
import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.RegistroPrecio
import com.example.gazoapp.model.ReporteSaturacion
import com.example.gazoapp.model.TipoCombustible
import java.time.LocalDateTime

/**
 * Concentra los arreglos de cada entidad. Los IDs son contadores monotónicos
 * (regla 2: nunca se reutilizan) y la eliminación de una gasolinera elimina en
 * cascada sus evaluaciones, reportes, cargas y registros de precio (regla 7).
 */
class GazoRepository {

    private val gasolineras = ArregloFijo<Gasolinera>(CAPACIDAD_GASOLINERAS)
    private val evaluaciones = ArregloFijo<Evaluacion>(CAPACIDAD_EVALUACIONES)
    private val reportes = ArregloFijo<ReporteSaturacion>(CAPACIDAD_REPORTES)
    private val cargas = ArregloFijo<CargaCombustible>(CAPACIDAD_CARGAS)
    private val registrosPrecio = ArregloFijo<RegistroPrecio>(CAPACIDAD_REGISTROS_PRECIO)

    private var siguienteIdGasolinera = 1
    private var siguienteIdEvaluacion = 1
    private var siguienteIdReporte = 1
    private var siguienteIdCarga = 1
    private var siguienteIdRegistroPrecio = 1

    // --- Gasolineras (RF-01, RF-02, RF-03, RF-04) ---

    fun gasolineras(): List<Gasolinera> = gasolineras.aLista()

    fun buscarGasolinera(id: Int): Gasolinera? = gasolineras.buscarPorId(id) { it.id }

    fun existeGasolinera(id: Int): Boolean = buscarGasolinera(id) != null

    fun agregarGasolinera(
        nombre: String,
        direccion: String,
        municipio: String,
        distanciaDesvioIdaKm: Double,
        marca: String = "",
        precioRegular: Double? = null,
        precioPremium: Double? = null,
        precioDiesel: Double? = null,
        activa: Boolean = true,
        fecha: LocalDateTime = LocalDateTime.now()
    ): Gasolinera? {
        val gasolinera = Gasolinera(
            id = siguienteIdGasolinera,
            nombre = nombre,
            marca = marca,
            direccion = direccion,
            municipio = municipio,
            precioRegular = precioRegular,
            precioPremium = precioPremium,
            precioDiesel = precioDiesel,
            distanciaDesvioIdaKm = distanciaDesvioIdaKm,
            activa = activa
        )
        if (!gasolineras.agregar(gasolinera)) return null
        siguienteIdGasolinera++

        precioRegular?.let { registrarPrecio(gasolinera.id, TipoCombustible.REGULAR, it, fecha) }
        precioPremium?.let { registrarPrecio(gasolinera.id, TipoCombustible.PREMIUM, it, fecha) }
        precioDiesel?.let { registrarPrecio(gasolinera.id, TipoCombustible.DIESEL, it, fecha) }
        return gasolinera
    }

    /** Edita el mismo objeto sin duplicarlo ni cambiar su ID (RF-03). */
    fun actualizarGasolinera(
        id: Int,
        nombre: String,
        direccion: String,
        municipio: String,
        distanciaDesvioIdaKm: Double,
        marca: String = "",
        precioRegular: Double? = null,
        precioPremium: Double? = null,
        precioDiesel: Double? = null,
        activa: Boolean = true,
        fecha: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        val estacion = buscarGasolinera(id) ?: return false
        val precioRegularAnterior = estacion.precioRegular
        val precioPremiumAnterior = estacion.precioPremium
        val precioDieselAnterior = estacion.precioDiesel

        estacion.nombre = nombre
        estacion.marca = marca
        estacion.direccion = direccion
        estacion.municipio = municipio
        estacion.precioRegular = precioRegular
        estacion.precioPremium = precioPremium
        estacion.precioDiesel = precioDiesel
        estacion.distanciaDesvioIdaKm = distanciaDesvioIdaKm
        estacion.activa = activa

        if (precioRegular != null && precioRegular != precioRegularAnterior) {
            registrarPrecio(id, TipoCombustible.REGULAR, precioRegular, fecha)
        }
        if (precioPremium != null && precioPremium != precioPremiumAnterior) {
            registrarPrecio(id, TipoCombustible.PREMIUM, precioPremium, fecha)
        }
        if (precioDiesel != null && precioDiesel != precioDieselAnterior) {
            registrarPrecio(id, TipoCombustible.DIESEL, precioDiesel, fecha)
        }
        return true
    }

    /** Elimina la gasolinera y, en cascada, sus evaluaciones, reportes, cargas y registros de precio. */
    fun eliminarGasolinera(id: Int): Boolean {
        val eliminada = gasolineras.eliminarSi { it.id == id } > 0
        if (eliminada) {
            evaluaciones.eliminarSi { it.gasolineraId == id }
            reportes.eliminarSi { it.gasolineraId == id }
            cargas.eliminarSi { it.gasolineraId == id }
            registrosPrecio.eliminarSi { it.gasolineraId == id }
        }
        return eliminada
    }

    // --- Evaluaciones de servicio (RF-07) ---

    fun evaluaciones(): List<Evaluacion> = evaluaciones.aLista()

    fun evaluacionesDe(gasolineraId: Int): List<Evaluacion> =
        evaluaciones.aLista().filter { it.gasolineraId == gasolineraId }

    fun agregarEvaluacion(
        gasolineraId: Int,
        atencion: Int,
        limpieza: Int,
        rapidez: Int,
        comentario: String? = null,
        fecha: LocalDateTime = LocalDateTime.now()
    ): Evaluacion? {
        if (!existeGasolinera(gasolineraId)) return null
        val evaluacion = Evaluacion(siguienteIdEvaluacion, gasolineraId, atencion, limpieza, rapidez, comentario, fecha)
        if (!evaluaciones.agregar(evaluacion)) return null
        siguienteIdEvaluacion++
        return evaluacion
    }

    // --- Reportes de saturación (RF-08) ---

    fun reportes(): List<ReporteSaturacion> = reportes.aLista()

    fun reportesDe(gasolineraId: Int): List<ReporteSaturacion> =
        reportes.aLista().filter { it.gasolineraId == gasolineraId }

    fun agregarReporteSaturacion(
        gasolineraId: Int,
        vehiculosEnEspera: Int,
        esperaMinutos: Int,
        fechaHora: LocalDateTime = LocalDateTime.now()
    ): ReporteSaturacion? {
        if (!existeGasolinera(gasolineraId)) return null
        val reporte = ReporteSaturacion(siguienteIdReporte, gasolineraId, vehiculosEnEspera, esperaMinutos, fechaHora)
        if (!reportes.agregar(reporte)) return null
        siguienteIdReporte++
        return reporte
    }

    // --- Mis cargas (mejora de innovación) ---

    fun cargas(): List<CargaCombustible> = cargas.aLista()

    fun cargasDe(gasolineraId: Int): List<CargaCombustible> =
        cargas.aLista().filter { it.gasolineraId == gasolineraId }

    fun agregarCargaCombustible(
        gasolineraId: Int,
        tipoCombustible: TipoCombustible,
        litros: Double,
        precioPorLitro: Double,
        fecha: LocalDateTime = LocalDateTime.now()
    ): CargaCombustible? {
        if (!existeGasolinera(gasolineraId)) return null
        val carga = CargaCombustible(siguienteIdCarga, gasolineraId, tipoCombustible, litros, precioPorLitro, fecha)
        if (!cargas.agregar(carga)) return null
        siguienteIdCarga++
        return carga
    }

    // --- Historial de precios (mejora de innovación) ---

    fun registrosPrecio(): List<RegistroPrecio> = registrosPrecio.aLista()

    fun registrosPrecioDe(gasolineraId: Int, tipoCombustible: TipoCombustible? = null): List<RegistroPrecio> =
        registrosPrecio.aLista().filter {
            it.gasolineraId == gasolineraId && (tipoCombustible == null || it.tipoCombustible == tipoCombustible)
        }

    private fun registrarPrecio(
        gasolineraId: Int,
        tipoCombustible: TipoCombustible,
        precio: Double,
        fecha: LocalDateTime
    ): RegistroPrecio? {
        val registro = RegistroPrecio(siguienteIdRegistroPrecio, gasolineraId, tipoCombustible, precio, fecha)
        if (!registrosPrecio.agregar(registro)) return null
        siguienteIdRegistroPrecio++
        return registro
    }

    companion object {
        const val CAPACIDAD_GASOLINERAS = 30
        const val CAPACIDAD_EVALUACIONES = 300
        const val CAPACIDAD_REPORTES = 150
        const val CAPACIDAD_CARGAS = 50
        const val CAPACIDAD_REGISTROS_PRECIO = 300
    }
}
