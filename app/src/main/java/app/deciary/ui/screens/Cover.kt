package app.deciary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.deciary.data.Leather
import app.deciary.ui.components.CoverSwatch
import app.deciary.ui.components.DiaryCover
import app.deciary.ui.components.LabeledField
import app.deciary.ui.components.PrimaryButton
import app.deciary.ui.components.TwoToneTitle
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Fraunces
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
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().imePadding().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp)) {
        Box(Modifier.fillMaxWidth().height(48.dp)) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(end = 0.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = c.ink)
                }
            }
            if (firstRun) Text("Last step", style = Type.small.copy(fontWeight = FontWeight.Bold), color = c.inkQuiet, modifier = Modifier.align(Alignment.Center))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TwoToneTitle("Make it", "yours")
                Text("Choose a cover and put your name on it.", style = Type.body, color = c.inkQuiet)
            }
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(c.tint).padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 30.dp)) {
                DiaryCover(name.trim().ifEmpty { "My diary" }, leather)
            }
            Spacer(Modifier.height(22.dp))
            Text("Cover", style = Type.bodyStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = c.ink)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Leather.entries.forEach { l ->
                    val on = l == leather
                    val shape = RoundedCornerShape(18.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(c.surface)
                            .border(if (on) 2.5.dp else 1.dp, if (on) c.ink else c.line, shape)
                            .semantics { selected = on }
                            .clickable(role = Role.RadioButton) { leather = l }
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CoverSwatch(l, Modifier.fillMaxWidth().height(60.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(l.label, style = Type.small.copy(fontWeight = FontWeight.Bold), color = c.ink)
                        Text(l.snap, style = Type.small.copy(fontSize = 11.sp, lineHeight = 14.sp), color = c.inkQuiet)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            LabeledField(
                label = "Name on the cover",
                suffix = "· optional",
                value = name,
                onValueChange = { if (it.length <= 24) name = it },
                textStyle = Type.body.copy(fontFamily = Fraunces, fontSize = 18.sp),
            )
            Spacer(Modifier.height(24.dp))
        }
        PrimaryButton(if (firstRun) "Open my diary" else "Save cover", onClick = { onSave(name, leather) })
    }
}
