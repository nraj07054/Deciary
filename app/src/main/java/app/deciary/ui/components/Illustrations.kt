package app.deciary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Scene art drawn in Compose, in a soft 3D style: lit from the top left, with volume gradients,
// glossy highlights, bevelled edges and a contact shadow. Drawn rather than bitmaps, so it is sharp
// at any size. Each piece draws on a 100 × 100 grid scaled to its box.

private val Violet = Color(0xFF6E4FE6)
private val VioletLight = Color(0xFFA48BFF)
private val VioletDeep = Color(0xFF4A31B8)
private val Lavender = Color(0xFFC9B8FA)
private val Night = Color(0xFF1D1A33)
private val Paper = Color(0xFFFFFFFF)
private val Gold = Color(0xFFFFD45C)
private val GoldMid = Color(0xFFFFB238)
private val Amber = Color(0xFFF2852E)
private val Rose = Color(0xFFFF6B8B)
private val Crimson = Color(0xFFD81B5A)
private val Sky = Color(0xFF6CCBFA)
private val Blue = Color(0xFF2F7BEA)
private val BlueDeep = Color(0xFF1C4FB5)
private val Silver = Color(0xFFF1EFF7)
private val SilverMid = Color(0xFFC9C3DB)
private val SilverDeep = Color(0xFF8E87A8)

@Composable
private fun Grid(size: Dp, modifier: Modifier, draw: DrawScope.(u: Float) -> Unit) {
    Canvas(modifier.size(size)) { draw(this.size.minDimension / 100f) }
}

private fun at(u: Float, x: Float, y: Float) = Offset(x * u, y * u)
private fun sz(u: Float, w: Float, h: Float) = Size(w * u, h * u)
private fun cr(u: Float, r: Float) = CornerRadius(r * u)

private fun roundRect(u: Float, x: Float, y: Float, w: Float, h: Float, r: Float) =
    Path().apply { addRoundRect(RoundRect(Rect(at(u, x, y), sz(u, w, h)), cr(u, r))) }

/** A soft contact shadow on the ground under an object. */
private fun DrawScope.contactShadow(u: Float, cx: Float, cy: Float, rx: Float, ry: Float, alpha: Float = 0.22f) {
    val c = at(u, cx, cy)
    scale(1f, ry / rx, pivot = c) {
        drawCircle(Brush.radialGradient(0f to Night.copy(alpha = alpha), 1f to Color.Transparent, center = c, radius = rx * u), rx * u, c)
    }
}

/** A glossy highlight: white fading out from (cx, cy). */
private fun DrawScope.gloss(u: Float, cx: Float, cy: Float, r: Float, alpha: Float = 0.6f) {
    val c = at(u, cx, cy)
    drawCircle(Brush.radialGradient(0f to Color.White.copy(alpha = alpha), 1f to Color.Transparent, center = c, radius = r * u), r * u, c)
}

/** Fills [path] with a top-left-lit gradient, then adds a gloss and a darker bottom edge inside it. */
private fun DrawScope.solid(u: Float, path: Path, light: Color, mid: Color, dark: Color, from: Offset, to: Offset, glossAt: Offset? = null, glossR: Float = 22f) {
    drawPath(path, Brush.linearGradient(0f to light, 0.55f to mid, 1f to dark, start = from, end = to))
    clipPath(path) {
        val b = path.getBounds()
        drawRect(Brush.verticalGradient(0.7f to Color.Transparent, 1f to Night.copy(alpha = 0.18f), startY = b.top, endY = b.bottom), b.topLeft, b.size)
        if (glossAt != null) gloss(u, glossAt.x / u, glossAt.y / u, glossR)
    }
}

private fun star(u: Float, cx: Float, cy: Float, r: Float) = Path().apply {
    moveTo(cx * u, (cy - r) * u)
    quadraticTo(cx * u, cy * u, (cx + r) * u, cy * u)
    quadraticTo(cx * u, cy * u, cx * u, (cy + r) * u)
    quadraticTo(cx * u, cy * u, (cx - r) * u, cy * u)
    quadraticTo(cx * u, cy * u, cx * u, (cy - r) * u)
    close()
}

private fun heartPath(u: Float, cx: Float, cy: Float, s: Float) = Path().apply {
    fun p(x: Float, y: Float) = at(u, cx + x * s, cy + y * s)
    val a = p(0f, 0.92f)
    moveTo(a.x, a.y)
    val c1 = p(-1.05f, 0.2f); val c2 = p(-1.02f, -0.72f); val e1 = p(-0.5f, -0.78f)
    cubicTo(c1.x, c1.y, c2.x, c2.y, e1.x, e1.y)
    val c3 = p(-0.18f, -0.82f); val c4 = p(0f, -0.58f); val e2 = p(0f, -0.42f)
    cubicTo(c3.x, c3.y, c4.x, c4.y, e2.x, e2.y)
    val c5 = p(0f, -0.58f); val c6 = p(0.18f, -0.82f); val e3 = p(0.5f, -0.78f)
    cubicTo(c5.x, c5.y, c6.x, c6.y, e3.x, e3.y)
    val c7 = p(1.02f, -0.72f); val c8 = p(1.05f, 0.2f)
    cubicTo(c7.x, c7.y, c8.x, c8.y, a.x, a.y)
    close()
}

