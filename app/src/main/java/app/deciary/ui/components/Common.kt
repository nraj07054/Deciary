package app.deciary.ui.components

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.deciary.R
import app.deciary.data.Mood
import app.deciary.data.Verdict
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type
import app.deciary.ui.theme.color

@DrawableRes
fun Verdict.art(): Int = when (this) {
    Verdict.EarnedIt -> R.drawable.art_1st_place_medal
    Verdict.BadLuck -> R.drawable.art_cloud_with_rain
    Verdict.LuckyBreak -> R.drawable.art_four_leaf_clover
    Verdict.Lesson -> R.drawable.art_light_bulb
}

@Composable
fun Art(@DrawableRes res: Int, size: Dp, modifier: Modifier = Modifier) {
    Image(painterResource(res), contentDescription = null, modifier = modifier.size(size))
}

/** A heading in two parts; the second part is italic in the accent colour, as in the design. */
@Composable
fun TwoToneTitle(
    a: String,
    b: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = Type.display,
    accent: Color = Deciary.colors.accentText,
) {
    Text(
        buildAnnotatedString {
            append(a)
            append(" ")
            withStyle(SpanStyle(color = accent, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium)) { append(b) }
        },
        style = style,
        color = Deciary.colors.ink,
        modifier = modifier,
    )
}

@Composable
fun TopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    backLabel: String = "Back",
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 8.dp, top = 8.dp).heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(backIcon, contentDescription = backLabel, tint = Deciary.colors.ink) }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        if (title != null) {
            Text(title, style = Type.heading, color = Deciary.colors.ink, modifier = Modifier.padding(start = 4.dp))
        }
        Spacer(Modifier.weight(1f))
        actions()
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    color: Color = Deciary.colors.accent,
    contentColor: Color = Deciary.colors.onAccent,
    trailingIcon: ImageVector? = null,
) {
    // The design's soft glow under the main button, in the button's own colour.
    val glow = if (enabled && !Deciary.colors.isDark) Modifier.shadow(14.dp, CircleShape, ambientColor = color, spotColor = color) else Modifier
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp).then(glow),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color, contentColor = contentColor,
            disabledContainerColor = Deciary.colors.line, disabledContentColor = Deciary.colors.inkQuiet,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = Type.button)
        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = CircleShape,
        border = BorderStroke(1.5.dp, Deciary.colors.ink),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Deciary.colors.ink),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = Type.button)
    }
}

@Composable
fun QuietButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Deciary.colors.accentText) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp)) {
        Text(text, style = Type.bodyStrong.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline), color = color)
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    color: Color = Deciary.colors.surface,
    corner: Dp = 22.dp,
    padding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    var m = modifier
        .shadow(if (Deciary.colors.isDark) 0.dp else 10.dp, shape, ambientColor = Color(0x331D1A33), spotColor = Color(0x331D1A33))
        .clip(shape)
        .background(color)
    if (Deciary.colors.isDark) m = m.border(1.dp, Deciary.colors.line, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(padding), content = content)
}

@Composable
fun Kicker(text: String, modifier: Modifier = Modifier, color: Color = Deciary.colors.inkQuiet) {
    Text(text.uppercase(), style = Type.label, color = color, modifier = modifier)
}

@Composable
fun MoodTag(mood: Mood, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(mood.color()))
        Spacer(Modifier.width(6.dp))
        Text(mood.label, style = Type.small, color = Deciary.colors.inkQuiet)
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, dot: Color? = null) {
    val c = Deciary.colors
    Row(
        modifier
            .heightIn(min = 44.dp)
            .clip(CircleShape)
            .background(if (selected) c.ink else c.surface)
            .border(1.dp, if (selected) c.ink else c.line, CircleShape)
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = Type.bodyStrong, color = if (selected) c.bg else c.ink)
    }
}

/** The three-way picker used on the review screen: a soft tray with the chosen answer raised in white. */
@Composable
fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = Deciary.colors
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (c.isDark) c.tint else Color(0xFFEEEAF7)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { o ->
            val on = o == selected
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .then(if (on) Modifier.shadow(3.dp, shape, ambientColor = Color(0x261D1A33), spotColor = Color(0x261D1A33)) else Modifier)
                    .clip(shape)
                    .background(if (on) c.surface else Color.Transparent)
                    .semantics { this.selected = on }
                    .clickable(role = Role.RadioButton) { onSelect(o) },
                contentAlignment = Alignment.Center,
            ) {
                Text(label(o), style = Type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = if (on) c.ink else c.inkQuiet)
            }
        }
    }
}

