package com.example.museodelosmuseos.data.remote

import com.example.museodelosmuseos.data.model.ImagenesMuseosDemo
import com.example.museodelosmuseos.data.local.entity.Museo
import kotlinx.coroutines.delay

class MuseoApiService {

    suspend fun obtenerMuseosIberoamerica(): List<Museo> {

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
                urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-colonial",
                codigoQr = "MUSEO_COLONIAL_BOGOTA",
                imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_COLONIAL_BOGOTA").url
            ),
            Museo(
                nombre = "Museo Nacional de Bellas Artes",
                pais = "Chile",
                ciudad = "Santiago",
                descripcion = "Principal centro de difusión de las artes visuales en Chile, con colecciones históricas y salas de exposiciones patrimoniales.",
                clasificacion = "Bellas Artes",
                latitud = -33.4358,
                longitud = -70.6433,
                urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-nacional-de-bellas-artes-chile",
                codigoQr = "MUSEO_BELLAS_ARTES_CHILE",
                imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_BELLAS_ARTES_CHILE").url
            ),
            Museo(
                nombre = "Museo Nacional del Prado",
                pais = "España",
                ciudad = "Madrid",
                descripcion = "Una de las pinacotecas más importantes del mundo, con obras maestras de Velázquez, Goya, El Greco y otros maestros universales.",
                clasificacion = "Historia y Pintura",
                latitud = 40.4138,
                longitud = -3.6921,
                urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-del-prado",
                codigoQr = "MUSEO_PRADO_MADRID",
                imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_PRADO_MADRID").url
            ),
            Museo(
                nombre = "Museo de Arte Moderno",
                pais = "México",
                ciudad = "Ciudad de México",
                descripcion = "Espacio emblemático dedicado a preservar y difundir el arte moderno mexicano del siglo XX, situado en el bosque de Chapultepec.",
                clasificacion = "Arte Moderno",
                latitud = 19.4225,
                longitud = -99.1795,
                urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-de-arte-moderno-mexico",
                codigoQr = "MUSEO_MAM_MEXICO",
                imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_MAM_MEXICO").url
            ),
            Museo(
                nombre = "Museo de Arte de Lima (MALI)",
                pais = "Perú",
                ciudad = "Lima",
                descripcion = "Exhibe más de 3,000 años de historia del arte peruano, desde la era precolombina, colonial y republicana hasta la contemporánea.",
                clasificacion = "Arte y Arqueología",
                latitud = -12.0601,
                longitud = -77.0369,
                urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-de-arte-de-lima-mali",
                codigoQr = "MUSEO_MALI_LIMA",
                imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_MALI_LIMA").url
            )
        )
    }
}
