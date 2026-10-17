package com.example.museodelosmuseos.ui.screens.registro

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
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
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.ErrorRojo
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import com.example.museodelosmuseos.ui.theme.VerdeExito

/**
 * ============================================================================
 * PANTALLA 2: REGISTRO DE USUARIO (ROL CLIENTE)
 * ============================================================================
 * Cumple con los criterios de la pauta:
 * - Formulario para registrar nombre, correo, contraseña y selección de avatar.
 * - Validación desacoplada mediante ViewModel y StateFlow (IE 2.2.1).
 * - Retroalimentación visual clara con íconos por campo y mensajes de error (IE 2.1.2).
 * - Almacenamiento local del perfil en Room / SQLite (IE 2.3.1).
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    // 1. collectAsStateWithLifecycle(): Suscripción consciente del ciclo de vida (estándar de clase)
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarContrasena by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Registro de Usuario",
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
                // ---- ENCABEZADO Y EMBLEMA ----
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AzulMuseoMedio)
                        .border(1.5.dp, DoradoPatrimonial, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = DoradoBrillante,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "Crear tu cuenta",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario
                )
                Text(
                    text = "Únete a la comunidad de Museo de los Museos y explora el patrimonio iberoamericano",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    textAlign = TextAlign.Center
                )

                // Mensaje de error general si el correo ya existe en Room
                if (estado.errorServidor != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ErrorRojo.copy(alpha = 0.2f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ErrorRojo, ErrorRojo)))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = ErrorRojo
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = estado.errorServidor ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ---- CAMPO 1: NOMBRE COMPLETO ----
                CampoTextoConIcono(
                    etiqueta = "Nombre completo",
                    campo = estado.nombre,
                    iconoPrincipal = Icons.Filled.Person,
                    onValorCambia = viewModel::cambiarNombre,
                    mostrarError = estado.intentoEnviar || estado.nombre.error != null
                )

                // ---- CAMPO 2: CORREO ELECTRÓNICO ----
                CampoTextoConIcono(
                    etiqueta = "Correo electrónico",
                    campo = estado.correo,
                    iconoPrincipal = Icons.Filled.Email,
                    tipoTeclado = KeyboardType.Email,
                    onValorCambia = viewModel::cambiarCorreo,
                    mostrarError = estado.intentoEnviar || estado.correo.error != null
                )

                // ---- CAMPO 3: CONTRASEÑA ----
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

                // ---- SELECCIÓN DE AVATAR FICTICIO ----
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selecciona tu avatar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DoradoBrillante
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ItemAvatar(
                            id = "avatar_1",
                            nombre = "Investigador",
                            icono = Icons.Filled.School,
                            seleccionado = estado.avatarSeleccionado == "avatar_1",
                            onSeleccionar = { viewModel.seleccionarAvatar("avatar_1") }
                        )
                        ItemAvatar(
                            id = "avatar_2",
                            nombre = "Curador",
                            icono = Icons.Filled.Palette,
                            seleccionado = estado.avatarSeleccionado == "avatar_2",
                            onSeleccionar = { viewModel.seleccionarAvatar("avatar_2") }
                        )
                        ItemAvatar(
                            id = "avatar_3",
                            nombre = "Lector",
                            icono = Icons.Filled.Face,
                            seleccionado = estado.avatarSeleccionado == "avatar_3",
                            onSeleccionar = { viewModel.seleccionarAvatar("avatar_3") }
                        )
                        ItemAvatar(
                            id = "avatar_4",
                            nombre = "Visitante",
                            icono = Icons.Filled.Person,
                            seleccionado = estado.avatarSeleccionado == "avatar_4",
                            onSeleccionar = { viewModel.seleccionarAvatar("avatar_4") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ---- BOTÓN DE REGISTRO ----
                Button(
                    onClick = {
                        viewModel.registrar(onExito = onRegistroExitoso)
                    },
                    enabled = !estado.guardando,
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
                    if (estado.guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = FondoOscuro,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Registrarme",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // ---- ENLACE A INICIAR SESIÓN ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes cuenta?",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onIrALogin) {
                        Text(
                            text = "Inicia sesión",
                            color = DoradoBrillante,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente modular para un campo de texto con ícono principal,
 * retroalimentación visual de error o éxito e integración al UDF.
 */
@Composable
private fun CampoTextoConIcono(
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

/**
 * Elemento de avatar circular seleccionable.
 */
@Composable
private fun ItemAvatar(
    id: String,
    nombre: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onSeleccionar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onSeleccionar)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (seleccionado) DoradoPatrimonial else AzulCard)
                .border(
                    width = if (seleccionado) 2.5.dp else 1.dp,
                    color = if (seleccionado) DoradoBrillante else DoradoPatrimonial.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = nombre,
                tint = if (seleccionado) FondoOscuro else DoradoPatrimonial,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelSmall,
            color = if (seleccionado) DoradoBrillante else TextoSecundario,
            fontSize = 11.sp
        )
    }
}
