# Museo de los Museos 

Aplicación móvil nativa desarrollada en Kotlinpara la asignatura Desarrollo de Aplicaciones Móviles


Integrantes del Equipo

* Martín Castañeda 
* Matías Korner 

---

## Funcionalidades y Pantallas Implementadas (15 Pantallas)

La aplicación cuenta con 15 pantallas:

1. **Pantalla 1 — Bienvenida / Splash:** Presentación patrimonial con transiciones animadas y selección de acceso.
2. **Pantalla 2 — Registro de Usuario:** Formulario con validación en tiempo real de correos, contraseñas y asignación de roles (Cliente / Administrador).
3. **Pantalla 3 — Autenticación e Inicio de Sesión:** Validación criptográfica de credenciales contra base de datos local y restauración de sesiones persistentes.
4. **Pantalla 4 — Dashboard Principal:** Pantalla de inicio con carrusel interactivo de museos destacados, accesos rápidos y panel de novedades.
5. **Pantalla 5 — Mapa Interactivo de Museos:** Georreferenciación con OpenStreetMap (OSMDroid), cálculo de distancia geográfica y visualización de pines patrimoniales.
6. **Pantalla 6 — Detalle del Museo:** Ficha histórica completa, fotos de alta resolución, enlaces a recorridos 360° e **integración de clima en tiempo real**.
7. **Pantalla 7 — Escáner QR Patrimonial:** Lector óptico integrado mediante Google Code Scanner para escaneo de códigos QR en salas de exhibición.
8. **Pantalla 8 — Resultado del Escaneo QR:** Despliegue de fichas técnicas de obras y registro automático en el historial de visitas del usuario.
9. **Pantalla 9 — Catálogo de Tienda Cultural:** Catálogo de artículos patrimoniales (libros, artesanías, boletos) con filtros por categoría y control de stock.
10. **Pantalla 10 — Carrito de Compras y Cotización:** Selección de cantidades, desglose de subtotal, costo de despacho regional/nacional, cálculo de IVA y confirmación de pedidos.
11. **Pantalla 11 — Colección de Museos Favoritos:** Marcado y gestión personalizada de museos de interés para usuarios con rol Cliente.
12. **Pantalla 12 — Muro de Avisos y Novedades:** Muro comunitario con control de permisos para publicación de avisos culturales y eventos.
13. **Pantalla 13 — Gestión de Solicitudes Culturales (CRUD Microservicio Spring Boot):** Módulo central de interacción cliente-servidor con operaciones en tiempo real sobre base de datos H2 y respaldo local en Room.
14. **Pantalla 14 — Centro de Notificaciones:** Sistema de alertas internas y notificaciones por usuario para cambios de estado de solicitudes y pedidos.
15. **Pantalla 15 — Perfil de Usuario y Configuración:** Gestión de información personal, captura de fotografía de avatar con cámara del dispositivo o selección desde galería, persistencia en almacenamiento seguro y cierre de sesión.

---

## Endpoints Utilizados

### 1. API Externa Remota — Open-Meteo Weather API
Permite consultar las condiciones meteorológicas en vivo en las coordenadas de cada museo para planificar visitas a espacios abiertos o cerrados.
* **Base URL:** `https://api.open-meteo.com/`
* **Endpoint:** `GET /v1/forecast`
* **Parámetros:** `latitude`, `longitude`, `current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m`
* **Cliente en Android:** Implementado con Retrofit 2 y Gson Converter (`ClimaApiService`, `RetrofitClimaInstance`, `ClimaRepository`).

### 2. Microservicio Propio — Spring Boot REST API
Microservicio desarrollado en Java 21 con Spring Boot 3.4 y Spring Data JPA, respaldado por base de datos en memoria H2.
* **Base URL:** `http://localhost:8080/` (o IP local Wi-Fi / `http://10.0.2.2:8080/` en emulador).
* **Consola de Base de Datos H2:** `http://localhost:8080/h2-console`
  * **JDBC URL:** `jdbc:h2:mem:museodb`
  * **Usuario:** `sa` | **Contraseña:** *(vacío)*

#### Operaciones CRUD expuestas:
| Método | Endpoint | Descripción |
| :---: | :--- | :--- |
| **GET** | `/api/solicitudes` | Listar todas las solicitudes culturales registradas. |
| **GET** | `/api/solicitudes/{id}` | Consultar detalle de una solicitud específica. |
| **POST** | `/api/solicitudes` | Crear una nueva solicitud cultural (título, descripción, museo, solicitante). |
| **PUT** | `/api/solicitudes/{id}` | Actualizar estado (`PENDIENTE`, `EN_REVISION`, `APROBADA`, `RECHAZADA`) y respuesta técnica. |
| **DELETE** | `/api/solicitudes/{id}` | Eliminar una solicitud cultural de la base de datos. |

---

## Pasos para Ejecutar el Proyecto

### 1. Ejecutar el Microservicio Spring Boot (Backend)
1. Abrir una terminal en el directorio del backend:
   ```bash
   cd backend-spring
   ```
2. Iniciar el servidor Spring Boot:
mvnw.cmd spring-boot:run

3. El microservicio estará escuchando en `http://localhost:8080/api/solicitudes`.

### 2. Ejecutar la Aplicación Android Móvil
1. Abrir la carpeta raíz del proyecto en Android Studio
2. Sincronizar las dependencias de Gradle.
3. Para conectar un USB con depuración activada, ejecutar en la terminal del PC:
   ```powershell
   adb reverse tcp:8080 tcp:8080
   ```
4. Presionar Run 'app' en Android Studio.

---

## Pruebas Unitarias y Cobertura de Código (JaCoCo)

### Comandos de ejecución:
```powershell
.\gradlew.bat testDebugUnitTest

.\gradlew.bat verificarCoberturaLogicaDebug
```

---

##  Generación de APK Firmado Digitalmente (.jks)


### Datos de la (Keystore):
* **Ruta del almacén:** `app/keystore/museo_release.jks` (y respaldo en `release/museo_release.jks`)
* **Validez:** 10.000 días
* **Alias:** `museo_key`
* **Contraseña de almacén y clave:** `museo123`


El archivo ejecutable firmado generado se encuentra en:
* **Ubicación en el repositorio:** `release/app-release.apk` 


<img width="666" height="647" alt="image" src="https://github.com/user-attachments/assets/a87157c9-bd8f-4254-8023-b819fc166810" />


---


enlace de trello: https://trello.com/b/ZMMK5x3w/mi-tablero-de-trello
