package app.deciary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.deciary.R
import app.deciary.data.Mood
import app.deciary.data.ThemeChoice

@Immutable
data class DeciaryColors(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val inkQuiet: Color,
    val line: Color,
    val tint: Color,
    val accent: Color,
    val onAccent: Color,
    val accentText: Color,
    val wax: Color,
    val danger: Color,
    val isDark: Boolean,
)

val LightColors = DeciaryColors(
    bg = Color(0xFFF5F2FB),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF1D1A33),
    inkQuiet = Color(0xFF5E587A),
    line = Color(0xFFE4DEF2),
    tint = Color(0xFFEDE7FD),
    accent = Color(0xFF6E4FE6),
    onAccent = Color(0xFFFFFFFF),
    accentText = Color(0xFF5A3DD0),
    wax = Color(0xFFC23C72),
    danger = Color(0xFFC0392B),
    isDark = false,
)

val DarkColors = DeciaryColors(
    bg = Color(0xFF15122A),
    surface = Color(0xFF221E3D),
    ink = Color(0xFFEEEAF8),
    inkQuiet = Color(0xFFB3ADCC),
    line = Color(0xFF35305A),
    tint = Color(0xFF2C2547),
    accent = Color(0xFFA48BFF),
    onAccent = Color(0xFF15122A),
    accentText = Color(0xFFC9B8FA),
    wax = Color(0xFFE07AA3),
    danger = Color(0xFFE06A5C),
    isDark = true,
)

fun Mood.color(): Color = when (this) {
    Mood.Calm -> Color(0xFF2F9C95)
    Mood.Rushed -> Color(0xFFE5674F)
    Mood.Anxious -> Color(0xFF8E7CC3)
    Mood.Excited -> Color(0xFFD9578C)
    Mood.Tired -> Color(0xFF7A8699)
}

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: Int) = Font(
    R.font.fraunces,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.Setting("opsz", 72f)),
)

@OptIn(ExperimentalTextApi::class)
private fun noto(weight: Int) = Font(
    R.font.noto_sans,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Fraunces = FontFamily(fraunces(400), fraunces(500), fraunces(600), fraunces(700), fraunces(800))
val NotoSans = FontFamily(noto(400), noto(500), noto(600), noto(700), noto(800))

private val LocalColors = staticCompositionLocalOf { LightColors }

object Deciary {
    val colors: DeciaryColors
        @Composable @ReadOnlyComposable get() = LocalColors.current
}

object Type {
    val display = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 37.sp, letterSpacing = (-0.36).sp)
    val title = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 30.sp)
    val heading = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 25.sp)
    val letter = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp)
    val body = TextStyle(fontFamily = NotoSans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp)
    val bodyStrong = body.copy(fontWeight = FontWeight.SemiBold)
    /** The intro line under a page title. */
    val lead = TextStyle(fontFamily = NotoSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val small = TextStyle(fontFamily = NotoSans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)
    val label = TextStyle(fontFamily = NotoSans, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.2.sp)
    val button = TextStyle(fontFamily = NotoSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp)
}

@Composable
fun DeciaryTheme(choice: ThemeChoice = ThemeChoice.System, content: @Composable () -> Unit) {
    val dark = when (choice) {
        ThemeChoice.System -> isSystemInDarkTheme()
        ThemeChoice.Light -> false
        ThemeChoice.Dark -> true
    }
    val c = if (dark) DarkColors else LightColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.accent, onPrimary = c.onAccent, background = c.bg, onBackground = c.ink,
            surface = c.surface, onSurface = c.ink, onSurfaceVariant = c.inkQuiet, outline = c.line,
            surfaceContainerHigh = c.surface, surfaceContainerHighest = c.tint, error = c.danger,
        )
    } else {
        lightColorScheme(
            primary = c.accent, onPrimary = c.onAccent, background = c.bg, onBackground = c.ink,
            surface = c.surface, onSurface = c.ink, onSurfaceVariant = c.inkQuiet, outline = c.line,
            surfaceContainerHigh = c.surface, surfaceContainerHighest = c.tint, error = c.danger,
        )
    }
    val typography = Typography(
        bodyLarge = Type.body, bodyMedium = Type.body, bodySmall = Type.small,
        labelLarge = Type.button, titleLarge = Type.heading, headlineSmall = Type.heading,
    )
    CompositionLocalProvider(LocalColors provides c) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