@Composable
fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    minLines: Int = 1,
    singleLine: Boolean = false,
    letterStyle: Boolean = false,
) {
    val c = Deciary.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder, color = c.inkQuiet.copy(alpha = 0.7f)) }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        singleLine = singleLine,
        textStyle = if (letterStyle) Type.letter.copy(color = c.ink) else Type.body.copy(color = c.ink),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.accent, unfocusedBorderColor = c.line,
            focusedContainerColor = c.surface, unfocusedContainerColor = c.surface,
            focusedLabelColor = c.accentText, unfocusedLabelColor = c.inkQuiet, cursorColor = c.accent,
        ),
    )
}

/**
 * A text field in the design's style: a bold label above (with an optional quiet suffix such as
 * "· optional") and a plain white box with a soft 1.5 dp outline that turns violet on focus.
 */
@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    placeholder: String? = null,
    textStyle: androidx.compose.ui.text.TextStyle = Type.body.copy(fontSize = 16.sp),
    minHeight: Dp = 52.dp,
    singleLine: Boolean = true,
    error: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    labelSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fieldColor: Color = Deciary.colors.surface,
) {
    val c = Deciary.colors
    var focused by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    // The box carries the whole label as its name, so screen readers read it once (and tests find it by it).
    val name = if (suffix != null) "$label $suffix" else label
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            buildAnnotatedString {
                append(label)
                if (suffix != null) withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = c.inkQuiet)) { append(" $suffix") }
            },
            style = Type.bodyStrong.copy(fontSize = labelSize, fontWeight = FontWeight.Bold),
            color = c.ink,
            modifier = Modifier.clearAndSetSemantics {},
        )
        val shape = RoundedCornerShape(16.dp)
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = c.ink),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(c.accent),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clip(shape)
                .background(fieldColor)
                .border(1.5.dp, if (error) c.danger else if (focused) c.accent else c.line, shape)
                .onFocusChanged { focused = it.isFocused }
                .semantics { contentDescription = name },
            decorationBox = { inner ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) {
                    if (value.isEmpty() && placeholder != null) Text(placeholder, style = textStyle, color = c.inkQuiet.copy(alpha = 0.75f))
                    inner()
                }
            },
        )
    }
}

/** A wax seal with the owner's initial: a pressed rim, a little depth and a heavy Fraunces letter. */
@Composable
fun WaxSeal(initial: String, size: Dp, modifier: Modifier = Modifier) {
    val c = Deciary.colors
    val wax = Color(0xFFC23C72)
    Box(
        modifier
            .size(size)
            .shadow(size * 0.16f, CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(CircleShape)
            .background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(Color(0xFFD9578C), wax, Color(0xFF9E2C5B)), center = androidx.compose.ui.geometry.Offset.Unspecified))
            .border(size * 0.065f, Color.White.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // A soft shade low inside the rim, as if the wax is domed.
        Box(Modifier.matchParentSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.22f))))
        Text(
            initial,
            style = Type.display.copy(fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { (size * 0.44f).toSp() }, lineHeight = with(androidx.compose.ui.platform.LocalDensity.current) { (size * 0.5f).toSp() }),
            color = Color.White,
        )
    }
}

fun initialOf(name: String): String = name.trim().firstOrNull()?.uppercase() ?: "D"

/** Light status and navigation bar icons while this screen shows, for screens drawn on a dark background. */
@Composable
fun LightBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose { }
        val bars = WindowCompat.getInsetsController(window, view)
        val status = bars.isAppearanceLightStatusBars
        val nav = bars.isAppearanceLightNavigationBars
        bars.isAppearanceLightStatusBars = false
        bars.isAppearanceLightNavigationBars = false
        onDispose {
            bars.isAppearanceLightStatusBars = status
            bars.isAppearanceLightNavigationBars = nav
        }
    }
}