/** A glossy 3D heart. */
private fun DrawScope.heart3d(u: Float, cx: Float, cy: Float, s: Float) {
    val h = heartPath(u, cx, cy, s)
    drawPath(h, Brush.radialGradient(0f to Color(0xFFFF9AAE), 0.45f to Rose, 1f to Crimson, center = at(u, cx - s * 0.35f, cy - s * 0.35f), radius = s * 1.6f * u))
    clipPath(h) {
        drawRect(Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color(0x55800030), startY = (cy) * u, endY = (cy + s) * u))
    }
    drawOval(Color.White.copy(alpha = 0.55f), topLeft = at(u, cx - s * 0.66f, cy - s * 0.62f), size = sz(u, s * 0.36f, s * 0.2f))
}

/** Gold four-point stars with a warm glow. */
@Composable
fun SparklesArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    fun sparkle(cx: Float, cy: Float, r: Float) {
        gloss(u, cx, cy, r * 1.1f, 0.0f)
        drawCircle(Brush.radialGradient(0f to Gold.copy(alpha = 0.45f), 1f to Color.Transparent, center = at(u, cx, cy), radius = r * 0.9f * u), r * 0.9f * u, at(u, cx, cy))
        drawPath(star(u, cx, cy, r), Brush.radialGradient(0f to Color(0xFFFFF6D6), 0.35f to Gold, 1f to Amber, center = at(u, cx - r * 0.15f, cy - r * 0.15f), radius = r * u))
    }
    sparkle(60f, 52f, 36f)
    sparkle(20f, 22f, 15f)
    sparkle(22f, 80f, 10f)
}

/**
 * A closed letter: lit paper body, shaded flap, soft folds. [heart] puts a glossy heart on it;
 * [initial] a wax seal where the flap meets, which [sealModifier] lets a screen animate.
 */
@Composable
fun EnvelopeArt(
    width: Dp,
    modifier: Modifier = Modifier,
    initial: String? = null,
    heart: Boolean = false,
    sealModifier: Modifier = Modifier,
    sealSize: Dp = width * 0.24f,
) {
    val height = width * 0.7f
    Box(modifier.size(width, height)) {
        Canvas(Modifier.size(width, height)) {
            val u = this.size.width / 100f
            // The grid here is 100 wide and 70 high.
            contactShadow(u, 50f, 66f, 46f, 6f, 0.25f)
            val body = roundRect(u, 2f, 2f, 96f, 62f, 8f)
            drawPath(body, Brush.linearGradient(0f to Color.White, 1f to Color(0xFFE5DCFB), start = at(u, 10f, 2f), end = at(u, 90f, 64f)))
            clipPath(body) {
                // The lower folds as soft creases.
                val left = Path().apply { moveTo(2f * u, 64f * u); lineTo(50f * u, 30f * u); lineTo(50f * u, 64f * u); close() }
                drawPath(left, Brush.linearGradient(0f to Color(0x14000000), 1f to Color.Transparent, start = at(u, 2f, 64f), end = at(u, 50f, 40f)))
                val right = Path().apply { moveTo(98f * u, 64f * u); lineTo(50f * u, 30f * u); lineTo(50f * u, 64f * u); close() }
                drawPath(right, Brush.linearGradient(0f to Color(0x22000000), 1f to Color.Transparent, start = at(u, 98f, 64f), end = at(u, 50f, 40f)))
                drawLine(Color(0x33A48BFF), at(u, 2f, 64f), at(u, 46f, 33f), strokeWidth = 0.8f * u)
                drawLine(Color(0x33A48BFF), at(u, 98f, 64f), at(u, 54f, 33f), strokeWidth = 0.8f * u)
                // A violet glow along the bottom, light coming through the paper.
                drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to Lavender.copy(alpha = 0.7f), startY = 48f * u, endY = 64f * u), at(u, 0f, 48f), sz(u, 100f, 16f))
                // The flap's shadow on the body, then the flap itself.
                val flapShadow = Path().apply { moveTo(2f * u, 2f * u); lineTo(50f * u, 42f * u); lineTo(98f * u, 2f * u); close() }
                drawPath(flapShadow, Color(0x1F3B2A7A))
                val flap = Path().apply { moveTo(2f * u, 2f * u); lineTo(50f * u, 38f * u); lineTo(98f * u, 2f * u); close() }
                drawPath(flap, Brush.verticalGradient(0f to Color.White, 1f to Color(0xFFF1ECFE), startY = 2f * u, endY = 38f * u))
                drawPath(flap, Color(0x40A48BFF), style = Stroke(0.8f * u))
                gloss(u, 22f, 10f, 26f, 0.7f)
            }
            drawPath(body, Color(0x1A6E4FE6), style = Stroke(0.6f * u))
            if (heart) heart3d(u, 50f, 33f, 22f)
        }
        if (initial != null) {
            val seal = sealSize
            WaxSeal(initial, seal, sealModifier.align(Alignment.TopCenter).padding(top = height * (38f / 70f) - seal / 2))
        }
    }
}

