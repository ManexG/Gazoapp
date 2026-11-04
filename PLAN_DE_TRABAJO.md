# GazoSmart – Plan de trabajo (Proyecto Integrador, Etapa 1)

Estado de partida: proyecto Android Studio vacío (plantilla "Hello Android"), Kotlin + Compose + Material3, minSdk 24. Sin dependencias extra; solo falta añadir `navigation-compose` si se decide usarlo (opcional, ver Fase 1).

## Decisiones de arquitectura (fijar antes de programar)

- **Sin dependencias externas.** Navegación con `ModalNavigationDrawer` + un `enum class Pantalla` y `mutableStateOf` (suficiente para 8 pantallas); evita `navigation-compose`.
- **Estado:** un `GazoViewModel` (o clase `Repositorio` con `mutableStateListOf`) que concentre los 3 arreglos. Usar `mutableStateListOf` / `SnapshotStateList` para que la UI se actualice (requisito del PDF sobre arreglos no observables).
- **Arreglos de capacidad fija:** `arrayOfNulls<Gasolinera>(30)` envuelto en una clase `ArregloFijo<T>` con `agregar/eliminar/buscarPorId` + contador observable. Capacidades propuestas (justificarlas en el documento): evaluaciones = 300 (10 por estación), saturación = 150 (5 por estación).
- **IDs:** contadores `siguienteId` por entidad que nunca decrecen (regla de negocio 2).
- **Eliminación (RF-04):** eliminar en cascada evaluaciones y reportes de la estación (sin huérfanos); documentarlo.
- **Separación:** `model/`, `logic/` (funciones puras y testeables), `data/` (repositorio + datos de prueba), `ui/` (screens, components, theme).

## Estructura de paquetes

```
com.example.gazoapp
├── model/    Gasolinera, Evaluacion, ReporteSaturacion, TipoCombustible, NivelSaturacion
├── data/     ArregloFijo, GazoRepository, DatosPrueba
├── logic/    Validaciones, Estadisticas (min/max/prom + empates), Costos, Recomendador
├── ui/
│   ├── theme/
│   ├── components/  TarjetaGasolinera, CampoNumerico, EstadoVacio, DialogoConfirmacion
│   └── screens/     Inicio, Catalogo, Detalle, Formulario, Comparador, Servicio,
│                    Saturacion, Calculadora, Recomendaciones
└── MainActivity.kt
```

## Fases

### Fase 0 – Preparación (0.5 día)
- [ ] Acordar equipo, reparto de módulos y rama/convención de commits.
- [ ] Inicializar git; limpiar plantilla; crear paquetes.
- [ ] Elegir la mejora de innovación (recomendada: **Comparación ponderada** + **Semáforo de precios**; reutilizan lógica de RF-06/RF-11 y son baratas de implementar).

### Fase 1 – Modelo y datos (1 día)
- [ ] `data class` Gasolinera, Evaluacion, ReporteSaturacion con los campos del §6.
- [ ] `enum` TipoCombustible (Regular/Premium/Diésel) y NivelSaturacion (Baja 0–2, Media 3–5, Alta 6+).
- [ ] `ArregloFijo<T>` con capacidad, conteo observable y búsqueda por ID.
- [ ] `GazoRepository`: CRUD de estaciones, alta de evaluaciones/reportes (validando que la estación exista, regla 6), eliminación en cascada, IDs no reutilizables.
- [ ] `DatosPrueba`: ~8–10 estaciones ficticias incluyendo A/B/C del §2, un empate de mínimo, una inactiva, una sin evaluaciones, una con un solo combustible.

### Fase 2 – Lógica pura (1.5 días) — base de los casos de prueba
- [ ] `Validaciones`: obligatorios, precios > 0, al menos un combustible, distancia ≥ 0, litros/rendimiento > 0, esperas ≥ 0.
- [ ] `Estadisticas`: mín/máx/promedio por combustible (solo activas con precio), **lista de empates**, promedio de evaluación = media de (aten+limp+rap)/3 por evaluación, `null` si no hay evaluaciones (no cero).
- [ ] `Costos`: costo de carga, distancia extra = ida×2, litros de desvío, costo extra, total.
- [ ] `Recomendador`: 4 criterios (menor precio, menor costo estimado, mejor atención, menor espera) → resultado + texto explicativo, o "datos insuficientes".
- [ ] Clasificación de saturación y antigüedad del último reporte ("hace X min").
- [ ] **Tests unitarios JUnit** de esta capa (cubren casos 3, 7, 8, 9, 12–14, 17 del §13).

### Fase 3 – Esqueleto de UI y navegación (1 día)
- [ ] `Scaffold` + `TopAppBar` + `ModalNavigationDrawer` con las 8 pantallas.
- [ ] Aviso visible: "Datos ficticios/en memoria; se pierden al cerrar la app".
- [ ] Componentes reutilizables (tarjeta, estado vacío, diálogo de confirmación, campo numérico con error).
- [ ] Tema Material3 con foco en accesibilidad (contraste, tamaños táctiles, uso a una mano: FAB abajo).

