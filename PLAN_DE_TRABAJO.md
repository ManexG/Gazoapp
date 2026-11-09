# GazoSmart – Plan de trabajo (Proyecto Integrador, Etapa 1)

Estado de partida: paquetes creados y modelo de datos inicial implementado (`model/`, `data/ArregloFijo`). Kotlin + Compose + Material3, minSdk 24. Sin dependencias extra.

## Decisiones de arquitectura (fijadas)

- **Sin dependencias externas.** Navegación con `ModalNavigationDrawer` + un `enum class Pantalla` y `mutableStateOf` (suficiente para las pantallas previstas); evita `navigation-compose`.
- **Estado:** un `GazoViewModel` (o clase `Repositorio`) que concentre los arreglos de cada entidad.
- **Arreglos de capacidad fija:** `ArregloFijo<T>` (ya implementada en `data/`) envuelve un `arrayOfNulls` y expone `cantidad` como estado observable de Compose (`mutableStateOf`), con `agregar/eliminarEnPosicion/eliminarSi/buscarPorId/indiceDe/aLista`. Capacidades propuestas (justificarlas en el documento):
  - Gasolineras: 30
  - Evaluaciones: 300 (10 por estación)
  - Reportes de saturación: 150 (5 por estación)
  - Cargas de combustible ("Mis cargas"): 50
  - Registros de precio ("Historial de precios"): 300 (10 por estación/combustible)
- **IDs:** contadores `siguienteId` por entidad que nunca decrecen (regla de negocio 2).
- **Eliminación (RF-04):** eliminar en cascada evaluaciones, reportes, cargas y registros de precio de la estación (sin huérfanos); documentarlo.
- **Separación:** `model/`, `logic/` (funciones puras y testeables), `data/` (repositorio + datos de prueba), `ui/` (screens, components, theme).

## Mejoras de innovación elegidas

Se descartaron el resto de opciones del catálogo (perfil de vehículo, favoritos, semáforo de precios, comparación ponderada, gráficas locales) para concentrar el esfuerzo en estas dos:

1. **Mis cargas** — el usuario registra abastecimientos ficticios (estación, combustible, litros, precio, fecha). La app calcula el costo total y muestra un historial personal con el gasto acumulado.
2. **Historial de precios** — cada alta de estación o cambio de precio genera un `RegistroPrecio` con fecha. La app muestra la tendencia (lista cronológica y variación simple sube/baja/sin cambio) de un combustible en una estación.

Ambas reutilizan lógica ya planeada (costo de carga, promedios) y comparten el mismo patrón de arreglo fijo + ID estable que el resto de entidades, por lo que no exigen infraestructura nueva.

## Estructura de paquetes

```
com.example.gazoapp
├── model/    Gasolinera, Evaluacion, ReporteSaturacion, TipoCombustible,
│             NivelSaturacion, CargaCombustible, RegistroPrecio
├── data/     ArregloFijo, GazoRepository, DatosPrueba
├── logic/    Validaciones, Estadisticas (min/max/prom + empates), Costos,
│             Recomendador, HistorialPrecios
├── ui/
│   ├── theme/
│   ├── components/  TarjetaGasolinera, CampoNumerico, EstadoVacio, DialogoConfirmacion
│   └── screens/      Inicio, Catalogo, Detalle, Formulario, Comparador, Servicio,
│                      Saturacion, Calculadora, Recomendaciones, MisCargas, HistorialPrecios
└── MainActivity.kt
```

## Fases

### Fase 0 – Preparación (0.5 día)
- [ ] Acordar equipo, reparto de módulos y rama/convención de commits.
- [x] Inicializar git; limpiar plantilla; crear paquetes (`model/`, `data/`, `logic/`, `ui/components/`, `ui/screens/`).
- [x] Elegir la mejora de innovación: **Mis cargas** + **Historial de precios**.

### Fase 1 – Modelo y datos (1 día)
- [x] `data class` Gasolinera, Evaluacion, ReporteSaturacion con los campos del README §7.
- [x] `enum` TipoCombustible (Regular/Premium/Diésel) y NivelSaturacion (Baja 0–2, Media 3–5, Alta 6+), con `desdeVehiculosEnEspera`.
- [x] `data class` CargaCombustible y RegistroPrecio (modelos de la mejora de innovación).
- [x] `ArregloFijo<T>` con capacidad, conteo observable y búsqueda/eliminación por predicado.
- [x] `GazoRepository`: CRUD de estaciones, alta de evaluaciones/reportes/cargas/registros de precio (validando que la estación exista, regla 6), eliminación en cascada, IDs no reutilizables. Debe generar un `RegistroPrecio` automáticamente al crear una estación o modificar uno de sus precios.
- [x] `DatosPrueba`: ~8–10 estaciones ficticias incluyendo A/B/C del README §1, un empate de mínimo, una inactiva, una sin evaluaciones, una con un solo combustible, más algunas cargas e historial de precio de ejemplo.

