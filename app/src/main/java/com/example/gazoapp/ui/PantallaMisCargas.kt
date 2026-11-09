
package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.logic.HistorialPrecios
import com.example.gazoapp.logic.Tiempo
import com.example.gazoapp.logic.Validaciones
import com.example.gazoapp.model.TipoCombustible
import com.example.gazoapp.ui.components.CampoNumerico
import com.example.gazoapp.ui.components.EstadoVacio
import java.time.format.DateTimeFormatter

private val formateadorFechaCarga = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/**
 * Mejora de innovación "Mis cargas": formulario de abastecimiento ficticio
 * (estación, combustible, litros, precio) más historial cronológico con
 * costo por registro y gasto acumulado.
 */
@Composable
fun PantallaMisCargas(
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
    var litrosTexto by remember { mutableStateOf("") }
    var precioTexto by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<List<String>>(emptyList()) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }

    val estacionSeleccionada = gasolineras.find { it.id == estacionSeleccionadaId }
    val cargas = repositorio.cargas().sortedByDescending { it.fecha }
    val costoAcumulado = HistorialPrecios.costoTotalCargas(cargas)
    val capacidadAgotada = repositorio.cargasLlenas()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        item {
            Text("Mis cargas", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Text(
                "Registra tus abastecimientos ficticios y lleva el control de tu gasto.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (gasolineras.isEmpty()) {

            item {
                EstadoVacio(
                    "Registra primero una gasolinera en el catálogo para poder anotar cargas."
                )
            }

        } else {

            // FORMULARIO
            item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text("Nueva carga", style = MaterialTheme.typography.titleMedium)

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

                    CampoNumerico(litrosTexto, { litrosTexto = it }, "Litros")
                    CampoNumerico(precioTexto, { precioTexto = it }, "Precio por litro (MXN)")

                    if (capacidadAgotada) {
                        Text(
                            "Se alcanzó el máximo de cargas registradas " +
                                "(${GazoRepository.CAPACIDAD_CARGAS}).",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    errores.forEach { mensaje ->
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                    }

                    mensajeExito?.let { mensaje ->
                        Text(mensaje, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = {
                            mensajeExito = null
                            val estacion = estacionSeleccionada
                            val litros = litrosTexto.toDoubleOrNull()
                            val precio = precioTexto.toDoubleOrNull()

                            val mensajes = mutableListOf<String>()

                            if (estacion == null) {
                                mensajes += "Selecciona una estación"
                            }

                            if (litros == null || precio == null) {
                                mensajes += "Completa litros y precio con un número"
                            } else {
                                mensajes += Validaciones.validarCargaCombustible(
                                    litros,
                                    precio
                                )
                            }

                            if (mensajes.isEmpty() &&
                                estacion != null &&
                                litros != null &&
                                precio != null
                            ) {

                                val carga = repositorio.agregarCargaCombustible(
                                    gasolineraId = estacion.id,
                                    tipoCombustible = combustibleSeleccionado,
                                    litros = litros,
                                    precioPorLitro = precio
                                )

                                if (carga != null) {
                                    litrosTexto = ""
                                    precioTexto = ""
                                    errores = emptyList()
                                    mensajeExito =
                                        "Carga registrada: $%.2f MXN".format(carga.costoTotal)
                                } else {
                                    errores = listOf(
                                        "No se pudo registrar la carga " +
                                            "(capacidad máxima alcanzada o estación inválida)."
                                    )
                                }

                            } else {
                                errores = mensajes
                            }
                        },
                        enabled = !capacidadAgotada,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar carga")
                    }
                }
            }
            }
        }

        // RESUMEN
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Gasto acumulado", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$%.2f MXN en ${cargas.size} carga(s)".format(costoAcumulado),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        item {
            Text("Historial de cargas", style = MaterialTheme.typography.titleMedium)
        }

        if (cargas.isEmpty()) {

            item {
                EstadoVacio("Aún no registras ninguna carga.")
            }

        } else {

            items(cargas, key = { it.id }) { carga ->

                val estacion = repositorio.buscarGasolinera(carga.gasolineraId)

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
                                estacion?.nombre ?: "Estación eliminada",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "$%.2f".format(carga.costoTotal),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            "${carga.tipoCombustible.etiqueta} · " +
                                "${carga.litros} L · $%.2f/L".format(carga.precioPorLitro)
                        )

                        Text(
                            "${carga.fecha.format(formateadorFechaCarga)} " +
                                "(${Tiempo.descripcionAntiguedad(carga.fecha)})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
