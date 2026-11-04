
package com.example.gazoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TarjetaGasolinera(
    nombre: String,
    municipio: String,
    precioRegular: Double?,
    activa: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text(text = municipio)

            Text(
                text = if (activa) "Activa" else "Inactiva",
                color = if (activa)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Text(
                text = if (precioRegular != null)
                    "Regular: $%.2f MXN/L".format(precioRegular)
                else
                    "Regular: No disponible"
            )
        }
    }
}

@Composable
fun EstadoVacio(
    mensaje: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = mensaje,
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CampoNumerico(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(titulo)
        },
        text = {
            Text(mensaje)
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
