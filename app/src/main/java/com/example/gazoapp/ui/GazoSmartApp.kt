
package com.example.gazoapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazoapp.data.GazoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GazoSmartApp(repositorio: GazoRepository) {

    var pantallaActual by remember {
        mutableStateOf(Pantalla.INICIO)
    }

    // ID de la gasolinera que se editará.
    var gasolineraEditarId by remember {
        mutableStateOf<Int?>(null)
    }

    // ID de la gasolinera cuyos detalles se consultarán.
    var gasolineraDetalleId by remember {
        mutableStateOf<Int?>(null)
    }

    fun abrirPantalla(pantalla: Pantalla) {
        pantallaActual = pantalla
    }

    // Botón/gesto nativo de retroceso: vuelve a Inicio (o al Catálogo desde el
    // formulario de alta) en vez de cerrar la app; en Inicio sí la cierra.
    BackHandler(enabled = pantallaActual != Pantalla.INICIO) {
        pantallaActual = if (pantallaActual == Pantalla.FORMULARIO) Pantalla.CATALOGO else Pantalla.INICIO
    }

    val estadoMenu = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    // PANTALLAS DEL MENÚ PRINCIPAL
    val opciones = listOf(
        Pantalla.INICIO,
        Pantalla.CATALOGO,
        Pantalla.COMPARADOR,
        Pantalla.SERVICIO,
        Pantalla.SATURACION,
        Pantalla.CALCULADORA,
        Pantalla.RECOMENDACIONES,
        Pantalla.MIS_CARGAS,
        Pantalla.HISTORIAL_PRECIOS
    )

    ModalNavigationDrawer(
        drawerState = estadoMenu,

        drawerContent = {

            ModalDrawerSheet {

                Text(
                    text = "GazoSmart",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                HorizontalDivider()

                opciones.forEach { pantalla ->

                    NavigationDrawerItem(
                        label = {
                            Text(pantalla.titulo)
                        },

                        selected = pantallaActual == pantalla,

                        onClick = {
                            pantallaActual = pantalla

                            scope.launch {
                                estadoMenu.close()
                            }
                        },

                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                    )
                }
            }
        }

    ) {

        Scaffold(

            // BARRA SUPERIOR
            topBar = {

                TopAppBar(
                    title = {
                        Text(pantallaActual.titulo)
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    estadoMenu.open()
                                }
                            }
                        ) {
                            Text("☰")
                        }
                    }
                )
            },

            // BOTÓN PARA AGREGAR GASOLINERA
            floatingActionButton = {

                if (pantallaActual == Pantalla.CATALOGO) {

                    ExtendedFloatingActionButton(

                        onClick = {
                            gasolineraEditarId = null
                            gasolineraDetalleId = null

                            abrirPantalla(Pantalla.FORMULARIO)
                        },

                        text = {
                            Text("Agregar gasolinera")
                        },

                        icon = {
                            Text(
                                "+",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    )
                }
            }

        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Box(
                    modifier = Modifier.weight(1f)
                ) {

                    when (pantallaActual) {

                        Pantalla.INICIO -> PantallaInicio(
                            repositorio = repositorio,
                            onNavegar = { abrirPantalla(it) }
                        )

                        // CATÁLOGO
                        Pantalla.CATALOGO -> {

                            PantallaCatalogo(
                                repositorio = repositorio,

                                onEditar = { id ->
                                    gasolineraEditarId = id
                                    abrirPantalla(Pantalla.FORMULARIO)
                                },

                                onDetalle = { id ->
                                    gasolineraDetalleId = id
                                    abrirPantalla(Pantalla.DETALLE)
                                }
                            )
                        }

                        Pantalla.COMPARADOR -> PantallaComparador(repositorio)

                        Pantalla.SERVICIO -> PantallaServicio(repositorio)

                        Pantalla.SATURACION -> PantallaSaturacion(repositorio)

                        Pantalla.CALCULADORA -> PantallaCalculadora(repositorio)

                        Pantalla.RECOMENDACIONES -> PantallaRecomendaciones(repositorio)

                        Pantalla.MIS_CARGAS -> PantallaMisCargas(
                            repositorio = repositorio
                        )

                        Pantalla.HISTORIAL_PRECIOS -> PantallaHistorialPrecios()

                        // FORMULARIO DE REGISTRO Y EDICIÓN
                        Pantalla.FORMULARIO -> {

                            PantallaFormulario(
                                repositorio = repositorio,

                                gasolineraId = gasolineraEditarId,

                                onGuardar = {
                                    gasolineraEditarId = null
                                    gasolineraDetalleId = null

                                    abrirPantalla(Pantalla.CATALOGO)
                                },

                                onCancelar = {
                                    gasolineraEditarId = null
                                    gasolineraDetalleId = null

                                    abrirPantalla(Pantalla.CATALOGO)
                                }
                            )
                        }

                        // DETALLES DE GASOLINERA
                        Pantalla.DETALLE -> {

                            val id = gasolineraDetalleId

                            if (id != null) {

                                PantallaDetalleGasolinera(
                                    repositorio = repositorio,

                                    gasolineraId = id,

                                    onEditar = { idEditar ->
                                        gasolineraEditarId = idEditar
                                        abrirPantalla(Pantalla.FORMULARIO)
                                    },

                                    onEliminar = {
                                        repositorio.eliminarGasolinera(id)

                                        gasolineraDetalleId = null
                                        gasolineraEditarId = null

                                        abrirPantalla(Pantalla.CATALOGO)
                                    },

                                    onRegresar = {
                                        gasolineraDetalleId = null

                                        abrirPantalla(Pantalla.CATALOGO)
                                    }
                                )

                            } else {

                                PantallaInicio(
                                    repositorio = repositorio,
                                    onNavegar = { abrirPantalla(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
