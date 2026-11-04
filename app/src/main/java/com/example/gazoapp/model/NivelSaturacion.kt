package com.example.gazoapp.model

enum class NivelSaturacion(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    companion object {
        fun desdeVehiculosEnEspera(vehiculosEnEspera: Int): NivelSaturacion = when {
            vehiculosEnEspera <= 2 -> BAJA
            vehiculosEnEspera <= 5 -> MEDIA
            else -> ALTA
        }
    }
}
