# Pruebas unitarias y cobertura — Unidad 3

Fecha de verificación: 22 de noviembre de 2026. Base: `8391bcb`, rama `main`.

## Resultado

109 pruebas unitarias aprobadas, sin fallos, errores ni pruebas omitidas: las 32 originales y 77 nuevas. `assembleDebug` correcto. No se modificaron archivos Kotlin de producción, pantallas, esquema Room, migraciones ni backend.

| Medición de líneas | Inicial (32 pruebas) | Final (109 pruebas) |
| --- | ---: | ---: |
| Lógica: ViewModels, repositorios, modelos y estados | 10,55 % (206/1952) | 97,80 % (1909/1952) |
| Aplicación completa, incluido código Android y Compose | 3,64 % (276/7580) | 26,98 % (2045/7580) |

La lógica alcanza también 80,75 % de ramas de decisión (856/1060). El conjunto completo de la aplicación alcanza 34,94 % de ramas (856/2450). **No se afirma un 80 % de cobertura de toda la aplicación.**

| Grupo dentro del informe de lógica | Líneas | Ramas |
| --- | ---: | ---: |
| Los 14 ViewModels | 97,65 % | 76,65 % |
| Los 3 repositorios | 97,66 % | 83,70 % |
| Modelos, reglas y estados | 98,34 % | 84,80 % |

Estos porcentajes corresponden a una ejecución JVM de todas las pruebas unitarias. No incorporan los resultados de pruebas instrumentadas anteriores ni indican que todos los comportamientos estén libres de errores.

## Herramienta y alcance

JaCoCo 0.8.14, compatible con el JDK 25 del proyecto. Configuración en `app/build.gradle.kts`; informes HTML y XML.

- `coberturaDebug`: incluye todas las clases Kotlin de producción de la variante debug, incluidas pantallas Compose, componentes, navegación, entidades, Room, archivos de avatar y adaptadores de red.
- `coberturaLogicaDebug`: incluye todos los archivos de `data/model` y `data/repository`, todos los ViewModels y UiState, y los modelos de formularios en `ui/screens`, incluidas sus clases internas. No se excluye ningún ViewModel o repositorio por su porcentaje.
- Solo se omiten clases generadas por herramientas: `R`, `BuildConfig` e implementaciones `*_Impl` de Room. Los adaptadores Android y Composables permanecen visibles en el informe general; están fuera del informe específico de lógica, no se cuentan como probados.
- `verificarCoberturaLogicaDebug`: exige como mínimo 80 % de líneas y 80 % de ramas en el conjunto de lógica. No exige ese porcentaje individualmente a cada clase.

El informe general conserva el mismo denominador antes y después. La compilación utiliza la carpeta de clases Kotlin integrada de AGP 9.4.1; si se actualiza AGP, revisar esa ruta y la de datos `.exec` antes de comparar nuevos resultados.

## Dependencias de pruebas

Se reutilizan las dependencias ya presentes: JUnit 4.13.2, MockK 1.14.5, Kotest Assertions 5.9.1 y Coroutines Test 1.10.2. Sus versiones permanecen en `gradle/libs.versions.toml`. No se cambian dependencias del backend.

`BaseViewModelTest` instala `StandardTestDispatcher` como Main, permite avanzar las tareas de forma determinista, conserva los ViewModels en un `ViewModelStore` y los limpia al finalizar. Las pruebas usan flujos mutables para simular cambios reales de sesión y de datos. MockK simula repositorios, APIs, DAOs y almacenamiento de avatar; Kotest se utiliza en comprobaciones de resultados remotos y autenticación.

## Comportamientos comprobados

- **Autenticación y registro:** campos vacíos, formato de correo, contraseñas cortas, normalización, éxito, rechazo, callbacks y correo demo editado sin sobrescribir entradas personales.
- **Catálogo:** acceso por rol, cambio de sesión, búsqueda y categoría, selección, alta y edición, precio decimal, stock, desactivación, reactivación, disponibilidad y errores de lectura/escritura.
- **Detalle de artículo y carrito:** validación de cantidad y consulta de curso, stock acumulado, envío nacional/express, formularios incompletos, resumen revisado, prevención de envío duplicado, limpieza de estado por usuario y errores.
- **Favoritos y avisos:** filtros, confirmación de eliminación, URL de recorrido, rol para publicar, validación de formulario, lectura individual y masiva, navegación y errores.
- **Perfil:** carga, validaciones, preferencias, borradores independientes por usuario, cancelación de imagen, importación simulada, eliminación de borrador, guardado, errores y cierre de sesión. No se simula una cámara física como si estuviera probada.
- **Mapa, Dashboard y museos:** filtros combinados, selección, sincronización y respaldo local, destacados, preferencia del contador de avisos, cambio de pestaña, favoritos, descripción, revisión de ficha y estados del clima.
- **QR:** entrada vacía, código conocido/desconocido, registro con propietario, resultado y avance temporal de la animación.
- **Solicitudes:** historial local y montos históricos, filtros, cambio de estado por Administrador y fallos; carga, creación, edición, eliminación y errores remotos con APIs simuladas.
- **Clima:** conversión de datos, coordenadas enviadas, datos ausentes, fallo de red, códigos meteorológicos y límites de recomendaciones.
- **MuseoRepository real con DAOs simulados:** autenticación, correo duplicado, cantidades, cotizaciones obsoletas, escritura de pedido y líneas históricas, vaciado por usuario, curso duplicado, favoritos, correo de perfil normalizado, preferencias ajenas, concurrencia de estados y avisos privados.

