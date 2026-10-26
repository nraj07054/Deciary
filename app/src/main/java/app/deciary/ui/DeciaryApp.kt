package app.deciary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.deciary.AppViewModel
import app.deciary.data.Compared
import app.deciary.data.Safety
import app.deciary.ui.screens.ChallengeScreen
import app.deciary.ui.screens.CoverScreen
import app.deciary.ui.screens.DataActions
import app.deciary.ui.screens.DataScreen
import app.deciary.ui.screens.HomeScreen
import app.deciary.ui.screens.LetterScreen
import app.deciary.ui.screens.LockScreen
import app.deciary.ui.screens.PinSetupScreen
import app.deciary.ui.screens.ReviewScreen
import app.deciary.ui.screens.SafetySheet
import app.deciary.ui.screens.SealedScreen
import app.deciary.ui.screens.SettingsActions
import app.deciary.ui.screens.SettingsScreen
import app.deciary.ui.screens.ShelfScreen
import app.deciary.ui.screens.TypoFixScreen
import app.deciary.ui.screens.VerdictScreen
import app.deciary.ui.screens.WelcomeScreen
import app.deciary.ui.screens.WriteScreen
import app.deciary.ui.theme.Deciary
import app.deciary.ui.theme.Type

private object Routes {
    const val WELCOME = "welcome"
    const val PIN_SETUP = "pin?bio={bio}&onboarding={onboarding}"
    const val COVER = "cover?first={first}"
    const val HOME = "home"
    const val WRITE = "write"
    const val CHALLENGE = "challenge"
    const val SEALED = "sealed/{id}"
    const val LETTER = "letter/{id}"
    const val TYPO = "typo/{id}"
    const val REVIEW = "review/{id}"
    const val VERDICT = "verdict/{id}"
    const val SHELF = "shelf"
    const val SETTINGS = "settings"
    const val DATA = "data"

    fun pinSetup(bio: Boolean, onboarding: Boolean) = "pin?bio=$bio&onboarding=$onboarding"
    fun cover(first: Boolean) = "cover?first=$first"
    fun sealed(id: String) = "sealed/$id"
    fun letter(id: String) = "letter/$id"
    fun typo(id: String) = "typo/$id"
    fun review(id: String) = "review/$id"
    fun verdict(id: String) = "verdict/$id"
}

/**
 * Leaves a screen whose letter or review is gone. Only pops when this screen is still on top:
 * a screen on its way out (e.g. the verdict, right after stamping) must not pop the one replacing it.
 */
@Composable
private fun LeaveIfMissing(nav: NavHostController, entry: NavBackStackEntry) {
    LaunchedEffect(entry) {
        if (nav.currentBackStackEntry?.id == entry.id) nav.popBackStack()
    }
}

private fun NavHostController.goHome() = navigate(Routes.HOME) { popUpTo(graph.id) { inclusive = true } }