### Fase 2 – Lógica pura (1.5 días) — base de los casos de prueba
- [x] `Validaciones`: obligatorios, precios > 0, al menos un combustible, distancia ≥ 0, litros/rendimiento > 0, esperas ≥ 0.
- [x] `Estadisticas`: mín/máx/promedio por combustible (solo activas con precio), **lista de empates**, promedio de evaluación = media de (aten+limp+rap)/3 por evaluación, `null` si no hay evaluaciones (no cero).
- [x] `Costos`: costo de carga, distancia extra = ida×2, litros de desvío, costo extra, total.
- [x] `Recomendador`: 4 criterios (menor precio, menor costo estimado, mejor atención, menor espera) → resultado + texto explicativo, o "datos insuficientes".
- [x] `HistorialPrecios`: ordenar registros de un combustible/estación por fecha y clasificar la variación entre los dos últimos (sube/baja/sin cambio); costo total acumulado de "Mis cargas" por estación y global.
- [x] Clasificación de saturación y antigüedad del último reporte ("hace X min") — `Tiempo.descripcionAntiguedad`.
- [x] **Tests unitarios JUnit** de esta capa (cubren casos 2, 3, 7, 8, 9, 10, 12–14, 17 del README §11; 45 tests, 0 fallos).

### Fase 3 – Esqueleto de UI y navegación (1 día)
- [x] `Scaffold` + `TopAppBar` + `ModalNavigationDrawer` con las 10 pantallas (8 base + Mis cargas + Historial de precios).
- [x] Aviso visible: "Datos ficticios/en memoria; se pierden al cerrar la app".
- [x] Componentes reutilizables (tarjeta, estado vacío, diálogo de confirmación, campo numérico con error).
- [x] Tema Material3 con foco en accesibilidad (contraste, tamaños táctiles, uso a una mano: FAB abajo). Pendiente menor para Fase 7: el ícono del menú usa `Text("☰")` en vez de `Icon` con `contentDescription`.

### Fase 4 – Pantallas CRUD (2 días)
- [ ] **Catálogo** (RF-02, RF-05): `LazyColumn` + `Card`, búsqueda insensible a mayúsculas (nombre/municipio), filtros por combustible, estado y nivel de saturación; "Sin evaluaciones"/"Sin reporte reciente".
- [ ] **Formulario reutilizable** (RF-01, RF-03): alta y edición con `OutlinedTextField`, validaciones con mensajes claros, FAB para agregar, bloqueo al llenarse la capacidad; al guardar, registra el `RegistroPrecio` correspondiente si hay precio nuevo o modificado.
- [ ] **Detalle**: datos, precios, evaluaciones, últimos reportes, editar/eliminar, accesos a "Mis cargas" e "Historial de precios" de esa estación.
- [ ] **Eliminar** (RF-04): `AlertDialog` de confirmación + cascada (incluye cargas y registros de precio).

### Fase 5 – Pantallas analíticas (2 días)
- [x] **Inicio / Panel** (RF-12): totales, activas, mín/máx/prom por combustible, mejor evaluada (solo con datos suficientes), accesos rápidos.
- [x] **Comparador de precios** (RF-06): tres combustibles, orden, extremos, empates mostrados, exclusión de inactivas/sin precio.
- [x] **Servicio** (RF-07): lista de estaciones con promedio, detalle con historial de evaluaciones, formulario 1–5.
- [x] **Saturación** (RF-08): lista/detalle con nivel por color + texto, antigüedad, leyenda "reporte manual, no en vivo", formulario de registro.
- [x] **Calculadora** (RF-09, RF-10): carga simple y comparador de desvíos ordenado por costo total; muestra supuestos y limitaciones (sin desgaste, peajes ni tiempo).
- [x] **Recomendaciones** (RF-11): selector de criterio + estación sugerida + explicación.

### Fase 6 – Reto de innovación: Mis cargas + Historial de precios (1 día)
- [x] **Mis cargas**: formulario (estación, combustible, litros, precio, fecha) con validaciones (litros y precio > 0); lista cronológica de cargas propias con costo total por registro y acumulado; bloqueo al llenarse la capacidad del arreglo.
- [x] **Historial de precios**: generación automática de `RegistroPrecio` desde el repositorio al crear/editar precios; pantalla con lista cronológica por estación y combustible, indicando subida/bajada respecto al registro anterior; estado vacío "Sin historial" cuando solo exista un registro.
- [x] Enlazar ambas pantallas desde el Detalle de la estación y desde el menú principal.

