
package com.example.gazoapp.data

import com.example.gazoapp.model.CargaCombustible
import com.example.gazoapp.model.Evaluacion
import com.example.gazoapp.model.Gasolinera
import com.example.gazoapp.model.RegistroPrecio
import com.example.gazoapp.model.ReporteSaturacion
import com.example.gazoapp.model.TipoCombustible
import java.time.LocalDateTime

/**
 * Concentra los arreglos de cada entidad.
 *
 * Los IDs son contadores monotónicos y nunca se reutilizan.
 * Al eliminar una gasolinera también se eliminan sus registros relacionados.
 */
class GazoRepository {

    private val gasolineras =
        ArregloFijo<Gasolinera>(CAPACIDAD_GASOLINERAS)

    private val evaluaciones =
        ArregloFijo<Evaluacion>(CAPACIDAD_EVALUACIONES)

    private val reportes =
        ArregloFijo<ReporteSaturacion>(CAPACIDAD_REPORTES)

    private val cargas =
        ArregloFijo<CargaCombustible>(CAPACIDAD_CARGAS)

    private val registrosPrecio =
        ArregloFijo<RegistroPrecio>(CAPACIDAD_REGISTROS_PRECIO)

    private var siguienteIdGasolinera = 1
    private var siguienteIdEvaluacion = 1
    private var siguienteIdReporte = 1
    private var siguienteIdCarga = 1
    private var siguienteIdRegistroPrecio = 1

    // =====================================================
    // VALIDACIÓN DE GASOLINERAS
    // =====================================================

    private fun datosGasolineraValidos(
        nombre: String,
        direccion: String,
        municipio: String,
        distanciaDesvioIdaKm: Double,
        precioRegular: Double?,
        precioPremium: Double?,
        precioDiesel: Double?
    ): Boolean {

        // Campos obligatorios.
        if (nombre.isBlank()) return false
        if (direccion.isBlank()) return false
        if (municipio.isBlank()) return false

        // Distancia válida.
        if (!distanciaDesvioIdaKm.isFinite()) return false
        if (distanciaDesvioIdaKm < 0) return false

        // Debe existir al menos un precio.
        val precios = listOf(
            precioRegular,
            precioPremium,
            precioDiesel
        ).filterNotNull()

        if (precios.isEmpty()) return false

        // Todos los precios registrados deben ser positivos y finitos.
        if (precios.any { !it.isFinite() || it <= 0 }) {
            return false
        }

        return true
    }

    // =====================================================
    // GASOLINERAS
    // =====================================================

    fun gasolineras(): List<Gasolinera> =
        gasolineras.aLista()

    fun buscarGasolinera(id: Int): Gasolinera? =
        gasolineras.buscarPorId(id) { it.id }

    fun existeGasolinera(id: Int): Boolean =
        buscarGasolinera(id) != null

    // =====================================================
    // AGREGAR GASOLINERA
    // =====================================================

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

        // Validar los datos antes de crear el objeto.
        if (!datosGasolineraValidos(
                nombre,
                direccion,
                municipio,
                distanciaDesvioIdaKm,
                precioRegular,
                precioPremium,
                precioDiesel
            )
        ) {
            return null
        }

        // Crear la gasolinera.
        val gasolinera = Gasolinera(
            id = siguienteIdGasolinera,
            nombre = nombre.trim(),
            marca = marca.trim(),
            direccion = direccion.trim(),
            municipio = municipio.trim(),
            precioRegular = precioRegular,
            precioPremium = precioPremium,
            precioDiesel = precioDiesel,
            distanciaDesvioIdaKm = distanciaDesvioIdaKm,
            activa = activa
        )

        // El arreglo impide superar la capacidad de 30.
        if (!gasolineras.agregar(gasolinera)) {
            return null
        }

        // Incrementar ID solamente si se agregó correctamente.
        siguienteIdGasolinera++

        // Registrar historial de precios.
        precioRegular?.let {
            registrarPrecio(
                gasolinera.id,
                TipoCombustible.REGULAR,
                it,
                fecha
            )
        }

        precioPremium?.let {
            registrarPrecio(
                gasolinera.id,
                TipoCombustible.PREMIUM,
                it,
                fecha
            )
        }

        precioDiesel?.let {
            registrarPrecio(
                gasolinera.id,
                TipoCombustible.DIESEL,
                it,
                fecha
            )
        }

