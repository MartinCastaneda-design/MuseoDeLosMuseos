# Integración local de pantallas 1–15

Fecha: 22 de octubre de 2026.

- Rama temporal: `codex/integracion-pantallas-1-15`.
- Base de main: `1e5f46e95691622f2596849f24991d049ea421c4`.
- Rama incorporada: `feature/matias`, punta `7a3f2778ae4bf60a08a974a90436eb8334d1b1b2`.
- Integración mediante merge, conservando ambos historiales. No se publica ni se fusiona con main durante esta revisión.

## Resolución de conflictos

Se resolvieron nueve bloques en cuatro archivos, conservando el resto de la combinación automática:

| Archivo | Resolución |
| --- | --- |
| `AppDatabase.kt` | Room 7 y las migraciones 2→3→4→5→6→7; precarga protegida del catálogo; actualización selectiva de imágenes de main. |
| `MuseoRepository.kt` | Operaciones de perfil y preferencias con validación de sesión; conservación de compras, avisos e historial. |
| `PantallaDashboard.kt` | Componente compartido de fotografías reales, accesos de las pantallas 9–15 y diseño existente. |
| `PantallaDetalleMuseo.kt` | Fotografía local y sus créditos, acceso a favoritos del Cliente y opciones existentes de Investigador/Administrador. |

## Ajustes necesarios para integrar ambos trabajos

- Se conserva `ImageLoaderFactory` de main con sus cachés de Coil y configuración de red.
- `ImagenesMuseosDemo` centraliza las cinco URL verificadas y las direcciones antiguas exactas.
- Las fotografías reales locales tienen prioridad para los registros de demostración. Una URL personalizada sigue cargándose con Coil y conserva respaldo local ante fallo.
- La actualización automática solo sustituye una URL Unsplash exacta del conjunto anterior de demostración. No reemplaza URL personalizadas ni cadenas vacías elegidas para usar el respaldo local.
- La inicialización de Room se ejecuta desde un único callback y dentro de una transacción. No depende del correo editable del Administrador ni inicia o cambia sesiones automáticamente.
- La precarga no recrea perfiles por sus correos anteriores ni repone stock cuando el catálogo ya fue inicializado.
- El resultado QR reutiliza también el componente fotográfico, conservando sus controles y dimensiones.
- No se modifica el esquema Room 7 ni se habilitan migraciones destructivas.
- Las licencias y atribuciones permanecen en `FOTOGRAFIAS_MUSEOS.md` y en los recursos incluidos en el APK.

## Verificaciones

- `assembleDebug`, `testDebugUnitTest` y `assembleDebugAndroidTest`: correctos.
- 32 pruebas unitarias sin fallos.
- 20 pruebas Android de datos y permisos sin fallos, ejecutadas directamente con `am instrument` sobre bases aisladas.
- Incluye pruebas nuevas de actualización selectiva/idempotente de imágenes, conservación de perfiles, sesión, stock y preferencias, y migración de una base aislada v2 a v7 conservando usuarios, museos y favoritos.
- `lintDebug`: correcto, sin errores; 28 advertencias informativas o de mantenimiento. No se cambian versiones para eliminar esos avisos.
- Instalación de la aplicación integrada en Samsung SM-A146M con `install -r`, conservando los datos de la instalación existente.
- La prueba automatizada de interfaz y la comprobación manual del Samsung están pendientes de desbloquear el teléfono al redactar este informe.

## Revisión previa a main y al video

Revisar el resultado con ambos integrantes antes de fusionar con main. El video debe mostrar navegación y roles, formularios validados, persistencia al reiniciar, fotos locales, carrito/envío, historial, avisos y avatar con cámara. GPS, lectura QR física y recorridos externos requieren su comprobación específica; no se consideran verificados solo porque el proyecto compile.
