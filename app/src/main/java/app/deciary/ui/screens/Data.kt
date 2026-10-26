package app.deciary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.CircularProgressIndicator
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
import app.deciary.R
import app.deciary.data.NotABackupException
import app.deciary.data.Prefs
import app.deciary.data.WrongPasswordException
import app.deciary.ui.components.Art
import app.deciary.ui.components.Card
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
    val exportJson: suspend (Uri) -> Result<Unit>,
    val exportCsv: suspend (Uri) -> Result<Unit>,
    val checkPin: (String) -> Boolean,
    val deleteEverything: () -> Unit,
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
    val createJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) work {
            val r = a.exportJson(uri)
            dialog = if (r.isSuccess) DataDialog.Message("Exported", "Saved as JSON. This file isn't locked, so anyone with it can read your letters.") else failed(r.exceptionOrNull())
        }
    }
    val createCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) work {
            val r = a.exportCsv(uri)
            dialog = if (r.isSuccess) DataDialog.Message("Exported", "Saved as CSV. This file isn't locked, so anyone with it can read your letters.") else failed(r.exceptionOrNull())
        }
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        TopBar(title = "Your data", onBack = a.onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Everything lives on this phone. Keep a copy safe.", style = Type.body, color = c.inkQuiet, modifier = Modifier.padding(horizontal = 4.dp))
            Card(color = c.tint) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Kicker(if (busy) "Working" else "Last backup")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when {
                                busy -> "One moment"
                                prefs.lastBackupAt == 0L -> "Never"
                                else -> daysAgo(prefs.lastBackupAt).replaceFirstChar { it.uppercase() }
                            },
                            style = Type.title, color = c.ink,
                        )
                    }
                    if (busy) CircularProgressIndicator(color = c.accent) else Art(R.drawable.art_inbox_tray, 60.dp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "A backup is one file, locked with a password you choose. Save it to Downloads, a USB stick or your laptop.",
                    style = Type.small, color = c.inkQuiet,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Back up now", onClick = { dialog = DataDialog.BackupPassword }, enabled = !busy && letterCount > 0)
            }

            Card(padding = 0.dp) {
                ActionRow(R.drawable.art_outbox_tray, "Restore from a backup", "Needs the backup password") {
                    openBackup.launch(arrayOf("*/*"))
                }
                HorizontalDivider(color = c.line, modifier = Modifier.padding(start = 72.dp))
                ActionRow(R.drawable.art_file_cabinet, "Export as JSON", "For moving to another app") {
                    createJson.launch("deciary-$stamp.json")
                }
                HorizontalDivider(color = c.line, modifier = Modifier.padding(start = 72.dp))
                ActionRow(R.drawable.art_bookmark_tabs, "Export as CSV", "Opens in any spreadsheet") {
                    createCsv.launch("deciary-$stamp.csv")
                }
            }

            Card(padding = 0.dp) {
                ActionRow(
                    R.drawable.art_wastebasket, "Delete everything",
                    if (prefs.lockOn) "Needs your PIN and typing DELETE. Can't be undone." else "Needs typing DELETE. Can't be undone.",
                    danger = true,
                ) { dialog = DataDialog.Delete }
            }
            Spacer(Modifier.height(16.dp))
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
            createBackup.launch("deciary-backup-$stamp.deciary")
        }
        is DataDialog.RestorePassword -> PasswordDialog(
            title = "Restore from a backup",
            body = "Letters already on this phone stay. The backup adds any that are missing.",
            confirmField = false,
            // Restoring is an edge state: plain, calm wording, no playfulness.
            error = if (d.wrong) "That password doesn't open this backup. Your letters on this phone are untouched." else null,
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
            onDismiss = { dialog = DataDialog.None },
            onDelete = { dialog = DataDialog.None; a.deleteEverything() },
        )
    }
}

@Composable
private fun ActionRow(@DrawableRes art: Int, label: String, sub: String, danger: Boolean = false, onClick: () -> Unit) {
    val c = Deciary.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(art, 36.dp)
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = Type.bodyStrong, color = if (danger) c.danger else c.ink)
            Text(sub, style = Type.small, color = c.inkQuiet)
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
private fun DeleteDialog(needsPin: Boolean, checkPin: (String) -> Boolean, onDismiss: () -> Unit, onDelete: () -> Unit) {
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

