package app.deciary

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the real app from a fresh install the way a person would: first run, writing and
 * sealing a letter, opening it, the shelf, settings and your data. Saves a screenshot of each
 * screen to /sdcard/Download/deciary-shots so CI can collect them.
 */
@RunWith(AndroidJUnit4::class)
class WalkthroughTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private var shot = 0

    private fun sh(cmd: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(cmd).use { pfd ->
            java.io.FileInputStream(pfd.fileDescriptor).use { it.readBytes() }
        }
    }

    private fun screenshot(name: String) {
        rule.waitForIdle()
        Thread.sleep(700)
        shot++
        sh("screencap -p /sdcard/Download/deciary-shots/%02d-%s.png".format(shot, name))
    }

    /** Retries a tap that lands while a window (such as the keyboard) is still animating away. */
    private fun retry(action: () -> Unit) {
        repeat(4) { attempt ->
            try {
                rule.waitForIdle()
                action()
                return
            } catch (e: AssertionError) {
                if (attempt == 3 || e.message?.contains("inject") != true) throw e
                Thread.sleep(1000)
            }
        }
    }

    /** Some buttons (the extended FAB) keep their label only in the unmerged tree; look there if needed. */
    private fun tap(text: String) = retry {
        val merged = rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        rule.onNodeWithText(text, useUnmergedTree = !merged).performClick()
    }

    private fun tapDescribed(description: String) = retry { rule.onNodeWithContentDescription(description).performClick() }

    private fun type(label: String, value: String) {
        // Fields are named by their label: as text on Material fields, as a description on the design's own fields.
        rule.onNode(hasSetTextAction() and (hasText(label) or hasContentDescription(label))).performScrollTo().performTextInput(value)
        Espresso.closeSoftKeyboard()
    }

    @Test
    fun walkThroughTheApp() {
        sh("mkdir -p /sdcard/Download/deciary-shots")
        // Android 13+ asks for notifications when the first letter is sealed; grant it so the prompt doesn't cover the app.
        if (android.os.Build.VERSION.SDK_INT >= 33) sh("pm grant app.deciary android.permission.POST_NOTIFICATIONS")

        // First run
        screenshot("welcome-1")
        tap("Continue")
        screenshot("welcome-2")
        tap("Continue")
        screenshot("welcome-3")
        tap("Not now")
        type("Name on the cover · optional", "Priya")
        tap("Plum")
        screenshot("cover")
        tap("Open my diary")
        screenshot("home-empty")

        // Writing a letter
        tap("Write your first letter")
        type("The decision", "Accept the Pune job offer?")
        type("What you chose", "Yes, I'm taking it")
        screenshot("write-decision")
        tap("Next")
        type("Your prediction", "By April I'll be settled in, and glad I moved.")
        type("Why you chose it · optional", "Better team, and Pune is closer to family.")
        screenshot("write-prediction")
        tap("Next")
        tap("Excited")
        screenshot("write-mood-date")
        tap("Next")
        tapDescribed("85 percent, Confident")
        screenshot("write-how-sure")
        tap("Challenge me first")
        screenshot("challenge-1")
        tap("Noted · next question")
        tap("Noted · next question")
        screenshot("challenge-3")
        tap("I've thought about this · Seal it")
        screenshot("sealed")
        tap("Done")
        screenshot("home-with-letter")

        // Opening it (early, since it was just sealed)
        tap("Yes, I'm taking it")
        screenshot("letter-sealed")
        tap("Open it early")
        screenshot("open-early-dialog")
        tap("Open now")
        type("What happened?", "Settled in by March. Miss the old team a little.")
        retry { rule.onNodeWithText("Better").performScrollTo().performClick() }
        screenshot("then-and-now")
        tap("See verdict")
        screenshot("verdict")
        tap("Stamp it: Earned it")
        // Stamping lands on the opened letter, not on Home.
        rule.waitForIdle()
        rule.onNodeWithText("EARNED IT", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("WHAT HAPPENED", useUnmergedTree = true).assertExists()
        screenshot("letter-opened")

        // Shelf, settings, data
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        tap("See all")
        screenshot("shelf")
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        tapDescribed("Settings")
        screenshot("settings")
        retry { rule.onNodeWithText("Your data").performScrollTo().performClick() }
        screenshot("your-data")
    }
}
