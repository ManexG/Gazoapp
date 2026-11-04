package com.example.gazoapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GazoSmartApp() {

    var pantallaActual by remember {
        mutableStateOf(Pantalla.INICIO)
    }

    fun abrirPantalla(pantalla: Pantalla) {
        pantallaActual = pantalla
    }

    val estadoMenu = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val opciones = listOf(
        Pantalla.INICIO,
        Pantalla.CATALOGO,
        Pantalla.COMPARADOR,
        Pantalla.SERVICIO,
        Pantalla.SATURACION,
        Pantalla.CALCULADORA,
        Pantalla.RECOMENDACIONES
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

            floatingActionButton = {
                if (pantallaActual == Pantalla.CATALOGO) {
                    ExtendedFloatingActionButton(
                        onClick = {
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

                        Pantalla.INICIO -> PantallaInicio()

                        Pantalla.CATALOGO -> PantallaCatalogo()

                        Pantalla.COMPARADOR -> PantallaComparador()

                        Pantalla.SERVICIO -> PantallaServicio()

                        Pantalla.SATURACION -> PantallaSaturacion()

                        Pantalla.CALCULADORA -> PantallaCalculadora()

                        Pantalla.RECOMENDACIONES -> PantallaRecomendaciones()

                        Pantalla.FORMULARIO -> PantallaFormulario(
                            onRegresar = {
                                abrirPantalla(Pantalla.CATALOGO)
                            }
                        )
                    }
                }

                Text(
                    text = "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }


    }
}
