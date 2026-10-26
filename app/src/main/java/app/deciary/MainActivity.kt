package app.deciary

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.deciary.data.ThemeChoice
import app.deciary.ui.DeciaryApp
import app.deciary.ui.theme.DeciaryTheme

/** A FragmentActivity so the fingerprint prompt can attach to it. */
class MainActivity : FragmentActivity() {
    private val vm: AppViewModel by viewModels()

    /** Coming back within this window (e.g. from the file picker) doesn't re-lock. */
    private val lockGraceMs = 15_000L
    private var stoppedAt = 0L

    private val lockObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            stoppedAt = SystemClock.elapsedRealtime()
        }

        override fun onStart(owner: LifecycleOwner) {
            if (stoppedAt != 0L && SystemClock.elapsedRealtime() - stoppedAt > lockGraceMs) vm.lock()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lockObserver)

        setContent {
            val prefs by vm.prefs.collectAsStateWithLifecycle()
            val dark = when (prefs.theme) {
                ThemeChoice.System -> isSystemInDarkTheme()
                ThemeChoice.Light -> false
                ThemeChoice.Dark -> true
            }
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                }
                enableEdgeToEdge(style, style)
            }
            // With the lock on, hide the diary from the recent-apps preview and screenshots.
            DisposableEffect(prefs.lockOn) {
                if (prefs.lockOn) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                onDispose { }
            }
            DeciaryTheme(prefs.theme) {
                DeciaryApp(vm)
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lockObserver)
        super.onDestroy()
    }
}
