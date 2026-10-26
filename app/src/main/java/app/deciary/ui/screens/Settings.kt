package app.deciary.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.deciary.Biometrics
import app.deciary.BuildConfigInfo
import app.deciary.R
import app.deciary.data.Prefs
import app.deciary.data.ThemeChoice
import app.deciary.ui.components.Art
import app.deciary.ui.components.Card
import app.deciary.ui.components.CoverSwatch
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.TopBar
import app.deciary.ui.daysAgo
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

class SettingsActions(
    val onBack: () -> Unit,
    val onCover: () -> Unit,
    val onData: () -> Unit,
    val onLockOn: () -> Unit,
    val onLockOff: (pin: String) -> Boolean,
    val onBiometric: (Boolean) -> Unit,
    val onReminders: (Boolean) -> Unit,
    val onReminderHour: (Int) -> Unit,
    val onReviewMonths: (Int) -> Unit,
    val onTheme: (ThemeChoice) -> Unit,
)

private enum class Dialog { None, LockOff, Hour, Months, Theme, Faq }

private val reviewChoices = listOf(1 to "1 month", 3 to "3 months", 6 to "6 months", 12 to "1 year")

private fun hourLabel(h: Int) = "%d:00".format(h)

@Composable
fun SettingsScreen(prefs: Prefs, a: SettingsActions) {
    val c = Deciary.colors
    val context = LocalContext.current
    var dialog by remember { mutableStateOf(Dialog.None) }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        TopBar(title = "Settings", onBack = a.onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(onClick = a.onCover, padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoverSwatch(prefs.leather, Modifier.width(64.dp).height(48.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(prefs.coverName, style = Type.heading, color = c.ink)
                        Text("${prefs.leather.label} leather · tap to change", style = Type.small, color = c.inkQuiet)
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.inkQuiet)
                }
            }

            Card(color = Color(0xFF1D1A33)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Kicker("Offline by design", color = Color(0xFFC9B8FA))
                        Spacer(Modifier.height(4.dp))
                        Text("0 bytes sent", style = Type.title, color = Color(0xFFEEEAF8))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Deciary has no internet permission, so Android itself stops it from sending anything.",
                            style = Type.small, color = Color(0xFFB3ADCC),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Art(R.drawable.art_shield, 56.dp)
                }
            }

            Group("Privacy") {
                ToggleRow(
                    R.drawable.art_locked_with_key, Color(0xFFFDF0F5), "App lock",
                    if (prefs.lockOn) (if (prefs.biometric) "PIN and fingerprint" else "PIN") else "Off",
                    prefs.lockOn,
                ) { on -> if (on) a.onLockOn() else dialog = Dialog.LockOff }
                if (prefs.lockOn && Biometrics.available(context)) {
                    Divider()
                    ToggleRow(R.drawable.art_eye, Color(0xFFEDE7FD), "Fingerprint", if (prefs.biometric) "On" else "Off", prefs.biometric, a.onBiometric)
                }
                Divider()
                LinkRow(
                    R.drawable.art_card_file_box, Color(0xFFEDE7FD), "Your data",
                    if (prefs.lastBackupAt == 0L) "Not backed up yet" else "Backed up ${daysAgo(prefs.lastBackupAt)}",
                    a.onData,
                )
            }

            Group("Letters") {
                ToggleRow(
                    R.drawable.art_bell, Color(0xFFFFF4DE), "Reminders",
                    if (prefs.reminders) "On the day, ${hourLabel(prefs.reminderHour)}" else "Off",
                    prefs.reminders, a.onReminders,
                )
                if (prefs.reminders) {
                    Divider()
                    LinkRow(R.drawable.art_hourglass_not_done, Color(0xFFFFF4DE), "Reminder time", hourLabel(prefs.reminderHour)) { dialog = Dialog.Hour }
                }
                Divider()
                LinkRow(
                    R.drawable.art_spiral_calendar, Color(0xFFE8F1FD), "Default review date",
                    "${reviewChoices.firstOrNull { it.first == prefs.reviewMonths }?.second ?: "${prefs.reviewMonths} months"} after sealing",
                ) { dialog = Dialog.Months }
            }

            Group("Look") {
                LinkRow(R.drawable.art_artist_palette, Color(0xFFFFF4DE), "Theme", prefs.theme.label) { dialog = Dialog.Theme }
            }

            Group("Help") {
                LinkRow(R.drawable.art_red_question_mark, Color(0xFFFDF0F5), "Questions people ask", "Where is my data? Lost phone?") { dialog = Dialog.Faq }
            }

            Text(
                "Deciary ${BuildConfigInfo.versionName(context)}",
                style = Type.small, color = c.inkQuiet,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp),
            )
        }
    }

    when (dialog) {
        Dialog.None -> Unit
        Dialog.LockOff -> PinConfirmDialog(
            title = "Turn off app lock?",
            body = "Anyone holding your phone could open your diary. Enter your PIN to turn it off.",
            confirm = "Turn off",
            onDismiss = { dialog = Dialog.None },
            onPin = { pin -> a.onLockOff(pin).also { if (it) dialog = Dialog.None } },
        )
        Dialog.Hour -> OptionsDialog(
            "Remind me at", (6..22).map { it to hourLabel(it) }, prefs.reminderHour,
            onDismiss = { dialog = Dialog.None }, onPick = { a.onReminderHour(it); dialog = Dialog.None },
        )
        Dialog.Months -> OptionsDialog(
            "Open letters after", reviewChoices, prefs.reviewMonths,
            onDismiss = { dialog = Dialog.None }, onPick = { a.onReviewMonths(it); dialog = Dialog.None },
        )
        Dialog.Theme -> OptionsDialog(
            "Theme", ThemeChoice.entries.map { it to it.label }, prefs.theme,
            onDismiss = { dialog = Dialog.None }, onPick = { a.onTheme(it); dialog = Dialog.None },
        )
        Dialog.Faq -> AlertDialog(
            onDismissRequest = { dialog = Dialog.None },
            title = { Text("Questions people ask", style = Type.heading) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Faq("Where is my data?", "Only in this app, on this phone. There's no account and no cloud. Deciary can't connect to the internet.")
                    Faq("What if I lose my phone?", "Your letters go with it, unless you've made a backup. Back up from Your data and keep the file somewhere safe, like a laptop or a USB stick.")
                    Faq("What if I forget my PIN?", "There's no way to reset it without losing your letters, because nobody else holds a key. If you have a backup, you can reinstall the app and restore it.")
                    Faq("Why can't I edit a sealed letter?", "So future you sees what you really thought. Small typos can be fixed for 10 minutes after sealing.")
                }
            },
            confirmButton = { TextButton(onClick = { dialog = Dialog.None }) { Text("Got it") } },
        )
    }
}

