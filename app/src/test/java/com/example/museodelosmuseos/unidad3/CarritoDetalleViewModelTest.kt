package com.example.museodelosmuseos.unidad3
import com.example.museodelosmuseos.ui.screens.carrito.*
import com.example.museodelosmuseos.ui.screens.detallearticulo.*
import com.example.museodelosmuseos.data.model.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarritoDetalleViewModelTest : BaseViewModelTest() {
    @Test fun carritoValidaCadaPasoAntesDeConfirmar() = runTest(dispatcher) {
        val vm = conservar(CarritoViewModel(repo, 3)); runCurrent(); assertEquals(2000L, vm.uiState.value.subtotal)
        vm.revisar(); assertTrue(vm.uiState.value.error!!.contains("destino")); vm.destino(DestinoEnvio.NACIONAL)
        vm.revisar(); assertTrue(vm.uiState.value.error!!.contains("modalidad")); vm.modalidad(ModalidadEnvio.ESTANDAR)
        vm.revisar(); assertNotNull(vm.uiState.value.errorUbicacion); vm.ubicacion("Santiago"); vm.revisar(); assertNotNull(vm.uiState.value.errorDireccion)
        vm.direccion("Calle Museo 123"); vm.revisar(); assertEquals(6500L, vm.uiState.value.revision!!.cotizacion.total)
        assertEquals(6500L, vm.uiState.value.cotizacion!!.total); vm.cancelarRevision(); assertNull(vm.uiState.value.revision)
        vm.modalidad(ModalidadEnvio.EXPRESS); vm.revisar(); assertEquals(8750L, vm.uiState.value.revision!!.cotizacion.total)
        coVerify(exactly = 0) { repo.confirmarCarrito(any(), any(), any(), any(), any(), any()) }
    }
    @Test fun carritoConfirmaConResumenYEvitaDobleEnvio() = runTest(dispatcher) {
        coEvery { repo.confirmarCarrito(any(), any(), any(), any(), any(), any()) } returns "pedido123"
        val vm = conservar(CarritoViewModel(repo, 3)); runCurrent(); vm.destino(DestinoEnvio.NACIONAL); vm.modalidad(ModalidadEnvio.ESTANDAR); vm.ubicacion("Santiago"); vm.direccion("Calle Museo 123"); vm.revisar()
        vm.confirmar(); vm.confirmar(); vm.destino(DestinoEnvio.INTERNACIONAL); runCurrent()
        coVerify(exactly = 1) { repo.confirmarCarrito(3, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR, "Santiago", "Calle Museo 123", match { it.total == 6500L }) }
        assertNull(vm.uiState.value.revision); assertFalse(vm.uiState.value.guardando); assertTrue(vm.uiState.value.confirmacion!!.contains("6500"))
    }
    @Test fun carritoMuestraErroresDeStockYReiniciaAlCambiarUsuario() = runTest(dispatcher) {
        coEvery { repo.cambiarCantidadCarrito(any(), any(), any()) } throws IllegalStateException("Stock insuficiente")
        val vm = conservar(CarritoViewModel(repo, 3)); runCurrent(); vm.cantidad(2, 9); runCurrent(); assertEquals("Stock insuficiente", vm.uiState.value.error)
        vm.eliminar(2); runCurrent(); coVerify { repo.eliminarDelCarrito(3, 2) }
        lineas.value = emptyList(); runCurrent(); vm.revisar(); assertTrue(vm.uiState.value.error!!.contains("Agrega"))
        sesion.value = cliente.copy(usuarioId = 8); runCurrent(); assertTrue(vm.uiState.value.lineas.isEmpty()); assertNotNull(vm.uiState.value.error)
    }
    @Test fun carritoCapturaErrorDeFlujoYProductoSinStock() = runTest(dispatcher) {
        every { repo.observarLineasCarrito(3) } returns flow { throw IllegalStateException("Room") }
        val vm = conservar(CarritoViewModel(repo, 3)); runCurrent(); assertFalse(vm.uiState.value.cargando); assertNotNull(vm.uiState.value.error)
        assertNull(CarritoUiState(lineas = listOf(LineaCarrito(articulo.copy(stock = 0), 1)), destino = DestinoEnvio.NACIONAL, modalidad = ModalidadEnvio.ESTANDAR).cotizacion)
        assertEquals(0L, CarritoUiState(lineas = listOf(LineaCarrito(articulo.copy(precio = Double.NaN), 1))).subtotal)
    }
    @Test fun detalleAgregaSoloCantidadValidaYRecuperaError() = runTest(dispatcher) {
        val vm = conservar(DetalleArticuloViewModel(repo, 2, 3)); runCurrent(); assertEquals(articulo, vm.uiState.value.articulo)
        vm.cambiarCantidad("6"); vm.enviar(); assertNotNull(vm.uiState.value.error); coVerify(exactly = 0) { repo.agregarAlCarrito(any(), any(), any()) }
        vm.cambiarCantidad("2"); vm.enviar(); vm.enviar(); runCurrent(); coVerify(exactly = 1) { repo.agregarAlCarrito(3, 2, 2) }
        assertFalse(vm.uiState.value.guardando); assertEquals("1", vm.uiState.value.cantidad); assertNotNull(vm.uiState.value.confirmacion)
        coEvery { repo.agregarAlCarrito(any(), any(), any()) } throws IllegalStateException("Cambió stock")
        vm.enviar(); runCurrent(); assertEquals("Cambió stock", vm.uiState.value.error)
    }
    @Test fun detalleCursoValidaConsultaYPersistenciaDuplicada() = runTest(dispatcher) {
        every { repo.observarArticuloCatalogo(2) } returns flowOf(articulo.copy(categoria = "CURSO"))
        val vm = conservar(DetalleArticuloViewModel(repo, 2, 3)); runCurrent(); assertTrue(vm.uiState.value.esCurso)
        vm.cambiarConsulta("corta"); vm.enviar(); assertNotNull(vm.uiState.value.error)
        vm.cambiarConsulta("Necesito conocer los horarios del curso"); vm.enviar(); runCurrent()
        coVerify { repo.solicitarInformacion(3, 2, "Necesito conocer los horarios del curso") }; assertNotNull(vm.uiState.value.confirmacion)
        sesion.value = null; runCurrent(); assertNull(vm.uiState.value.articulo); assertNotNull(vm.uiState.value.error)
    }
    @Test fun detalleMuestraFalloDeDatosSinQuedarCargando() = runTest(dispatcher) {
        every { repo.observarArticuloCatalogo(2) } returns flow { throw IllegalStateException("Room") }
        val vm = conservar(DetalleArticuloViewModel(repo, 2, 3)); runCurrent(); assertFalse(vm.uiState.value.cargando); assertNotNull(vm.uiState.value.error)
    }
}
