
package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.logic.Tiempo
import com.example.gazoapp.model.TipoCombustible
import java.util.Locale

@Composable
fun PantallaDetalleGasolinera(
    repositorio: GazoRepository,
    gasolineraId: Int,
    onEditar: (Int) -> Unit,
    onEliminar: () -> Unit,
    onRegresar: () -> Unit,
    onVerCargas: (Int) -> Unit = {},
    onVerHistorial: (Int) -> Unit = {}
) {

    val estacion = repositorio.buscarGasolinera(gasolineraId)

    var mostrarDialogoEliminar by remember {
        mutableStateOf(false)
    }

    if (estacion == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text("No se encontró la gasolinera.")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRegresar) {
                Text("Regresar")
            }
        }

        return
    }

    // EVALUACIONES
    val evaluaciones = repositorio
        .evaluacionesDe(gasolineraId)
        .sortedByDescending { it.fecha }

    val promedio = if (evaluaciones.isNotEmpty()) {
        evaluaciones.map { it.calificacion }.average()
    } else {
        0.0
    }

    // ÚLTIMO REPORTE
    val ultimoReporte = repositorio
        .reportesDe(gasolineraId)
        .maxByOrNull { it.fechaHora }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Detalles de gasolinera",
            style = MaterialTheme.typography.headlineMedium
        )

        // INFORMACIÓN GENERAL
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = estacion.nombre,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = estacion.marca.ifBlank {
                        "Sin marca registrada"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (estacion.activa) {
                        "Estación activa"
                    } else {
                        "Estación inactiva"
                    },
                    color = if (estacion.activa) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        // DIRECCIÓN Y DISTANCIA
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Información general",
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()

                Text("Dirección: ${estacion.direccion}")

                Text("Municipio: ${estacion.municipio}")

                Text(
                    "Distancia de desvío: ${estacion.distanciaDesvioIdaKm} km"
                )
            }
        }

        // PRECIOS
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Precios de combustible",
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()

                TipoCombustible.entries.forEach { tipo ->

                    val precio = estacion.precioPorTipo(tipo)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(tipo.etiqueta)

                        Text(
                            text = precio?.let {
                                formatoPrecioDetalle(it)
                            } ?: "No disponible"
                        )
                    }
                }
            }
        }

        // EVALUACIONES
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Evaluaciones",
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()

                if (evaluaciones.isEmpty()) {

                    Text(
                        "Esta gasolinera todavía no tiene evaluaciones."
                    )

                } else {

                    Text(
                        text = "Calificación promedio: ${
                            String.format(Locale.US, "%.1f", promedio)
                        } / 5",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Total de evaluaciones: ${evaluaciones.size}")

                    HorizontalDivider()

                    evaluaciones.forEach { evaluacion ->

                        Text(
                            text = "Calificación: ${
                                String.format(
                                    Locale.US,
                                    "%.1f",
                                    evaluacion.calificacion
                                )
                            } / 5",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Atención: ${evaluacion.atencion}/5")
                        Text("Limpieza: ${evaluacion.limpieza}/5")
                        Text("Rapidez: ${evaluacion.rapidez}/5")

                        if (!evaluacion.comentario.isNullOrBlank()) {
                            Text(
                                "Comentario: ${evaluacion.comentario}"
                            )
                        }

                        Text(
                            text = Tiempo.descripcionAntiguedad(
                                evaluacion.fecha
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()
                    }
                }
            }
        }

        // SATURACIÓN
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Reporte de saturación",
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider()

                if (ultimoReporte == null) {

                    Text("No existen reportes de saturación.")

                } else {

                    Text(
                        text = "Nivel: ${ultimoReporte.nivel.etiqueta}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Vehículos en espera: ${ultimoReporte.vehiculosEnEspera}"
                    )

                    Text(
                        "Tiempo de espera: ${ultimoReporte.esperaMinutos} minutos"
                    )

                    Text(
                        "Registrado: ${
                            Tiempo.descripcionAntiguedad(
                                ultimoReporte.fechaHora
                            )
                        }"
                    )
                }
            }
        }

        // MEJORAS DE INNOVACIÓN: MIS CARGAS E HISTORIAL DE PRECIOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(
                onClick = {
                    onVerCargas(estacion.id)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Mis cargas")
            }

            OutlinedButton(
                onClick = {
                    onVerHistorial(estacion.id)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Historial de precios")
            }
        }

        // BOTONES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedButton(
                onClick = onRegresar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Regresar")
            }

            Button(
                onClick = {
                    onEditar(estacion.id)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Editar")
            }
        }

        OutlinedButton(
            onClick = {
                mostrarDialogoEliminar = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Eliminar gasolinera")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // CONFIRMACIÓN DE ELIMINACIÓN
    if (mostrarDialogoEliminar) {

        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEliminar = false
            },

            title = {
                Text("Eliminar gasolinera")
            },

            text = {
                Text(
                    "¿Estás seguro de eliminar ${estacion.nombre}? " +
                            "También se eliminarán sus evaluaciones y reportes relacionados."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                        onEliminar()
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEliminar = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun formatoPrecioDetalle(precio: Double): String {
    return "\$${String.format(Locale.US, "%.2f", precio)}"
}
