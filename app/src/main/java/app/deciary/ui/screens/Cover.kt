package app.deciary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.deciary.data.Leather
import app.deciary.ui.components.CoverSwatch
import app.deciary.ui.components.DiaryCover
import app.deciary.ui.components.Field
import app.deciary.ui.components.Kicker
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.TopBar
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

/** Choose a leather and put a name on the cover. Used at the end of onboarding and from Settings. */
@Composable
fun CoverScreen(
    initialName: String,
    initialLeather: Leather,
    firstRun: Boolean,
    onBack: (() -> Unit)?,
    onSave: (String, Leather) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var leather by rememberSaveable { mutableStateOf(initialLeather) }
    val c = Deciary.colors
    Column(Modifier.fillMaxSize().background(c.bg).imePadding().navigationBarsPadding()) {
        TopBar(onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            if (firstRun) Kicker("Last step", color = c.accentText)
            Spacer(Modifier.height(6.dp))
            TwoToneTitle("Make it", "yours")
            Spacer(Modifier.height(8.dp))
            Text("Choose a cover and put your name on it.", style = Type.body, color = c.inkQuiet)
            Spacer(Modifier.height(22.dp))
            DiaryCover(name.trim().ifEmpty { "My diary" }, leather)
            Spacer(Modifier.height(26.dp))
            Kicker("Cover")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Leather.entries.forEach { l ->
                    val on = l == leather
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(if (on) 2.5.dp else 1.dp, if (on) c.ink else c.line, RoundedCornerShape(18.dp))
                            .background(c.surface)
                            .semantics { selected = on }
                            .clickable(role = Role.RadioButton) { leather = l }
                            .padding(10.dp),
                    ) {
                        CoverSwatch(l, Modifier.fillMaxWidth().height(54.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(l.label, style = Type.bodyStrong, color = c.ink)
                        Text(l.snap, style = Type.small, color = c.inkQuiet)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Field(
                value = name,
                onValueChange = { if (it.length <= 24) name = it },
                label = "Name on the cover · optional",
                singleLine = true,
            )
            Spacer(Modifier.height(24.dp))
        }
        PrimaryButton(
            if (firstRun) "Open my diary" else "Save cover",
            onClick = { onSave(name, leather) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

