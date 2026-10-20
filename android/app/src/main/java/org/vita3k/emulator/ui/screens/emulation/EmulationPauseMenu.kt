package org.vita3k.emulator.ui.screens.emulation

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.Emulator
import org.vita3k.emulator.R
import org.vita3k.emulator.data.RestartRequiredSetting
import org.vita3k.emulator.ui.components.OverlayEditorPalette
import org.vita3k.emulator.ui.rememberConnectedGamepads
import org.vita3k.emulator.ui.screens.settings.ControllerMappingSections
import org.vita3k.emulator.ui.screens.settings.SettingsCategory
import org.vita3k.emulator.ui.screens.settings.SettingsCategoryBody
import org.vita3k.emulator.ui.screens.settings.SettingsCategoryStrip
import org.vita3k.emulator.ui.screens.settings.SettingsHelpEntry
import org.vita3k.emulator.ui.screens.settings.SettingsHelpSheet
import org.vita3k.emulator.ui.screens.settings.SettingsLoadingState
import org.vita3k.emulator.ui.screens.settings.SettingsNote
import org.vita3k.emulator.ui.screens.settings.OverlaySettingsRows
import org.vita3k.emulator.ui.screens.settings.settingsFilterChipColors
import org.vita3k.emulator.ui.screens.settings.settingsCategories
import org.vita3k.emulator.ui.components.AppButton
import org.vita3k.emulator.ui.components.AppDialog
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.DialogAction
import org.vita3k.emulator.ui.components.EyebrowLabel
import org.vita3k.emulator.ui.components.Panel
import org.vita3k.emulator.ui.components.Segmented
import org.vita3k.emulator.ui.components.ToolbarButton
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoSmall
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import org.vita3k.emulator.ui.theme.Vita3KTheme
import org.vita3k.emulator.ui.viewmodel.EmulationSessionViewModel
import org.vita3k.emulator.ui.viewmodel.SettingsViewModel

private enum class PauseSettingsScope {
    Global,
    CustomConfig
}

object EmulationPauseMenuHost {
    @JvmStatic
    fun attach(
        activity: Emulator,
        composeView: ComposeView,
        sessionViewModel: EmulationSessionViewModel,
        settingsViewModel: SettingsViewModel,
        globalSettingsViewModel: SettingsViewModel
    ) {
        composeView.setContent {
            Vita3KTheme {
                EmulationPauseMenu(
                    activity = activity,
                    sessionViewModel = sessionViewModel,
                    settingsViewModel = settingsViewModel,
                    globalSettingsViewModel = globalSettingsViewModel,
                    hostView = composeView
                )
            }
        }
    }
}

private enum class PauseMenuTab(val labelRes: Int, val icon: ImageVector) {
    Session(R.string.emulation_tab_session, Icons.Default.Pause),
    Controls(R.string.emulation_tab_controls, Icons.Default.TouchApp),
    Controller(R.string.emulation_tab_controller, Icons.Default.SportsEsports),
    Settings(R.string.emulation_tab_settings, Icons.Default.Settings)
}

private fun formatRestartRequiredSettings(
    context: android.content.Context,
    settings: List<RestartRequiredSetting>
): String = settings.joinToString(separator = "\n- ", prefix = "- ") { setting ->
    context.getString(setting.labelResId)
}

