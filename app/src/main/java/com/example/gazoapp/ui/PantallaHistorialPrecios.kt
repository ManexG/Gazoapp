
package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.logic.HistorialPrecios
import com.example.gazoapp.logic.TendenciaPrecio
import com.example.gazoapp.logic.Tiempo
import com.example.gazoapp.model.RegistroPrecio
import com.example.gazoapp.model.TipoCombustible
import com.example.gazoapp.ui.components.EstadoVacio
import java.time.format.DateTimeFormatter

private val formateadorFechaHistorial = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private data class FilaHistorial(
    val registro: RegistroPrecio,
    val tendencia: TendenciaPrecio?
)

private fun colorTendencia(tendencia: TendenciaPrecio?): Color = when (tendencia) {
    TendenciaPrecio.SUBE -> Color(0xFFC62828)
    TendenciaPrecio.BAJA -> Color(0xFF2E7D32)
    TendenciaPrecio.SIN_CAMBIO -> Color(0xFF757575)
    null -> Color(0xFF757575)
}

private fun textoTendencia(tendencia: TendenciaPrecio?): String = when (tendencia) {
    TendenciaPrecio.SUBE -> "▲ Subió"
    TendenciaPrecio.BAJA -> "▼ Bajó"
    TendenciaPrecio.SIN_CAMBIO -> "— Sin cambio"
    null -> "Primer registro"
}

/**
 * Mejora de innovación "Historial de precios": lista cronológica de
 * RegistroPrecio de una estación/combustible, indicando subida/bajada
 * respecto al registro anterior. Los registros se generan automáticamente
 * en el repositorio al dar de alta o editar precios.
 */
@Composable
fun PantallaHistorialPrecios(
    repositorio: GazoRepository,
    gasolineraPreseleccionadaId: Int? = null
) {
    val gasolineras = repositorio.gasolineras()

    var estacionSeleccionadaId by remember(gasolineraPreseleccionadaId) {
        mutableStateOf(
            gasolineraPreseleccionadaId ?: gasolineras.firstOrNull()?.id
        )
    }

    var estacionMenuExpandido by remember { mutableStateOf(false) }
    var combustibleSeleccionado by remember { mutableStateOf(TipoCombustible.REGULAR) }

    val estacionSeleccionada = gasolineras.find { it.id == estacionSeleccionadaId }

    val registros = if (estacionSeleccionada != null) {
        repositorio.registrosPrecioDe(estacionSeleccionada.id, combustibleSeleccionado)
    } else {
        emptyList()
    }

    val ordenadosAsc = HistorialPrecios.historialOrdenado(registros)

    val filas = ordenadosAsc.mapIndexed { indice, registro ->
        val anterior = ordenadosAsc.getOrNull(indice - 1)
        FilaHistorial(
            registro = registro,
            tendencia = anterior?.let {
                HistorialPrecios.tendenciaEntre(registro.precio, it.precio)
            }
        )
    }.reversed()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        item {
            Text("Historial de precios", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Text(
                "Tendencia local de precios por estación y combustible, " +
                    "generada a partir de las altas y ediciones registradas en la app.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (gasolineras.isEmpty()) {

            item {
                EstadoVacio("Registra primero una gasolinera en el catálogo.")
            }

        } else {

            item {
                Box {
                    OutlinedButton(
                        onClick = {
                            estacionMenuExpandido = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = estacionSeleccionada?.nombre
                                ?: "Selecciona una estación",
                            modifier = Modifier.weight(1f)
                        )
                        Text(if (estacionMenuExpandido) "▲" else "▼")
                    }

                    DropdownMenu(
                        expanded = estacionMenuExpandido,
                        onDismissRequest = {
                            estacionMenuExpandido = false
                        },
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        gasolineras.forEach { estacion ->
                            DropdownMenuItem(
                                text = {
                                    Text(estacion.nombre)
                                },
                                onClick = {
                                    estacionSeleccionadaId = estacion.id
                                    estacionMenuExpandido = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoCombustible.entries.forEach { tipo ->
                        FilterChip(
                            selected = tipo == combustibleSeleccionado,
                            onClick = {
                                combustibleSeleccionado = tipo
                            },
                            label = {
                                Text(tipo.etiqueta)
                            }
                        )
                    }
                }
            }

            if (filas.size < 2) {

                item {
                    EstadoVacio(
                        "Sin historial: aún no hay suficientes registros de " +
                            "${combustibleSeleccionado.etiqueta} para " +
                            "${estacionSeleccionada?.nombre ?: "esta estación"}."
                    )
                }

            } else {

                items(filas, key = { it.registro.id }) { fila ->

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "$%.2f".format(fila.registro.precio),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    textoTendencia(fila.tendencia),
                                    color = colorTendencia(fila.tendencia),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Text(
                                "${fila.registro.fecha.format(formateadorFechaHistorial)} " +
                                    "(${Tiempo.descripcionAntiguedad(fila.registro.fecha)})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
