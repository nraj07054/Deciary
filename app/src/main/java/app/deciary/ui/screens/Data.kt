package app.deciary.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import app.deciary.ui.components.FileCabinetArt
import app.deciary.ui.components.IconTile
import app.deciary.ui.components.LabeledField
import app.deciary.ui.components.TwoToneTitle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.deciary.data.NotABackupException
import app.deciary.data.Prefs
import app.deciary.data.WrongPasswordException
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.TopBar
import app.deciary.ui.daysAgo
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type
import kotlinx.coroutines.launch
import java.time.LocalDate

class DataActions(
    val onBack: () -> Unit,
    val backup: suspend (Uri, String) -> Result<Unit>,
    val restore: suspend (Uri, String) -> Result<Int>,
    val checkPin: (String) -> Boolean,
    val deleteEverything: () -> Unit,
    val pinWaitMs: () -> Long = { 0L },
    /** Called just before the system file picker opens, so returning from it doesn't lock the app. */
    val beforePicker: () -> Unit = {},
)

private sealed interface DataDialog {
    data object None : DataDialog
    data object BackupPassword : DataDialog
    data class RestorePassword(val uri: Uri, val wrong: Boolean = false) : DataDialog
    data object Delete : DataDialog
    data class Message(val title: String, val body: String) : DataDialog
}

@Composable
fun DataScreen(prefs: Prefs, letterCount: Int, a: DataActions) {
    val c = Deciary.colors
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<DataDialog>(DataDialog.None) }
    var busy by remember { mutableStateOf(false) }
    var backupPassword by remember { mutableStateOf("") }
    val stamp = LocalDate.now().toString()

    fun work(block: suspend () -> Unit) {
        busy = true
        scope.launch {
            block()
            busy = false
        }
    }

    fun failed(e: Throwable?) = DataDialog.Message("That didn't work", e?.message ?: "Something went wrong. Your letters on this phone are untouched.")

    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val pw = backupPassword
        backupPassword = ""
        if (uri != null) work {
            val r = a.backup(uri, pw)
            dialog = if (r.isSuccess) {
                DataDialog.Message("Backed up", "$letterCount letters saved and locked with your password. Keep the file and the password somewhere safe.")
            } else {
                failed(r.exceptionOrNull())
            }
        }
    }
    val openBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) dialog = DataDialog.RestorePassword(uri)
    }
    Column(Modifier.fillMaxSize().background(c.bg)) {
        TopBar(onBack = a.onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TwoToneTitle("Your", "data")
                Text("Everything lives on this phone. Keep a copy safe.", style = Type.body.copy(fontSize = 14.sp), color = c.inkQuiet)
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val shape = RoundedCornerShape(28.dp)
                Column(Modifier.fillMaxWidth().shadow(14.dp, shape, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(shape).background(c.surface)) {
                    Box(Modifier.fillMaxWidth().height(118.dp).background(c.tint).padding(horizontal = 20.dp)) {
                        FileCabinetArt(104.dp, Modifier.align(Alignment.TopEnd).padding(top = 8.dp))
                        Column(Modifier.align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Kicker(if (busy) "Working" else "Last backup", color = if (c.isDark) c.accentText else Color(0xFF4A3A8C))
                            Text(
                                when {
                                    busy -> "One moment"
                                    prefs.lastBackupAt == 0L -> "Never"
                                    else -> daysAgo(prefs.lastBackupAt).replaceFirstChar { it.uppercase() }
                                },
                                style = Type.display.copy(fontSize = 30.sp, lineHeight = 32.sp), color = c.ink,
                            )
                        }
                    }
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            "A backup is one file, locked with a password you choose. Save it to Downloads, a USB stick or your laptop.",
                            style = Type.body.copy(fontSize = 14.sp, lineHeight = 20.sp), color = c.inkQuiet,
                        )
                        if (busy) {
                            LinearProgressIndicator(
                                Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                color = c.accent, trackColor = Color(0xFFEEEAF7),
                            )
                        }
                        PrimaryButton("Back up now", onClick = { dialog = DataDialog.BackupPassword }, enabled = !busy && letterCount > 0)
                    }
                }

                val list = RoundedCornerShape(22.dp)
                Column(Modifier.fillMaxWidth().shadow(10.dp, list, ambientColor = Color(0x731D1A33), spotColor = Color(0x731D1A33)).clip(list).background(c.surface).padding(horizontal = 14.dp)) {
                    ActionRow(Icons.Rounded.Download, Color(0xFFE8F1FD), Color(0xFF2F6FD0), "Restore from a backup", "Needs the backup password") {
                        a.beforePicker()
                        openBackup.launch(arrayOf("*/*"))
                    }
                }

                Spacer(Modifier.height(24.dp))
                Box(
                    Modifier.fillMaxWidth().clip(list).background(c.surface).border(1.5.dp, if (c.isDark) c.danger.copy(alpha = 0.5f) else Color(0xFFF2C9D8), list).padding(horizontal = 14.dp),
                ) {
                    ActionRow(
                        Icons.Rounded.DeleteOutline, Color(0xFFFDF0F5), c.danger, "Delete everything",
                        if (prefs.lockOn) "Needs your PIN and typing DELETE. Can't be undone." else "Needs typing DELETE. Can't be undone.",
                        danger = true,
                    ) { dialog = DataDialog.Delete }
                }
            }
        }
    }

    when (val d = dialog) {
        DataDialog.None -> Unit
        is DataDialog.Message -> AlertDialog(
            onDismissRequest = { dialog = DataDialog.None },
            title = { Text(d.title, style = Type.heading) },
            text = { Text(d.body, style = Type.body) },
            confirmButton = { TextButton(onClick = { dialog = DataDialog.None }) { Text("OK") } },
        )
        DataDialog.BackupPassword -> PasswordDialog(
            title = "Choose a backup password",
            body = "You'll need it to restore. Nobody can recover it for you.",
            confirmField = true,
            error = null,
            onDismiss = { dialog = DataDialog.None },
        ) { pw ->
            backupPassword = pw
            dialog = DataDialog.None
            a.beforePicker()
            createBackup.launch("deciary-backup-$stamp.deciary")
        }
        is DataDialog.RestorePassword -> RestoreSheet(
            uri = d.uri,
            wrong = d.wrong,
            onChange = { dialog = DataDialog.None; a.beforePicker(); openBackup.launch(arrayOf("*/*")) },
            onDismiss = { dialog = DataDialog.None },
        ) { pw ->
            dialog = DataDialog.None
            work {
                val r = a.restore(d.uri, pw)
                val e = r.exceptionOrNull()
                dialog = when {
                    r.isSuccess -> DataDialog.Message("Restored", "${r.getOrNull()} letters read from the backup.")
                    e is WrongPasswordException -> DataDialog.RestorePassword(d.uri, wrong = true)
                    e is NotABackupException -> DataDialog.Message("Not a backup", "That file isn't a Deciary backup. Choose the .deciary file you saved.")
                    else -> failed(e)
                }
            }
        }
        DataDialog.Delete -> DeleteDialog(
            needsPin = prefs.lockOn,
            checkPin = a.checkPin,
            pinWaitMs = a.pinWaitMs,
            onDismiss = { dialog = DataDialog.None },
            onDelete = { dialog = DataDialog.None; a.deleteEverything() },
        )
    }
}

