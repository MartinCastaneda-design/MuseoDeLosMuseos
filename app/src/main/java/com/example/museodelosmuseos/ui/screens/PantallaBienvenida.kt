package com.example.museodelosmuseos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.MuseoDeLosMuseosTheme
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario

/**
 * ============================================================================
 * PANTALLA 1: BIENVENIDA / INICIO
 * ============================================================================
 * Roles: Administrador, Investigador y Cliente.
 *
 * Características:
 * - Identidad cultural "Museo de los Museos".
 * - Breve reseña de servicios educativos y patrimoniales de Iberoamérica.
 * - Animación inicial fluida (entrada escalonada + pulso sutil del emblema).
 * - Acceso claro a mapa de museos virtuales, catálogo y cursos.
 * - Botón principal "Comenzar" hacia selección de rol o registro.
 * - Botón secundario "Ya tengo cuenta" hacia inicio de sesión.
 * ============================================================================
 */
@Composable
fun PantallaBienvenida(
    onComenzarClick: () -> Unit,
    onIniciarSesionClick: () -> Unit,
    onExplorarMapaClick: () -> Unit
) {
    // Control de animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // Animación continua de pulso sutil para el ícono de templo/museo (IE 2.2.2)
    val transicionInfinita = rememberInfiniteTransition(label = "pulso_emblema")
    val escalaEmblema by transicionInfinita.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Scaffold(
        containerColor = FondoOscuro
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AzulMuseoProfundo,
                            FondoOscuro,
                            Color(0xFF070E12)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // ---- 1. EMBLEMA INSTITUCIONAL CON ANIMACIÓN ----
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(escalaEmblema)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AzulMuseoMedio, AzulMuseoProfundo)
                            )
                        )
                        .border(2.dp, DoradoPatrimonial, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = "Emblema del Museo",
                        tint = DoradoBrillante,
                        modifier = Modifier.size(46.dp)
                    )
                }

                // ---- 2. TEXTOS DE BIENVENIDA CON ANIMACIÓN DE ENTRADA ----
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800)) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(800)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Museo de los Museos",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = DoradoPatrimonial,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Conecta, aprende y explora el patrimonio cultural de Iberoamérica",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoPrimario,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Text(
                            text = "Plataforma móvil integral para el fomento de la educación museológica, la investigación aplicada y la difusión de colecciones virtuales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // ---- 3. TARJETA DESTACADA (SERVICIOS CULTURALES) ----
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AzulCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(DoradoPatrimonial.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Servicios de la Plataforma",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DoradoBrillante
                        )

                        ItemServicioCultural(
                            icono = Icons.Filled.Map,
                            titulo = "Mapa de Museos Virtuales",
                            descripcion = "Explora pinacotecas, arte colonial y monumentos con visitas digitales interactivas."
                        )

                        ItemServicioCultural(
                            icono = Icons.Filled.School,
                            titulo = "Cursos Profesionales",
                            descripcion = "Capacitación especializada en curaduría, museología y conservación patrimonial."
                        )

                        ItemServicioCultural(
                            icono = Icons.Filled.AutoStories,
                            titulo = "Catálogo y Publicaciones",
                            descripcion = "Adquiere libros especializados, guías y material técnico para investigadores."
                        )
                    }
                }

                // ---- 4. BOTONES DE ACCIÓN PRINCIPAL ----
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onComenzarClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoradoPatrimonial,
                            contentColor = FondoOscuro
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Comenzar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onIniciarSesionClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DoradoPatrimonial
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.horizontalGradient(
                                listOf(DoradoPatrimonial, DoradoBrillante)
                            )
                        )
                    ) {
                        Text(
                            text = "Ya tengo cuenta / Iniciar sesión",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    OutlinedButton(
                        onClick = onExplorarMapaClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextoSecundario
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = DoradoPatrimonial
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explorar Mapa de Museos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // ---- 5. PIE INSTITUCIONAL ----
                Text(
                    text = "Red de Museos Virtuales de Iberoamérica • 2025",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoApagado,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

/**
 * Fila reutilizable para resumir cada servicio cultural en la pantalla de bienvenida.
 */
@Composable
private fun ItemServicioCultural(
    icono: ImageVector,
    titulo: String,
    descripcion: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AzulMuseoMedio),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = DoradoPatrimonial,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextoPrimario
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaBienvenidaPreview() {
    MuseoDeLosMuseosTheme {
        PantallaBienvenida(
            onComenzarClick = {},
            onIniciarSesionClick = {},
            onExplorarMapaClick = {}
        )
    }
}
