package com.example.museodelosmuseos.ui.components

import androidx.annotation.DrawableRes
import com.example.museodelosmuseos.R

data class FotoMuseoLocal(@param:DrawableRes val recurso: Int, val autor: String, val fuente: String, val licencia: String, val licenciaUrl: String)

/** Identidad estable del museo; no usa IDs Room ni coincidencias parciales de nombres. */
object FotosMuseos {
    fun paraCodigo(codigoQr: String): FotoMuseoLocal? = when (codigoQr.uppercase(java.util.Locale.ROOT)) {
        "MUSEO_COLONIAL_BOGOTA" -> FotoMuseoLocal(R.drawable.museo_colonial_bogota, "Javier Benítez", "https://commons.wikimedia.org/wiki/File:Fachada_del_Museo_de_Arte_Colonial.JPG", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0")
        "MUSEO_MALI_LIMA" -> FotoMuseoLocal(R.drawable.museo_mali_lima, "David León Vilca", "https://commons.wikimedia.org/wiki/File:Museo_de_arte_de_Lima.jpg", "CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0")
        "MUSEO_MAM_MEXICO" -> FotoMuseoLocal(R.drawable.museo_arte_moderno_mexico, "OliAzor", "https://commons.wikimedia.org/wiki/File:Museo_de_arte_Moderno.jpg", "CC BY 4.0", "https://creativecommons.org/licenses/by/4.0")
        "MUSEO_BELLAS_ARTES_CHILE" -> FotoMuseoLocal(R.drawable.museo_bellas_artes_santiago, "Jorge Barrios Riquelme", "https://commons.wikimedia.org/wiki/File:Museo_Nacional_de_Bellas_Artes_de_Chile.jpg", "CC BY-SA 3.0", "https://creativecommons.org/licenses/by-sa/3.0")
        "MUSEO_PRADO_MADRID" -> FotoMuseoLocal(R.drawable.museo_prado_madrid, "Emilio J. Rodríguez Posada; edición de color y perspectiva: Rexcanis (2020)", "https://commons.wikimedia.org/wiki/File:Museo_del_Prado_2016_(25185969599).jpg", "CC BY-SA 2.0", "https://creativecommons.org/licenses/by-sa/2.0")
        else -> null
    }
}
