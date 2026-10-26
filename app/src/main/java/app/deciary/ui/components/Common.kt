package app.deciary.ui.components

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
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
fun TwoToneTitle(a: String, b: String, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = Type.display) {
    val accent = Deciary.colors.accentText
    Text(
        buildAnnotatedString {
            append(a)
            append(" ")
            withStyle(SpanStyle(color = accent, fontStyle = FontStyle.Italic)) { append(b) }
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
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp).heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(backIcon, contentDescription = backLabel, tint = Deciary.colors.ink) }
        } else {
            Spacer(Modifier.width(12.dp))
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
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
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
        Text(text, style = Type.bodyStrong, color = color)
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

/** The pill-shaped three-way picker used on the review screen. */
@Composable
fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = Deciary.colors
    Row(
        modifier.fillMaxWidth().clip(CircleShape).background(c.tint).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { o ->
            val on = o == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .then(if (on) Modifier.shadow(3.dp, CircleShape) else Modifier)
                    .clip(CircleShape)
                    .background(if (on) c.surface else Color.Transparent)
                    .semantics { this.selected = on }
                    .clickable(role = Role.RadioButton) { onSelect(o) },
                contentAlignment = Alignment.Center,
            ) {
                Text(label(o), style = Type.bodyStrong, color = if (on) c.ink else c.inkQuiet)
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

/** A wax seal with the owner's initial. */
@Composable
fun WaxSeal(initial: String, size: Dp, modifier: Modifier = Modifier) {
    val c = Deciary.colors
    Box(
        modifier
            .size(size)
            .shadow(8.dp, CircleShape, spotColor = c.wax)
            .clip(CircleShape)
            .background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(c.wax.copy(alpha = 0.85f), c.wax, Color(0xFF8E2452)))),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(size * 0.72f).border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape))
        Text(initial, style = Type.heading.copy(fontSize = Type.heading.fontSize * (size.value / 56f)), color = Color.White)
    }
}

fun initialOf(name: String): String = name.trim().firstOrNull()?.uppercase() ?: "D"
