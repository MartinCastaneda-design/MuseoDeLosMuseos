package com.example.museodelosmuseos.data.model.clima

data class InfoClima(
    val temperatura: Double,
    val condicion: String,
    val icono: String,
    val humedad: Int,
    val vientoKmH: Double,
    val recomendacionVisita: String
)

object InterpretadorClima {

    fun interpretar(codigo: Int): Pair<String, String> {
        return when (codigo) {
            0 -> "Cielo despejado" to "☀️"
            1 -> "Mayormente despejado" to "🌤️"
            2 -> "Parcialmente nublado" to "⛅"
            3 -> "Nublado" to "☁️"
            45, 48 -> "Niebla" to "🌫️"
            51, 53, 55 -> "Llovizna" to "🌦️"
            61, 63, 65 -> "Lluvia" to "🌧️"
            71, 73, 75 -> "Nieve" to "❄️"
            80, 81, 82 -> "Chubascos" to "🌧️"
            95, 96, 99 -> "Tormenta eléctrica" to "⛈️"
            else -> "Condición templada" to "🌤️"
        }
    }

    fun sugerirVisita(codigo: Int, temperatura: Double): String {
        return when {
            codigo in listOf(61, 63, 65, 80, 81, 82, 95, 96, 99) ->
                "Lluvia en la ciudad. ¡Momento ideal para explorar las salas virtuales en 360° desde la app!"
            temperatura >= 18.0 && codigo in listOf(0, 1, 2) ->
                "Clima excelente para visitar el museo presencialmente y disfrutar sus jardines."
            temperatura < 12.0 ->
                "Día fresco en la ciudad. Recuerda llevar abrigo para tu recorrido presencial."
            else ->
                "Condiciones favorables para planificar tu visita cultural."
        }
    }
}