/** An envelope flying in, with speed lines behind it. */
@Composable
fun IncomingEnvelopeArt(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size), contentAlignment = Alignment.CenterEnd) {
        Canvas(Modifier.size(size)) {
            val u = this.size.minDimension / 100f
            listOf(Triple(38f, 4f, 22f), Triple(50f, 10f, 14f), Triple(62f, 2f, 20f)).forEach { (y, x, len) ->
                drawLine(Brush.horizontalGradient(listOf(Sky.copy(alpha = 0.2f), Sky), startX = x * u, endX = (x + len) * u), at(u, x, y), at(u, x + len, y), strokeWidth = 4.5f * u, cap = StrokeCap.Round)
            }
        }
        Box(Modifier.size(size * 0.7f, size * 0.7f * 0.7f).rotate(-8f)) { EnvelopeArt(size * 0.7f) }
    }
}

/** A postage stamp with a heart; its perforations are cut in [edge], the colour behind it. */
@Composable
fun StampArt(size: Dp, edge: Color, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 52f, 94f, 40f, 5f, 0.25f)
    drawRoundRect(Brush.linearGradient(listOf(Color.White, Color(0xFFF4F0FE)), start = at(u, 10f, 6f), end = at(u, 90f, 94f)), at(u, 10f, 6f), sz(u, 80f, 88f), cr(u, 4f))
    for (i in 0..7) {
        drawCircle(edge, 4.2f * u, at(u, 14f + i * 10.3f, 6f))
        drawCircle(edge, 4.2f * u, at(u, 14f + i * 10.3f, 94f))
    }
    for (i in 0..8) {
        drawCircle(edge, 4.2f * u, at(u, 10f, 10f + i * 10f))
        drawCircle(edge, 4.2f * u, at(u, 90f, 10f + i * 10f))
    }
    val inner = roundRect(u, 20f, 16f, 60f, 68f, 3f)
    drawPath(inner, Brush.linearGradient(listOf(Color(0xFFB6E3FF), Color(0xFFD9CCFF), Color(0xFFFFD3E2)), start = at(u, 20f, 16f), end = at(u, 80f, 84f)))
    clipPath(inner) {
        drawCircle(Color.White.copy(alpha = 0.6f), 16f * u, at(u, 68f, 26f))
        drawOval(Color.White.copy(alpha = 0.5f), at(u, 14f, 66f), sz(u, 40f, 22f))
    }
    heart3d(u, 50f, 52f, 18f)
}

private fun shieldPath(u: Float, inset: Float = 0f): Path {
    val k = 1f - inset
    fun x(v: Float) = (50f + (v - 50f) * k) * u
    fun y(v: Float) = (52f + (v - 52f) * k) * u
    return Path().apply {
        moveTo(x(50f), y(4f))
        cubicTo(x(64f), y(13f), x(78f), y(16f), x(90f), y(16f))
        lineTo(x(90f), y(46f))
        cubicTo(x(90f), y(72f), x(72f), y(88f), x(50f), y(98f))
        cubicTo(x(28f), y(88f), x(10f), y(72f), x(10f), y(46f))
        lineTo(x(10f), y(16f))
        cubicTo(x(22f), y(16f), x(36f), y(13f), x(50f), y(4f))
        close()
    }
}

/** A two-tone shield with a polished silver rim. */
@Composable
fun ShieldArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    val rim = shieldPath(u)
    drawPath(rim, Brush.linearGradient(0f to Silver, 0.5f to SilverMid, 1f to SilverDeep, start = at(u, 14f, 6f), end = at(u, 86f, 96f)))
    drawPath(rim, Color.White.copy(alpha = 0.7f), style = Stroke(1.2f * u))
    val face = shieldPath(u, 0.16f)
    drawPath(face, Color(0x33000000), style = Stroke(2.2f * u))
    clipPath(face) {
        drawRect(Brush.linearGradient(listOf(Color(0xFF5AA2F7), Blue, BlueDeep), start = at(u, 14f, 10f), end = at(u, 50f, 96f)), at(u, 0f, 0f), sz(u, 50f, 100f))
        drawRect(Brush.linearGradient(listOf(Color(0xFFA5E4FD), Sky, Color(0xFF2E9BE0)), start = at(u, 50f, 10f), end = at(u, 90f, 96f)), at(u, 50f, 0f), sz(u, 50f, 100f))
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color(0x33FFFFFF), Color.Transparent), startX = 44f * u, endX = 56f * u), at(u, 44f, 0f), sz(u, 12f, 100f))
        gloss(u, 30f, 24f, 30f, 0.55f)
        drawRect(Brush.verticalGradient(0.7f to Color.Transparent, 1f to Color(0x33000040)), at(u, 0f, 0f), sz(u, 100f, 100f))
    }
}

