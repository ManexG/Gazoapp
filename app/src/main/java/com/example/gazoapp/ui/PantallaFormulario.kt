
package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.ui.components.CampoNumerico

private const val LIMITE_GASOLINERAS = 30

@Composable
fun PantallaFormulario(
    repositorio: GazoRepository,
    gasolineraId: Int? = null,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {

    // Consultar la estación cuando se está editando.
    val estacion = remember(gasolineraId) {
        gasolineraId?.let {
            repositorio.buscarGasolinera(it)
        }
    }

    var nombre by remember(gasolineraId) {
        mutableStateOf(estacion?.nombre ?: "")
    }

    var marca by remember(gasolineraId) {
        mutableStateOf(estacion?.marca ?: "")
    }

    var direccion by remember(gasolineraId) {
        mutableStateOf(estacion?.direccion ?: "")
    }

    var municipio by remember(gasolineraId) {
        mutableStateOf(estacion?.municipio ?: "")
    }

    var precioRegular by remember(gasolineraId) {
        mutableStateOf(estacion?.precioRegular?.toString() ?: "")
    }

    var precioPremium by remember(gasolineraId) {
        mutableStateOf(estacion?.precioPremium?.toString() ?: "")
    }

    var precioDiesel by remember(gasolineraId) {
        mutableStateOf(estacion?.precioDiesel?.toString() ?: "")
    }

    var distancia by remember(gasolineraId) {
        mutableStateOf(
            estacion?.distanciaDesvioIdaKm?.toString() ?: ""
        )
    }

    var activa by remember(gasolineraId) {
        mutableStateOf(estacion?.activa ?: true)
    }

    var mostrarErrores by remember(gasolineraId) {
        mutableStateOf(false)
    }

    var errorGuardado by remember(gasolineraId) {
        mutableStateOf<String?>(null)
    }

    // Convertir texto a número.
    fun convertirNumero(valor: String): Double? {
        return valor.trim()
            .replace(",", ".")
            .toDoubleOrNull()
    }

    val regular = convertirNumero(precioRegular)
    val premium = convertirNumero(precioPremium)
    val diesel = convertirNumero(precioDiesel)
    val distanciaNumero = convertirNumero(distancia)

    // Validaciones de campos obligatorios.
    val errorNombre = if (
        mostrarErrores && nombre.isBlank()
    ) {
        "El nombre es obligatorio"
    } else null

    val errorDireccion = if (
        mostrarErrores && direccion.isBlank()
    ) {
        "La dirección es obligatoria"
    } else null

    val errorMunicipio = if (
        mostrarErrores && municipio.isBlank()
    ) {
        "El municipio es obligatorio"
    } else null

    val errorDistancia = when {
        !mostrarErrores -> null
        distancia.isBlank() -> "Ingresa la distancia"
        distanciaNumero == null -> "Ingresa un número válido"
        distanciaNumero < 0 -> "La distancia no puede ser negativa"
        else -> null
    }

    // Validar precios opcionales.
    fun validarPrecio(
        valor: String,
        numero: Double?
    ): String? {

        if (!mostrarErrores || valor.isBlank()) {
            return null
        }

        return when {
            numero == null -> "Ingresa un precio válido"
            numero <= 0 -> "El precio debe ser mayor a cero"
            else -> null
        }
    }

    val errorRegular = validarPrecio(precioRegular, regular)
    val errorPremium = validarPrecio(precioPremium, premium)
    val errorDiesel = validarPrecio(precioDiesel, diesel)

    // Contar gasolineras registradas.
    val cantidadGasolineras = repositorio.gasolineras().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = if (gasolineraId == null) {
                "Registrar gasolinera"
            } else {
                "Editar gasolinera"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Completa los datos de la estación.")

        // INFORMACIÓN GENERAL
        Text(
            "Información general",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorGuardado = null
            },
            label = { Text("Nombre de la gasolinera") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorNombre != null,
            supportingText = {
                errorNombre?.let { Text(it) }
            }
        )

        OutlinedTextField(
            value = marca,
            onValueChange = { marca = it },
            label = { Text("Marca") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorDireccion != null,
            supportingText = {
                errorDireccion?.let { Text(it) }
            }
        )

        OutlinedTextField(
            value = municipio,
            onValueChange = { municipio = it },
            label = { Text("Municipio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMunicipio != null,
            supportingText = {
                errorMunicipio?.let { Text(it) }
            }
        )

        CampoNumerico(
            valor = distancia,
            onCambio = { distancia = it },
            etiqueta = "Distancia de desvío (km)",
            error = errorDistancia
        )

        HorizontalDivider()

        // PRECIOS
        Text(
            "Precios de combustible",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            "Ingresa al menos un precio. Los demás pueden quedar vacíos.",
            style = MaterialTheme.typography.bodySmall
        )

        CampoNumerico(
            valor = precioRegular,
            onCambio = { precioRegular = it },
            etiqueta = "Precio Regular",
            error = errorRegular
        )

        CampoNumerico(
            valor = precioPremium,
            onCambio = { precioPremium = it },
            etiqueta = "Precio Premium",
            error = errorPremium
        )

        CampoNumerico(
            valor = precioDiesel,
            onCambio = { precioDiesel = it },
            etiqueta = "Precio Diésel",
            error = errorDiesel
        )

        HorizontalDivider()

        // ESTADO
        Text(
            "Estado de la gasolinera",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                if (activa) "Gasolinera activa" else "Gasolinera inactiva"
            )

            Switch(
                checked = activa,
                onCheckedChange = {
                    activa = it
                }
            )
        }

        HorizontalDivider()

        // CAPACIDAD DEL REPOSITORIO
        Text(
            "Gasolineras registradas: $cantidadGasolineras / $LIMITE_GASOLINERAS",
            style = MaterialTheme.typography.bodyMedium
        )

        if (gasolineraId == null &&
            cantidadGasolineras >= LIMITE_GASOLINERAS
        ) {
            Text(
                "Se alcanzó el límite de 30 gasolineras.",
                color = MaterialTheme.colorScheme.error
            )
        }

        // MENSAJE GENERAL
        errorGuardado?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // GUARDAR
        Button(
            onClick = {

                mostrarErrores = true
                errorGuardado = null

                // Comprobar campos.
                val datosValidos =
                    nombre.isNotBlank() &&
                            direccion.isNotBlank() &&
                            municipio.isNotBlank() &&
                            distanciaNumero != null &&
                            distanciaNumero >= 0 &&
                            (precioRegular.isBlank() ||
                                    (regular != null && regular > 0)) &&
                            (precioPremium.isBlank() ||
                                    (premium != null && premium > 0)) &&
                            (precioDiesel.isBlank() ||
                                    (diesel != null && diesel > 0)) &&
                            (regular != null ||
                                    premium != null ||
                                    diesel != null)

                if (!datosValidos) {
                    errorGuardado =
                        "Revisa los campos. Ingresa al menos un combustible con precio válido."
                    return@Button
                }

                // Comprobar que la estación exista al editar.
                if (gasolineraId != null && estacion == null) {
                    errorGuardado =
                        "No se encontró la gasolinera que deseas editar."
                    return@Button
                }

                // Comprobar límite al registrar.
                if (gasolineraId == null &&
                    cantidadGasolineras >= LIMITE_GASOLINERAS
                ) {
                    errorGuardado =
                        "No puedes registrar más de 30 gasolineras."
                    return@Button
                }

                // Registrar o actualizar.
                val guardado = if (gasolineraId == null) {

                    repositorio.agregarGasolinera(
                        nombre = nombre.trim(),
                        marca = marca.trim(),
                        direccion = direccion.trim(),
                        municipio = municipio.trim(),
                        distanciaDesvioIdaKm = distanciaNumero!!,
                        precioRegular = regular,
                        precioPremium = premium,
                        precioDiesel = diesel,
                        activa = activa
                    ) != null

                } else {

                    repositorio.actualizarGasolinera(
                        id = gasolineraId,
                        nombre = nombre.trim(),
                        marca = marca.trim(),
                        direccion = direccion.trim(),
                        municipio = municipio.trim(),
                        distanciaDesvioIdaKm = distanciaNumero!!,
                        precioRegular = regular,
                        precioPremium = premium,
                        precioDiesel = diesel,
                        activa = activa
                    )
                }

                if (guardado) {
                    onGuardar()
                } else {
                    errorGuardado =
                        "No se pudo guardar. Verifica el límite de registros y los datos."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (gasolineraId == null) {
                    "Registrar gasolinera"
                } else {
                    "Guardar cambios"
                }
            )
        }

        // CANCELAR
        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}
