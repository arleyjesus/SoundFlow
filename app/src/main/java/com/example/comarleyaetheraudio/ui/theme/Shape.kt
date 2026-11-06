package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Escala de formas (MASTER.md §5). Úsese `MaterialTheme.shapes.*`, nunca `RoundedCornerShape(n.dp)` suelto. */
val SoundFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // badges
    small = RoundedCornerShape(8.dp),        // miniaturas en lista
    medium = RoundedCornerShape(12.dp),      // cards
    large = RoundedCornerShape(16.dp),       // mini player, sheets
    extraLarge = RoundedCornerShape(24.dp)   // carátula grande
)
