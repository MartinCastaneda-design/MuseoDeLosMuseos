package com.example.museodelosmuseos.unidad3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before

val cliente = SesionActiva(usuarioId = 3, nombre = "María", correo = "maria@test.cl", rol = "CLIENTE", avatar = "avatar_1")
val administrador = cliente.copy(rol = "ADMINISTRADOR")
val usuario = Usuario(3, "María", "maria@test.cl", "clave123", "CLIENTE")
val museo = Museo(1, "Museo de Arte", "Chile", "Santiago", "Colección de arte", "Arte", -33.4, -70.6, "https://museo.cl/visita", "MUSEO_1")
val articulo = ArticuloCatalogo(2, "Libro de arte", "Publicación sobre patrimonio", "PUBLICACION", 1000.0, stock = 5)

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseViewModelTest {
    val dispatcher = StandardTestDispatcher()
    lateinit var repo: MuseoRepository
    val sesion = MutableStateFlow<SesionActiva?>(cliente)
    val museos = MutableStateFlow(listOf(museo))
    val articulos = MutableStateFlow(listOf(articulo))
    val lineas = MutableStateFlow(listOf(LineaCarrito(articulo, 2)))
    private val store = ViewModelStore()
    fun <T : ViewModel> conservar(vm: T): T { store.put("vm-${System.identityHashCode(vm)}", vm); return vm }
    @Before fun preparar() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        every { repo.observarSesion() } returns sesion
        every { repo.observarMuseos() } returns museos
        every { repo.observarCatalogo(any()) } returns articulos
        every { repo.observarMuseo(any()) } returns flowOf(museo)
        every { repo.observarArticuloCatalogo(any()) } returns flowOf(articulo)
        every { repo.observarFavoritos(any()) } returns museos
        every { repo.esFavorito(any(), any()) } returns flowOf(false)
        every { repo.observarLineasCarrito(any()) } returns lineas
        every { repo.observarCarrito(any()) } returns flowOf(emptyList())
        every { repo.observarSolicitudesInformacion(any()) } returns flowOf(emptyList())
        every { repo.observarHistorialSolicitudes(any()) } returns flowOf(emptyList())
        every { repo.observarHistorialQr(any()) } returns flowOf(emptyList())
        every { repo.observarAvisos(any()) } returns flowOf(emptyList())
        every { repo.observarPreferencias(any()) } returns flowOf(PreferenciasUsuario(3))
        every { repo.observarPerfilActivo(any()) } returns flowOf(usuario)
        coEvery { repo.obtenerSesion() } answers { sesion.value }
        coEvery { repo.correoPerfilDemo(any()) } returns null
        coEvery { repo.sincronizarMuseosRemotos() } returns Result.success(1)
    }
    @After fun terminar() { store.clear(); Dispatchers.resetMain(); unmockkAll() }
}
