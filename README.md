# GazoSmart ⛽

**Comparador inteligente de precios, atención y tiempos de espera en gasolineras.**
Proyecto Integrador – Etapa 1 · Aplicación Android con datos locales.

> ⚠️ **Aviso:** en esta etapa todos los datos son **ficticios o capturados manualmente** con fines académicos. Ningún precio, calificación o nivel de saturación es oficial, verificado ni en tiempo real. Los datos viven en memoria y **se pierden al cerrar la app**.

---

## Tabla de contenido

1. [Descripción y problemática](#1-descripción-y-problemática)
2. [Objetivos](#2-objetivos)
3. [Alcance de la Etapa 1](#3-alcance-de-la-etapa-1)
4. [Stack tecnológico](#4-stack-tecnológico)
5. [Funcionalidades](#5-funcionalidades)
6. [Pantallas](#6-pantallas)
7. [Modelo de información](#7-modelo-de-información)
8. [Fórmulas y algoritmos](#8-fórmulas-y-algoritmos)
9. [Reglas de negocio](#9-reglas-de-negocio)
10. [Arquitectura y estructura del proyecto](#10-arquitectura-y-estructura-del-proyecto)
11. [Casos de prueba mínimos](#11-casos-de-prueba-mínimos)
12. [Reto de innovación](#12-reto-de-innovación)
13. [Instalación y ejecución](#13-instalación-y-ejecución)
14. [Entregables](#14-entregables)
15. [Documentación adicional](#15-documentación-adicional)

---

## 1. Descripción y problemática

El precio del combustible afecta el presupuesto de quienes usan motocicletas, automóviles u otros vehículos para trabajar, estudiar o desplazarse. Entre estaciones de una misma ciudad hay diferencias de **precio, atención, limpieza y tiempo de espera**, y elegir solo el menor precio por litro puede ser engañoso si implica un desvío largo o una fila considerable.

**GazoSmart** organiza observaciones, contrasta alternativas y estima el costo económico de abastecer, **explicando los criterios** usados.

### Ejemplo del reto

Se necesitan 20 L de gasolina regular y hay tres estaciones ficticias:

| Estación | Regular (MXN/L) | Desvío de ida | Espera reportada |
|---|---|---|---|
| A | $23.20 | 6 km | 15 min |
| B | $23.70 | 1 km | 3 min |
| C | $24.10 | 2 km | 0 min |

¿Cuál conviene considerando precio, distancia, consumo adicional y tiempo de espera? La app responde con algoritmos transparentes.

## 2. Objetivos

**General:** desarrollar una app móvil para el público en general que permita registrar y consultar estaciones de servicio, comparar precios, evaluar la atención, registrar saturación y calcular el costo estimado de abastecimiento y desplazamiento, usando Kotlin, Jetpack Compose y arreglos de objetos.

**Específicos:**

1. Modelar las entidades del problema con clases y objetos Kotlin.
2. Implementar un CRUD completo de estaciones con arreglos de capacidad fija.
3. Construir pantallas y navegación con Jetpack Compose y Material 3.
4. Aplicar validaciones y reglas de negocio coherentes.
5. Calcular precios mínimos, máximos y promedios por combustible.
6. Registrar evaluaciones de servicio y observaciones de saturación con fecha y hora.
7. Calcular costo de carga y costo adicional por desplazamiento.
8. Generar recomendaciones explicables según el criterio elegido.
9. Probar escenarios normales, extremos y de ausencia de datos.

## 3. Alcance de la Etapa 1

| Incluido | No incluido |
|---|---|
| Funcionamiento sin conexión | Internet y APIs |
| Registros locales en memoria (arreglos) | Bases de datos |
| CRUD de estaciones y algoritmos reales | GPS y mapas |
| Datos de prueba | Autenticación y sincronización |

La estructura de clases está pensada para facilitar su integración en etapas posteriores.

## 4. Stack tecnológico

| Componente | Detalle |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Plataforma | Android (`minSdk 24`, `targetSdk 37`) |
| Build | Gradle (Kotlin DSL) |
| Pruebas | JUnit, Compose UI Test |
| Almacenamiento | Arreglos de objetos en memoria |
| Dependencias externas | Ninguna adicional |

## 5. Funcionalidades

| Código | Funcionalidad | Resumen |
|---|---|---|
| RF-01 | Registro de estaciones | Formulario validado, ID único automático, bloqueo al agotar capacidad |
| RF-02 | Catálogo y detalle | `LazyColumn` con `Card`; indica "Sin evaluaciones" / "Sin reporte reciente" |
| RF-03 | Modificación | Edita el mismo objeto sin duplicar ni cambiar su ID |
| RF-04 | Eliminación | `AlertDialog` de confirmación; elimina también evaluaciones y reportes asociados |
| RF-05 | Búsqueda y filtros | Por nombre o municipio (sin distinguir mayúsculas); por combustible, estado y saturación |
| RF-06 | Precios y rankings | Mínimos, máximos y promedios de regular, premium y diésel; muestra empates; excluye inactivas y precios ausentes |
| RF-07 | Evaluaciones | Atención, limpieza y rapidez de 1 a 5; promedio por evaluación y general |
| RF-08 | Saturación | Vehículos en fila, espera estimada, fecha/hora y antigüedad del reporte |
| RF-09 | Calculadora de carga | Litros × precio, en MXN con dos decimales |
| RF-10 | Comparador de desvíos | Costo de carga + combustible extra del desvío de ida y vuelta, ordenado por costo total |
| RF-11 | Recomendaciones | Criterios: menor precio, menor costo estimado, mejor atención, menor espera, con explicación |
| RF-12 | Panel estadístico | Totales, activas, mín/máx/promedio y estación mejor evaluada |

**Clasificación de saturación:** 0–2 vehículos = **baja** · 3–5 = **media** · 6+ = **alta**.

## 6. Pantallas

| Pantalla | Contenido esencial |
|---|---|
| Inicio | Indicadores y accesos rápidos |
| Catálogo | Lista, búsqueda, filtros y tarjetas |
| Registro / edición | Formulario reutilizable con validaciones |
| Comparador de precios | Regular, premium, diésel; ordenamiento y extremos |
| Servicio | Evaluaciones y promedios |
| Saturación | Últimos reportes y niveles |
| Calculadora | Litros, rendimiento, desvíos y costos |
| Recomendaciones | Selección de criterio y explicación |
| Mis cargas | Registro y lista de abastecimientos ficticios propios, con costo total |
| Historial de precios | Tendencia de precio por estación y combustible a lo largo del tiempo |

Componentes: `ModalNavigationDrawer`, `Scaffold`, `TopAppBar`, `LazyColumn`, `Card`, `OutlinedTextField`, `Button`, `FloatingActionButton`, `AlertDialog`. Diseño orientado a accesibilidad, claridad y uso con una sola mano.

## 7. Modelo de información

### Gasolinera

| Campo | Tipo | Regla |
|---|---|---|
| `id` | `Int` | Único, asignado automáticamente |
| `nombre` | `String` | Obligatorio |
| `marca` | `String` | Opcional |
| `direccion` | `String` | Obligatorio |
| `municipio` | `String` | Obligatorio |
| `precioRegular` / `precioPremium` / `precioDiesel` | `Double?` | > 0 si existe; al menos uno válido |
| `distanciaDesvioIdaKm` | `Double` | ≥ 0; es una referencia declarada, **no** una distancia geográfica calculada |
| `activa` | `Boolean` | Estado de operación |

### Evaluación de servicio

`id`, `gasolineraId`, `atencion` (1–5), `limpieza` (1–5), `rapidez` (1–5), `comentario` (opcional), `fecha`.

### Reporte de saturación

`id`, `gasolineraId`, `vehiculosEnEspera` (≥ 0), `esperaMinutos` (≥ 0), `fechaHora`.

### Carga de combustible (mejora "Mis cargas")

`id`, `gasolineraId`, `tipoCombustible`, `litros` (> 0), `precioPorLitro` (> 0), `fecha`. `costoTotal` se calcula como `litros × precioPorLitro`.

### Registro de precio (mejora "Historial de precios")

`id`, `gasolineraId`, `tipoCombustible`, `precio` (> 0), `fecha`. Se genera al dar de alta una estación o al modificar uno de sus precios.

> Evaluaciones, reportes, cargas y registros de precio se vinculan al **ID estable** de la gasolinera, nunca a su posición en el arreglo. Cada entidad tiene su propio arreglo.

## 8. Fórmulas y algoritmos

| Cálculo | Expresión |
|---|---|
| Costo de carga | `litros × precio por litro` |
| Distancia extra total | `desvío de ida × 2` |
| Litros consumidos en desvío | `distancia extra total / rendimiento (km/L)` |
| Costo extra de desvío | `litros consumidos × precio por litro` |
| **Costo total estimado** | `costo de carga + costo extra de desvío` |
| Promedio de precios | `suma de precios válidos / cantidad de estaciones válidas` |
| Calificación de una evaluación | `(atención + limpieza + rapidez) / 3` |

**Supuestos y limitaciones (se muestran en la interfaz):** el desvío se recorre de ida y vuelta, el rendimiento es constante y se usa el precio de la propia estación. No se consideran desgaste, peajes ni valor del tiempo.

**Promedio por estación:** primero se calcula la calificación de cada evaluación y luego se promedian. Una estación sin evaluaciones **no** recibe cero.

## 9. Reglas de negocio

1. Capacidad inicial de **30 gasolineras**; las capacidades de evaluaciones y saturación se definen y justifican por el equipo.
2. Los IDs son únicos y **no se reutilizan** durante la ejecución, aunque se eliminen registros.
3. Nombre, dirección y municipio son obligatorios; al menos un combustible con precio válido.
4. No se aceptan precios, litros ni rendimiento ≤ 0; distancias y esperas no pueden ser negativas.
5. Solo se comparan estaciones **activas** que ofrecen el combustible seleccionado.
6. No se crean evaluaciones ni reportes para estaciones inexistentes.
7. La eliminación requiere confirmación y no deja datos huérfanos.
8. Los empates de precio o calificación se muestran correctamente.
9. Cada reporte de saturación indica cuándo se capturó; uno antiguo no representa el estado actual.
10. La app distingue entre datos ficticios, observaciones del usuario y resultados calculados.
11. Se manejan arreglos vacíos o llenos, campos inválidos y ausencia de resultados sin cierres inesperados.
12. Los datos se guardan solo en memoria; la app no promete conservación permanente.

## 10. Arquitectura y estructura del proyecto

Se separan **modelos**, **lógica de cálculo** y **componentes de interfaz**, evitando concentrar la lógica en un solo composable. Los cambios en los arreglos se exponen como estado observable para que la UI nunca quede desactualizada.

```
app/src/main/java/com/example/gazoapp/
├── model/        Gasolinera, Evaluacion, ReporteSaturacion, CargaCombustible,
│                 RegistroPrecio, enums
├── data/         Arreglos de capacidad fija, repositorio, datos de prueba
├── logic/        Validaciones, estadísticas, costos, recomendador
├── ui/
│   ├── theme/        Color, Type, Theme
│   ├── components/   Tarjetas, diálogos, campos reutilizables
│   └── screens/      Pantallas de la app
└── MainActivity.kt
```

> La estructura mostrada es la planeada; las carpetas se irán creando conforme avance el desarrollo (ver [PLAN_DE_TRABAJO.md](PLAN_DE_TRABAJO.md)).

## 11. Casos de prueba mínimos

| # | Caso |
|---|---|
| 1 | Registrar una estación válida con un solo combustible |
| 2 | Rechazar campos obligatorios vacíos |
| 3 | Rechazar precios negativos o cero |
| 4 | Modificar una estación sin duplicarla |
| 5 | Eliminar con confirmación y mantener el arreglo consistente |
| 6 | Buscar por nombre ignorando mayúsculas |
| 7 | Calcular mínimo, máximo y promedio con valores conocidos |
| 8 | Mostrar correctamente un empate de precio mínimo |
| 9 | Registrar varias evaluaciones y verificar el promedio |
| 10 | Mostrar "Sin evaluaciones" cuando no existan |
| 11 | Clasificar saturación baja, media y alta con fecha y hora |
| 12 | Calcular el importe de una carga con dos decimales |
| 13 | Comparar dos estaciones considerando desvío y rendimiento |
| 14 | Rechazar rendimiento igual a cero |
| 15 | Manejar estación inactiva y combustible no disponible |
| 16 | Manejar arreglo vacío y capacidad agotada |
| 17 | Solicitar recomendación de atención o espera sin datos suficientes |

## 12. Reto de innovación

**Mejoras elegidas:**

- **Mis cargas:** registro de abastecimientos ficticios del usuario (estación, combustible, litros, precio y fecha), con costo total calculado y un historial personal de gasto.
- **Historial de precios:** cada alta o cambio de precio de una estación genera un `RegistroPrecio` con fecha; permite mostrar la tendencia local (sube/baja) de un combustible en una estación a lo largo del tiempo.

## 13. Instalación y ejecución

**Requisitos:** Android Studio (versión reciente con soporte para SDK 37), JDK 11+ y un emulador o dispositivo con Android 7.0 (API 24) o superior.

```bash
git clone https://github.com/ManexG/Gazoapp.git
cd Gazoapp
```

1. Abre la carpeta en Android Studio y espera la sincronización de Gradle.
2. Selecciona un emulador o dispositivo.
3. Pulsa **Run ▶** o ejecuta:

```bash
./gradlew installDebug
```

Para generar el APK de depuración:

```bash
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`.

Para correr las pruebas unitarias:

```bash
./gradlew test
```

## 14. Entregables

- [ ] Proyecto completo de Android Studio con código compilable
- [ ] APK de depuración instalable y funcional
- [ ] Documento breve de diseño (problema social, clases, reglas de negocio, navegación)
- [ ] Explicación de algoritmos (mínimos, máximos, promedios, costos, recomendaciones)
- [ ] Evidencia de pruebas con entradas y resultados esperados/obtenidos
- [ ] Capturas de las pantallas principales
- [ ] Demostración en emulador o dispositivo y defensa técnica

## 15. Documentación adicional

- [PLAN_DE_TRABAJO.md](PLAN_DE_TRABAJO.md): fases, cronograma, reparto y riesgos.

---

_Proyecto académico · GazoSmart · Etapa 1_
