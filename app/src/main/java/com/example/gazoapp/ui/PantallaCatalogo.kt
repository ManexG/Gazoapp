
package com.example.gazoapp.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.logic.Tiempo
import com.example.gazoapp.model.*
import java.util.Locale

private enum class FiltroEstado {
    TODAS, ACTIVAS, INACTIVAS
}

@Composable
fun PantallaCatalogo(
    repositorio: GazoRepository,
    onEditar: (Int) -> Unit,
    onDetalle: (Int) -> Unit
) {

    var busqueda by remember {
        mutableStateOf("")
    }

    var combustibleSeleccionado by remember {
        mutableStateOf<TipoCombustible?>(null)
    }

    var nivelSeleccionado by remember {
        mutableStateOf<NivelSaturacion?>(null)
    }

    var filtroEstado by remember {
        mutableStateOf(FiltroEstado.ACTIVAS)
    }

    var estacionEliminar by remember {
        mutableStateOf<Gasolinera?>(null)
    }

    val estaciones = repositorio.gasolineras()

    val estacionesFiltradas = estaciones.filter { estacion ->

        val coincideBusqueda =
            estacion.nombre.contains(busqueda, ignoreCase = true) ||
                    estacion.marca.contains(busqueda, ignoreCase = true) ||
                    estacion.direccion.contains(busqueda, ignoreCase = true) ||
                    estacion.municipio.contains(busqueda, ignoreCase = true)

        val coincideCombustible =
            combustibleSeleccionado == null ||
                    estacion.precioPorTipo(combustibleSeleccionado!!) != null

        val coincideEstado = when (filtroEstado) {
            FiltroEstado.TODAS -> true
            FiltroEstado.ACTIVAS -> estacion.activa
            FiltroEstado.INACTIVAS -> !estacion.activa
        }

        val ultimoReporte = repositorio
            .reportesDe(estacion.id)
            .maxByOrNull { it.fechaHora }

        val coincideSaturacion =
            nivelSeleccionado == null ||
                    ultimoReporte?.nivel == nivelSeleccionado

        coincideBusqueda &&
                coincideCombustible &&
                coincideEstado &&
                coincideSaturacion
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // BUSCADOR
        OutlinedTextField(
            value = busqueda,
            onValueChange = {
                busqueda = it
            },
            label = {
                Text("Buscar gasolinera")
            },
            placeholder = {
                Text("Nombre, marca o municipio")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // FILTRO DE ESTADO
        Text(
            text = "Estado de la estación",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected = filtroEstado == FiltroEstado.ACTIVAS,
                onClick = {
                    filtroEstado = FiltroEstado.ACTIVAS
                },
                label = {
                    Text("Activas")
                }
            )

            FilterChip(
                selected = filtroEstado == FiltroEstado.INACTIVAS,
                onClick = {
                    filtroEstado = FiltroEstado.INACTIVAS
                },
                label = {
                    Text("Inactivas")
                }
            )

            FilterChip(
                selected = filtroEstado == FiltroEstado.TODAS,
                onClick = {
                    filtroEstado = FiltroEstado.TODAS
                },
                label = {
                    Text("Todas")
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FILTRO DE COMBUSTIBLE
        Text(
            text = "Tipo de combustible",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected = combustibleSeleccionado == null,
                onClick = {
                    combustibleSeleccionado = null
                },
                label = {
                    Text("Todos")
                }
            )

            TipoCombustible.entries.forEach { tipo ->

                FilterChip(
                    selected = combustibleSeleccionado == tipo,
                    onClick = {
                        combustibleSeleccionado =
                            if (combustibleSeleccionado == tipo) {
                                null
                            } else {
                                tipo
                            }
                    },
                    label = {
                        Text(tipo.etiqueta)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // FILTRO DE SATURACIÓN
        Text(
            text = "Nivel de saturación",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected = nivelSeleccionado == null,
                onClick = {
                    nivelSeleccionado = null
                },
                label = {
                    Text("Todos")
                }
            )

            NivelSaturacion.entries.forEach { nivel ->

                FilterChip(
                    selected = nivelSeleccionado == nivel,
                    onClick = {
                        nivelSeleccionado =
                            if (nivelSeleccionado == nivel) {
                                null
                            } else {
                                nivel
                            }
                    },
                    label = {
                        Text(nivel.etiqueta)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Resultados: ${estacionesFiltradas.size}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // LISTADO DE GASOLINERAS
        if (estacionesFiltradas.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron gasolineras.")
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = estacionesFiltradas,
                    key = { it.id }
                ) { estacion ->

                    TarjetaGasolinera(
                        estacion = estacion,
                        repositorio = repositorio,

                        onDetalle = {
                            onDetalle(estacion.id)
                        },

                        onEditar = {
                            onEditar(estacion.id)
                        },

                        onEliminar = {
                            estacionEliminar = estacion
                        }
                    )
                }
            }
        }
    }

    // CONFIRMACIÓN PARA ELIMINAR
    estacionEliminar?.let { estacion ->

        AlertDialog(
            onDismissRequest = {
                estacionEliminar = null
            },

            title = {
                Text("Eliminar gasolinera")
            },

            text = {
                Text(
                    "¿Estás seguro de eliminar ${estacion.nombre}? " +
                            "Esta acción también eliminará sus registros relacionados."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        repositorio.eliminarGasolinera(estacion.id)
                        estacionEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        estacionEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// TARJETA DE GASOLINERA
@Composable
private fun TarjetaGasolinera(
    estacion: Gasolinera,
    repositorio: GazoRepository,
    onDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {

    val evaluaciones = repositorio.evaluacionesDe(estacion.id)

    val promedio = evaluaciones
        .map { it.calificacion }
        .average()

    val ultimoReporte = repositorio
        .reportesDe(estacion.id)
        .maxByOrNull { it.fechaHora }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // NOMBRE Y ESTADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = estacion.nombre,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = estacion.marca,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = if (estacion.activa) "Activa" else "Inactiva",
                    color = if (estacion.activa) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Dirección: ${estacion.direccion}")
            Text("Municipio: ${estacion.municipio}")

            Text(
                text = "Desvío de ida: ${estacion.distanciaDesvioIdaKm} km"
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // PRECIOS
            Text(
                text = "Precios de combustible",
                style = MaterialTheme.typography.titleSmall
            )

            TipoCombustible.entries.forEach { tipo ->

                val precio = estacion.precioPorTipo(tipo)

                if (precio != null) {
                    Text(
                        text = "${tipo.etiqueta}: ${formatoPrecio(precio)}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // EVALUACIONES
            Text(
                text = "Evaluaciones",
                style = MaterialTheme.typography.titleSmall
            )

            if (evaluaciones.isEmpty()) {

                Text("Sin evaluaciones registradas")

            } else {

                Text(
                    text = "Calificación promedio: " +
                            String.format(Locale.US, "%.1f", promedio) +
                            " / 5"
                )

                Text("Total de evaluaciones: ${evaluaciones.size}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // SATURACIÓN
            Text(
                text = "Reporte de saturación",
                style = MaterialTheme.typography.titleSmall
            )

            if (ultimoReporte == null) {

                Text("Sin reportes de saturación")

            } else {

                Text("Nivel: ${ultimoReporte.nivel.etiqueta}")

                Text(
                    "Vehículos en espera: ${ultimoReporte.vehiculosEnEspera}"
                )

                Text(
                    "Tiempo de espera: ${ultimoReporte.esperaMinutos} min"
                )

                Text(
                    "Registrado: ${
                        Tiempo.descripcionAntiguedad(ultimoReporte.fechaHora)
                    }"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            // BOTONES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    onClick = onDetalle
                ) {
                    Text("Detalles")
                }

                TextButton(
                    onClick = onEditar
                ) {
                    Text("Editar")
                }

                TextButton(
                    onClick = onEliminar
                ) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatoPrecio(precio: Double): String {
    return "\$${String.format(Locale.US, "%.2f", precio)}"
}
