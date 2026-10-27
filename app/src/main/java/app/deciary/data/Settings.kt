package app.deciary.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class Leather(val label: String, val snap: String) {
    Ink("Ink", "rose-gold snap"),
    Plum("Plum", "silver snap"),
    Tan("Tan", "brass snap"),
}

enum class ThemeChoice(val label: String) {
    System("Match the phone"),
    Light("Light"),
    Dark("Dark"),
}

data class Prefs(
    val onboarded: Boolean = false,
    val ownerName: String = "",
    val leather: Leather = Leather.Ink,
    val hasPin: Boolean = false,
    val biometric: Boolean = false,
    val reminders: Boolean = true,
    val reminderHour: Int = 9,
    val reviewMonths: Int = 3,
    val theme: ThemeChoice = ThemeChoice.System,
    val lastBackupAt: Long = 0L,
) {
    val lockOn: Boolean get() = hasPin
    val coverName: String get() = ownerName.trim().ifEmpty { "My diary" }
}

/** After five wrong PINs, each further miss adds a wait: 30 s, 1 min, 2 min, … up to 15 min. */
object PinLockout {
    const val FREE_TRIES = 5
    const val FIRST_MS = 30_000L
    const val MAX_MS = 15 * 60_000L

    fun delayAfter(fails: Int): Long =
        if (fails < FREE_TRIES) 0L else (FIRST_MS shl (fails - FREE_TRIES).coerceAtMost(10)).coerceAtMost(MAX_MS)
}

class Settings(context: Context) {
    private val sp = context.getSharedPreferences("deciary", Context.MODE_PRIVATE)
    private val _prefs = MutableStateFlow(read())
    val prefs: StateFlow<Prefs> = _prefs.asStateFlow()

    private fun read() = Prefs(
        onboarded = sp.getBoolean("onboarded", false),
        ownerName = sp.getString("ownerName", "") ?: "",
        leather = enumOr(sp.getString("leather", null), Leather.Ink),
        hasPin = sp.contains("pinHash"),
        biometric = sp.getBoolean("biometric", false),
        reminders = sp.getBoolean("reminders", true),
        reminderHour = sp.getInt("reminderHour", 9),
        reviewMonths = sp.getInt("reviewMonths", 3),
        theme = enumOr(sp.getString("theme", null), ThemeChoice.System),
        lastBackupAt = sp.getLong("lastBackupAt", 0L),
    )

    private fun edit(block: android.content.SharedPreferences.Editor.() -> Unit) {
        sp.edit().apply(block).apply()
        _prefs.value = read()
    }

    fun setOnboarded() = edit { putBoolean("onboarded", true) }
    fun setCover(name: String, leather: Leather) = edit {
        putString("ownerName", name.trim())
        putString("leather", leather.name)
    }
    fun setBiometric(on: Boolean) = edit { putBoolean("biometric", on) }
    fun setReminders(on: Boolean) = edit { putBoolean("reminders", on) }
    fun setReminderHour(hour: Int) = edit { putInt("reminderHour", hour) }
    fun setReviewMonths(months: Int) = edit { putInt("reviewMonths", months) }
    fun setTheme(theme: ThemeChoice) = edit { putString("theme", theme.name) }
    fun setLastBackup(at: Long) = edit { putLong("lastBackupAt", at) }

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        edit {
            putString("pinSalt", b64(salt))
            putString("pinHash", b64(hash(pin, salt)))
        }
    }

    fun clearPin() = edit {
        remove("pinSalt")
        remove("pinHash")
        remove("pinFails")
        remove("pinBlockedUntil")
        putBoolean("biometric", false)
    }

    /**
     * Checks a PIN. While a wait is running it returns false without checking, so guessing all
     * 10,000 PINs takes days rather than minutes. A right PIN clears the count of misses.
     */
    fun checkPin(pin: String, now: Long = System.currentTimeMillis()): Boolean {
        if (pinWaitMs(now) > 0) return false
        val salt = sp.getString("pinSalt", null) ?: return false
        val expected = sp.getString("pinHash", null) ?: return false
        val ok = java.security.MessageDigest.isEqual(
            b64(hash(pin, Base64.getDecoder().decode(salt))).toByteArray(),
            expected.toByteArray(),
        )
        if (ok) {
            sp.edit().remove("pinFails").remove("pinBlockedUntil").apply()
        } else {
            val fails = sp.getInt("pinFails", 0) + 1
            sp.edit().putInt("pinFails", fails).putLong("pinBlockedUntil", now + PinLockout.delayAfter(fails)).apply()
        }
        return ok
    }

    /** How long until another PIN can be tried; 0 when it can be tried now. */
    fun pinWaitMs(now: Long = System.currentTimeMillis()): Long {
        val until = sp.getLong("pinBlockedUntil", 0L)
        // Capped, so setting the clock back can't stretch a wait beyond the longest one.
        return (until - now).coerceIn(0L, PinLockout.MAX_MS)
    }

    /** Wipes every setting, used by "Delete everything". */
    fun clearAll() {
        sp.edit().clear().apply()
        _prefs.value = read()
    }

    private fun hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 60_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun b64(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)

    private inline fun <reified T : Enum<T>> enumOr(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default
}
