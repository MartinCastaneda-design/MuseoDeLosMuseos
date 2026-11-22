package com.example.museodelosmuseos.unidad3
import com.example.museodelosmuseos.ui.screens.catalogo.*
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModelTest : BaseViewModelTest() {
    @Test fun consultaDependeDelRolYCambiaConLaSesion() = runTest(dispatcher) {
        val vm = conservar(CatalogoViewModel(repo)); runCurrent(); assertEquals(listOf(articulo), vm.uiState.value.articulos)
        verify { repo.observarCatalogo(true) }; vm.iniciarCreacion(); assertFalse(vm.uiState.value.mostrarDialogoFormulario)
        vm.alternarMostrarSoloActivos(); assertTrue(vm.uiState.value.mostrarSoloActivos)
        vm.guardarFormulario(); assertNotNull(vm.uiState.value.mensajeError)
        sesion.value = administrador; runCurrent(); verify { repo.observarCatalogo(false) }
        vm.alternarMostrarSoloActivos(); assertFalse(vm.uiState.value.mostrarSoloActivos)
        sesion.value = cliente.copy(rol = "INVESTIGADOR"); runCurrent(); assertTrue(vm.uiState.value.articulos.isEmpty())
        sesion.value = null; runCurrent(); assertNull(vm.uiState.value.sesion)
    }
    @Test fun busquedaCategoriaYSeleccionFiltranSinCambiarDatos() = runTest(dispatcher) {
        val vm = conservar(CatalogoViewModel(repo)); runCurrent(); vm.actualizarBusqueda("Libro"); vm.seleccionarCategoria(CategoriaArticulo.PUBLICACION)
        assertEquals(listOf(articulo), vm.uiState.value.articulosFiltrados)
        vm.actualizarBusqueda("no existe"); assertTrue(vm.uiState.value.articulosFiltrados.isEmpty())
        vm.seleccionarArticulo(2); assertEquals(2L, vm.uiState.value.articuloSeleccionadoId); vm.limpiarSeleccionArticulo(); assertNull(vm.uiState.value.articuloSeleccionadoId)
    }
    private fun completar(vm: CatalogoViewModel) {
        vm.cambiarTituloFormulario(" Libro nuevo "); vm.cambiarDescripcionFormulario(" Descripción patrimonial ")
        vm.cambiarCategoriaFormulario(CategoriaArticulo.PUBLICACION); vm.cambiarPrecioFormulario("1500,50"); vm.cambiarStockFormulario("4")
        vm.cambiarImagenUrlFormulario(" https://museo.cl/libro.jpg "); vm.cambiarDisponibleFormulario(true); vm.cambiarActivoFormulario(true)
    }
    @Test fun creaArticuloConPrecioDecimalYCamposNormalizados() = runTest(dispatcher) {
        sesion.value = administrador; coEvery { repo.guardarArticuloCatalogo(any()) } returns 20
        val vm = conservar(CatalogoViewModel(repo)); runCurrent(); vm.iniciarCreacion(); completar(vm); vm.guardarFormulario()
        assertTrue(vm.uiState.value.guardando); runCurrent(); assertFalse(vm.uiState.value.guardando); assertFalse(vm.uiState.value.mostrarDialogoFormulario)
        coVerify { repo.guardarArticuloCatalogo(match { it.titulo == "Libro nuevo" && it.precio == 1500.50 && it.stock == 4 && it.imagenUrl == "https://museo.cl/libro.jpg" }) }
        assertTrue(vm.uiState.value.mensajeConfirmacion!!.contains("20")); vm.limpiarMensajeConfirmacion(); assertNull(vm.uiState.value.mensajeConfirmacion)
    }
    @Test fun editaArticuloExistenteSinInsertarOtro() = runTest(dispatcher) {
        sesion.value = administrador; val vm = conservar(CatalogoViewModel(repo)); runCurrent()
        vm.iniciarEdicion(articulo); assertEquals("1000", vm.uiState.value.formulario.precio.valor); vm.cambiarPrecioFormulario("1200.25"); vm.cambiarImagenUrlFormulario(""); vm.guardarFormulario(); runCurrent()
        coVerify { repo.actualizarArticuloCatalogo(match { it.id == 2L && it.precio == 1200.25 && it.imagenUrl.isNotBlank() }) }
        coVerify(exactly = 0) { repo.guardarArticuloCatalogo(any()) }
        vm.iniciarEdicion(articulo.copy(precio = 12.5, categoria = "OTRA")); assertEquals("12.5", vm.uiState.value.formulario.precio.valor)
        assertEquals(CategoriaArticulo.CURSO, vm.uiState.value.formulario.categoria); vm.cancelarFormulario(); assertFalse(vm.uiState.value.mostrarDialogoFormulario)
    }
    @Test fun validaCamposYRechazaCaracteresNoNumericos() = runTest(dispatcher) {
        sesion.value = administrador; val vm = conservar(CatalogoViewModel(repo)); runCurrent(); vm.iniciarCreacion(); vm.guardarFormulario()
        assertTrue(vm.uiState.value.formulario.intentoEnviar); assertNotNull(vm.uiState.value.formulario.titulo.error)
        vm.cambiarTituloFormulario("ab"); vm.cambiarDescripcionFormulario("corta"); vm.cambiarPrecioFormulario("0"); vm.cambiarStockFormulario("")
        assertNotNull(vm.uiState.value.formulario.descripcion.error); assertNotNull(vm.uiState.value.formulario.precio.error); assertNotNull(vm.uiState.value.formulario.stock.error)
        vm.cambiarPrecioFormulario("abc"); assertEquals("0", vm.uiState.value.formulario.precio.valor)
        vm.cambiarStockFormulario("4"); vm.cambiarStockFormulario("-1"); vm.cambiarStockFormulario("123456"); assertEquals("4", vm.uiState.value.formulario.stock.valor)
        coVerify(exactly = 0) { repo.guardarArticuloCatalogo(any()) }
    }
    @Test fun operacionesAdministrativasInformanExitoYErrores() = runTest(dispatcher) {
        sesion.value = administrador; val vm = conservar(CatalogoViewModel(repo)); runCurrent()
        vm.desactivarArticulo(2); runCurrent(); coVerify { repo.desactivarArticuloCatalogo(2) }; assertNotNull(vm.uiState.value.mensajeConfirmacion)
        vm.reactivarArticulo(2); vm.cambiarDisponibilidad(2, false); runCurrent(); coVerify { repo.reactivarArticuloCatalogo(2) }; coVerify { repo.cambiarDisponibilidadArticulo(2, false) }
        coEvery { repo.desactivarArticuloCatalogo(any()) } throws IllegalStateException("sin disco")
        coEvery { repo.reactivarArticuloCatalogo(any()) } throws IllegalStateException("sin disco")
        coEvery { repo.cambiarDisponibilidadArticulo(any(), any()) } throws IllegalStateException("sin disco")
        vm.desactivarArticulo(2); runCurrent(); assertTrue(vm.uiState.value.mensajeError!!.contains("sin disco"))
        vm.reactivarArticulo(2); runCurrent(); assertTrue(vm.uiState.value.mensajeError!!.contains("reactivar"))
        vm.cambiarDisponibilidad(2, true); runCurrent(); assertTrue(vm.uiState.value.mensajeError!!.contains("disponibilidad")); vm.limpiarMensajeError(); assertNull(vm.uiState.value.mensajeError)
    }
    @Test fun errorDeConsultaYGuardadoNoDejanCargaActiva() = runTest(dispatcher) {
        sesion.value = administrador; every { repo.observarCatalogo(any()) } returns flow { throw IllegalStateException("consulta") }
        val vm = conservar(CatalogoViewModel(repo)); runCurrent(); assertFalse(vm.uiState.value.cargando); assertNotNull(vm.uiState.value.mensajeError)
        coEvery { repo.guardarArticuloCatalogo(any()) } throws IllegalStateException("disco lleno")
        vm.iniciarCreacion(); completar(vm); vm.guardarFormulario(); runCurrent(); assertFalse(vm.uiState.value.guardando); assertTrue(vm.uiState.value.mensajeError!!.contains("disco lleno"))
    }
}
