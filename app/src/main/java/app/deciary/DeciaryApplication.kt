package app.deciary

import android.app.Application
import app.deciary.data.LetterStore
import app.deciary.data.Settings
import app.deciary.reminders.Reminders

class DeciaryApplication : Application() {
    lateinit var letters: LetterStore
        private set
    lateinit var settings: Settings
        private set

    override fun onCreate() {
        super.onCreate()
        letters = LetterStore(this)
        settings = Settings(this)
        Reminders.ensureChannel(this)
        val prefs = settings.prefs.value
        if (prefs.onboarded && prefs.reminders) Reminders.schedule(this, prefs.reminderHour)
    }
}