/** A confetti-covered notebook with a page block and a ribbon. */
@Composable
fun NotebookArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 54f, 94f, 40f, 6f)
    // Page block peeking out on the right and bottom.
    drawRoundRect(Brush.verticalGradient(listOf(Color.White, Color(0xFFE4DDF5))), at(u, 24f, 12f), sz(u, 64f, 80f), cr(u, 6f))
    for (i in 1..5) drawLine(Color(0x1A3B2A7A), at(u, 84f + i * 0.6f, 16f), at(u, 84f + i * 0.6f, 88f), strokeWidth = 0.5f * u)
    val cover = roundRect(u, 16f, 6f, 68f, 82f, 7f)
    solid(u, cover, Color(0xFF8A72E0), Color(0xFF5B43B4), Color(0xFF3B2A86), at(u, 16f, 6f), at(u, 84f, 88f), at(u, 30f, 16f), 30f)
    drawRect(Brush.horizontalGradient(listOf(Color(0x552E2458), Color(0x002E2458)), startX = 16f * u, endX = 30f * u), at(u, 16f, 6f), sz(u, 14f, 82f))
    drawLine(Color(0x33FFFFFF), at(u, 24f, 9f), at(u, 24f, 85f), strokeWidth = 0.8f * u)
    val confetti = listOf(
        Triple(38f, 18f, Color(0xFFFF8FB4)), Triple(54f, 16f, Sky), Triple(70f, 20f, Color.White),
        Triple(42f, 32f, Color.White), Triple(60f, 30f, Color(0xFFFF8FB4)), Triple(74f, 36f, Sky),
        Triple(36f, 46f, Sky), Triple(52f, 46f, Gold), Triple(68f, 50f, Color(0xFFFF8FB4)),
        Triple(44f, 60f, Color(0xFFFF8FB4)), Triple(60f, 62f, Color.White), Triple(74f, 66f, Gold),
        Triple(38f, 74f, Gold), Triple(54f, 76f, Sky),
    )
    confetti.forEachIndexed { i, (cx, cy, color) ->
        rotate(if (i % 2 == 0) 35f else -30f, pivot = at(u, cx, cy)) {
            drawLine(Color(0x33000000), at(u, cx - 3.5f, cy + 0.8f), at(u, cx + 3.5f, cy + 0.8f), strokeWidth = 3.2f * u, cap = StrokeCap.Round)
            drawLine(color, at(u, cx - 3.5f, cy), at(u, cx + 3.5f, cy), strokeWidth = 3.2f * u, cap = StrokeCap.Round)
        }
    }
    // A ribbon bookmark hanging from the bottom.
    val ribbon = Path().apply { moveTo(66f * u, 86f * u); lineTo(74f * u, 86f * u); lineTo(74f * u, 98f * u); lineTo(70f * u, 95f * u); lineTo(66f * u, 98f * u); close() }
    drawPath(ribbon, Brush.verticalGradient(listOf(Color(0xFFFF6B8B), Crimson), startY = 86f * u, endY = 98f * u))
}

/** A gold padlock with a lavender key standing beside it. */
@Composable
fun PadlockArt(size: Dp, modifier: Modifier = Modifier, key: Boolean = true) = Grid(size, modifier) { u ->
    val dx = if (key) -6f else 0f
    contactShadow(u, 46f, 94f, 44f, 6f, 0.26f)
    if (key) {
        // The key leans against the lock's right side, partly behind it.
        val keyBrush = Brush.linearGradient(listOf(Color(0xFFF1ECFF), Lavender, Color(0xFF9F87F0)), start = at(u, 74f, 10f), end = at(u, 98f, 92f))
        rotate(10f, pivot = at(u, 86f, 60f)) {
            drawRoundRect(keyBrush, at(u, 82f, 34f), sz(u, 9f, 56f), cr(u, 4.5f))
            drawRoundRect(keyBrush, at(u, 88f, 70f), sz(u, 9f, 6f), cr(u, 2f))
            drawRoundRect(keyBrush, at(u, 88f, 80f), sz(u, 9f, 6f), cr(u, 2f))
            drawCircle(keyBrush, 13f * u, at(u, 86.5f, 24f))
            gloss(u, 82f, 18f, 9f, 0.8f)
            drawCircle(Color(0xFF8F77E6), 4f * u, at(u, 86.5f, 20f))
            drawRoundRect(Color.White.copy(alpha = 0.5f), at(u, 84f, 40f), sz(u, 2f, 40f), cr(u, 1f))
        }
    }
    // Shackle: polished metal tube.
    val shackle = Path().apply {
        moveTo((25f + dx) * u, 50f * u)
        lineTo((25f + dx) * u, 32f * u)
        arcTo(Rect(at(u, 25f + dx, 10f), sz(u, 42f, 42f)), 180f, 180f, false)
        lineTo((67f + dx) * u, 50f * u)
    }
    drawPath(shackle, SilverDeep, style = Stroke(11f * u, cap = StrokeCap.Round))
    drawPath(shackle, Brush.horizontalGradient(listOf(SilverMid, Silver, Color.White, SilverMid), startX = (20f + dx) * u, endX = (72f + dx) * u), style = Stroke(8f * u, cap = StrokeCap.Round))
    drawPath(shackle, Color.White.copy(alpha = 0.8f), style = Stroke(1.6f * u, cap = StrokeCap.Round))
    // Body.
    val body = roundRect(u, 10f + dx, 42f, 72f, 50f, 13f)
    drawPath(body, Brush.linearGradient(0f to Color(0xFFFFE89A), 0.35f to Gold, 0.75f to GoldMid, 1f to Amber, start = at(u, 10f + dx, 42f), end = at(u, 82f + dx, 92f)))
    clipPath(body) {
        drawRect(Brush.verticalGradient(0.72f to Color.Transparent, 1f to Color(0x55B4400A), startY = 42f * u, endY = 92f * u))
        gloss(u, 24f + dx, 50f, 22f, 0.75f)
        drawRoundRect(Color.White.copy(alpha = 0.35f), at(u, 16f + dx, 45f), sz(u, 60f, 3f), cr(u, 1.5f))
    }
    // Keyhole, cut in.
    val hole = Path().apply {
        addOval(Rect(at(u, 39f + dx, 57f), sz(u, 14f, 14f)))
        addRoundRect(RoundRect(Rect(at(u, 42.5f + dx, 64f), sz(u, 7f, 17f)), cr(u, 3.5f)))
    }
    drawPath(hole, Brush.verticalGradient(listOf(Color(0xFF2A1F55), Color(0xFF4B3B8F)), startY = 57f * u, endY = 81f * u))
    drawLine(Color.White.copy(alpha = 0.5f), at(u, 42f + dx, 81.5f), at(u, 50f + dx, 81.5f), strokeWidth = 1f * u, cap = StrokeCap.Round)
}