        return gasolinera
    }

    // =====================================================
    // ACTUALIZAR GASOLINERA
    // =====================================================

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

        // Comprobar que exista la gasolinera.
        val estacion = buscarGasolinera(id) ?: return false

        // Validar los nuevos datos.
        if (!datosGasolineraValidos(
                nombre,
                direccion,
                municipio,
                distanciaDesvioIdaKm,
                precioRegular,
                precioPremium,
                precioDiesel
            )
        ) {
            return false
        }

        // Guardar precios anteriores.
        val precioRegularAnterior = estacion.precioRegular
        val precioPremiumAnterior = estacion.precioPremium
        val precioDieselAnterior = estacion.precioDiesel

        // Actualizar información.
        estacion.nombre = nombre.trim()
        estacion.marca = marca.trim()
        estacion.direccion = direccion.trim()
        estacion.municipio = municipio.trim()

        estacion.precioRegular = precioRegular
        estacion.precioPremium = precioPremium
        estacion.precioDiesel = precioDiesel

        estacion.distanciaDesvioIdaKm = distanciaDesvioIdaKm
        estacion.activa = activa

        // Registrar cambios de precios.
        if (
            precioRegular != null &&
            precioRegular != precioRegularAnterior
        ) {
            registrarPrecio(
                id,
                TipoCombustible.REGULAR,
                precioRegular,
                fecha
            )
        }

        if (
            precioPremium != null &&
            precioPremium != precioPremiumAnterior
        ) {
            registrarPrecio(
                id,
                TipoCombustible.PREMIUM,
                precioPremium,
                fecha
            )
        }

        if (
            precioDiesel != null &&
            precioDiesel != precioDieselAnterior
        ) {
            registrarPrecio(
                id,
                TipoCombustible.DIESEL,
                precioDiesel,
                fecha
            )
        }

        return true
    }

    // =====================================================
    // ELIMINAR GASOLINERA
    // =====================================================

    fun eliminarGasolinera(id: Int): Boolean {

        val eliminada =
            gasolineras.eliminarSi { it.id == id } > 0

        if (eliminada) {

            // Eliminación en cascada.
            evaluaciones.eliminarSi {
                it.gasolineraId == id
            }

            reportes.eliminarSi {
                it.gasolineraId == id
            }

            cargas.eliminarSi {
                it.gasolineraId == id
            }

            registrosPrecio.eliminarSi {
                it.gasolineraId == id
            }
        }

        return eliminada
    }

    // =====================================================
    // EVALUACIONES DE SERVICIO
    // =====================================================

    fun evaluaciones(): List<Evaluacion> =
        evaluaciones.aLista()

    fun evaluacionesDe(gasolineraId: Int): List<Evaluacion> =
        evaluaciones.aLista().filter {
            it.gasolineraId == gasolineraId
        }

    fun agregarEvaluacion(
        gasolineraId: Int,
        atencion: Int,
        limpieza: Int,
        rapidez: Int,
        comentario: String? = null,
        fecha: LocalDateTime = LocalDateTime.now()
    ): Evaluacion? {

        if (!existeGasolinera(gasolineraId)) return null

        if (atencion !in 1..5) return null
        if (limpieza !in 1..5) return null
        if (rapidez !in 1..5) return null

        val evaluacion = Evaluacion(
            siguienteIdEvaluacion,
            gasolineraId,
            atencion,
            limpieza,
            rapidez,
            comentario,
            fecha
        )

        if (!evaluaciones.agregar(evaluacion)) {
            return null
        }

        siguienteIdEvaluacion++

        return evaluacion
    }

    // =====================================================
    // REPORTES DE SATURACIÓN
    // =====================================================

    fun reportes(): List<ReporteSaturacion> =
        reportes.aLista()

    fun reportesDe(gasolineraId: Int): List<ReporteSaturacion> =
        reportes.aLista().filter {
            it.gasolineraId == gasolineraId
        }

    fun agregarReporteSaturacion(
        gasolineraId: Int,
        vehiculosEnEspera: Int,
        esperaMinutos: Int,
        fechaHora: LocalDateTime = LocalDateTime.now()
    ): ReporteSaturacion? {

        if (!existeGasolinera(gasolineraId)) return null
        if (vehiculosEnEspera < 0) return null
        if (esperaMinutos < 0) return null

        val reporte = ReporteSaturacion(
            siguienteIdReporte,
            gasolineraId,
            vehiculosEnEspera,
            esperaMinutos,
            fechaHora
        )

        if (!reportes.agregar(reporte)) {
            return null
        }

        siguienteIdReporte++

        return reporte
    }

    // =====================================================
    // MIS CARGAS
    // =====================================================

    fun cargas(): List<CargaCombustible> =
        cargas.aLista()

    fun cargasDe(gasolineraId: Int): List<CargaCombustible> =
        cargas.aLista().filter {
            it.gasolineraId == gasolineraId
        }

    fun cargasLlenas(): Boolean =
        cargas.lleno

    fun agregarCargaCombustible(
        gasolineraId: Int,
        tipoCombustible: TipoCombustible,
        litros: Double,
        precioPorLitro: Double,
        fecha: LocalDateTime = LocalDateTime.now()
    ): CargaCombustible? {

        if (!existeGasolinera(gasolineraId)) return null
        if (!litros.isFinite() || litros <= 0) return null
        if (!precioPorLitro.isFinite() || precioPorLitro <= 0) return null

        val carga = CargaCombustible(
            siguienteIdCarga,
            gasolineraId,
            tipoCombustible,
            litros,
            precioPorLitro,
            fecha
        )

        if (!cargas.agregar(carga)) {
            return null
        }

        siguienteIdCarga++

        return carga
    }

    // =====================================================
    // HISTORIAL DE PRECIOS
    // =====================================================

    fun registrosPrecio(): List<RegistroPrecio> =
        registrosPrecio.aLista()

    fun registrosPrecioDe(
        gasolineraId: Int,
        tipoCombustible: TipoCombustible? = null
    ): List<RegistroPrecio> {

        return registrosPrecio.aLista().filter {
            it.gasolineraId == gasolineraId &&
                    (tipoCombustible == null ||
                            it.tipoCombustible == tipoCombustible)
        }
    }

    private fun registrarPrecio(
        gasolineraId: Int,
        tipoCombustible: TipoCombustible,
        precio: Double,
        fecha: LocalDateTime
    ): RegistroPrecio? {

        val registro = RegistroPrecio(
            siguienteIdRegistroPrecio,
            gasolineraId,
            tipoCombustible,
            precio,
            fecha
        )

        if (!registrosPrecio.agregar(registro)) {
            return null
        }

        siguienteIdRegistroPrecio++

        return registro
    }

    // =====================================================
    // CAPACIDADES
    // =====================================================

    companion object {

        const val CAPACIDAD_GASOLINERAS = 30
        const val CAPACIDAD_EVALUACIONES = 300
        const val CAPACIDAD_REPORTES = 150
        const val CAPACIDAD_CARGAS = 50
        const val CAPACIDAD_REGISTROS_PRECIO = 300
    }
}
