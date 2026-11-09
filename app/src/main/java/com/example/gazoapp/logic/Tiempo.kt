package com.example.gazoapp.logic

import java.time.Duration
import java.time.LocalDateTime

/**
 * Describe la antigüedad de una fecha en texto ("hace X min/h/días"), para
 * que la UI nunca sugiera que un reporte manual de saturación es el estado
 * actual (regla 9).
 */
object Tiempo {

    fun descripcionAntiguedad(fecha: LocalDateTime, ahora: LocalDateTime = LocalDateTime.now()): String {
        val duracion = Duration.between(fecha, ahora)
        val minutos = duracion.toMinutes()
        val horas = duracion.toHours()
        val dias = duracion.toDays()
        return when {
            minutos < 1 -> "hace instantes"
            minutos < 60 -> "hace $minutos min"
            horas < 24 -> "hace $horas h"
            dias == 1L -> "hace 1 día"
            else -> "hace $dias días"
        }
    }
}
