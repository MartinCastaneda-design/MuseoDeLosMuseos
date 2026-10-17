package com.example.museodelosmuseos.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.model.Rol
import com.example.museodelosmuseos.ui.screens.registro.CampoFormulario
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.ErrorRojo
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import com.example.museodelosmuseos.ui.theme.VerdeExito

/**
 * ============================================================================
 * PANTALLA 3: SELECCIÓN / INICIO DE SESIÓN
 * ============================================================================
 * Roles: Administrador, Investigador y Cliente.
 *
 * Cumple con los criterios de la pauta:
 * - Permite seleccionar el rol e iniciar sesión con credenciales académicas de prueba.
 * - Identifica al usuario activo y habilita sus funciones.
 * - Al cambiar de perfil, actualiza la sesión en Room sin mezclar información.
 * - Validación desacoplada de la UI mediante ViewModel y StateFlow.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(
    viewModel: LoginViewModel,
    onVolver: () -> Unit,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarContrasena by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DoradoPatrimonial
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = DoradoPatrimonial
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMuseoProfundo
                )
            )
        }
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .background(
                    Brush.verticalGradient(
                        listOf(AzulMuseoProfundo, FondoOscuro)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .widthIn(max = 500.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- ENCABEZADO ----
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(AzulMuseoMedio)
                        .border(1.5.dp, DoradoPatrimonial, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = DoradoBrillante,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Selecciona tu tipo de cuenta",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Toca un rol para autocompletar credenciales académicas de prueba o escribe tus datos personales:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )

                // ---- 1. TARJETAS DE SELECCIÓN DE ROL / DEMO (REQUISITO PAUTA) ----
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaSeleccionRol(
                        rol = Rol.ADMINISTRADOR,
                        titulo = "Administrador",
                        descripcion = "Gestión completa del sistema, contenidos y avisos",
                        icono = Icons.Filled.AdminPanelSettings,
                        seleccionado = estado.rolSeleccionado == Rol.ADMINISTRADOR,
                        alSeleccionar = { viewModel.seleccionarRolDemo(Rol.ADMINISTRADOR) }
                    )

                    TarjetaSeleccionRol(
                        rol = Rol.INVESTIGADOR,
                        titulo = "Investigador",
                        descripcion = "Acceso a fichas museológicas, cámara y lector QR",
                        icono = Icons.Filled.School,
                        seleccionado = estado.rolSeleccionado == Rol.INVESTIGADOR,
                        alSeleccionar = { viewModel.seleccionarRolDemo(Rol.INVESTIGADOR) }
                    )

                    TarjetaSeleccionRol(
                        rol = Rol.CLIENTE,
                        titulo = "Cliente",
                        descripcion = "Explora museos virtuales, guarda favoritos y solicita compras",
                        icono = Icons.Filled.Person,
                        seleccionado = estado.rolSeleccionado == Rol.CLIENTE,
                        alSeleccionar = { viewModel.seleccionarRolDemo(Rol.CLIENTE) }
                    )
                }

                // ---- MENSAJE DE ERROR ----
                if (estado.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ErrorRojo.copy(alpha = 0.2f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(ErrorRojo, ErrorRojo))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Error, contentDescription = null, tint = ErrorRojo)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = estado.error ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ---- 2. CAMPOS DE CREDENCIALES ----
                CampoLoginConIcono(
                    etiqueta = "Correo electrónico",
                    campo = estado.correo,
                    iconoPrincipal = Icons.Filled.Email,
                    tipoTeclado = KeyboardType.Email,
                    onValorCambia = viewModel::cambiarCorreo,
                    mostrarError = estado.intentoEnviar || estado.correo.error != null
                )

                OutlinedTextField(
                    value = estado.contrasena.valor,
                    onValueChange = viewModel::cambiarContrasena,
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (estado.contrasena.error != null && estado.intentoEnviar) ErrorRojo else DoradoPatrimonial
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(
                                imageVector = if (mostrarContrasena) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (mostrarContrasena) "Ocultar" else "Mostrar",
                                tint = TextoSecundario
                            )
                        }
                    },
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = estado.contrasena.error != null && estado.intentoEnviar,
                    supportingText = if (estado.contrasena.error != null && estado.intentoEnviar) {
                        { Text(text = estado.contrasena.error ?: "", color = ErrorRojo) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoradoPatrimonial,
                        unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.5f),
                        errorBorderColor = ErrorRojo,
                        focusedTextColor = TextoPrimario,
                        unfocusedTextColor = TextoPrimario,
                        focusedContainerColor = AzulCard,
                        unfocusedContainerColor = AzulCard
                    )
                )

                // ---- BOTÓN DE INICIO DE SESIÓN ----
                Button(
                    onClick = {
                        viewModel.iniciarSesion(onExito = onLoginExitoso)
                    },
                    enabled = !estado.cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoradoPatrimonial,
                        contentColor = FondoOscuro
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    if (estado.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = FondoOscuro,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // ---- ENLACE A REGISTRO ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes una cuenta aún?",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onIrARegistro) {
                        Text(
                            text = "Regístrate aquí",
                            color = DoradoBrillante,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Nota: Las cuentas de Administrador e Investigador están provistas con fines académicos.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoApagado,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta interactiva para seleccionar el rol de usuario o cargar su perfil de prueba.
 */
@Composable
private fun TarjetaSeleccionRol(
    rol: Rol,
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    seleccionado: Boolean,
    alSeleccionar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alSeleccionar),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) AzulMuseoMedio else AzulCard
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (seleccionado) {
                    listOf(DoradoBrillante, DoradoPatrimonial)
                } else {
                    listOf(DoradoPatrimonial.copy(alpha = 0.2f), Color.Transparent)
                }
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (seleccionado) DoradoPatrimonial else AzulMuseoProfundo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (seleccionado) FondoOscuro else DoradoPatrimonial,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (seleccionado) DoradoBrillante else TextoPrimario
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• DEMO",
                        style = MaterialTheme.typography.labelSmall,
                        color = DoradoPatrimonial,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = if (seleccionado) DoradoBrillante else TextoApagado,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CampoLoginConIcono(
    etiqueta: String,
    campo: CampoFormulario,
    iconoPrincipal: ImageVector,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    onValorCambia: (String) -> Unit,
    mostrarError: Boolean
) {
    val tieneError = mostrarError && campo.error != null
    val esValido = campo.esValido && campo.valor.isNotBlank()

    OutlinedTextField(
        value = campo.valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                imageVector = iconoPrincipal,
                contentDescription = null,
                tint = when {
                    tieneError -> ErrorRojo
                    esValido -> VerdeExito
                    else -> DoradoPatrimonial
                }
            )
        },
        trailingIcon = {
            when {
                tieneError -> Icon(Icons.Filled.Error, contentDescription = "Error", tint = ErrorRojo)
                esValido -> Icon(Icons.Filled.CheckCircle, contentDescription = "Válido", tint = VerdeExito)
            }
        },
        isError = tieneError,
        supportingText = if (tieneError) {
            { Text(text = campo.error ?: "", color = ErrorRojo) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DoradoPatrimonial,
            unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.5f),
            errorBorderColor = ErrorRojo,
            focusedTextColor = TextoPrimario,
            unfocusedTextColor = TextoPrimario,
            focusedContainerColor = AzulCard,
            unfocusedContainerColor = AzulCard
        )
    )
}
