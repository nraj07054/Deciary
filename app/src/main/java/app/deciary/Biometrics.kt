package app.deciary

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object Biometrics {
    private const val AUTH = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    fun available(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTH) == BiometricManager.BIOMETRIC_SUCCESS

    fun prompt(context: Context, onSuccess: () -> Unit) {
        val activity = context.findActivity() ?: return
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Open your diary")
            .setSubtitle("Only you can open this")
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(AUTH)
            .build()
        prompt.authenticate(info)
    }

    private fun Context.findActivity(): FragmentActivity? = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
