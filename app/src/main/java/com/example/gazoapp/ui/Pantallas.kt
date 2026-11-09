
package com.example.gazoapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.logic.Estadisticas
import com.example.gazoapp.logic.ResultadoExtremo
import com.example.gazoapp.logic.Tiempo
import com.example.gazoapp.logic.Validaciones
import com.example.gazoapp.model.NivelSaturacion
import com.example.gazoapp.model.TipoCombustible
import com.example.gazoapp.ui.components.CampoNumerico
import com.example.gazoapp.ui.components.EstadoVacio
import com.example.gazoapp.ui.components.TarjetaGasolinera
import java.time.format.DateTimeFormatter

private fun textoExtremo(resultado: ResultadoExtremo?): String {
    if (resultado == null) return "Sin datos"
    val nombres = resultado.estaciones.joinToString(", ") { it.nombre }
    return "$%.2f ($nombres)".format(resultado.valor)
}

private fun textoPromedio(promedio: Double?): String =
    if (promedio == null) "Sin datos" else "$%.2f".format(promedio)

/** Panel estadístico (RF-12): totales, mín/máx/promedio por combustible y mejor evaluada. */
@Composable
fun PantallaInicio(
    repositorio: GazoRepository,
    onNavegar: (Pantalla) -> Unit
) {
    val gasolineras = repositorio.gasolineras()
    val evaluaciones = repositorio.evaluaciones()
    val activas = gasolineras.count { it.activa }
    val mejorEvaluada = Estadisticas.estacionMejorEvaluada(gasolineras, evaluaciones)
    val promedioMejorEvaluada = mejorEvaluada?.let {
        Estadisticas.promedioEvaluacionesDeEstacion(it.id, evaluaciones)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Bienvenido a GazoSmart",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Compara precios, atención y tiempos de espera " +
                    "para elegir una gasolinera."
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Gasolineras registradas",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${gasolineras.size} en total, $activas activas")
            }
        }

        TipoCombustible.entries.forEach { tipo ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(tipo.etiqueta, style = MaterialTheme.typography.titleMedium)
                    Text("Mínimo: ${textoExtremo(Estadisticas.precioMinimo(gasolineras, tipo))}")
                    Text("Máximo: ${textoExtremo(Estadisticas.precioMaximo(gasolineras, tipo))}")
                    Text("Promedio: ${textoPromedio(Estadisticas.precioPromedio(gasolineras, tipo))}")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Estación mejor evaluada", style = MaterialTheme.typography.titleMedium)
                if (mejorEvaluada != null && promedioMejorEvaluada != null) {
                    Text(mejorEvaluada.nombre)
                    Text("Promedio de evaluación: %.2f sobre 5".format(promedioMejorEvaluada))
                } else {
                    Text("Sin datos suficientes")
                }
            }
        }

        Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onNavegar(Pantalla.CATALOGO) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ver catálogo de gasolineras") }

            OutlinedButton(
                onClick = { onNavegar(Pantalla.COMPARADOR) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Comparar precios") }

            OutlinedButton(
                onClick = { onNavegar(Pantalla.CALCULADORA) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Calcular costo de carga") }

            OutlinedButton(
                onClick = { onNavegar(Pantalla.RECOMENDACIONES) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Pedir una recomendación") }
        }
    }
}


@Composable
fun PantallaCatalogo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Catálogo de gasolineras",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Ejemplo visual con datos ficticios")

        TarjetaGasolinera(
            nombre = "Estación Centro",
            municipio = "Zitácuaro",
            precioRegular = 23.50,
            activa = true,
            onClick = {}
        )

        TarjetaGasolinera(
            nombre = "Gasolinera Norte",
            municipio = "Morelia",
            precioRegular = 24.20,
            activa = false,
            onClick = {}
        )
    }
}