/** A seedling: two veined leaves on a stem in a mound of soil. */
@Composable
fun SeedlingArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 50f, 96f, 30f, 4f)
    drawLine(Brush.horizontalGradient(listOf(Color(0xFF5BB04F), Color(0xFF2E7D32)), startX = 47f * u, endX = 53f * u), at(u, 50f, 86f), at(u, 50f, 44f), strokeWidth = 6f * u, cap = StrokeCap.Round)
    val left = Path().apply { moveTo(50f * u, 52f * u); cubicTo(40f * u, 30f * u, 18f * u, 28f * u, 6f * u, 36f * u); cubicTo(16f * u, 58f * u, 40f * u, 60f * u, 50f * u, 52f * u); close() }
    val right = Path().apply { moveTo(50f * u, 46f * u); cubicTo(58f * u, 20f * u, 86f * u, 14f * u, 98f * u, 22f * u); cubicTo(90f * u, 46f * u, 64f * u, 56f * u, 50f * u, 46f * u); close() }
    listOf(left to at(u, 14f, 34f), right to at(u, 80f, 20f)).forEach { (leaf, lit) ->
        drawPath(leaf, Brush.radialGradient(0f to Color(0xFFC8F08A), 0.5f to Color(0xFF6CC24A), 1f to Color(0xFF2E8B3A), center = lit, radius = 50f * u))
        clipPath(leaf) { gloss(u, lit.x / u + 6f, lit.y / u + 4f, 14f, 0.45f) }
    }
    drawPath(Path().apply { moveTo(48f * u, 50f * u); quadraticTo(30f * u, 38f * u, 12f * u, 38f * u) }, Color(0x66FFFFFF), style = Stroke(1.2f * u, cap = StrokeCap.Round))
    drawPath(Path().apply { moveTo(52f * u, 45f * u); quadraticTo(70f * u, 26f * u, 92f * u, 23f * u) }, Color(0x66FFFFFF), style = Stroke(1.2f * u, cap = StrokeCap.Round))
    val soil = Path().apply { moveTo(20f * u, 96f * u); cubicTo(22f * u, 78f * u, 78f * u, 78f * u, 80f * u, 96f * u); close() }
    solid(u, soil, Color(0xFFB4826A), Color(0xFF8A5A44), Color(0xFF5E3A2C), at(u, 30f, 80f), at(u, 70f, 98f), at(u, 40f, 84f), 14f)
    listOf(at(u, 36f, 90f), at(u, 58f, 88f), at(u, 66f, 93f), at(u, 46f, 94f)).forEach { drawCircle(Color(0x33000000), 1.2f * u, it) }
}

/** A blue fountain pen, nib down to the left. */
@Composable
fun PenArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 46f, 94f, 32f, 4f, 0.18f)
    rotate(45f, pivot = at(u, 50f, 50f)) {
        val barrel = roundRect(u, 40f, 2f, 20f, 58f, 10f)
        drawPath(barrel, Brush.horizontalGradient(0f to Color(0xFF7DB4FA), 0.35f to Blue, 1f to BlueDeep, startX = 40f * u, endX = 60f * u))
        drawRoundRect(Color.White.copy(alpha = 0.55f), at(u, 43.5f, 6f), sz(u, 3f, 48f), cr(u, 1.5f))
        // Clip.
        drawRoundRect(Brush.horizontalGradient(listOf(Silver, SilverDeep), startX = 56f * u, endX = 60f * u), at(u, 56f, 6f), sz(u, 3.5f, 30f), cr(u, 1.75f))
        // Metal ring.
        drawRect(Brush.horizontalGradient(listOf(Color.White, SilverMid, SilverDeep), startX = 40f * u, endX = 60f * u), at(u, 40f, 50f), sz(u, 20f, 7f))
        // Nib.
        val nib = Path().apply { moveTo(41f * u, 57f * u); lineTo(59f * u, 57f * u); cubicTo(58f * u, 72f * u, 53f * u, 86f * u, 50f * u, 97f * u); cubicTo(47f * u, 86f * u, 42f * u, 72f * u, 41f * u, 57f * u); close() }
        drawPath(nib, Brush.horizontalGradient(listOf(Color(0xFFFFF0C2), Gold, Color(0xFFC77A1E)), startX = 41f * u, endX = 59f * u))
        drawLine(Color(0xFF6B4410), at(u, 50f, 70f), at(u, 50f, 94f), strokeWidth = 1.2f * u)
        drawCircle(Color(0xFF6B4410), 2f * u, at(u, 50f, 70f))
    }
}

