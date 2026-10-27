package app.deciary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.deciary.data.Leather
import app.deciary.ui.theme.Fraunces
import kotlin.random.Random

/** The cover's shape in the design: a slim 10 dp edge at the spine, 26 dp corners on the open side. */
private fun coverShape(left: Dp, right: Dp) = RoundedCornerShape(topStart = left, bottomStart = left, topEnd = right, bottomEnd = right)

/**
 * The diary cover: leather with a spine, stitching, the owner's name pressed in and a strap with a snap.
 * It is 150 dp tall at any width, as in the design. When [onLock] is set, tapping the cover locks the diary.
 */
@Composable
fun DiaryCover(
    name: String,
    leather: Leather,
    modifier: Modifier = Modifier,
    onLock: (() -> Unit)? = null,
) {
    val p = leather.palette()
    val shape = coverShape(10.dp, 26.dp)
    val density = LocalDensity.current
    Box(
        modifier.fillMaxWidth().height(150.dp)
            .shadow(22.dp, shape, ambientColor = Color(0x991D1A33), spotColor = Color(0x991D1A33))
            .clip(shape)
            .then(if (onLock != null) Modifier.semantics { contentDescription = "Lock the diary" }.clickable(onClick = onLock) else Modifier),
    ) {
        Canvas(Modifier.matchParentSize()) { drawCover(p, strap = true) }
        val nameStyle = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.ExtraBold, fontSize = with(density) { 44.dp.toSp() }, letterSpacing = with(density) { (-1).dp.toSp() })
        Box(Modifier.padding(start = 40.dp, end = 110.dp).offset(y = 38.dp)) {
            // Pressed into the leather: a light edge below, a dark edge above, then the letters.
            Text(name, style = nameStyle, color = p.hi, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.offset(y = 3.dp))
            Text(name, style = nameStyle, color = p.lo, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.offset(y = 1.dp))
            Text(name, style = nameStyle, color = p.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            "Only you can open this",
            style = TextStyle(fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, fontSize = with(density) { 15.dp.toSp() }),
            color = p.ink.copy(alpha = 0.85f),
            modifier = Modifier.padding(start = 41.dp).offset(y = 96.dp),
        )
    }
}

/** A small leather swatch for the cover picker: spine and stitching, no name or strap. */
@Composable
fun CoverSwatch(leather: Leather, modifier: Modifier = Modifier) {
    val p = leather.palette()
    Box(modifier.clip(coverShape(8.dp, 14.dp))) {
        Canvas(Modifier.matchParentSize()) { drawCover(p, strap = false) }
    }
}

/**
 * Draws stitched leather in dp, so the spine and strap keep their size at any width. Used for the
 * cover, its swatches and the full-screen lock. [strapY] is the strap's centre (the middle by default).
 */