@Composable
private fun Faq(q: String, a: String) {
    Column {
        Text(q, style = Type.bodyStrong, color = Deciary.colors.ink)
        Text(a, style = Type.body, color = Deciary.colors.inkQuiet)
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Column {
        Kicker(title, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp))
        Card(padding = 0.dp) { content() }
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = Deciary.colors.line, modifier = Modifier.padding(start = 72.dp))

@Composable
private fun Tile(@DrawableRes art: Int, tile: Color) {
    val bg = if (Deciary.colors.isDark) Deciary.colors.tint else tile
    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(bg), contentAlignment = Alignment.Center) { Art(art, 26.dp) }
}

@Composable
private fun RowText(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = Type.bodyStrong, color = Deciary.colors.ink)
        Text(value, style = Type.small, color = Deciary.colors.inkQuiet)
    }
}

@Composable
private fun ToggleRow(@DrawableRes art: Int, tile: Color, label: String, value: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = Deciary.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).toggleable(checked, role = Role.Switch, onValueChange = onChange).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tile(art, tile)
        Spacer(Modifier.width(16.dp))
        RowText(label, value, Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = c.accent, checkedThumbColor = c.onAccent, uncheckedTrackColor = c.line, uncheckedBorderColor = c.line),
        )
    }
}

@Composable
private fun LinkRow(@DrawableRes art: Int, tile: Color, label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tile(art, tile)
        Spacer(Modifier.width(16.dp))
        RowText(label, value, Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Deciary.colors.inkQuiet)
    }
}

@Composable
fun <T> OptionsDialog(title: String, options: List<Pair<T, String>>, selected: T, onDismiss: () -> Unit, onPick: (T) -> Unit) {
    val c = Deciary.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Type.heading) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(value == selected, role = Role.RadioButton) { onPick(value) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = c.accent))
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = Type.body, color = c.ink)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Asks for the PIN before something sensitive. [onPin] returns whether it matched. */
@Composable
fun PinConfirmDialog(title: String, body: String, confirm: String, onDismiss: () -> Unit, onPin: (String) -> Boolean, extra: @Composable () -> Unit = {}, canConfirm: Boolean = true) {
    var pin by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Type.heading) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(body, style = Type.body)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) { pin = it; wrong = false } },
                    label = { Text("PIN") },
                    singleLine = true,
                    isError = wrong,
                    supportingText = if (wrong) ({ Text("That PIN didn't match.") }) else null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                extra()
            }
        },
        confirmButton = {
            TextButton(onClick = { if (!onPin(pin)) wrong = true }, enabled = pin.length == PIN_LENGTH && canConfirm) {
                Text(confirm, color = Deciary.colors.danger)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