@Composable
private fun EmulationPauseMenu(
    activity: Emulator,
    sessionViewModel: EmulationSessionViewModel,
    settingsViewModel: SettingsViewModel,
    globalSettingsViewModel: SettingsViewModel,
    hostView: ComposeView
) {
    val uiState = sessionViewModel.uiState
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(PauseMenuTab.Session) }
    var pendingHelp by remember { mutableStateOf<SettingsHelpEntry?>(null) }
    var pendingRestartSettings by remember { mutableStateOf<List<RestartRequiredSetting>?>(null) }
    var requestedSettingsScope by remember { mutableStateOf<PauseSettingsScope?>(null) }
    val connectedGamepads = rememberConnectedGamepads(context)
    val menuVisibilityState = remember { MutableTransitionState(false) }
    val controlsVisibilityState = remember { MutableTransitionState(false) }

    LaunchedEffect(uiState.titleId) {
        if (uiState.titleId.isNotBlank()) {
            settingsViewModel.load(uiState.titleId)
        }
    }

    LaunchedEffect(uiState.showMenu) {
        if (uiState.showMenu && !globalSettingsViewModel.isLoaded(titleId = null)) {
            globalSettingsViewModel.load(titleId = null)
        }
    }

    LaunchedEffect(uiState.showMenu) {
        if (uiState.showMenu) {
            selectedTab = PauseMenuTab.Session
        }
        menuVisibilityState.targetState = uiState.showMenu
    }

    LaunchedEffect(uiState.isEditingControls) {
        controlsVisibilityState.targetState = uiState.isEditingControls
    }

    val hostVisible = menuVisibilityState.currentState ||
        menuVisibilityState.targetState ||
        controlsVisibilityState.currentState ||
        controlsVisibilityState.targetState ||
        uiState.showExitConfirmation ||
        settingsViewModel.operationResult != null ||
        globalSettingsViewModel.operationResult != null ||
        pendingRestartSettings != null ||
        pendingHelp != null

    SideEffect {
        hostView.visibility = if (hostVisible) View.VISIBLE else View.GONE
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ControlsEditorBar(
            visibleState = controlsVisibilityState,
            onDone = { sessionViewModel.finishControlsEditor(activity) },
            onReset = { sessionViewModel.resetControlsLayout(activity) },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visibleState = menuVisibilityState,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Mocha.Crust.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { sessionViewModel.closeMenu(activity) }
                    )
            )
        }

        AnimatedVisibility(
            visibleState = menuVisibilityState,
            enter = slideInHorizontally(initialOffsetX = { it / 8 }) + fadeIn(tween(240)),
            exit = slideOutHorizontally(targetOffsetX = { it / 8 }) + fadeOut(tween(180)),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.displayCutout.asPaddingValues())
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                PauseDrawer(
                    activity = activity,
                    sessionViewModel = sessionViewModel,
                    settingsViewModel = settingsViewModel,
                    globalSettingsViewModel = globalSettingsViewModel,
                    connectedGamepads = connectedGamepads,
                    selectedTab = selectedTab,
                    onSelectedTabChange = {
                        if (it != PauseMenuTab.Settings) {
                            requestedSettingsScope = null
                        }
                        selectedTab = it
                    },
                    onOpenSettingsTab = {
                        requestedSettingsScope = PauseSettingsScope.CustomConfig
                        selectedTab = PauseMenuTab.Settings
                    },
                    requestedSettingsScope = requestedSettingsScope,
                    onSettingsScopeRequestConsumed = { requestedSettingsScope = null },
                    onNeedsRestart = { pendingRestartSettings = it },
                    onShowHelp = { pendingHelp = it }
                )
            }
        }
    }

    if (uiState.showExitConfirmation) {
        AppDialog(
            title = stringResource(R.string.emulation_exit_title),
            onDismiss = { sessionViewModel.dismissExitConfirmation() },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { sessionViewModel.dismissExitConfirmation() }),
                DialogAction(stringResource(R.string.emulation_exit), { sessionViewModel.confirmExit(activity) }, ButtonTone.Danger)
            )
        ) {
            Text(stringResource(R.string.emulation_exit_message))
        }
    }

    pendingRestartSettings?.let { settings ->
        AppDialog(
            title = stringResource(R.string.settings_restart_required_title),
            onDismiss = {},
            dismissible = false,
            actions = listOf(
                DialogAction(stringResource(R.string.settings_restart_later), { pendingRestartSettings = null }),
                DialogAction(stringResource(R.string.settings_restart_app), {
                    pendingRestartSettings = null
                    activity.restartApp(uiState.titleId, "", "")
                }, ButtonTone.Accent)
            )
        ) {
            Text(stringResource(R.string.settings_restart_required_summary))
            Text(stringResource(R.string.settings_restart_required_message, formatRestartRequiredSettings(context, settings)), color = Mocha.Text)
        }
    }

    listOf(settingsViewModel, globalSettingsViewModel).forEach { viewModel ->
        viewModel.operationResult?.let { result ->
            AppDialog(
                title = if (result.isError) stringResource(R.string.settings_opt_error) else stringResource(R.string.settings_success_title),
                onDismiss = { viewModel.dismissOperationResult() },
                actions = listOf(DialogAction(stringResource(R.string.action_ok), { viewModel.dismissOperationResult() }, ButtonTone.Accent))
            ) {
                Text(result.message)
            }
        }
    }

    pendingHelp?.let { help ->
        SettingsHelpSheet(help = help, onDismiss = { pendingHelp = null })
    }
}

