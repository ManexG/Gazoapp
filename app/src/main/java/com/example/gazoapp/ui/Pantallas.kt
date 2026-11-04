
package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaInicio() {
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Gasolineras registradas",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Sin datos todavía")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Estaciones activas",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Sin datos todavía")
            }
        }
    }
}

@Composable
fun PantallaCatalogo() {
    PantallaEnConstruccion(
        "Catálogo de gasolineras",
        "Aquí se mostrarán las estaciones, sus precios y filtros."
    )
}

@Composable
fun PantallaComparador() {
    PantallaEnConstruccion(
        "Comparador de precios",
        "Aquí se compararán los precios de regular, premium y diésel."
    )
}

@Composable
fun PantallaServicio() {
    PantallaEnConstruccion(
        "Evaluaciones de servicio",
        "Aquí se calificará la atención, limpieza y rapidez."
    )
}

@Composable
fun PantallaSaturacion() {
    PantallaEnConstruccion(
        "Reportes de saturación",
        "Aquí aparecerán los vehículos en espera y tiempos estimados."
    )
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
fun PantallaFormulario() {
    PantallaEnConstruccion(
        "Registro de gasolineras",
        "Aquí estará el formulario para agregar o modificar estaciones."
    )
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
