package com.example.gazoapp.data

import com.example.gazoapp.model.TipoCombustible
import java.time.LocalDateTime

/**
 * Carga ~9 gasolineras ficticias en el repositorio, cubriendo los escenarios
 * exigidos por el plan de trabajo: el ejemplo A/B/C del README §1, un empate
 * de precio mínimo, una estación inactiva, una sin evaluaciones, una con un
 * solo combustible disponible, además de cargas e historial de precio.
 */
object DatosPrueba {

    fun cargar(repositorio: GazoRepository) {
        val ahora = LocalDateTime.now()

        // A/B/C: ejemplo del README §1 (20 L de regular, distinto desvío y espera).
        val a = repositorio.agregarGasolinera(
            nombre = "Estación A",
            marca = "Pemex",
            direccion = "Av. Reforma 100",
            municipio = "Centro",
            precioRegular = 23.50,
            precioPremium = 25.40,
            precioDiesel = 24.20,
            distanciaDesvioIdaKm = 6.0,
            fecha = ahora.minusDays(10)
        )
        val b = repositorio.agregarGasolinera(
            nombre = "Estación B",
            marca = "Shell",
            direccion = "Calle Juárez 220",
            municipio = "Centro",
            precioRegular = 23.40,
            precioPremium = 25.60,
            distanciaDesvioIdaKm = 1.0,
            fecha = ahora.minusDays(8)
        )
        val c = repositorio.agregarGasolinera(
            nombre = "Estación C",
            marca = "BP",
            direccion = "Blvd. Independencia 45",
            municipio = "Norte",
            precioRegular = 24.10,
            precioPremium = 26.00,
            precioDiesel = 24.50,
            distanciaDesvioIdaKm = 2.0,
            fecha = ahora.minusDays(7)
        )

        // Historial de precios: A baja de precio, B sube (demuestran ambas tendencias).
        if (a != null) {
            repositorio.actualizarGasolinera(
                id = a.id, nombre = a.nombre, marca = a.marca, direccion = a.direccion,
                municipio = a.municipio, precioRegular = 23.20, precioPremium = a.precioPremium,
                precioDiesel = a.precioDiesel, distanciaDesvioIdaKm = a.distanciaDesvioIdaKm,
                activa = true, fecha = ahora.minusDays(2)
            )
        }
        if (b != null) {
            repositorio.actualizarGasolinera(
                id = b.id, nombre = b.nombre, marca = b.marca, direccion = b.direccion,
                municipio = b.municipio, precioRegular = 23.70, precioPremium = b.precioPremium,
                precioDiesel = b.precioDiesel, distanciaDesvioIdaKm = b.distanciaDesvioIdaKm,
                activa = true, fecha = ahora.minusDays(1)
            )
        }

        // D: empata el precio mínimo de regular con A ($23.20).
        val d = repositorio.agregarGasolinera(
            nombre = "Estación D",
            marca = "Mobil",
            direccion = "Av. Insurgentes 310",
            municipio = "Sur",
            precioRegular = 23.20,
            precioPremium = 25.90,
            precioDiesel = 24.80,
            distanciaDesvioIdaKm = 4.0,
            fecha = ahora.minusDays(5)
        )

        // E: inactiva (debe excluirse de comparaciones aunque tenga el precio más bajo).
        repositorio.agregarGasolinera(
            nombre = "Estación E",
            marca = "G500",
            direccion = "Camino Real 12",
            municipio = "Centro",
            precioRegular = 22.90,
            precioPremium = 24.80,
            precioDiesel = 23.90,
            distanciaDesvioIdaKm = 3.0,
            activa = false,
            fecha = ahora.minusDays(20)
        )

        // F: sin evaluaciones registradas.
        val f = repositorio.agregarGasolinera(
            nombre = "Estación F",
            marca = "Oxxo Gas",
            direccion = "Calle Hidalgo 88",
            municipio = "Oriente",
            precioRegular = 23.85,
            precioPremium = 25.75,
            precioDiesel = 24.60,
            distanciaDesvioIdaKm = 5.0,
            fecha = ahora.minusDays(6)
        )

        // G: ofrece un solo combustible (diésel).
        repositorio.agregarGasolinera(
            nombre = "Estación G",
            marca = "Diésel Express",
            direccion = "Carretera Poniente km 3",
            municipio = "Poniente",
            precioDiesel = 24.30,
            distanciaDesvioIdaKm = 8.0,
            fecha = ahora.minusDays(4)
        )

        // H, I: estaciones adicionales para completar el catálogo.
        val h = repositorio.agregarGasolinera(
            nombre = "Estación H",
            marca = "Arco",
            direccion = "Av. Constitución 500",
            municipio = "Centro",
            precioRegular = 23.60,
            precioPremium = 25.50,
            precioDiesel = 24.10,
            distanciaDesvioIdaKm = 3.5,
            fecha = ahora.minusDays(9)
        )
        repositorio.agregarGasolinera(
            nombre = "Estación I",
            marca = "Hidrosina",
            direccion = "Libramiento Sur 77",
            municipio = "Sur",
            precioRegular = 24.00,
            distanciaDesvioIdaKm = 7.0,
            fecha = ahora.minusDays(3)
        )

        // Evaluaciones de servicio (F queda deliberadamente sin ninguna).
        if (a != null) {
            repositorio.agregarEvaluacion(a.id, atencion = 5, limpieza = 4, rapidez = 4, fecha = ahora.minusDays(9))
            repositorio.agregarEvaluacion(a.id, atencion = 4, limpieza = 4, rapidez = 3, fecha = ahora.minusDays(4))
            repositorio.agregarEvaluacion(a.id, atencion = 5, limpieza = 5, rapidez = 5, fecha = ahora.minusDays(1))
        }
        if (b != null) {
            repositorio.agregarEvaluacion(b.id, atencion = 3, limpieza = 3, rapidez = 4, fecha = ahora.minusDays(6))
            repositorio.agregarEvaluacion(b.id, atencion = 4, limpieza = 3, rapidez = 4, fecha = ahora.minusDays(2))
        }
        if (c != null) {
            repositorio.agregarEvaluacion(c.id, atencion = 2, limpieza = 3, rapidez = 2, fecha = ahora.minusDays(5))
        }
        if (d != null) {
            repositorio.agregarEvaluacion(d.id, atencion = 4, limpieza = 4, rapidez = 4, fecha = ahora.minusDays(3))
        }
        if (h != null) {
            repositorio.agregarEvaluacion(h.id, atencion = 5, limpieza = 4, rapidez = 5, fecha = ahora.minusDays(1))
        }

        // Reportes de saturación: baja, media y alta.
        if (a != null) repositorio.agregarReporteSaturacion(a.id, vehiculosEnEspera = 7, esperaMinutos = 15, fechaHora = ahora.minusMinutes(20))
        if (b != null) repositorio.agregarReporteSaturacion(b.id, vehiculosEnEspera = 1, esperaMinutos = 3, fechaHora = ahora.minusMinutes(45))
        if (c != null) repositorio.agregarReporteSaturacion(c.id, vehiculosEnEspera = 4, esperaMinutos = 8, fechaHora = ahora.minusHours(2))
        if (f != null) repositorio.agregarReporteSaturacion(f.id, vehiculosEnEspera = 0, esperaMinutos = 0, fechaHora = ahora.minusDays(1))

        // Mis cargas: historial personal de abastecimientos ficticios.
        if (a != null) {
            repositorio.agregarCargaCombustible(a.id, TipoCombustible.REGULAR, litros = 20.0, precioPorLitro = 23.20, fecha = ahora.minusDays(1))
            repositorio.agregarCargaCombustible(a.id, TipoCombustible.PREMIUM, litros = 12.5, precioPorLitro = 25.40, fecha = ahora.minusDays(4))
        }
        if (b != null) {
            repositorio.agregarCargaCombustible(b.id, TipoCombustible.REGULAR, litros = 15.0, precioPorLitro = 23.70, fecha = ahora.minusDays(2))
        }
        if (h != null) {
            repositorio.agregarCargaCombustible(h.id, TipoCombustible.DIESEL, litros = 30.0, precioPorLitro = 24.10, fecha = ahora.minusDays(1))
        }
    }
}