internal fun DrawScope.drawCover(p: LeatherPalette, strap: Boolean, mini: Boolean = !strap, strapY: Float = size.height / 2) {
    val d = density
    val w = size.width
    val h = size.height
    drawRect(Brush.radialGradient(0f to p.light, 0.65f to p.base, 1f to p.dark, center = Offset(w * 0.42f, h * 0.38f), radius = maxOf(w, h) * 0.85f))
    // Grain: a fixed scatter of tiny light and dark specks.
    val rnd = Random(7)
    repeat((w * h / (130f * d)).toInt().coerceAtMost(5000)) {
        val c = if (rnd.nextBoolean()) p.hi.copy(alpha = 0.10f) else p.lo.copy(alpha = 0.18f)
        drawCircle(c, radius = (0.4f + rnd.nextFloat() * 0.9f) * d, center = Offset(rnd.nextFloat() * w, rnd.nextFloat() * h))
    }
    // Spine.
    val spine = (if (mini) 10f else 18f) * d
    drawRect(Brush.horizontalGradient(0f to Color(0x59000000), 0.7f to Color(0x1F000000), 1f to Color(0x14FFFFFF), endX = spine), size = Size(spine, h))
    drawLine(Color(0x40000000), Offset(spine + 0.5f * d, 0f), Offset(spine + 0.5f * d, h), strokeWidth = d)
    // Stitching along the top, the open side and the bottom.
    val inset = (if (mini) 7f else 10f) * d
    val start = (if (mini) 16f else 30f) * d
    val r = (if (mini) 10f else 20f) * d
    val stitch = Path().apply {
        moveTo(start, inset)
        lineTo(w - inset - r, inset)
        arcTo(Rect(Offset(w - inset - 2 * r, inset), Size(2 * r, 2 * r)), -90f, 90f, false)
        lineTo(w - inset, h - inset - r)
        arcTo(Rect(Offset(w - inset - 2 * r, h - inset - 2 * r), Size(2 * r, 2 * r)), 0f, 90f, false)
        lineTo(start, h - inset)
    }
    val dash = PathEffect.dashPathEffect(floatArrayOf(5f * d, 4f * d))
    drawPath(stitch, Color(0x40000000), style = Stroke(2.2f * d, pathEffect = dash))
    drawPath(stitch, p.thread, style = Stroke(1.8f * d, cap = StrokeCap.Round, pathEffect = dash))
    if (strap) drawStrap(p, strapY)
}

/** The strap that comes in from the open side, with its own stitching and the metal snap. */
private fun DrawScope.drawStrap(p: LeatherPalette, cy: Float) {
    val d = density
    val w = size.width
    val left = w - 98f * d
    val top = cy - 24f * d
    val bottom = cy + 24f * d
    val strap = Path().apply {
        moveTo(left, top)
        lineTo(w, top)
        lineTo(w, bottom)
        lineTo(left, bottom)
        arcTo(Rect(Offset(left - 24f * d, top), Size(48f * d, 48f * d)), 90f, 180f, false)
        close()
    }
    // Drop shadow under the strap.
    translate(top = 3f * d) { drawPath(strap, Color(0x40000000)) }
    drawPath(strap, Brush.verticalGradient(listOf(p.light, p.dark), startY = top, endY = bottom))
    clipPath(strap) {
        val rnd = Random(11)
        repeat(260) {
            drawCircle(p.lo.copy(alpha = 0.2f), (0.4f + rnd.nextFloat() * 0.8f) * d, Offset(left - 24f * d + rnd.nextFloat() * 122f * d, top + rnd.nextFloat() * 48f * d))
        }
    }
    val dash = PathEffect.dashPathEffect(floatArrayOf(4f * d, 3.5f * d))
    val sTop = cy - 18f * d
    val sBottom = cy + 18f * d
    val sl = left + 4f * d
    val stitch = Path().apply {
        moveTo(w, sTop)
        lineTo(sl, sTop)
        arcTo(Rect(Offset(sl - 18f * d, sTop), Size(36f * d, 36f * d)), 270f, -180f, false)
        lineTo(w, sBottom)
    }
    drawPath(stitch, p.thread.copy(alpha = 0.9f), style = Stroke(1.5f * d, pathEffect = dash))
    // The snap.
    val c = Offset(left + 14f * d, cy)
    val rad = 15f * d
    drawCircle(Color(0x59000000), rad, c + Offset(0f, 3f * d))
    drawCircle(Brush.radialGradient(0f to p.metal0, 0.55f to p.metal1, 1f to p.metal2, center = c - Offset(rad * 0.3f, rad * 0.4f), radius = rad * 1.6f), rad, c)
    drawCircle(Color(0x38000000), 10.5f * d, c, style = Stroke(1.2f * d))
    drawCircle(Color(0x59FFFFFF), rad, c, style = Stroke(0.8f * d))
    rotate(-30f, pivot = c + Offset(-5f * d, -6f * d)) {
        drawOval(Color(0x99FFFFFF), topLeft = c + Offset(-9.5f * d, -8.6f * d), size = Size(9f * d, 5.2f * d))
    }
}
