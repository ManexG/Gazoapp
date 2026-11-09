package com.example.gazoapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class ArregloFijo<T>(private val capacidad: Int) {

    private val elementos = arrayOfNulls<Any?>(capacidad)

    var cantidad by mutableIntStateOf(0)
        private set

    val lleno: Boolean
        get() = cantidad >= capacidad

    fun agregar(elemento: T): Boolean {
        if (lleno) return false
        elementos[cantidad] = elemento
        cantidad++
        return true
    }

    fun eliminarEnPosicion(posicion: Int): Boolean {
        if (posicion !in 0 until cantidad) return false
        for (i in posicion until cantidad - 1) {
            elementos[i] = elementos[i + 1]
        }
        elementos[cantidad - 1] = null
        cantidad--
        return true
    }

    fun eliminarSi(predicado: (T) -> Boolean): Int {
        var eliminados = 0
        var i = 0
        while (i < cantidad) {
            @Suppress("UNCHECKED_CAST")
            if (predicado(elementos[i] as T)) {
                eliminarEnPosicion(i)
                eliminados++
            } else {
                i++
            }
        }
        return eliminados
    }

    fun buscarPorId(id: Int, obtenerId: (T) -> Int): T? {
        for (i in 0 until cantidad) {
            @Suppress("UNCHECKED_CAST")
            val actual = elementos[i] as T
            if (obtenerId(actual) == id) return actual
        }
        return null
    }

    fun indiceDe(predicado: (T) -> Boolean): Int {
        for (i in 0 until cantidad) {
            @Suppress("UNCHECKED_CAST")
            if (predicado(elementos[i] as T)) return i
        }
        return -1
    }

    fun aLista(): List<T> {
        @Suppress("UNCHECKED_CAST")
        return (0 until cantidad).map { elementos[it] as T }
    }
}
