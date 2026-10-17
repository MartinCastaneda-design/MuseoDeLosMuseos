package com.example.museodelosmuseos.data.remote

import com.example.museodelosmuseos.data.local.entity.Museo
import kotlinx.coroutines.delay

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: MuseoApiService (API REST Ficticia)
 * ============================================================================
 * Cumple con el criterio de la pauta:
 * "Consulta datos de una API REST ficticia... puede usar SQLite como caché"
 *
 * Simula llamadas HTTP de red hacia un backend REST institucional con
 * latencia realista, retornando el catálogo de museos iberoamericanos.
 * ============================================================================
 */
class MuseoApiService {

    /**
     * Simula la petición HTTP: GET /api/v1/museos/iberoamerica
     */
    suspend fun obtenerMuseosIberoamerica(): List<Museo> {
        // Simular latencia de red de una petición REST
        delay(600)

        return listOf(
            Museo(
                nombre = "Museo de Arte Colonial",
                pais = "Colombia",
                ciudad = "Bogotá",
                descripcion = "Recorre siglos de historia a través de nuestras colecciones de arte colonial iberoamericano. Explora salas virtuales, exposiciones y material educativo.",
                clasificacion = "Arte Colonial",
                latitud = 4.5975,
                longitud = -74.0758,
                urlVisitaVirtual = "https://museocolonial.gov.co/recorrido-virtual",
                codigoQr = "MUSEO_COLONIAL_BOGOTA"
            ),
            Museo(
                nombre = "Museo Nacional de Bellas Artes",
                pais = "Chile",
                ciudad = "Santiago",
                descripcion = "Principal centro de difusión de las artes visuales en Chile, con colecciones históricas y salas de exposiciones patrimoniales.",
                clasificacion = "Bellas Artes",
                latitud = -33.4358,
                longitud = -70.6433,
                urlVisitaVirtual = "https://www.mnba.gob.cl/recorrido-virtual",
                codigoQr = "MUSEO_BELLAS_ARTES_CHILE"
            ),
            Museo(
                nombre = "Museo Nacional del Prado",
                pais = "España",
                ciudad = "Madrid",
                descripcion = "Una de las pinacotecas más importantes del mundo, con obras maestras de Velázquez, Goya, El Greco y otros maestros universales.",
                clasificacion = "Historia y Pintura",
                latitud = 40.4138,
                longitud = -3.6921,
                urlVisitaVirtual = "https://www.museodelprado.es/coleccion/obras-de-arte",
                codigoQr = "MUSEO_PRADO_MADRID"
            ),
            Museo(
                nombre = "Museo de Arte Moderno",
                pais = "México",
                ciudad = "Ciudad de México",
                descripcion = "Espacio emblemático dedicado a preservar y difundir el arte moderno mexicano del siglo XX, situado en el bosque de Chapultepec.",
                clasificacion = "Arte Moderno",
                latitud = 19.4225,
                longitud = -99.1795,
                urlVisitaVirtual = "https://mam.inba.gob.mx/visitas-virtuales",
                codigoQr = "MUSEO_MAM_MEXICO"
            ),
            Museo(
                nombre = "Museo de Arte de Lima (MALI)",
                pais = "Perú",
                ciudad = "Lima",
                descripcion = "Exhibe más de 3,000 años de historia del arte peruano, desde la era precolombina, colonial y republicana hasta la contemporánea.",
                clasificacion = "Arte y Arqueología",
                latitud = -12.0601,
                longitud = -77.0369,
                urlVisitaVirtual = "https://mali.pe/visita-virtual",
                codigoQr = "MUSEO_MALI_LIMA"
            )
        )
    }
}