/** An hourglass: wooden caps, clear glass, golden sand running down. */
@Composable
fun HourglassArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 50f, 96f, 36f, 4f)
    val top = Path().apply { moveTo(26f * u, 14f * u); lineTo(74f * u, 14f * u); cubicTo(74f * u, 34f * u, 56f * u, 42f * u, 52f * u, 50f * u); lineTo(48f * u, 50f * u); cubicTo(44f * u, 42f * u, 26f * u, 34f * u, 26f * u, 14f * u); close() }
    val bottom = Path().apply { moveTo(26f * u, 86f * u); lineTo(74f * u, 86f * u); cubicTo(74f * u, 66f * u, 56f * u, 58f * u, 52f * u, 50f * u); lineTo(48f * u, 50f * u); cubicTo(44f * u, 58f * u, 26f * u, 66f * u, 26f * u, 86f * u); close() }
    val glass = Brush.horizontalGradient(listOf(Color(0xFFCFE6FB), Color(0xFFF4FAFF), Color(0xFFA9D2F5)), startX = 26f * u, endX = 74f * u)
    drawPath(top, glass)
    drawPath(bottom, glass)
    val sand = Brush.verticalGradient(listOf(Color(0xFFFFE08A), GoldMid, Amber))
    clipPath(top) { drawPath(Path().apply { moveTo(20f * u, 26f * u); lineTo(80f * u, 26f * u); lineTo(80f * u, 52f * u); lineTo(20f * u, 52f * u); close() }, sand) }
    drawLine(GoldMid, at(u, 50f, 48f), at(u, 50f, 74f), strokeWidth = 1.8f * u)
    clipPath(bottom) {
        drawPath(Path().apply { moveTo(26f * u, 88f * u); cubicTo(34f * u, 72f * u, 44f * u, 68f * u, 50f * u, 68f * u); cubicTo(56f * u, 68f * u, 66f * u, 72f * u, 74f * u, 88f * u); close() }, sand)
    }
    val rim = Color(0xFF86B8E6)
    drawPath(top, rim, style = Stroke(1.8f * u))
    drawPath(bottom, rim, style = Stroke(1.8f * u))
    drawLine(Color.White, at(u, 31f, 19f), at(u, 37f, 32f), strokeWidth = 2.6f * u, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.8f), at(u, 32f, 80f), at(u, 36f, 70f), strokeWidth = 2.2f * u, cap = StrokeCap.Round)
    // Wooden caps with a lit top face.
    listOf(5f, 84f).forEach { y ->
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFE8A74A), Color(0xFFB86A22)), startY = y * u, endY = (y + 11f) * u), at(u, 16f, y), sz(u, 68f, 11f), cr(u, 4f))
        drawRoundRect(Color.White.copy(alpha = 0.35f), at(u, 19f, y + 1.5f), sz(u, 62f, 2.5f), cr(u, 1.25f))
    }
}

/** A crystal ball with sparkles inside, on a lavender stand. */
@Composable
fun CrystalBallArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 50f, 97f, 30f, 3.5f)
    drawCircle(Brush.radialGradient(0f to VioletLight.copy(alpha = 0.45f), 1f to Color.Transparent, center = at(u, 50f, 44f), radius = 48f * u), 48f * u, at(u, 50f, 44f))
    val stand = Path().apply { moveTo(26f * u, 97f * u); cubicTo(26f * u, 90f * u, 30f * u, 82f * u, 36f * u, 78f * u); lineTo(64f * u, 78f * u); cubicTo(70f * u, 82f * u, 74f * u, 90f * u, 74f * u, 97f * u); close() }
    solid(u, stand, Color(0xFFF0EAFF), Lavender, Color(0xFF8F77E6), at(u, 26f, 78f), at(u, 74f, 97f), at(u, 36f, 82f), 12f)
    drawCircle(Brush.radialGradient(0f to Color(0xFF9B7CF5), 0.45f to Color(0xFF4B33A8), 1f to Color(0xFF17112E), center = at(u, 40f, 32f), radius = 50f * u), 37f * u, at(u, 50f, 44f))
    // Swirl of light inside.
    drawArc(Brush.sweepGradient(listOf(Color.Transparent, Color(0x66C9B8FA), Color.Transparent), center = at(u, 50f, 46f)), 200f, 220f, false, at(u, 26f, 24f), sz(u, 48f, 44f), style = Stroke(3f * u, cap = StrokeCap.Round))
    drawPath(star(u, 42f, 40f, 12f), Brush.radialGradient(listOf(Color(0xFFFFF6D6), Gold), center = at(u, 42f, 40f), radius = 12f * u))
    drawPath(star(u, 62f, 30f, 7f), Gold)
    drawPath(star(u, 60f, 58f, 5f), Gold)
    drawOval(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.75f), Color.Transparent), center = at(u, 34f, 22f), radius = 12f * u), at(u, 22f, 16f), sz(u, 24f, 12f))
    drawCircle(Color.White.copy(alpha = 0.18f), 37f * u, at(u, 50f, 44f), style = Stroke(1.2f * u))
}

