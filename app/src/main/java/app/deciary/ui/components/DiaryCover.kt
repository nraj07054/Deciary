package app.deciary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.deciary.data.Leather
import app.deciary.ui.theme.Type

/**
 * The diary cover: the owner's name pressed into the leather and a snap.
 * When [onSnap] is set, tapping the snap locks the diary.
 */
@Composable
fun DiaryCover(
    name: String,
    leather: Leather,
    modifier: Modifier = Modifier,
    onSnap: (() -> Unit)? = null,
) {
    val p = leather.palette()
    LeatherSurface(leather, modifier.fillMaxWidth().height(190.dp)) {
        Column(
            Modifier.align(Alignment.CenterStart).padding(start = 30.dp, end = 80.dp),
        ) {
            // Pressed-in look: a dark shadow above and a light edge below the letters.
            Text(
                name,
                style = Type.display.copy(
                    fontSize = 32.sp,
                    shadow = Shadow(p.lo.copy(alpha = 0.8f), offset = Offset(0f, -2f), blurRadius = 1f),
                ),
                color = p.ink.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text("Only you can open this", style = Type.small, color = p.ink.copy(alpha = 0.72f))
        }
        val snapModifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 22.dp)
            .size(48.dp)
            .clip(CircleShape)
        Box(
            if (onSnap != null) {
                snapModifier.semantics { contentDescription = "Lock the diary" }.clickable(onClick = onSnap)
            } else {
                snapModifier
            },
            contentAlignment = Alignment.Center,
        ) {
            Snap(leather, Modifier.size(34.dp))
        }
    }
}

@Composable
fun CoverSwatch(leather: Leather, modifier: Modifier = Modifier) {
    LeatherSurface(leather, modifier, corner = 14.dp, stitchInset = 5.dp) {
        Snap(leather, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(14.dp))
    }
}