### Fase 7 – Pruebas y pulido (1.5 días)
- [ ] Ejecutar los 17 casos mínimos del README §11 y registrar entrada / esperado / obtenido (tabla para el entregable 5).
- [ ] Casos extremos: arreglo vacío, capacidad agotada (incluye "Mis cargas" e "Historial de precios"), estación inactiva, combustible no disponible, campos inválidos, rotación de pantalla (usar `rememberSaveable`/ViewModel).
- [ ] Revisar que ninguna pantalla afirme datos oficiales o "en tiempo real" y que se distingan datos ficticios / observaciones / calculados (regla 10).
- [ ] Revisión de accesibilidad (contentDescription, contraste, tamaños).

### Fase 8 – Entregables (1.5 días)
- [ ] APK de depuración (`Build > Build APK(s)`) y prueba de instalación.
- [ ] Documento de diseño: problema social, clases, reglas de negocio, navegación.
- [ ] Explicación de algoritmos (mín/máx/prom, costos, recomendaciones, tendencia de precios) con ejemplo numérico del README §1.
- [ ] Evidencia de pruebas + capturas de pantallas principales (incluye Mis cargas e Historial de precios).
- [ ] Ensayo de demo en emulador y preparación de la defensa técnica (por qué arreglos de capacidad fija, IDs estables, cascada, capacidades elegidas, por qué estas dos mejoras).

## Cronograma resumido (≈ 12 días-persona; paralelizable en equipo de 3)

| Fase | Duración | Dependencias |
|---|---|---|
| 0 Preparación | 0.5 d | — |
| 1 Modelo y datos | 1 d | 0 |
| 2 Lógica + tests | 1.5 d | 1 |
| 3 Esqueleto UI | 1 d | 0 (paralelo a 1–2) |
| 4 CRUD UI | 2 d | 1, 3 |
| 5 Pantallas analíticas | 2 d | 2, 3 |
| 6 Innovación (Mis cargas + Historial de precios) | 1 d | 4, 5 |
| 7 Pruebas y pulido | 1.5 d | 6 |
| 8 Entregables | 1.5 d | 7 (documentos pueden empezar desde Fase 2) |

Reparto sugerido para 3 personas: **A** = Fases 1–2 (datos y lógica, incluye `HistorialPrecios`); **B** = Fases 3–4 (navegación y CRUD); **C** = Fase 5 + Fase 6 (pantallas analíticas y las dos mejoras de innovación); todos en 7–8.

## Riesgos y cómo mitigarlos

| Riesgo | Mitigación |
|---|---|
| UI desactualizada por arreglo no observable | `mutableStateOf`/`SnapshotStateList` + contador observable desde el inicio (ya en `ArregloFijo`) |
| IDs reutilizados o referencias huérfanas | Contador monotónico + cascada en el repositorio, con test |
| Promedios incorrectos (ceros, división entre 0) | Devolver `null` y mostrar "Sin evaluaciones"; tests con valores conocidos |
| Empates mal mostrados | Estadísticas devuelven `List<Gasolinera>` de empatados, no una sola |
| Lógica metida en composables | Funciones puras en `logic/` y revisión de código por pares |
| Afirmar datos "en vivo" | Textos fijos de aviso y antigüedad del reporte en toda vista de saturación |
| Historial de precios crece sin control | `ArregloFijo` con capacidad fija (300) y criterio documentado si se llena (p. ej. bloquear alta y avisar) |
| "Mis cargas" sin estación válida | Repositorio valida que la estación exista antes de crear la carga (misma regla 6 que evaluaciones/reportes) |

## Trazabilidad requisitos → fase

RF-01/03 → F4 · RF-02/05 → F4 · RF-04 → F1+F4 · RF-06 → F2+F5 · RF-07 → F2+F5 · RF-08 → F2+F5 · RF-09/10 → F2+F5 · RF-11 → F2+F5 · RF-12 → F5 · Reglas 1–12 → F1/F2 · Casos de prueba 1–17 → F2 (unitarios) y F7 (manuales) · Mejoras "Mis cargas"/"Historial de precios" → F1 (modelo, ya hecho) + F2 (`HistorialPrecios`) + F6 (pantallas) · Entregables 1–7 → F8
