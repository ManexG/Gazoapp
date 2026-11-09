package com.example.gazoapp.logic

/**
 * Funciones puras de validación (reglas 3 y 4). Cada función devuelve el
 * mensaje de error a mostrar en el formulario, o `null` si el valor es válido.
 */
object Validaciones {

    fun validarObligatorio(valor: String, campo: String): String? =
        if (valor.isBlank()) "$campo es obligatorio" else null

    fun validarAlMenosUnCombustible(
        precioRegular: Double?,
        precioPremium: Double?,
        precioDiesel: Double?
    ): String? =
        if (precioRegular == null && precioPremium == null && precioDiesel == null)
            "Debe registrarse el precio de al menos un combustible"
        else null

    fun validarPrecio(precio: Double?, campo: String = "El precio"): String? =
        if (precio != null && precio <= 0.0) "$campo debe ser mayor que cero" else null

    fun validarDistancia(distanciaDesvioIdaKm: Double): String? =
        if (distanciaDesvioIdaKm < 0.0) "La distancia de desvío no puede ser negativa" else null

    fun validarLitros(litros: Double): String? =
        if (litros <= 0.0) "Los litros deben ser mayores que cero" else null

    fun validarRendimiento(rendimientoKmPorLitro: Double): String? =
        if (rendimientoKmPorLitro <= 0.0) "El rendimiento debe ser mayor que cero" else null

    fun validarNoNegativo(valor: Int, campo: String): String? =
        if (valor < 0) "$campo no puede ser negativo" else null

    /** Reúne todas las validaciones del alta/edición de una gasolinera (RF-01, RF-03). */
    fun validarGasolinera(
        nombre: String,
        direccion: String,
        municipio: String,
        precioRegular: Double?,
        precioPremium: Double?,
        precioDiesel: Double?,
        distanciaDesvioIdaKm: Double
    ): List<String> = listOfNotNull(
        validarObligatorio(nombre, "El nombre"),
        validarObligatorio(direccion, "La dirección"),
        validarObligatorio(municipio, "El municipio"),
        validarAlMenosUnCombustible(precioRegular, precioPremium, precioDiesel),
        validarPrecio(precioRegular, "El precio regular"),
        validarPrecio(precioPremium, "El precio premium"),
        validarPrecio(precioDiesel, "El precio de diésel"),
        validarDistancia(distanciaDesvioIdaKm)
    )

    /** Validación de una evaluación de servicio (RF-07): calificaciones entre 1 y 5. */
    fun validarEvaluacion(atencion: Int, limpieza: Int, rapidez: Int): List<String> {
        val rango = 1..5
        return listOfNotNull(
            if (atencion !in rango) "La atención debe estar entre 1 y 5" else null,
            if (limpieza !in rango) "La limpieza debe estar entre 1 y 5" else null,
            if (rapidez !in rango) "La rapidez debe estar entre 1 y 5" else null
        )
    }

    /** Validación de un reporte de saturación (RF-08). */
    fun validarReporteSaturacion(vehiculosEnEspera: Int, esperaMinutos: Int): List<String> = listOfNotNull(
        validarNoNegativo(vehiculosEnEspera, "El número de vehículos en espera"),
        validarNoNegativo(esperaMinutos, "El tiempo de espera")
    )

    /** Validación de una carga de combustible ("Mis cargas"). */
    fun validarCargaCombustible(litros: Double, precioPorLitro: Double): List<String> = listOfNotNull(
        validarLitros(litros),
        validarPrecio(precioPorLitro, "El precio por litro")
    )
}