@Composable
private fun ActionRow(icon: ImageVector, tile: Color, tint: Color, label: String, sub: String, danger: Boolean = false, onClick: () -> Unit) {
    val c = Deciary.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, if (c.isDark) c.tint else tile, if (c.isDark) (if (danger) c.danger else c.accentText) else tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = Type.body.copy(fontSize = if (danger) 17.sp else 15.sp, fontWeight = if (danger) FontWeight.Bold else FontWeight.SemiBold), color = if (danger) c.danger else c.ink)
            Text(sub, style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet)
        }
    }
}

/** Restoring, as a bottom sheet: the chosen file, its password, and a plain error if it's wrong. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestoreSheet(uri: android.net.Uri, wrong: Boolean, onChange: () -> Unit, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val c = Deciary.colors
    val context = LocalContext.current
    var pw by remember { mutableStateOf("") }
    val name = remember(uri) {
        runCatching {
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null }
        }.getOrNull()?.substringBeforeLast('.') ?: "Your backup"
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(40.dp, 4.dp).clip(CircleShape).background(c.line)) },
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Restore from a backup", style = Type.title, color = c.ink)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.bg).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(Icons.Rounded.Download, if (c.isDark) c.tint else Color(0xFFE8F1FD), if (c.isDark) c.accentText else Color(0xFF2F6FD0), size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(name, style = Type.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Letters already here stay; missing ones are added", style = Type.small.copy(fontSize = 12.sp), color = c.inkQuiet)
                }
                Text("Change", style = Type.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = c.accentText, modifier = Modifier.clickable(role = Role.Button, onClick = onChange).padding(8.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField(
                    "Backup password", pw, { pw = it }, error = wrong,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, capitalization = KeyboardCapitalization.None),
                )
                if (wrong) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = Color(0xFFA93226), modifier = Modifier.size(16.dp).padding(top = 1.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("That password doesn't open this backup. Check it and try again.", style = Type.small.copy(lineHeight = 19.sp), color = Color(0xFFA93226))
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PrimaryButton(if (wrong) "Try again" else "Restore", onClick = { onConfirm(pw) }, enabled = pw.isNotEmpty())
                Box(Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancel", style = Type.body.copy(fontWeight = FontWeight.Bold), color = c.ink)
                }
            }
        }
    }
}

@Composable
private fun PasswordDialog(
    title: String,
    body: String,
    confirmField: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var pw by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    val mismatch = confirmField && again.isNotEmpty() && again != pw
    val ok = pw.length >= 6 && (!confirmField || again == pw)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Type.heading) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(body, style = Type.body)
                if (error != null) Text(error, style = Type.body, color = Deciary.colors.danger)
                OutlinedTextField(
                    pw, { pw = it }, label = { Text("Password") }, singleLine = true,
                    supportingText = if (confirmField) ({ Text("At least 6 characters") }) else null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, capitalization = KeyboardCapitalization.None),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (confirmField) {
                    OutlinedTextField(
                        again, { again = it }, label = { Text("Same password again") }, singleLine = true, isError = mismatch,
                        supportingText = if (mismatch) ({ Text("These don't match yet.") }) else null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(pw) }, enabled = ok) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeleteDialog(needsPin: Boolean, checkPin: (String) -> Boolean, pinWaitMs: () -> Long, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    val typedOk = typed.trim() == "DELETE"
    val field: @Composable () -> Unit = {
        OutlinedTextField(
            typed, { typed = it }, label = { Text("Type DELETE") }, singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    val body = "Every letter and setting on this phone will be erased. Backups you saved elsewhere aren't touched. This can't be undone."
    if (needsPin) {
        PinConfirmDialog(
            title = "Delete everything?",
            body = body,
            confirm = "Delete everything",
            onDismiss = onDismiss,
            onPin = { pin -> checkPin(pin).also { if (it && typedOk) onDelete() } },
            extra = field,
            canConfirm = typedOk,
            pinWaitMs = pinWaitMs,
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete everything?", style = Type.heading) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(body, style = Type.body); field() } },
            confirmButton = {
                TextButton(onClick = onDelete, enabled = typedOk) { Text("Delete everything", color = Deciary.colors.danger) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        )
    }
}