@Composable
private fun PauseDrawer(
    activity: Emulator,
    sessionViewModel: EmulationSessionViewModel,
    settingsViewModel: SettingsViewModel,
    globalSettingsViewModel: SettingsViewModel,
    connectedGamepads: List<org.vita3k.emulator.ConnectedGamepad>,
    selectedTab: PauseMenuTab,
    onSelectedTabChange: (PauseMenuTab) -> Unit,
    onOpenSettingsTab: () -> Unit,
    requestedSettingsScope: PauseSettingsScope?,
    onSettingsScopeRequestConsumed: () -> Unit,
    onNeedsRestart: (List<RestartRequiredSetting>) -> Unit,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    val uiState = sessionViewModel.uiState
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.statusMessage) {
        if (!uiState.statusMessage.isNullOrBlank()) {
            scrollState.animateScrollTo(0)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 420.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Mocha.Mantle.copy(alpha = 0.98f),
        contentColor = Mocha.Text,
        border = BorderStroke(1.dp, Mocha.Surface0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PauseHeader(
                title = uiState.gameTitle.ifBlank { uiState.titleId.ifBlank { stringResource(R.string.emulation_menu_title) } },
                subtitle = uiState.titleId.ifBlank { stringResource(R.string.emulation_menu_subtitle) },
                onClose = { sessionViewModel.closeMenu(activity) }
            )

            uiState.statusMessage?.let { message ->
                PauseStatusBanner(message = message)
            }

            PauseTabRow(
                selected = selectedTab,
                onSelected = onSelectedTabChange
            )

            when (selectedTab) {
                PauseMenuTab.Session -> SessionTab(
                    activity = activity,
                    sessionViewModel = sessionViewModel,
                    settingsViewModel = settingsViewModel,
                    onOpenSettingsTab = onOpenSettingsTab,
                    onNeedsRestart = onNeedsRestart
                )
                PauseMenuTab.Controls -> ControlsTab(
                    activity = activity,
                    sessionViewModel = sessionViewModel,
                    onShowHelp = onShowHelp
                )
                PauseMenuTab.Controller -> ControllerTab(
                    activity = activity,
                    sessionViewModel = sessionViewModel,
                    globalSettingsViewModel = globalSettingsViewModel,
                    connectedGamepads = connectedGamepads,
                    onShowHelp = onShowHelp
                )
                PauseMenuTab.Settings -> EmbeddedSettingsTab(
                    activity = activity,
                    sessionViewModel = sessionViewModel,
                    settingsViewModel = settingsViewModel,
                    globalSettingsViewModel = globalSettingsViewModel,
                    titleId = uiState.titleId,
                    requestedScope = requestedSettingsScope,
                    onScopeRequestConsumed = onSettingsScopeRequestConsumed,
                    onSaved = { restartRequired ->
                        if (restartRequired.isEmpty()) {
                            sessionViewModel.showStatusMessage(activity.getString(R.string.emulation_settings_saved))
                        } else {
                            onNeedsRestart(restartRequired)
                        }
                    },
                    onShowHelp = onShowHelp
                )
            }
        }
    }
}

@Composable
private fun PauseHeader(
    title: String,
    subtitle: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(subtitle, style = MonoSmall, color = Mocha.Mauve, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Mocha.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        ToolbarButton(Icons.Default.Close, stringResource(R.string.emulation_menu_close), onClose)
    }
}

@Composable
private fun PauseTabRow(
    selected: PauseMenuTab,
    onSelected: (PauseMenuTab) -> Unit
) {
    Segmented(
        options = PauseMenuTab.entries.map { it.icon to stringResource(it.labelRes) },
        selectedIndex = PauseMenuTab.entries.indexOf(selected),
        onSelect = { onSelected(PauseMenuTab.entries[it]) },
        iconsOnly = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PauseStatusBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Mocha.Green.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Done, contentDescription = null, tint = Mocha.Green, modifier = Modifier.size(18.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Mocha.Green)
    }
}

