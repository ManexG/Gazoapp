
package com.example.gazoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gazoapp.data.DatosPrueba
import com.example.gazoapp.data.GazoRepository
import com.example.gazoapp.ui.GazoSmartApp
import com.example.gazoapp.ui.theme.GazoappTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Crear el repositorio y cargar datos de prueba.
        val repositorio = GazoRepository()
        DatosPrueba.cargar(repositorio)

        setContent {
            GazoappTheme {
                GazoSmartApp(repositorio)
            }
        }
    }
}