/** A set of brass balance scales with lavender pans. */
@Composable
fun BalanceArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 50f, 96f, 28f, 3.5f)
    val brass = Brush.horizontalGradient(listOf(Color(0xFFFFE8A0), GoldMid, Color(0xFFC0701E)))
    drawRoundRect(Brush.verticalGradient(listOf(GoldMid, Color(0xFFB0621A)), startY = 86f * u, endY = 96f * u), at(u, 30f, 86f), sz(u, 40f, 10f), cr(u, 5f))
    drawRoundRect(brass, at(u, 46.5f, 14f), sz(u, 7f, 74f), cr(u, 3.5f))
    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFFFE8A0), Color(0xFFC0701E)), startY = 21f * u, endY = 28f * u), at(u, 10f, 21f), sz(u, 80f, 7f), cr(u, 3.5f))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF4CC), GoldMid), center = at(u, 48f, 13f), radius = 7f * u), 7f * u, at(u, 50f, 15f))
    listOf(18f, 82f).forEach { x ->
        drawLine(SilverDeep, at(u, x, 27f), at(u, x - 13f, 58f), strokeWidth = 1.4f * u)
        drawLine(SilverDeep, at(u, x, 27f), at(u, x + 13f, 58f), strokeWidth = 1.4f * u)
        val pan = Path().apply { moveTo((x - 17f) * u, 57f * u); cubicTo((x - 15f) * u, 70f * u, (x + 15f) * u, 70f * u, (x + 17f) * u, 57f * u); close() }
        solid(u, pan, Color(0xFFF3EEFF), Lavender, Color(0xFF8C73E8), at(u, x - 17f, 57f), at(u, x + 17f, 68f), at(u, x - 8f, 59f), 10f)
        drawOval(Brush.verticalGradient(listOf(Color(0xFF9F87F0), Color(0xFFD9CCFF)), startY = 55f * u, endY = 59f * u), at(u, x - 17f, 55f), sz(u, 34f, 4f))
    }
}

/** An eye, glossy, with a violet iris. */
@Composable
fun EyeArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    val almond = Path().apply {
        moveTo(6f * u, 50f * u)
        cubicTo(24f * u, 18f * u, 76f * u, 18f * u, 94f * u, 50f * u)
        cubicTo(76f * u, 82f * u, 24f * u, 82f * u, 6f * u, 50f * u)
        close()
    }
    contactShadow(u, 50f, 86f, 34f, 4f, 0.16f)
    drawPath(almond, Brush.radialGradient(0f to Color.White, 0.7f to Color(0xFFF1EEF8), 1f to Color(0xFFCFC8E2), center = at(u, 46f, 44f), radius = 50f * u))
    clipPath(almond) {
        drawCircle(Brush.radialGradient(0f to Color(0xFFC9B8FA), 0.45f to Violet, 1f to Color(0xFF2E1F7A), center = at(u, 46f, 44f), radius = 24f * u), 21f * u, at(u, 50f, 50f))
        for (a in 0 until 360 step 30) {
            rotate(a.toFloat(), pivot = at(u, 50f, 50f)) { drawLine(Color(0x33FFFFFF), at(u, 50f, 39f), at(u, 50f, 32f), strokeWidth = 1f * u) }
        }
        drawCircle(Night, 8.5f * u, at(u, 50f, 50f))
        drawCircle(Color.White, 4.5f * u, at(u, 43f, 43f))
        drawCircle(Color.White.copy(alpha = 0.6f), 2f * u, at(u, 57f, 56f))
        drawRect(Brush.verticalGradient(0f to Color(0x332A1F55), 0.3f to Color.Transparent), at(u, 0f, 18f), sz(u, 100f, 40f))
    }
    drawPath(almond, Brush.verticalGradient(listOf(Color(0xFF3B2E6E), Color(0xFF6E5BA8))), style = Stroke(3f * u))
}

