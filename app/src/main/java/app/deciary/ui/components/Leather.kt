package app.deciary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.deciary.data.Leather
import kotlin.random.Random

/** Colours for each leather, matching design/tools/leathergen.py. */
data class LeatherPalette(
    val base: Color, val dark: Color, val light: Color, val thread: Color, val ink: Color,
    val hi: Color, val lo: Color, val metal0: Color, val metal1: Color, val metal2: Color,
)

fun Leather.palette(): LeatherPalette = when (this) {
    Leather.Ink -> LeatherPalette(
        Color(0xFF2A2547), Color(0xFF120F24), Color(0xFF433C6E), Color(0xFFC9B8FA), Color(0xFFEEEAF8),
        Color(0xFF4D4680), Color(0xFF07050F), Color(0xFFFFE1D6), Color(0xFFE0A08C), Color(0xFF9A5A4A),
    )
    Leather.Plum -> LeatherPalette(
        Color(0xFF5B3FB0), Color(0xFF2E1C6B), Color(0xFF7A5ED6), Color(0xFFD9CCFF), Color(0xFFF3EEFF),
        Color(0xFF8F7AE0), Color(0xFF24145A), Color(0xFFFFFFFF), Color(0xFFC9C4D6), Color(0xFF7C7690),
    )
    Leather.Tan -> LeatherPalette(
        Color(0xFFA8693A), Color(0xFF5E3517), Color(0xFFC98B55), Color(0xFFF4E1C1), Color(0xFFFFF6EA),
        Color(0xFFD29A6A), Color(0xFF4A2711), Color(0xFFFFF1C2), Color(0xFFD4A44A), Color(0xFF8C6420),
    )
}

/** Stitched leather with a soft grain, drawn behind [content]. */
@Composable
fun LeatherSurface(
    leather: Leather,
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    stitchInset: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val p = leather.palette()
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) { drawLeather(p, corner.toPx(), stitchInset.toPx()) }
        content()
    }
}

private fun DrawScope.drawLeather(p: LeatherPalette, corner: Float, inset: Float) {
    val r = CornerRadius(corner, corner)
    drawRoundRect(
        brush = Brush.linearGradient(listOf(p.light, p.base, p.dark), start = Offset.Zero, end = Offset(size.width, size.height)),
        cornerRadius = r,
    )
    // Grain: a fixed scatter of tiny light and dark specks.
    val rnd = Random(7)
    val specks = (size.width * size.height / 140f).toInt().coerceAtMost(4000)
    repeat(specks) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        val c = if (rnd.nextBoolean()) p.hi.copy(alpha = 0.10f) else p.lo.copy(alpha = 0.16f)
        drawCircle(c, radius = 0.6f + rnd.nextFloat() * 1.1f, center = Offset(x, y))
    }
    // Soft sheen along the top, shade along the bottom.
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.10f), 0.35f to Color.Transparent,
            0.8f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.22f),
        ),
        cornerRadius = r,
    )
    val stitch = Stroke(width = 1.6f * density, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f * density, 5f * density)))
    val sr = (corner - inset).coerceAtLeast(4f)
    drawRoundRect(
        color = p.lo.copy(alpha = 0.5f),
        topLeft = Offset(inset, inset + density),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = CornerRadius(sr, sr),
        style = stitch,
    )
    drawRoundRect(
        color = p.thread.copy(alpha = 0.85f),
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = CornerRadius(sr, sr),
        style = stitch,
    )
}

/** The metal snap. On the cover it doubles as the lock button. */
@Composable
fun Snap(leather: Leather, modifier: Modifier = Modifier) {
    val p = leather.palette()
    Canvas(modifier) {
        val c = Offset(size.width / 2, size.height / 2)
        val rad = size.minDimension / 2
        drawCircle(Color.Black.copy(alpha = 0.35f), rad, c + Offset(0f, rad * 0.12f))
        drawCircle(
            Brush.radialGradient(listOf(p.metal0, p.metal1, p.metal2), center = c - Offset(rad * 0.35f, rad * 0.35f), radius = rad * 1.6f),
            rad, c,
        )
        drawCircle(p.metal2.copy(alpha = 0.6f), rad * 0.62f, c, style = Stroke(width = rad * 0.12f))
        drawCircle(Color.White.copy(alpha = 0.55f), rad * 0.18f, c - Offset(rad * 0.3f, rad * 0.3f))
    }
}
