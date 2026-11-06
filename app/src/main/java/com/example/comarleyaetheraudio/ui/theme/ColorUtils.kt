package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp

/** Relación de contraste WCAG entre dos colores (1..21). */
fun Color.contrastRatio(other: Color): Float {
    val a = this.luminance() + 0.05f
    val b = other.luminance() + 0.05f
    return if (a > b) a / b else b / a
}

/** Color de contenido (texto/ícono) legible sobre este color. */
fun Color.onColor(): Color =
    if (this.contrastRatio(Color(0xFF111111)) >= this.contrastRatio(Color.White)) Color(0xFF111111) else Color.White

/**
 * Ajusta la luminancia de este color hasta alcanzar [minRatio] contra [background].
 * Mantiene el matiz: se mezcla progresivamente hacia blanco (fondos oscuros) o negro (fondos claros).
 * Contrato MASTER.md §3: todo color dinámico (Palette) pasa por aquí antes de usarse.
 */
fun Color.ensureContrast(background: Color, minRatio: Float = 4.5f): Color {
    val opaqueBg = background.copy(alpha = 1f)
    val base = this.copy(alpha = 1f).compositeOver(opaqueBg)
    if (base.contrastRatio(opaqueBg) >= minRatio) return base
    val target = if (opaqueBg.luminance() < 0.5f) Color.White else Color.Black
    var fraction = 0.1f
    while (fraction <= 1f) {
        val candidate = lerp(base, target, fraction)
        if (candidate.contrastRatio(opaqueBg) >= minRatio) return candidate
        fraction += 0.1f
    }
    return target
}