/** A blue mailbox with a letter inside and its red flag up. */
@Composable
fun MailboxArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 56f, 97f, 22f, 3f)
    drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFFB4826A), Color(0xFF7A4C3A), Color(0xFF5E3A2C)), startX = 50f * u, endX = 62f * u), at(u, 50f, 62f), sz(u, 12f, 35f), cr(u, 2f))
    // The long side, with its rounded top running back.
    val side = Path().apply {
        moveTo(28f * u, 66f * u); lineTo(28f * u, 22f * u); lineTo(72f * u, 22f * u)
        cubicTo(82f * u, 22f * u, 90f * u, 30f * u, 90f * u, 40f * u); lineTo(90f * u, 66f * u); close()
    }
    solid(u, side, Color(0xFF6FB0FA), Color(0xFF3A85EC), BlueDeep, at(u, 28f, 22f), at(u, 90f, 66f), at(u, 60f, 26f), 20f)
    drawLine(Color.White.copy(alpha = 0.5f), at(u, 30f, 24.5f), at(u, 72f, 24.5f), strokeWidth = 1.4f * u, cap = StrokeCap.Round)
    // The front face, an arch, with the slot and a letter peeking out.
    val face = Path().apply { moveTo(10f * u, 66f * u); lineTo(10f * u, 38f * u); cubicTo(10f * u, 14f * u, 46f * u, 14f * u, 46f * u, 38f * u); lineTo(46f * u, 66f * u); close() }
    solid(u, face, Color(0xFFB3D8FD), Color(0xFF6FB0FA), Color(0xFF3A85EC), at(u, 10f, 18f), at(u, 46f, 66f), at(u, 18f, 24f), 16f)
    val slot = Path().apply { moveTo(15f * u, 62f * u); lineTo(15f * u, 39f * u); cubicTo(15f * u, 21f * u, 41f * u, 21f * u, 41f * u, 39f * u); lineTo(41f * u, 62f * u); close() }
    drawPath(slot, Brush.verticalGradient(listOf(Color(0xFF1A1440), Color(0xFF3B2E6E)), startY = 22f * u, endY = 62f * u))
    rotate(-8f, pivot = at(u, 28f, 50f)) {
        drawRoundRect(Color(0x33000000), at(u, 17f, 43f), sz(u, 26f, 18f), cr(u, 2f))
        drawRoundRect(Brush.verticalGradient(listOf(Color.White, Color(0xFFE6DDFB))), at(u, 16f, 41f), sz(u, 26f, 18f), cr(u, 2f))
        drawLine(Lavender, at(u, 16f, 41f), at(u, 29f, 51f), strokeWidth = 1.6f * u)
        drawLine(Lavender, at(u, 42f, 41f), at(u, 29f, 51f), strokeWidth = 1.6f * u)
    }
    // Flag.
    drawRoundRect(Brush.horizontalGradient(listOf(Color.White, SilverMid, SilverDeep), startX = 72f * u, endX = 76f * u), at(u, 72f, 8f), sz(u, 4f, 40f), cr(u, 2f))
    val flag = roundRect(u, 74f, 5f, 19f, 13f, 2.5f)
    solid(u, flag, Color(0xFFFFA48A), Color(0xFFF26A4B), Color(0xFFC2402A), at(u, 74f, 5f), at(u, 93f, 18f), at(u, 78f, 7f), 8f)
    drawCircle(Brush.radialGradient(listOf(Color.White, SilverDeep), center = at(u, 73f, 45f), radius = 4f * u), 3.5f * u, at(u, 74f, 46f))
}

/** A violet two-drawer file cabinet. */
@Composable
fun FileCabinetArt(size: Dp, modifier: Modifier = Modifier) = Grid(size, modifier) { u ->
    contactShadow(u, 52f, 97f, 30f, 3.5f, 0.26f)
    rotate(6f, pivot = at(u, 50f, 50f)) {
        val body = roundRect(u, 22f, 4f, 56f, 92f, 8f)
        drawRoundRect(Color(0xFF2A1F55), at(u, 24f, 6f), sz(u, 56f, 92f), cr(u, 8f))
        solid(u, body, Color(0xFFB9A6F5), Color(0xFF7B60DE), Color(0xFF4A31B8), at(u, 22f, 4f), at(u, 78f, 96f), at(u, 32f, 12f), 26f)
        listOf(10f, 52f).forEach { y ->
            val drawer = roundRect(u, 28f, y, 44f, 38f, 5f)
            drawPath(drawer, Brush.linearGradient(listOf(Color(0xFF5B43B4), Color(0xFF3B2A86)), start = at(u, 28f, y), end = at(u, 72f, y + 38f)))
            drawLine(Color.White.copy(alpha = 0.35f), at(u, 30f, y + 1f), at(u, 70f, y + 1f), strokeWidth = 1f * u, cap = StrokeCap.Round)
            // Label card and pull handle.
            drawRoundRect(Brush.verticalGradient(listOf(Color.White, Color(0xFFE4DBFF)), startY = (y + 8f) * u, endY = (y + 16f) * u), at(u, 38f, y + 8f), sz(u, 24f, 8f), cr(u, 2.5f))
            drawRoundRect(Color(0x55000000), at(u, 40f, y + 24f), sz(u, 20f, 5f), cr(u, 2.5f))
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFE4DBFF), VioletLight), startY = (y + 22f) * u, endY = (y + 26f) * u), at(u, 40f, y + 22f), sz(u, 20f, 4.5f), cr(u, 2.25f))
        }
    }
}

/** A Material icon on a tinted tile: the art for settings and data rows. */
@Composable
fun IconTile(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    Box(modifier.size(size).clip(shape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}