@Composable
private fun SessionTab(
    activity: Emulator,
    sessionViewModel: EmulationSessionViewModel,
    settingsViewModel: SettingsViewModel,
    onOpenSettingsTab: () -> Unit,
    onNeedsRestart: (List<RestartRequiredSetting>) -> Unit
) {
    val uiState = sessionViewModel.uiState
    val settingsLoaded = uiState.titleId.isNotBlank() && settingsViewModel.isLoaded(uiState.titleId)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppButton(
                text = stringResource(if (uiState.isPaused) R.string.emulation_resume else R.string.emulation_pause),
                onClick = { sessionViewModel.togglePause(activity) },
                icon = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )
            AppButton(
                text = stringResource(R.string.emulation_exit),
                onClick = { sessionViewModel.requestExit() },
                tone = ButtonTone.Danger,
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )
        }

        CustomConfigSection(
            settingsLoaded = settingsLoaded,
            hasCustomConfig = settingsViewModel.hasCustomConfig,
            saving = settingsViewModel.saving,
            onSave = {
                settingsViewModel.save(forceCustomConfig = true) { restartRequired ->
                    sessionViewModel.promoteOverlayToGameOverride(activity)
                    if (restartRequired.isEmpty()) {
                        sessionViewModel.showStatusMessage(activity.getString(R.string.emulation_settings_saved))
                    } else {
                        onNeedsRestart(restartRequired)
                    }
                }
            },
            onReset = {
                settingsViewModel.deleteCustomConfig { restartRequired ->
                    sessionViewModel.clearGameOverlayOverride(activity)
                    if (restartRequired.isEmpty()) {
                        sessionViewModel.showStatusMessage(activity.getString(R.string.emulation_settings_saved))
                    } else {
                        onNeedsRestart(restartRequired)
                    }
                }
            },
            onOpenSettings = onOpenSettingsTab
        )

        if (!settingsLoaded) {
            SettingsLoadingState(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CustomConfigSection(
    settingsLoaded: Boolean,
    hasCustomConfig: Boolean,
    saving: Boolean,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Panel(contentPadding = PaddingValues(16.dp), spacing = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (hasCustomConfig) Mocha.Lavender else Mocha.Surface2)
            )
            Text(
                text = stringResource(R.string.emulation_custom_config),
                style = MaterialTheme.typography.titleMedium,
                color = Mocha.Text
            )
        }
        Text(
            text = stringResource(if (hasCustomConfig) R.string.emulation_custom_config_active else R.string.emulation_custom_config_inactive),
            style = MaterialTheme.typography.bodySmall,
            color = Mocha.Subtext0
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppButton(
                text = stringResource(if (hasCustomConfig) R.string.emulation_save_config_short else R.string.emulation_create_config_short),
                onClick = onSave,
                icon = Icons.Default.Save,
                enabled = settingsLoaded && !saving,
                modifier = Modifier.weight(1f)
            )
            AppButton(
                text = stringResource(R.string.emulation_reset_config_short),
                onClick = onReset,
                tone = ButtonTone.Neutral,
                icon = Icons.Default.Restore,
                enabled = settingsLoaded && hasCustomConfig && !saving,
                modifier = Modifier.weight(1f)
            )
        }
        AppButton(
            text = stringResource(R.string.emulation_open_settings_short),
            onClick = onOpenSettings,
            tone = ButtonTone.Neutral,
            icon = Icons.Default.Settings,
            enabled = settingsLoaded,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ControlsTab(
    activity: Emulator,
    sessionViewModel: EmulationSessionViewModel,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    val uiState = sessionViewModel.uiState

    OverlaySettingsRows(
        overlayConfig = sessionViewModel.currentOverlayConfig(),
        onOverlayConfigChange = { updated ->
            sessionViewModel.updateOverlayConfig(activity) { updated }
        },
        onShowHelp = onShowHelp,
        controllerConnected = uiState.controllerConnected,
        onStartControlsEditor = { sessionViewModel.startControlsEditor(activity) },
        onResetControlsLayout = { sessionViewModel.resetControlsLayout(activity) }
    )
}

@Composable
private fun ControllerTab(
    activity: Emulator,
    sessionViewModel: EmulationSessionViewModel,
    globalSettingsViewModel: SettingsViewModel,
    connectedGamepads: List<org.vita3k.emulator.ConnectedGamepad>,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    if (!globalSettingsViewModel.isLoaded(titleId = null)) {
        SettingsLoadingState(modifier = Modifier.fillMaxWidth())
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppButton(
                text = stringResource(R.string.settings_discard),
                onClick = { globalSettingsViewModel.discardChanges() },
                tone = ButtonTone.Neutral,
                enabled = globalSettingsViewModel.isDirty && !globalSettingsViewModel.saving,
                modifier = Modifier.weight(1f)
            )
            AppButton(
                text = stringResource(R.string.settings_save_cd),
                onClick = {
                    globalSettingsViewModel.save { _ ->
                        globalSettingsViewModel.load(titleId = null, force = true)
                        sessionViewModel.showStatusMessage(activity.getString(R.string.emulation_settings_saved))
                    }
                },
                icon = Icons.Default.Save,
                enabled = globalSettingsViewModel.isDirty && !globalSettingsViewModel.saving,
                modifier = Modifier.weight(1f)
            )
        }

        ControllerMappingSections(
            cfg = globalSettingsViewModel.config,
            connectedGamepads = connectedGamepads,
            onUpdate = globalSettingsViewModel::update,
            onShowHelp = onShowHelp
        )
    }
}

@Composable
private fun EmbeddedSettingsTab(
    activity: Emulator,
    sessionViewModel: EmulationSessionViewModel,
    settingsViewModel: SettingsViewModel,
    globalSettingsViewModel: SettingsViewModel,
    titleId: String,
    requestedScope: PauseSettingsScope?,
    onScopeRequestConsumed: () -> Unit,
    onSaved: (List<RestartRequiredSetting>) -> Unit,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    val customSettingsLoaded = titleId.isNotBlank() && settingsViewModel.isLoaded(titleId)
    val globalSettingsLoaded = globalSettingsViewModel.isLoaded(titleId = null)
    val customCategories = remember {
        settingsCategories(isPerApp = true).filter { it != SettingsCategory.Controls }
    }
    val globalCategories = remember {
        settingsCategories(isPerApp = false).filter { it != SettingsCategory.Controls }
    }
    var activeScope by remember { mutableStateOf(PauseSettingsScope.Global) }
    var selectedCustomCategory by remember { mutableStateOf(SettingsCategory.Gpu) }
    var selectedGlobalCategory by remember { mutableStateOf(SettingsCategory.Gpu) }

    LaunchedEffect(requestedScope) {
        if (requestedScope != null) {
            activeScope = requestedScope
            onScopeRequestConsumed()
        }
    }

    LaunchedEffect(customCategories, selectedCustomCategory) {
        if (selectedCustomCategory !in customCategories) {
            selectedCustomCategory = customCategories.firstOrNull() ?: SettingsCategory.Core
        }
    }

    LaunchedEffect(globalCategories, selectedGlobalCategory) {
        if (selectedGlobalCategory !in globalCategories) {
            selectedGlobalCategory = globalCategories.firstOrNull() ?: SettingsCategory.Core
        }
    }

    val activeViewModel = if (activeScope == PauseSettingsScope.Global) {
        globalSettingsViewModel
    } else {
        settingsViewModel
    }
    val categories = if (activeScope == PauseSettingsScope.Global) {
        globalCategories
    } else {
        customCategories
    }
    val selectedCategory = if (activeScope == PauseSettingsScope.Global) {
        selectedGlobalCategory
    } else {
        selectedCustomCategory
    }
    val settingsLoaded = if (activeScope == PauseSettingsScope.Global) {
        globalSettingsLoaded
    } else {
        customSettingsLoaded
    }

    fun saveActiveSettings() {
        if (activeScope == PauseSettingsScope.Global) {
            globalSettingsViewModel.save { restartRequired ->
                if (!settingsViewModel.hasCustomConfig && !settingsViewModel.isDirty) {
                    settingsViewModel.load(titleId, force = true)
                }
                onSaved(restartRequired)
            }
        } else {
            settingsViewModel.save(forceCustomConfig = true, onSaved = onSaved)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!settingsLoaded) {
            SettingsLoadingState(modifier = Modifier.fillMaxWidth())
            return@Column
        }

        Segmented(
            options = listOf(
                null to stringResource(R.string.settings_title),
                null to stringResource(R.string.emulation_custom_config)
            ),
            selectedIndex = if (activeScope == PauseSettingsScope.Global) 0 else 1,
            onSelect = { activeScope = if (it == 0) PauseSettingsScope.Global else PauseSettingsScope.CustomConfig },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppButton(
                text = stringResource(R.string.settings_discard),
                onClick = { activeViewModel.discardChanges() },
                tone = ButtonTone.Neutral,
                enabled = activeViewModel.isDirty && !activeViewModel.saving,
                modifier = Modifier.weight(1f)
            )
            AppButton(
                text = stringResource(R.string.settings_save_cd),
                onClick = ::saveActiveSettings,
                icon = Icons.Default.Save,
                enabled = activeViewModel.isDirty && !activeViewModel.saving,
                modifier = Modifier.weight(1f)
            )
        }

        if (activeScope == PauseSettingsScope.CustomConfig) {
            SettingsNote(
                text = stringResource(
                    if (settingsViewModel.hasCustomConfig) {
                        R.string.settings_custom_config_banner
                    } else {
                        R.string.emulation_custom_config_inactive
                    }
                ),
                color = if (settingsViewModel.hasCustomConfig) Mocha.Lavender else Mocha.Subtext0
            )
        }

        SettingsCategoryStrip(
            contentPadding = PaddingValues(0.dp),
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = {
                if (activeScope == PauseSettingsScope.Global) {
                    selectedGlobalCategory = it
                } else {
                    selectedCustomCategory = it
                }
            }
        )

        SettingsCategoryBody(
            category = selectedCategory,
            cfg = activeViewModel.config,
            overlayConfig = sessionViewModel.currentOverlayConfig(),
            modulesList = activeViewModel.modulesList,
            modulesSearch = activeViewModel.modulesSearch,
            onModulesSearchChange = activeViewModel::onModulesSearchChange,
            onToggleModule = activeViewModel::toggleModule,
            supportedMemoryMappingMask = activeViewModel.supportedMemoryMappingMask,
            customDriverLoadStatus = activeViewModel.customDriverLoadStatus,
            availableCameras = activeViewModel.availableCameras,
            availableAdhocAddresses = activeViewModel.availableAdhocAddresses,
            currentStoragePath = activeViewModel.currentStoragePath,
            defaultStoragePath = activeViewModel.defaultStoragePath,
            onChangeStorageFolder = {},
            onResetStorageFolder = {},
            installedCustomDrivers = activeViewModel.installedCustomDrivers,
            customDriverBusy = activeViewModel.customDriverBusy,
            onInstallCustomDriver = {},
            onDownloadCustomDriver = {},
            onPickCameraImage = { isFront ->
                activity.requestImagePath { path ->
                    if (path != null) {
                        activeViewModel.setCameraImage(isFront, path)
                    }
                }
            },
            onRequestRemoveCustomDriver = { _ -> },
            isPerApp = activeScope == PauseSettingsScope.CustomConfig,
            showCustomDriverManagement = false,
            showCustomDriverSection = false,
            allowClearAllCustomConfigs = false,
            allowStorageFolderManagement = false,
            allowCameraImagePicker = true,
            onClearAllCustomConfigs = {},
            onSelectCustomDriver = activeViewModel::selectCustomDriver,
            onUpdate = activeViewModel::update,
            onOverlayConfigChange = { updated ->
                sessionViewModel.updateOverlayConfig(activity) { updated }
            },
            onStartControlsEditor = { sessionViewModel.startControlsEditor(activity) },
            onResetControlsLayout = { sessionViewModel.resetControlsLayout(activity) },
            controllerConnected = sessionViewModel.uiState.controllerConnected,
            onShowHelp = onShowHelp
        )

        SettingsNote(text = stringResource(R.string.emulation_runtime_note))
    }
}

@Composable
private fun ControlsEditorBar(
    visibleState: MutableTransitionState<Boolean>,
    onDone: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(180)),
        modifier = modifier
    ) {
        OverlayEditorPalette(
            onDone = onDone,
            onReset = onReset,
            modifier = modifier
        )
    }
}
