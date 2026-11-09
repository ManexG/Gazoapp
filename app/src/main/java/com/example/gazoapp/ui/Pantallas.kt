
package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.logic.Estadisticas
import com.example.gazoapp.logic.ResultadoExtremo
import com.example.gazoapp.model.TipoCombustible
import com.example.gazoapp.ui.components.TarjetaGasolinera

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