/** Comparador de precios (RF-06): orden ascendente, extremos y empates, excluyendo inactivas/sin precio. */
@Composable
fun PantallaComparador(repositorio: GazoRepository) {
    var tipoSeleccionado by remember { mutableStateOf(TipoCombustible.REGULAR) }

    val gasolineras = repositorio.gasolineras()
    val minimo = Estadisticas.precioMinimo(gasolineras, tipoSeleccionado)
    val maximo = Estadisticas.precioMaximo(gasolineras, tipoSeleccionado)
    val elegibles = gasolineras
        .filter { it.activa && it.precioPorTipo(tipoSeleccionado) != null }
        .sortedBy { it.precioPorTipo(tipoSeleccionado) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Comparador de precios", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoCombustible.entries.forEach { tipo ->
                FilterChip(
                    selected = tipo == tipoSeleccionado,
                    onClick = { tipoSeleccionado = tipo },
                    label = { Text(tipo.etiqueta) }
                )
            }
        }

        if (elegibles.isEmpty()) {
            EstadoVacio("No hay estaciones activas con precio de ${tipoSeleccionado.etiqueta} disponible")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(elegibles) { gasolinera ->
                    val precio = gasolinera.precioPorTipo(tipoSeleccionado)!!
                    val esMinimo = minimo?.estaciones?.contains(gasolinera) == true
                    val esMaximo = maximo?.estaciones?.contains(gasolinera) == true

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(gasolinera.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("$%.2f".format(precio), style = MaterialTheme.typography.titleMedium)
                            }
                            Text(gasolinera.municipio)
                            if (esMinimo) {
                                Text("Precio mínimo", color = MaterialTheme.colorScheme.primary)
                            }
                            if (esMaximo) {
                                Text("Precio máximo", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun textoConteoEvaluaciones(cantidad: Int): String =
    if (cantidad == 1) "1 evaluación" else "$cantidad evaluaciones"

private sealed class VistaServicio {
    data object Lista : VistaServicio()
    data class Detalle(val gasolineraId: Int) : VistaServicio()
    data class Formulario(val gasolineraId: Int) : VistaServicio()
}

/** Evaluaciones de servicio (RF-07): lista de estaciones -> detalle con evaluaciones -> formulario 1-5. */
@Composable
fun PantallaServicio(repositorio: GazoRepository) {
    var vista by remember { mutableStateOf<VistaServicio>(VistaServicio.Lista) }

    // Botón/gesto nativo de retroceso: retrocede un nivel (Formulario -> Detalle
    // -> Lista) en vez de salir de la pantalla; en Lista no se intercepta.
    BackHandler(enabled = vista !is VistaServicio.Lista) {
        vista = when (val vistaActual = vista) {
            is VistaServicio.Formulario -> VistaServicio.Detalle(vistaActual.gasolineraId)
            is VistaServicio.Detalle -> VistaServicio.Lista
            VistaServicio.Lista -> VistaServicio.Lista
        }
    }

    when (val vistaActual = vista) {
        is VistaServicio.Lista -> ListaEstacionesServicio(
            repositorio = repositorio,
            onSeleccionar = { gasolineraId -> vista = VistaServicio.Detalle(gasolineraId) }
        )

        is VistaServicio.Detalle -> DetalleServicioEstacion(
            repositorio = repositorio,
            gasolineraId = vistaActual.gasolineraId,
            onAgregarEvaluacion = { vista = VistaServicio.Formulario(vistaActual.gasolineraId) },
            onVolver = { vista = VistaServicio.Lista }
        )

        is VistaServicio.Formulario -> FormularioEvaluacionServicio(
            repositorio = repositorio,
            gasolineraId = vistaActual.gasolineraId,
            onGuardado = { vista = VistaServicio.Detalle(vistaActual.gasolineraId) },
            onCancelar = { vista = VistaServicio.Detalle(vistaActual.gasolineraId) }
        )
    }
}

@Composable
private fun ListaEstacionesServicio(
    repositorio: GazoRepository,
    onSeleccionar: (Int) -> Unit
) {
    val gasolineras = repositorio.gasolineras()
    val evaluaciones = repositorio.evaluaciones()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Evaluaciones de servicio", style = MaterialTheme.typography.headlineSmall)

        if (gasolineras.isEmpty()) {
            EstadoVacio("No hay gasolineras registradas todavía")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gasolineras) { gasolinera ->
                    val evaluacionesEstacion = evaluaciones.filter { it.gasolineraId == gasolinera.id }
                    val promedio = Estadisticas.promedioEvaluaciones(evaluacionesEstacion)

                    Card(
                        onClick = { onSeleccionar(gasolinera.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(gasolinera.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(gasolinera.municipio)
                            if (promedio != null) {
                                Text("Promedio: %.2f sobre 5 (${textoConteoEvaluaciones(evaluacionesEstacion.size)})".format(promedio))
                            } else {
                                Text("Sin evaluaciones")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleServicioEstacion(
    repositorio: GazoRepository,
    gasolineraId: Int,
    onAgregarEvaluacion: () -> Unit,
    onVolver: () -> Unit
) {
    val estacion = repositorio.buscarGasolinera(gasolineraId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Text("←", style = MaterialTheme.typography.headlineSmall) }
            Text(estacion?.nombre ?: "Estación", style = MaterialTheme.typography.headlineSmall)
        }

        if (estacion == null) {
            EstadoVacio("Estación no encontrada")
        } else {
            val evaluaciones = repositorio.evaluacionesDe(gasolineraId)
            val promedio = Estadisticas.promedioEvaluaciones(evaluaciones)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium)
                    if (promedio != null) {
                        Text("Promedio general: %.2f sobre 5 (${textoConteoEvaluaciones(evaluaciones.size)})".format(promedio))
                    } else {
                        Text("Sin evaluaciones")
                    }
                }
            }

            Button(onClick = onAgregarEvaluacion, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar evaluación")
            }

            evaluaciones.sortedByDescending { it.fecha }.forEach { evaluacion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Calificación: %.2f".format(evaluacion.calificacion),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Atención ${evaluacion.atencion} · Limpieza ${evaluacion.limpieza} · Rapidez ${evaluacion.rapidez}")
                        evaluacion.comentario?.let { Text(it) }
                        Text(
                            evaluacion.fecha.format(formateadorFecha),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioEvaluacionServicio(
    repositorio: GazoRepository,
    gasolineraId: Int,
    onGuardado: () -> Unit,
    onCancelar: () -> Unit
) {
    val estacion = repositorio.buscarGasolinera(gasolineraId)

    var atencionTexto by remember { mutableStateOf("") }
    var limpiezaTexto by remember { mutableStateOf("") }
    var rapidezTexto by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancelar) { Text("←", style = MaterialTheme.typography.headlineSmall) }
            Text(
                "Nueva evaluación" + (estacion?.let { " · ${it.nombre}" } ?: ""),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        if (estacion == null) {
            EstadoVacio("Estación no encontrada")
        } else {
            CampoNumerico(atencionTexto, { atencionTexto = it }, "Atención (1-5)")
            CampoNumerico(limpiezaTexto, { limpiezaTexto = it }, "Limpieza (1-5)")
            CampoNumerico(rapidezTexto, { rapidezTexto = it }, "Rapidez (1-5)")

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            errores.forEach { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val atencion = atencionTexto.toIntOrNull()
                    val limpieza = limpiezaTexto.toIntOrNull()
                    val rapidez = rapidezTexto.toIntOrNull()

                    val mensajes = mutableListOf<String>()
                    if (atencion == null || limpieza == null || rapidez == null) {
                        mensajes += "Completa atención, limpieza y rapidez con un número"
                    } else {
                        mensajes += Validaciones.validarEvaluacion(atencion, limpieza, rapidez)
                    }

                    if (mensajes.isEmpty() && atencion != null && limpieza != null && rapidez != null) {
                        repositorio.agregarEvaluacion(
                            gasolineraId = estacion.id,
                            atencion = atencion,
                            limpieza = limpieza,
                            rapidez = rapidez,
                            comentario = comentario.ifBlank { null }
                        )
                        onGuardado()
                    } else {
                        errores = mensajes
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar evaluación") }
        }
    }
}

@Composable
fun PantallaSaturacion(repositorio: GazoRepository) {
    var vista by remember { mutableStateOf<VistaSaturacion>(VistaSaturacion.Lista) }

    // Botón/gesto nativo de retroceso: retrocede un nivel (Formulario -> Detalle
    // -> Lista) en vez de salir de la pantalla; en Lista no se intercepta.
    BackHandler(enabled = vista !is VistaSaturacion.Lista) {
        vista = when (val vistaActual = vista) {
            is VistaSaturacion.Formulario -> VistaSaturacion.Detalle(vistaActual.gasolineraId)
            is VistaSaturacion.Detalle -> VistaSaturacion.Lista
            VistaSaturacion.Lista -> VistaSaturacion.Lista
        }
    }

    when (val vistaActual = vista) {
        is VistaSaturacion.Lista -> ListaEstacionesSaturacion(
            repositorio = repositorio,
            onSeleccionar = { gasolineraId -> vista = VistaSaturacion.Detalle(gasolineraId) }
        )

        is VistaSaturacion.Detalle -> DetalleSaturacionEstacion(
            repositorio = repositorio,
            gasolineraId = vistaActual.gasolineraId,
            onAgregarReporte = { vista = VistaSaturacion.Formulario(vistaActual.gasolineraId) },
            onVolver = { vista = VistaSaturacion.Lista }
        )

        is VistaSaturacion.Formulario -> FormularioReporteSaturacion(
            repositorio = repositorio,
            gasolineraId = vistaActual.gasolineraId,
            onGuardado = { vista = VistaSaturacion.Detalle(vistaActual.gasolineraId) },
            onCancelar = { vista = VistaSaturacion.Detalle(vistaActual.gasolineraId) }
        )
    }
}

private sealed class VistaSaturacion {
    data object Lista : VistaSaturacion()
    data class Detalle(val gasolineraId: Int) : VistaSaturacion()
    data class Formulario(val gasolineraId: Int) : VistaSaturacion()
}

private fun colorNivelSaturacion(nivel: NivelSaturacion): Color = when (nivel) {
    NivelSaturacion.BAJA -> Color(0xFF2E7D32)
    NivelSaturacion.MEDIA -> Color(0xFFF9A825)
    NivelSaturacion.ALTA -> Color(0xFFC62828)
}

@Composable
private fun ListaEstacionesSaturacion(
    repositorio: GazoRepository,
    onSeleccionar: (Int) -> Unit
) {
    val gasolineras = repositorio.gasolineras()
    val reportes = repositorio.reportes()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reportes de saturación", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Reporte manual, no en vivo: revisa la antigüedad antes de confiar en el nivel.",
            style = MaterialTheme.typography.bodySmall
        )

        if (gasolineras.isEmpty()) {
            EstadoVacio("No hay gasolineras registradas todavía")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gasolineras) { gasolinera ->
                    val ultimoReporte = reportes
                        .filter { it.gasolineraId == gasolinera.id }
                        .maxByOrNull { it.fechaHora }

                    Card(
                        onClick = { onSeleccionar(gasolinera.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(gasolinera.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(gasolinera.municipio)
                            if (ultimoReporte != null) {
                                Text(
                                    "${ultimoReporte.nivel.etiqueta} · ${Tiempo.descripcionAntiguedad(ultimoReporte.fechaHora)}",
                                    color = colorNivelSaturacion(ultimoReporte.nivel)
                                )
                            } else {
                                Text("Sin reportes")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleSaturacionEstacion(
    repositorio: GazoRepository,
    gasolineraId: Int,
    onAgregarReporte: () -> Unit,
    onVolver: () -> Unit
) {
    val estacion = repositorio.buscarGasolinera(gasolineraId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Text("←", style = MaterialTheme.typography.headlineSmall) }
            Text(estacion?.nombre ?: "Estación", style = MaterialTheme.typography.headlineSmall)
        }

        if (estacion == null) {
            EstadoVacio("Estación no encontrada")
        } else {
            val reportes = repositorio.reportesDe(gasolineraId).sortedByDescending { it.fechaHora }
            val ultimo = reportes.firstOrNull()

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Estado más reciente", style = MaterialTheme.typography.titleMedium)
                    if (ultimo != null) {
                        Text(
                            "${ultimo.nivel.etiqueta} · ${ultimo.vehiculosEnEspera} vehículos · ${ultimo.esperaMinutos} min de espera",
                            color = colorNivelSaturacion(ultimo.nivel)
                        )
                        Text(
                            Tiempo.descripcionAntiguedad(ultimo.fechaHora),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("Sin reportes")
                    }
                    Text(
                        "Reporte manual, no en vivo: un reporte antiguo no representa el estado actual.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(onClick = onAgregarReporte, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar reporte")
            }

            reportes.forEach { reporte ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            reporte.nivel.etiqueta,
                            style = MaterialTheme.typography.titleMedium,
                            color = colorNivelSaturacion(reporte.nivel)
                        )
                        Text("${reporte.vehiculosEnEspera} vehículos en espera · ${reporte.esperaMinutos} min")
                        Text(
                            "${reporte.fechaHora.format(formateadorFecha)} (${Tiempo.descripcionAntiguedad(reporte.fechaHora)})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioReporteSaturacion(
    repositorio: GazoRepository,
    gasolineraId: Int,
    onGuardado: () -> Unit,
    onCancelar: () -> Unit
) {
    val estacion = repositorio.buscarGasolinera(gasolineraId)

    var vehiculosTexto by remember { mutableStateOf("") }
    var esperaTexto by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancelar) { Text("←", style = MaterialTheme.typography.headlineSmall) }
            Text(
                "Nuevo reporte" + (estacion?.let { " · ${it.nombre}" } ?: ""),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        if (estacion == null) {
            EstadoVacio("Estación no encontrada")
        } else {
            CampoNumerico(vehiculosTexto, { vehiculosTexto = it }, "Vehículos en espera")
            CampoNumerico(esperaTexto, { esperaTexto = it }, "Espera estimada (min)")

            errores.forEach { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val vehiculos = vehiculosTexto.toIntOrNull()
                    val espera = esperaTexto.toIntOrNull()

                    val mensajes = mutableListOf<String>()
                    if (vehiculos == null || espera == null) {
                        mensajes += "Completa vehículos en espera y espera estimada con un número"
                    } else {
                        mensajes += Validaciones.validarReporteSaturacion(vehiculos, espera)
                    }

                    if (mensajes.isEmpty() && vehiculos != null && espera != null) {
                        repositorio.agregarReporteSaturacion(
                            gasolineraId = estacion.id,
                            vehiculosEnEspera = vehiculos,
                            esperaMinutos = espera
                        )
                        onGuardado()
                    } else {
                        errores = mensajes
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar reporte") }
        }
    }
}

@Composable
fun PantallaCalculadora() {
    PantallaEnConstruccion(
        "Calculadora",
        "Aquí se calcularán los costos de carga y desplazamiento."
    )
}

@Composable
fun PantallaRecomendaciones() {
    PantallaEnConstruccion(
        "Recomendaciones",
        "Aquí se sugerirán gasolineras según el criterio elegido."
    )
}

@Composable
fun PantallaMisCargas() {
    PantallaEnConstruccion(
        "Mis cargas",
        "Aquí se registrarán tus abastecimientos ficticios y el gasto acumulado."
    )
}

@Composable
fun PantallaHistorialPrecios() {
    PantallaEnConstruccion(
        "Historial de precios",
        "Aquí se mostrará la tendencia de precios de una estación a lo largo del tiempo."
    )
}


@Composable
fun PantallaFormulario(
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            "Registro de gasolineras",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Aquí estará el formulario para " +
                            "registrar y modificar gasolineras."
                )

                Text(
                    "Módulo en desarrollo",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al catálogo")
        }
    }
}


@Composable
fun PantallaEnConstruccion(
    titulo: String,
    descripcion: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(descripcion)

                Spacer(Modifier.height(12.dp))

                Text(
                    "Módulo en desarrollo",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
