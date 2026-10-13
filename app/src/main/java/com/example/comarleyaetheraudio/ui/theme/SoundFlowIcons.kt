package com.example.comarleyaetheraudio.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** SoundFlow line icons: shared geometry, cached vectors, no bitmap or network assets. */
object SoundFlowIcons {
    private fun glyph(name: String, filled: Boolean = false, draw: PathBuilder.() -> Unit) = ImageVector.Builder(
        name, 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = if (filled) SolidColor(Color.Black) else null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { draw() }
    }.build()
    val Home by lazy { glyph("FlowHome") {
        moveTo(3f, 10f); lineTo(12f, 3f); lineTo(21f, 10f)
        moveTo(5f, 9f); lineTo(5f, 19f); quadTo(5f,21f,7f,21f)
        lineTo(17f,21f); quadTo(19f,21f,19f,19f); lineTo(19f,9f)
        moveTo(9f,15f); lineTo(9f,17f); moveTo(12f,12f); lineTo(12f,18f); moveTo(15f,14f); lineTo(15f,17f)
    } }
    val Library by lazy { glyph("FlowLibrary") {
        moveTo(4f,5f); lineTo(4f,20f); moveTo(8f,3f); lineTo(8f,20f)
        moveTo(12f,6f); lineTo(18f,4f); lineTo(21f,18f); lineTo(15f,20f); close()
        moveTo(16f,9f); lineTo(17f,14f)
    } }
    val Search by lazy { glyph("FlowSearch") {
        moveTo(17f,10f); curveTo(17f,14f,14f,17f,10f,17f); curveTo(6f,17f,3f,14f,3f,10f)
        curveTo(3f,6f,6f,3f,10f,3f); curveTo(14f,3f,17f,6f,17f,10f); close()
        moveTo(15.5f,15.5f); lineTo(21f,21f)
        moveTo(7f,10f); lineTo(7f,12f); moveTo(10f,7f); lineTo(10f,13f); moveTo(13f,9f); lineTo(13f,11f)
    } }
    val Profile by lazy { glyph("FlowProfile") {
        moveTo(16f,7f); curveTo(16f,12f,8f,12f,8f,7f); curveTo(8f,2f,16f,2f,16f,7f); close()
        moveTo(4f,21f); lineTo(4f,19f); curveTo(4f,12f,20f,12f,20f,19f); lineTo(20f,21f)
        moveTo(9f,19f); lineTo(15f,19f)
    } }
    val Play by lazy { glyph("FlowPlay") {
        moveTo(8f,4f); quadTo(6f,3f,6f,6f); lineTo(6f,18f)
        quadTo(6f,21f,8f,20f); lineTo(19f,13f); quadTo(21f,12f,19f,11f); close()
    } }
    val Pause by lazy { glyph("FlowPause") {
        moveTo(7f,4f); lineTo(7f,20f); moveTo(10f,4f); lineTo(10f,20f)
        moveTo(15f,4f); lineTo(15f,20f); moveTo(18f,4f); lineTo(18f,20f)
    } }
    val Next by lazy { glyph("FlowNext") {
        moveTo(5f,5f); lineTo(15f,12f); lineTo(5f,19f); close()
        moveTo(19f,5f); lineTo(19f,19f)
    } }
    val Previous by lazy { glyph("FlowPrevious") {
        moveTo(19f,5f); lineTo(9f,12f); lineTo(19f,19f); close()
        moveTo(5f,5f); lineTo(5f,19f)
    } }
    val Shuffle by lazy { glyph("FlowShuffle") {
        moveTo(3f,6f); lineTo(6f,6f); curveTo(11f,6f,13f,18f,18f,18f); lineTo(21f,18f)
        moveTo(18f,15f); lineTo(21f,18f); lineTo(18f,21f)
        moveTo(3f,18f); lineTo(6f,18f); curveTo(11f,18f,13f,6f,18f,6f); lineTo(21f,6f)
        moveTo(18f,3f); lineTo(21f,6f); lineTo(18f,9f)
    } }
    val Lyrics by lazy { glyph("FlowLyrics") {
        moveTo(5f,3f); lineTo(19f,3f); lineTo(19f,21f); lineTo(5f,21f); close()
        moveTo(8f,7f); lineTo(16f,7f); moveTo(8f,11f); lineTo(16f,11f)
        moveTo(8f,15f); lineTo(14f,15f); moveTo(8f,18f); lineTo(11f,18f)
    } }
    val Timer by lazy { glyph("FlowTimer") {
        moveTo(9f,2f); lineTo(15f,2f); moveTo(12f,2f); lineTo(12f,5f)
        moveTo(20f,13f); curveTo(20f,24f,4f,24f,4f,13f); curveTo(4f,2f,20f,2f,20f,13f); close()
        moveTo(12f,8f); lineTo(12f,13f); lineTo(15f,15f)
    } }
    val Favorite by lazy { glyph("FlowFavorite") {
        moveTo(12f,21f); curveTo(9f,18f,2f,13f,2f,8f); curveTo(2f,2f,9f,1f,12f,7f)
        curveTo(15f,1f,22f,2f,22f,8f); curveTo(22f,13f,15f,18f,12f,21f); close()
    } }
    val FavoriteSelected by lazy { glyph("FlowFavoriteSelected", filled = true) {
        moveTo(12f,21f); curveTo(9f,18f,2f,13f,2f,8f); curveTo(2f,2f,9f,1f,12f,7f)
        curveTo(15f,1f,22f,2f,22f,8f); curveTo(22f,13f,15f,18f,12f,21f); close()
    } }
    val Info by lazy { glyph("FlowInfo") {
        moveTo(22f,12f); curveTo(22f,25f,2f,25f,2f,12f); curveTo(2f,-1f,22f,-1f,22f,12f); close()
        moveTo(12f,7f); lineTo(12f,7.1f); moveTo(12f,11f); lineTo(12f,17f)
    } }
    val Edit by lazy { glyph("FlowEdit") {
        moveTo(4f,15f); lineTo(15f,4f); lineTo(20f,9f); lineTo(9f,20f); lineTo(3f,21f); close()
        moveTo(12f,7f); lineTo(17f,12f); moveTo(14f,21f); lineTo(21f,21f)
    } }
    val Music by lazy { glyph("FlowMusic") {
        moveTo(10f,17f); lineTo(10f,5f); lineTo(21f,3f); lineTo(21f,15f)
        moveTo(10f,17f); curveTo(10f,23f,2f,23f,2f,18f); curveTo(2f,14f,10f,13f,10f,17f)
        moveTo(21f,15f); curveTo(21f,21f,13f,21f,13f,16f); curveTo(13f,12f,21f,11f,21f,15f)
    } }
    val SmartDJ by lazy { glyph("FlowSmartDJ") {
        moveTo(3f,12f); lineTo(3f,17f); moveTo(7f,7f); lineTo(7f,20f)
        moveTo(11f,10f); lineTo(11f,18f); moveTo(15f,13f); lineTo(15f,17f)
        moveTo(18f,2f); lineTo(19.5f,6.5f); lineTo(23f,8f); lineTo(19.5f,9.5f)
        lineTo(18f,14f); lineTo(16.5f,9.5f); lineTo(13f,8f); lineTo(16.5f,6.5f); close()
    } }
    val Rewind by lazy { glyph("FlowRewind") {
        moveTo(10f,5f); lineTo(3f,12f); lineTo(10f,19f)
        moveTo(20f,5f); lineTo(13f,12f); lineTo(20f,19f)
    } }
    val Forward by lazy { glyph("FlowForward") {
        moveTo(4f,5f); lineTo(11f,12f); lineTo(4f,19f)
        moveTo(14f,5f); lineTo(21f,12f); lineTo(14f,19f)
    } }
    val More by lazy { glyph("FlowMore") {
        moveTo(5f,12f); lineTo(5.1f,12f); moveTo(12f,12f); lineTo(12.1f,12f); moveTo(19f,12f); lineTo(19.1f,12f)
    } }
    val Back by lazy { glyph("FlowBack") {
        moveTo(11f,4f); lineTo(3f,12f); lineTo(11f,20f); moveTo(3f,12f); lineTo(21f,12f)
    } }
    val Playlist by lazy { glyph("FlowPlaylist") {
        moveTo(3f,5f); lineTo(15f,5f); moveTo(3f,10f); lineTo(12f,10f); moveTo(3f,15f); lineTo(10f,15f)
        moveTo(18f,10f); lineTo(18f,19f); curveTo(18f,23f,11f,23f,11f,19f)
        curveTo(11f,15f,18f,15f,18f,19f); moveTo(18f,10f); lineTo(22f,12f)
    } }
}