@Composable
fun DeciaryApp(vm: AppViewModel) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    val letters by vm.letters.collectAsStateWithLifecycle()
    val locked by vm.locked.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val showLock = prefs.lockOn && locked
    // Fixed for the life of the NavHost; later moves to and from onboarding are explicit navigations.
    val start = remember { if (prefs.onboarded) Routes.HOME else Routes.WELCOME }

    Box(Modifier.fillMaxSize().background(Deciary.colors.bg)) {
        Box(if (showLock) Modifier.fillMaxSize().clearAndSetSemantics { } else Modifier.fillMaxSize()) {
            NavHost(nav, startDestination = start) {
                composable(Routes.WELCOME) {
                    WelcomeScreen { wantsPin, bio ->
                        if (wantsPin) nav.navigate(Routes.pinSetup(bio, onboarding = true)) else nav.navigate(Routes.cover(true))
                    }
                }
                composable(
                    Routes.PIN_SETUP,
                    arguments = listOf(
                        navArgument("bio") { type = NavType.BoolType; defaultValue = false },
                        navArgument("onboarding") { type = NavType.BoolType; defaultValue = false },
                    ),
                ) { entry ->
                    val bio = entry.arguments?.getBoolean("bio") ?: false
                    val onboarding = entry.arguments?.getBoolean("onboarding") ?: false
                    val next = { if (onboarding) nav.navigate(Routes.cover(true)) else nav.popBackStack() }
                    PinSetupScreen(prefs.leather, onCancel = { next() }) { pin ->
                        vm.setPin(pin, bio)
                        next()
                    }
                }
                composable(Routes.COVER, arguments = listOf(navArgument("first") { type = NavType.BoolType; defaultValue = false })) { entry ->
                    val first = entry.arguments?.getBoolean("first") ?: false
                    CoverScreen(
                        initialName = prefs.ownerName,
                        initialLeather = prefs.leather,
                        firstRun = first,
                        onBack = { nav.popBackStack() },
                    ) { name, leather ->
                        if (first) {
                            vm.finishOnboarding(name, leather)
                            nav.goHome()
                        } else {
                            vm.setCover(name, leather)
                            nav.popBackStack()
                        }
                    }
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        prefs = prefs,
                        letters = letters,
                        onNew = { vm.startDraft(); nav.navigate(Routes.WRITE) },
                        onOpen = { nav.navigate(Routes.letter(it)) },
                        onShelf = { nav.navigate(Routes.SHELF) },
                        onSettings = { nav.navigate(Routes.SETTINGS) },
                        onData = { nav.navigate(Routes.DATA) },
                        onLock = vm::lock,
                    )
                }
                composable(Routes.WRITE) {
                    var leaving by remember { mutableStateOf(false) }
                    var safety by remember { mutableStateOf(false) }
                    val d = vm.draft
                    val needsCare = Safety.needsCare(d.question, d.choice, d.prediction, d.why)
                    val seal = { challenged: Boolean -> vm.seal(challenged) { id -> nav.navigate(Routes.sealed(id)) { popUpTo(Routes.HOME) } } }
                    WriteScreen(
                        draft = d,
                        step = vm.writeStep,
                        onDraft = { vm.draft = it },
                        onStep = { vm.writeStep = it },
                        onClose = { if (d.question.isNotBlank() || d.choice.isNotBlank()) leaving = true else nav.popBackStack() },
                        onSeal = { if (needsCare) safety = true else seal(false) },
                        onChallenge = { if (needsCare) safety = true else nav.navigate(Routes.CHALLENGE) },
                    )
                    if (safety) SafetySheet(onSave = { safety = false; seal(false) }, onBack = { safety = false })
                    if (leaving) {
                        AlertDialog(
                            onDismissRequest = { leaving = false },
                            title = { Text("Leave this letter?", style = Type.heading) },
                            text = { Text("What you've written so far won't be kept.", style = Type.body) },
                            confirmButton = { TextButton(onClick = { leaving = false; nav.popBackStack() }) { Text("Leave") } },
                            dismissButton = { TextButton(onClick = { leaving = false }) { Text("Keep writing") } },
                        )
                    }
                }
                composable(Routes.CHALLENGE) {
                    ChallengeScreen(
                        confidence = vm.draft.confidence,
                        onBack = { nav.popBackStack() },
                        onEdit = { vm.writeStep = 1; nav.popBackStack() },
                        onSeal = { vm.seal(challenged = true) { id -> nav.navigate(Routes.sealed(id)) { popUpTo(Routes.HOME) } } },
                    )
                }
                composable(Routes.SEALED) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    SealedScreen(letters.firstOrNull { it.id == id }, prefs, onDone = { nav.goHome() })
                }
                composable(Routes.LETTER) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val letter = letters.firstOrNull { it.id == id }
                    if (letter == null) {
                        LeaveIfMissing(nav, entry)
                    } else {
                        LetterScreen(
                            letter = letter,
                            ownerName = prefs.ownerName,
                            onBack = { nav.popBackStack() },
                            onOpen = { nav.navigate(Routes.review(id)) },
                            onFixTypo = { nav.navigate(Routes.typo(id)) },
                        )
                    }
                }
                composable(Routes.TYPO) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val letter = letters.firstOrNull { it.id == id }
                    var tooLate by remember { mutableStateOf(false) }
                    if (letter != null) {
                        TypoFixScreen(letter, onBack = { nav.popBackStack() }) { q, ch, p, w ->
                            if (vm.fixTypo(id, q, ch, p, w)) nav.popBackStack() else tooLate = true
                        }
                    }
                    if (tooLate) {
                        AlertDialog(
                            onDismissRequest = { nav.popBackStack() },
                            title = { Text("It's sealed now", style = Type.heading) },
                            text = { Text("The 10 minutes for typo fixes have passed, so the letter stays as you wrote it.", style = Type.body) },
                            confirmButton = { TextButton(onClick = { nav.popBackStack() }) { Text("OK") } },
                        )
                    }
                }
                composable(Routes.REVIEW) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val letter = letters.firstOrNull { it.id == id }
                    LaunchedEffect(id) { vm.startReview(id) }
                    val draft = vm.reviewDraft
                    if (letter != null && draft != null && draft.letterId == id) {
                        ReviewScreen(
                            letter = letter,
                            draft = draft,
                            onDraft = { vm.reviewDraft = it },
                            onBack = { nav.popBackStack() },
                            onNext = { nav.navigate(Routes.verdict(id)) },
                        )
                    }
                }
                composable(Routes.VERDICT) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val draft = vm.reviewDraft
                    if (draft != null && draft.letterId == id) {
                        VerdictScreen(
                            suggested = draft.suggested,
                            worse = draft.compared == Compared.Worse,
                            onBack = { nav.popBackStack() },
                        ) { verdict ->
                            vm.stamp(verdict) { nav.navigate(Routes.letter(id)) { popUpTo(Routes.HOME) } }
                        }
                    } else {
                        LeaveIfMissing(nav, entry)
                    }
                }
                composable(Routes.SHELF) {
                    ShelfScreen(letters, onBack = { nav.popBackStack() }, onOpen = { nav.navigate(Routes.letter(it)) })
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        prefs,
                        SettingsActions(
                            onBack = { nav.popBackStack() },
                            onCover = { nav.navigate(Routes.cover(false)) },
                            onData = { nav.navigate(Routes.DATA) },
                            onLockOn = { nav.navigate(Routes.pinSetup(bio = false, onboarding = false)) },
                            onLockOff = { pin -> vm.checkPin(pin).also { if (it) vm.turnLockOff() } },
                            onBiometric = vm::setBiometric,
                            onReminders = vm::setReminders,
                            onReminderHour = vm::setReminderHour,
                            onReviewMonths = vm::setReviewMonths,
                            onTheme = vm::setTheme,
                        ),
                    )
                }
                composable(Routes.DATA) {
                    DataScreen(
                        prefs,
                        letters.size,
                        DataActions(
                            onBack = { nav.popBackStack() },
                            backup = vm::backup,
                            restore = vm::restore,
                            exportJson = vm::exportJson,
                            exportCsv = vm::exportCsv,
                            checkPin = vm::checkPin,
                            deleteEverything = { vm.deleteEverything { nav.navigate(Routes.WELCOME) { popUpTo(nav.graph.id) { inclusive = true } } } },
                        ),
                    )
                }
            }
        }
        if (showLock) {
            LockScreen(
                name = prefs.coverName,
                leather = prefs.leather,
                biometric = prefs.biometric,
                onPin = vm::tryUnlock,
                onBiometric = vm::unlockWithBiometric,
            )
        }
    }
}
