package com.example.museodelosmuseos.unidad3

import com.example.museodelosmuseos.data.remote.clima.ClimaApiService
import com.example.museodelosmuseos.data.remote.microservicio.*
import com.example.museodelosmuseos.data.model.clima.*
import com.example.museodelosmuseos.data.repository.*
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class RepositoriosRemotosTest {
    @Test fun climaTransformaDatosYConservaCoordenadas() = runTest {
        val api = mockk<ClimaApiService>()
        coEvery { api.obtenerClimaActual(-33.4, -70.6, any()) } returns RespuestaClimaDto(-33.4, -70.6, ClimaActualDto(temperatura = 20.0, humedad = 60, codigoClima = 0, velocidadViento = 8.0))
        val clima = ClimaRepository(api).consultarClimaCiudad(-33.4, -70.6).getOrThrow()
        clima.temperatura shouldBe 20.0; clima.humedad shouldBe 60; clima.vientoKmH shouldBe 8.0; clima.condicion shouldBe "Cielo despejado"
        assertTrue(clima.recomendacionVisita.contains("presencialmente")); coVerify(exactly = 1) { api.obtenerClimaActual(-33.4, -70.6, any()) }
    }
    @Test fun climaSinActualYErrorDeRedSePropaganComoFallo() = runTest {
        val api = mockk<ClimaApiService>(); coEvery { api.obtenerClimaActual(any(), any(), any()) } returns RespuestaClimaDto(0.0, 0.0)
        assertTrue(ClimaRepository(api).consultarClimaCiudad(0.0, 0.0).exceptionOrNull() is IllegalStateException)
        val error = java.io.IOException("Sin red"); coEvery { api.obtenerClimaActual(any(), any(), any()) } throws error
        ClimaRepository(api).consultarClimaCiudad(0.0, 0.0).exceptionOrNull() shouldBe error
    }
    @Test fun interpretaCodigosClimaticosYLimitesDeRecomendacion() {
        val casos = mapOf(0 to "Cielo despejado", 1 to "Mayormente despejado", 2 to "Parcialmente nublado", 3 to "Nublado", 45 to "Niebla", 48 to "Niebla", 51 to "Llovizna", 53 to "Llovizna", 55 to "Llovizna", 61 to "Lluvia", 63 to "Lluvia", 65 to "Lluvia", 71 to "Nieve", 73 to "Nieve", 75 to "Nieve", 80 to "Chubascos", 81 to "Chubascos", 82 to "Chubascos", 95 to "Tormenta eléctrica", 96 to "Tormenta eléctrica", 99 to "Tormenta eléctrica", -1 to "Condición templada")
        casos.forEach { (codigo, esperado) -> InterpretadorClima.interpretar(codigo).first shouldBe esperado }
        assertTrue(InterpretadorClima.sugerirVisita(61, 25.0).contains("Lluvia"))
        assertTrue(InterpretadorClima.sugerirVisita(0, 18.0).contains("excelente"))
        assertTrue(InterpretadorClima.sugerirVisita(3, 11.9).contains("abrigo"))
        assertTrue(InterpretadorClima.sugerirVisita(3, 12.0).contains("favorables"))
    }
    private val dto = SolicitudCulturalDto(1, "Consulta", "Descripción cultural", "CURSO", "Museo", "Ana", "ana@test.cl")
    @Test fun crudRemotoDevuelveResultadosYArgumentosCorrectos() = runTest {
        val api = mockk<SolicitudMicroservicioApi>(); val repo = MicroservicioRepository(api)
        coEvery { api.listarSolicitudes() } returns listOf(dto); coEvery { api.obtenerSolicitudPorId(1) } returns dto
        coEvery { api.crearSolicitud(dto) } returns dto; coEvery { api.actualizarSolicitud(1, dto) } returns dto
        coEvery { api.eliminarSolicitud(1) } returns Response.success(Unit)
        repo.listarSolicitudes().getOrThrow() shouldBe listOf(dto); repo.obtenerSolicitud(1).getOrThrow() shouldBe dto
        repo.crearSolicitud(dto).getOrThrow() shouldBe dto; repo.actualizarSolicitud(1, dto).getOrThrow() shouldBe dto
        assertTrue(repo.eliminarSolicitud(1).getOrThrow())
        coVerify(exactly = 1) { api.actualizarSolicitud(1, dto) }; coVerify(exactly = 1) { api.eliminarSolicitud(1) }
    }
    @Test fun crudRemotoConservaErroresDeCadaOperacion() = runTest {
        val api = mockk<SolicitudMicroservicioApi>(); val repo = MicroservicioRepository(api); val error = java.io.IOException("Servidor caído")
        coEvery { api.listarSolicitudes() } throws error; coEvery { api.obtenerSolicitudPorId(any()) } throws error
        coEvery { api.crearSolicitud(any()) } throws error; coEvery { api.actualizarSolicitud(any(), any()) } throws error; coEvery { api.eliminarSolicitud(any()) } throws error
        listOf(repo.listarSolicitudes(), repo.obtenerSolicitud(1), repo.crearSolicitud(dto), repo.actualizarSolicitud(1, dto), repo.eliminarSolicitud(1)).forEach { it.exceptionOrNull() shouldBe error }
        coEvery { api.eliminarSolicitud(1) } returns Response.error(404, "No existe".toResponseBody())
        repo.eliminarSolicitud(1).exceptionOrNull()!!.message shouldBe "HTTP 404"
    }
}