En las pruebas JVM de `MuseoRepository`, el mock de `withTransaction` ejecuta el bloque real del repositorio con DAOs simulados. Esto permite comprobar decisiones y argumentos **sin demostrar atomicidad, rollback ni SQL real**. Esas garantías corresponden a las pruebas Room de `androidTest`, que se conservan sin cambios.

## Cobertura por archivo principal

Porcentajes de líneas; los detalles de ramas y líneas pendientes están en el HTML.

| Archivo | Líneas |
| --- | ---: |
| LoginViewModel.kt | 100,00 % |
| RegistroViewModel.kt | 100,00 % |
| CatalogoViewModel.kt | 99,23 % |
| MapaViewModel.kt | 98,39 % |
| DetalleMuseoViewModel.kt | 98,04 % |
| SolicitudesViewModel.kt | 98,14 % |
| EscanerQrViewModel.kt | 96,77 % |
| AvisosViewModel.kt | 96,23 % |
| DashboardViewModel.kt | 95,00 % |
| CarritoViewModel.kt | 94,92 % |
| FavoritosViewModel.kt | 94,74 % |
| PerfilViewModel.kt | 94,19 % |
| DetalleArticuloViewModel.kt | 93,55 % |
| ResultadoQrViewModel.kt | 100,00 % |
| MuseoRepository.kt | 99,59 % |
| ClimaRepository.kt | 94,74 % |
| MicroservicioRepository.kt | 87,18 % |

## Pendiente y límites

- Quedan ramas de cancelación y combinaciones de guardado en curso, cambios de sesión durante importación de avatar, valores nulos y constructores/configuración por defecto. Consultar el HTML; una línea ejecutada no implica todas sus ramas probadas.
- Ningún ViewModel ni repositorio queda completamente sin ejecutar; `MicroservicioRepository` conserva pendiente la configuración global de URL y su constructor por defecto. El DTO climático tiene 77,78 % de líneas por valores por defecto no ejercitados.
- El informe general muestra pendientes las pantallas, permisos reales, cámara, archivos Android, navegación visual y gran parte de Room. Necesitan pruebas instrumentadas o manuales, no subir artificialmente el porcentaje de JVM.
- Se conservan las 21 pruebas instrumentadas, pero no se ejecutaron de nuevo en esta tarea. Las 32 pruebas unitarias previas tampoco se editaron.
- Las respuestas remotas se simulan: falta comprobar HTTP real, configuración del servidor y flujo Android ↔ Spring Boot ↔ API externa. El backend de Martín y sus pruebas no se modificaron.
- No se genera ni se firma un APK de entrega. Solo se comprueba la compilación debug.

## Cómo repetir la comprobación

Desde la raíz, en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:coberturaDebug :app:verificarCoberturaLogicaDebug :app:assembleDebug --console=plain
```

No usar `--tests` al producir el informe final: medir una selección parcial produciría otros porcentajes. Si se repite sin cambios, Gradle puede reutilizar resultados válidos; `--rerun-tasks` fuerza una nueva ejecución cuando sea necesario.

Resultados generados, ignorados por Git:

- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/jacoco/coberturaLogicaDebug/html/index.html`
- `app/build/reports/jacoco/coberturaLogicaDebug/coberturaLogicaDebug.xml`
- `app/build/reports/jacoco/coberturaDebug/html/index.html`
- `app/build/reports/jacoco/coberturaDebug/coberturaDebug.xml`

Las mediciones iniciales se conservaron durante esta tarea como `inicial.xml` junto a cada informe. Los informes generados pueden borrarse con `clean`; este documento conserva la comparación inicial/final.

## Fuentes técnicas

- [JaCoCo 0.8.14: compatibilidad con Java 25](https://github.com/jacoco/jacoco/releases/tag/v0.8.14).
- [Configuración de informes y verificación de cobertura en Gradle](https://docs.gradle.org/current/userguide/jacoco_plugin.html).