### Fase 4 – Pantallas CRUD (2 días)
- [ ] **Catálogo** (RF-02, RF-05): `LazyColumn` + `Card`, búsqueda insensible a mayúsculas (nombre/municipio), filtros por combustible, estado y nivel de saturación; "Sin evaluaciones"/"Sin reporte reciente".
- [ ] **Formulario reutilizable** (RF-01, RF-03): alta y edición con `OutlinedTextField`, validaciones con mensajes claros, FAB para agregar, bloqueo al llenarse la capacidad.
- [ ] **Detalle**: datos, precios, evaluaciones, últimos reportes, editar/eliminar.
- [ ] **Eliminar** (RF-04): `AlertDialog` de confirmación + cascada.

### Fase 5 – Pantallas analíticas (2 días)
- [ ] **Inicio / Panel** (RF-12): totales, activas, mín/máx/prom por combustible, mejor evaluada (solo con datos suficientes), accesos rápidos.
- [ ] **Comparador de precios** (RF-06): tres combustibles, orden, extremos, empates mostrados, exclusión de inactivas/sin precio.
- [ ] **Servicio** (RF-07): formulario 1–5, promedios por evaluación y general, conteo.
- [ ] **Saturación** (RF-08): registro con fecha/hora, nivel por color + texto, antigüedad, leyenda "reporte manual, no en vivo".
- [ ] **Calculadora** (RF-09, RF-10): carga simple y comparador de desvíos ordenado por costo total; mostrar supuestos y limitaciones (sin desgaste, peajes ni tiempo).
- [ ] **Recomendaciones** (RF-11): selector de criterio + estación sugerida + explicación.

### Fase 6 – Reto de innovación (1 día)
- [ ] Implementar la(s) mejora(s) elegida(s) (ver Fase 0) e integrarla(s) en pantallas existentes.

### Fase 7 – Pruebas y pulido (1.5 días)
- [ ] Ejecutar los 17 casos mínimos del §13 y registrar entrada / esperado / obtenido (tabla para el entregable 5).
- [ ] Casos extremos: arreglo vacío, capacidad agotada, estación inactiva, combustible no disponible, campos inválidos, rotación de pantalla (usar `rememberSaveable`/ViewModel).
- [ ] Revisar que ninguna pantalla afirme datos oficiales o "en tiempo real" y que se distingan datos ficticios / observaciones / calculados (regla 10).
- [ ] Revisión de accesibilidad (contentDescription, contraste, tamaños).

### Fase 8 – Entregables (1.5 días)
- [ ] APK de depuración (`Build > Build APK(s)`) y prueba de instalación.
- [ ] Documento de diseño: problema social, clases, reglas de negocio, navegación.
- [ ] Explicación de algoritmos (mín/máx/prom, costos, recomendaciones) con ejemplo numérico del §2.
- [ ] Evidencia de pruebas + capturas de pantallas principales.
- [ ] Ensayo de demo en emulador y preparación de la defensa técnica (por qué arreglos de capacidad fija, IDs estables, cascada, capacidades elegidas).

## Cronograma resumido (≈ 12 días-persona; paralelizable en equipo de 3)

| Fase | Duración | Dependencias |
|---|---|---|
| 0 Preparación | 0.5 d | — |
| 1 Modelo y datos | 1 d | 0 |
| 2 Lógica + tests | 1.5 d | 1 |
| 3 Esqueleto UI | 1 d | 0 (paralelo a 1–2) |
| 4 CRUD UI | 2 d | 1, 3 |
| 5 Pantallas analíticas | 2 d | 2, 3 |
| 6 Innovación | 1 d | 4, 5 |
| 7 Pruebas y pulido | 1.5 d | 6 |
| 8 Entregables | 1.5 d | 7 (documentos pueden empezar desde Fase 2) |

Reparto sugerido para 3 personas: **A** = Fases 1–2 (datos y lógica); **B** = Fases 3–4 (navegación y CRUD); **C** = Fase 5 (pantallas analíticas); todos en 6–8.

## Riesgos y cómo mitigarlos

| Riesgo | Mitigación |
|---|---|
| UI desactualizada por arreglo no observable | `SnapshotStateList`/`StateFlow` + contador observable desde el inicio |
| IDs reutilizados o referencias huérfanas | Contador monotónico + cascada en el repositorio, con test |
| Promedios incorrectos (ceros, división entre 0) | Devolver `null` y mostrar "Sin evaluaciones"; tests con valores conocidos |
| Empates mal mostrados | Estadísticas devuelven `List<Gasolinera>` de empatados, no una sola |
| Lógica metida en composables | Funciones puras en `logic/` y revisión de código por pares |
| Afirmar datos "en vivo" | Textos fijos de aviso y antigüedad del reporte en toda vista de saturación |

## Trazabilidad requisitos → fase

RF-01/03 → F4 · RF-02/05 → F4 · RF-04 → F1+F4 · RF-06 → F2+F5 · RF-07 → F2+F5 · RF-08 → F2+F5 · RF-09/10 → F2+F5 · RF-11 → F2+F5 · RF-12 → F5 · Reglas 1–12 → F1/F2 · Casos de prueba 1–17 → F2 (unitarios) y F7 (manuales) · Entregables 1–7 → F8
