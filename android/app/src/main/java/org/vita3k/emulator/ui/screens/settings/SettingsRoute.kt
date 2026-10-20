package org.vita3k.emulator.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.vita3k.emulator.MainActivity
import org.vita3k.emulator.R
import org.vita3k.emulator.data.CustomDriverLoadStatus
import org.vita3k.emulator.data.RestartRequiredSetting
import org.vita3k.emulator.overlay.OverlayConfig
import org.vita3k.emulator.overlay.OverlayStore
import org.vita3k.emulator.ui.CUSTOM_DRIVER_DOWNLOAD_URL
import org.vita3k.emulator.ui.rememberConnectedGamepads
import org.vita3k.emulator.ui.components.AppButton
import org.vita3k.emulator.ui.components.AppDialog
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.DialogAction
import org.vita3k.emulator.ui.components.SearchField
import org.vita3k.emulator.ui.components.ToolbarButton
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoSmall
import org.vita3k.emulator.ui.viewmodel.SettingsViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check

private val CUSTOM_DRIVER_MIME_TYPES = arrayOf(
    "application/zip",
    "application/x-zip-compressed",
    "application/octet-stream"
)
private val IMAGE_MIME_TYPES = arrayOf("image/*")

private fun formatRestartRequiredSettings(
    context: android.content.Context,
    settings: List<RestartRequiredSetting>
): String = settings.joinToString(separator = "\n- ", prefix = "- ") { setting ->
    context.getString(setting.labelResId)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    titleId: String?,
    appName: String? = null,
    viewModel: SettingsViewModel,
    onStorageChanged: () -> Unit = {},
    onBack: () -> Unit
) {
    LaunchedEffect(titleId) {
        if (!viewModel.isLoaded(titleId)) {
            viewModel.load(titleId)
        }
    }

    val context = LocalContext.current
    val isPerApp = titleId != null
    val settingsLoaded = viewModel.isLoaded(titleId)
    val categories = remember(isPerApp) { settingsCategories(isPerApp) }
    val scrollState = rememberScrollState()
    val overlayScopeId = titleId.orEmpty()
    var overlayState by remember(titleId) { mutableStateOf(OverlayStore.defaultState(context)) }
    var originalOverlayState by remember(titleId) { mutableStateOf(overlayState) }
    val connectedGamepads = rememberConnectedGamepads(context)
    val controllerConnected = connectedGamepads.isNotEmpty()
    val overlayDirty = overlayState != originalOverlayState
    val hasPendingChanges = viewModel.isDirty || overlayDirty

    var selectedCategory by rememberSaveable(titleId) { mutableStateOf(SettingsCategory.Core) }
    var searchQuery by rememberSaveable(titleId) { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showResetDefaultsDialog by remember { mutableStateOf(false) }
    var showDeleteCustomConfigDialog by remember { mutableStateOf(false) }
    var showClearAllCustomConfigsDialog by remember { mutableStateOf(false) }
    var pendingCustomDriverRemoval by remember { mutableStateOf<String?>(null) }
    var pendingCameraImageTarget by remember { mutableStateOf<Boolean?>(null) }
    var activeHelp by remember { mutableStateOf<SettingsHelpEntry?>(null) }
    var searchActive by rememberSaveable(titleId) { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showOverlayEditor by rememberSaveable(titleId) { mutableStateOf(false) }
    var pendingRestartSettings by remember { mutableStateOf<List<RestartRequiredSetting>?>(null) }
    var closeAfterRestartNotice by remember { mutableStateOf(false) }

    fun requestStorageFolderChange() {
        val activity = context as? MainActivity ?: return
        activity.requestStorageFolderChange { path ->
            path?.let { viewModel.changeStorageFolder(it, onStorageChanged) }
        }
    }

    fun requestCustomDriverInstall() {
        val activity = context as? MainActivity ?: return
        activity.requestFilePath(CUSTOM_DRIVER_MIME_TYPES) { path ->
            path?.let(viewModel::installCustomDriver)
        }
    }

    fun closeSearch() {
        searchActive = false
        searchQuery = ""
    }

    fun pickCameraImage(isFront: Boolean) {
        val activity = context as? MainActivity ?: return
        pendingCameraImageTarget = isFront
        activity.requestFilePath(IMAGE_MIME_TYPES) { path ->
            val target = pendingCameraImageTarget
            pendingCameraImageTarget = null
            if (target != null && path != null) {
                viewModel.setCameraImage(target, path)
            }
        }
    }

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    LaunchedEffect(settingsLoaded, titleId, viewModel.hasCustomConfig) {
        if (settingsLoaded) {
            val loaded = OverlayStore.resolveState(
                context = context,
                scopeId = overlayScopeId,
                allowGameOverride = isPerApp && OverlayStore.hasGameOverride(context, overlayScopeId)
            )
            overlayState = loaded
            originalOverlayState = loaded
        }
    }

    fun reloadOverlayState() {
        val loaded = OverlayStore.resolveState(
            context = context,
            scopeId = overlayScopeId,
            allowGameOverride = isPerApp && OverlayStore.hasGameOverride(context, overlayScopeId)
        )
        overlayState = loaded
        originalOverlayState = loaded
    }

    fun persistOverlayStateChanges() {
        val normalized = overlayState.copy(
            config = overlayState.config.normalized(),
            layout = overlayState.layout.normalized()
        )
        overlayState = normalized
        if (isPerApp) {
            OverlayStore.saveGameOverride(context, overlayScopeId, normalized)
        } else {
            OverlayStore.saveGlobalState(context, normalized)
        }
        originalOverlayState = normalized
    }

    fun saveSettings(onSaved: (List<RestartRequiredSetting>) -> Unit) {
        if (overlayDirty) {
            persistOverlayStateChanges()
        }
        if (viewModel.isDirty) {
            viewModel.save(forceCustomConfig = isPerApp, onSaved = onSaved)
        } else {
            onSaved(emptyList())
        }
    }

    fun tryBack() {
        if (hasPendingChanges) showDiscardDialog = true else onBack()
    }

    BackHandler { tryBack() }

    LaunchedEffect(categories, selectedCategory) {
        if (selectedCategory !in categories) {
            selectedCategory = categories.firstOrNull() ?: SettingsCategory.Core
        }
    }

    LaunchedEffect(selectedCategory, searchQuery) {
        scrollState.scrollTo(0)
    }

    fun save() {
        saveSettings { restartRequired ->
            if (restartRequired.isNotEmpty()) {
                pendingRestartSettings = restartRequired
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            SettingsHeader(
                title = if (isPerApp) appName ?: titleId.orEmpty() else stringResource(R.string.settings_title),
                eyebrow = if (isPerApp) stringResource(R.string.app_menu_custom_config) else null,
                searchActive = searchActive,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onToggleSearch = { if (searchActive) closeSearch() else searchActive = true },
                onBack = { tryBack() },
                overflow = {
                    SettingsOverflowMenu(
                        expanded = showOverflowMenu,
                        onExpandedChange = { showOverflowMenu = it },
                        showDeleteCustomConfig = isPerApp && viewModel.hasCustomConfig,
                        onResetDefaults = {
                            showOverflowMenu = false
                            showResetDefaultsDialog = true
                        },
                        onDeleteCustomConfig = {
                            showOverflowMenu = false
                            showDeleteCustomConfigDialog = true
                        }
                    )
                }
            )

            if (!settingsLoaded) {
                SettingsLoadingState(modifier = Modifier.fillMaxSize())
            } else {
                val searchEntries = rememberSettingsSearchEntries(
                    isPerApp = isPerApp,
                    backendRenderer = viewModel.config.backendRenderer,
                    showCustomDriverOptions = viewModel.showCustomDriverOptions,
                    showTurboModeOption = viewModel.showCustomDriverOptions
                )

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val wide = maxWidth >= 760.dp
                    val pane: @Composable (Modifier) -> Unit = { paneModifier ->
                        SettingsContentPane(
                            modifier = paneModifier,
                            titleId = titleId,
                            isPerApp = isPerApp,
                            showCategoryStrip = !wide,
                            viewModel = viewModel,
                            overlayConfig = overlayState.config,
                            supportedMemoryMappingMask = viewModel.supportedMemoryMappingMask,
                            customDriverLoadStatus = viewModel.customDriverLoadStatus,
                            availableCameras = viewModel.availableCameras,
                            availableAdhocAddresses = viewModel.availableAdhocAddresses,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            onCloseSearch = ::closeSearch,
                            onSelectedCategoryChange = { selectedCategory = it },
                            searchEntries = searchEntries,
                            currentStoragePath = viewModel.currentStoragePath,
                            scrollState = scrollState,
                            controllerConnected = controllerConnected,
                            hasOverlayChanges = overlayDirty,
                            connectedGamepads = connectedGamepads,
                            categories = categories,
                            onChangeStorageFolder = ::requestStorageFolderChange,
                            onResetStorageFolder = { viewModel.resetStorageFolder(onStorageChanged) },
                            onInstallCustomDriver = ::requestCustomDriverInstall,
                            onDownloadCustomDriver = { openUrl(CUSTOM_DRIVER_DOWNLOAD_URL) },
                            onPickCameraImage = ::pickCameraImage,
                            onRequestRemoveCustomDriver = { pendingCustomDriverRemoval = it },
                            onClearAllCustomConfigs = { showClearAllCustomConfigsDialog = true },
                            onOverlayConfigChange = { updated ->
                                overlayState = overlayState.copy(config = updated.normalized())
                            },
                            onStartControlsEditor = {
                                showOverlayEditor = true
                            },
                            onResetControlsLayout = {
                                overlayState = overlayState.copy(layout = OverlayStore.defaultLayout(context))
                            },
                            onShowHelp = { activeHelp = it },
                        )
                    }
                    if (wide) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            SettingsCategoryRail(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = {
                                    selectedCategory = it
                                    closeSearch()
                                },
                                modifier = Modifier
                                    .width(232.dp)
                                    .fillMaxHeight()
                            )
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                                pane(Modifier.widthIn(max = 760.dp).fillMaxSize())
                            }
                        }
                    } else {
                        pane(Modifier.fillMaxSize())
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !searchActive && hasPendingChanges && !viewModel.customDriverBusy,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            UnsavedChangesBar(saving = viewModel.saving, onSave = ::save)
        }

        if (showOverlayEditor) {
            OverlayLayoutEditorDialog(
                overlayConfig = overlayState.config,
                overlayLayout = overlayState.layout,
                onDismiss = { updatedLayout ->
                    overlayState = overlayState.copy(layout = updatedLayout)
                    showOverlayEditor = false
                }
            )
        }
    }

    if (showDiscardDialog) {
        SettingsUnsavedChangesDialog(
            saving = viewModel.saving,
            onCancel = { showDiscardDialog = false },
            onSaveAndExit = {
                showDiscardDialog = false
                saveSettings { restartRequired ->
                    if (restartRequired.isEmpty()) {
                        onBack()
                    } else {
                        closeAfterRestartNotice = true
                        pendingRestartSettings = restartRequired
                    }
                }
            },
            onDiscard = {
                showDiscardDialog = false
                viewModel.discardChanges()
                overlayState = originalOverlayState
                onBack()
            }
        )
    }

    if (showResetDefaultsDialog) {
        AppDialog(
            title = stringResource(R.string.settings_confirm_reset_defaults_title),
            onDismiss = { showResetDefaultsDialog = false },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { showResetDefaultsDialog = false }),
                DialogAction(stringResource(R.string.settings_reset_defaults), {
                    showResetDefaultsDialog = false
                    viewModel.resetToDefaults()
                    overlayState = OverlayStore.defaultState(context)
                }, ButtonTone.Accent)
            )
        ) {
            Text(stringResource(R.string.settings_confirm_reset_defaults_message))
        }
    }

    if (showDeleteCustomConfigDialog) {
        AppDialog(
            title = stringResource(R.string.settings_confirm_delete_custom_config_title),
            onDismiss = { showDeleteCustomConfigDialog = false },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { showDeleteCustomConfigDialog = false }),
                DialogAction(stringResource(R.string.action_delete), {
                    showDeleteCustomConfigDialog = false
                    OverlayStore.deleteGameOverride(context, overlayScopeId)
                    reloadOverlayState()
                    viewModel.deleteCustomConfig { restartRequired ->
                        if (restartRequired.isNotEmpty()) {
                            pendingRestartSettings = restartRequired
                        }
                    }
                }, ButtonTone.Danger)
            )
        ) {
            Text(stringResource(R.string.settings_confirm_delete_custom_config_message, appName ?: titleId ?: ""))
        }
    }

    if (showClearAllCustomConfigsDialog) {
        AppDialog(
            title = stringResource(R.string.settings_confirm_clear_all_title),
            onDismiss = { showClearAllCustomConfigsDialog = false },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { showClearAllCustomConfigsDialog = false }),
                DialogAction(stringResource(R.string.action_delete), {
                    showClearAllCustomConfigsDialog = false
                    OverlayStore.clearAllGameOverrides(context)
                    viewModel.clearAllCustomConfigs()
                }, ButtonTone.Danger)
            )
        ) {
            Text(stringResource(R.string.settings_confirm_clear_all_message))
        }
    }

    pendingCustomDriverRemoval?.let { driverName ->
        AppDialog(
            title = stringResource(R.string.settings_confirm_remove_custom_driver_title),
            onDismiss = { pendingCustomDriverRemoval = null },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { pendingCustomDriverRemoval = null }),
                DialogAction(stringResource(R.string.action_delete), {
                    pendingCustomDriverRemoval = null
                    viewModel.removeCustomDriver(driverName)
                }, ButtonTone.Danger)
            )
        ) {
            Text(stringResource(R.string.settings_confirm_remove_custom_driver_message, driverName))
        }
    }

    viewModel.operationResult?.let { result ->
        AppDialog(
            title = if (result.isError) stringResource(R.string.settings_opt_error) else stringResource(R.string.settings_success_title),
            onDismiss = { viewModel.dismissOperationResult() },
            actions = listOf(DialogAction(stringResource(R.string.action_ok), { viewModel.dismissOperationResult() }, ButtonTone.Accent))
        ) {
            Text(result.message)
        }
    }

    pendingRestartSettings?.let { settings ->
        AppDialog(
            title = stringResource(R.string.settings_restart_required_title),
            onDismiss = {},
            dismissible = false,
            actions = listOf(DialogAction(stringResource(R.string.action_ok), {
                pendingRestartSettings = null
                val shouldClose = closeAfterRestartNotice
                closeAfterRestartNotice = false
                if (shouldClose) {
                    onBack()
                }
            }, ButtonTone.Accent))
        ) {
            Text(stringResource(R.string.settings_restart_required_summary))
            Text(
                stringResource(R.string.settings_restart_required_message, formatRestartRequiredSettings(context, settings)),
                color = Mocha.Text
            )
        }
    }

    activeHelp?.let { help ->
        SettingsHelpSheet(help = help, onDismiss = { activeHelp = null })
    }
}

@Composable
private fun SettingsContentPane(
    modifier: Modifier,
    titleId: String?,
    isPerApp: Boolean,
    showCategoryStrip: Boolean,
    viewModel: SettingsViewModel,
    overlayConfig: OverlayConfig,
    supportedMemoryMappingMask: Int,
    customDriverLoadStatus: CustomDriverLoadStatus,
    availableCameras: List<String>,
    availableAdhocAddresses: List<Pair<String, String>>,
    selectedCategory: SettingsCategory,
    searchQuery: String,
    onCloseSearch: () -> Unit,
    onSelectedCategoryChange: (SettingsCategory) -> Unit,
    searchEntries: List<SettingsSearchEntry>,
    currentStoragePath: String,
    scrollState: androidx.compose.foundation.ScrollState,
    controllerConnected: Boolean,
    hasOverlayChanges: Boolean,
    connectedGamepads: List<org.vita3k.emulator.ConnectedGamepad>,
    categories: List<SettingsCategory>,
    onChangeStorageFolder: () -> Unit,
    onResetStorageFolder: () -> Unit,
    onInstallCustomDriver: () -> Unit,
    onDownloadCustomDriver: () -> Unit,
    onPickCameraImage: (Boolean) -> Unit,
    onRequestRemoveCustomDriver: (String) -> Unit,
    onClearAllCustomConfigs: () -> Unit,
    onOverlayConfigChange: (OverlayConfig) -> Unit,
    onStartControlsEditor: (() -> Unit)?,
    onResetControlsLayout: (() -> Unit)?,
    onShowHelp: (SettingsHelpEntry) -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showCategoryStrip) {
            SettingsCategoryStrip(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onSelectedCategoryChange,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = if (showCategoryStrip) 0.dp else 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        if (isPerApp && viewModel.hasCustomConfig) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Mocha.Lavender.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Mocha.Lavender, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.settings_custom_config_banner),
                    style = MaterialTheme.typography.labelLarge,
                    color = Mocha.Lavender
                )
            }
        }

        if (searchQuery.isBlank()) {
            SettingsCategoryBody(
                category = selectedCategory,
                cfg = viewModel.config,
                overlayConfig = overlayConfig,
                modulesList = viewModel.modulesList,
                modulesSearch = viewModel.modulesSearch,
                onModulesSearchChange = viewModel::onModulesSearchChange,
                onToggleModule = viewModel::toggleModule,
                supportedMemoryMappingMask = supportedMemoryMappingMask,
                customDriverLoadStatus = customDriverLoadStatus,
                availableCameras = availableCameras,
                availableAdhocAddresses = availableAdhocAddresses,
                currentStoragePath = currentStoragePath,
                defaultStoragePath = viewModel.defaultStoragePath,
                onChangeStorageFolder = onChangeStorageFolder,
                onResetStorageFolder = onResetStorageFolder,
                installedCustomDrivers = viewModel.installedCustomDrivers,
                customDriverBusy = viewModel.customDriverBusy,
                onInstallCustomDriver = onInstallCustomDriver,
                onDownloadCustomDriver = onDownloadCustomDriver,
                onPickCameraImage = onPickCameraImage,
                onRequestRemoveCustomDriver = onRequestRemoveCustomDriver,
                isPerApp = isPerApp,
                showCustomDriverManagement = viewModel.showCustomDriverOptions,
                showTurboModeOption = viewModel.showCustomDriverOptions,
                onClearAllCustomConfigs = onClearAllCustomConfigs,
                onSelectCustomDriver = viewModel::selectCustomDriver,
                onUpdate = viewModel::update,
                onOverlayConfigChange = onOverlayConfigChange,
                onStartControlsEditor = onStartControlsEditor,
                onResetControlsLayout = onResetControlsLayout,
                controllerConnected = controllerConnected,
                connectedGamepads = connectedGamepads,
                onShowHelp = onShowHelp
            )
        } else {
            SettingsSearchResults(
                query = searchQuery,
                entries = searchEntries,
                onOpen = {
                    onSelectedCategoryChange(it)
                    onCloseSearch()
                },
                onShowHelp = onShowHelp
            )
        }

        }
    }
}

@Composable
internal fun SettingsHeader(
    title: String,
    eyebrow: String?,
    searchActive: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onBack: () -> Unit,
    overflow: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolbarButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back), onBack)
        if (searchActive) {
            SearchField(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = stringResource(R.string.settings_search_placeholder),
                autoFocus = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            ) {
                if (eyebrow != null) {
                    Text(eyebrow.uppercase(), style = MonoSmall, color = Mocha.Mauve)
                }
                Text(
                    title,
                    style = if (eyebrow != null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    color = Mocha.Text,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        ToolbarButton(
            if (searchActive) Icons.Default.Close else Icons.Default.Search,
            if (searchActive) stringResource(R.string.apps_list_cd_close_search) else stringResource(R.string.apps_list_cd_search),
            onToggleSearch
        )
        overflow()
    }
}

@Composable
internal fun UnsavedChangesBar(saving: Boolean, onSave: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Mocha.Mantle)
            .border(1.dp, Mocha.Surface1, RoundedCornerShape(18.dp))
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Mocha.Peach)
        )
        Text(
            stringResource(R.string.settings_unsaved_bar),
            style = MaterialTheme.typography.labelLarge,
            color = Mocha.Text,
            modifier = Modifier.weight(1f)
        )
        AppButton(
            text = stringResource(R.string.settings_save),
            onClick = onSave,
            icon = Icons.Default.Save,
            enabled = !saving
        )
    }
}

@Composable
private fun SettingsOverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showDeleteCustomConfig: Boolean,
    onResetDefaults: () -> Unit,
    onDeleteCustomConfig: () -> Unit
) {
    Box {
        ToolbarButton(Icons.Default.MoreVert, stringResource(R.string.settings_more_options), { onExpandedChange(true) })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            containerColor = Mocha.Mantle,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Mocha.Surface0)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings_reset_defaults), color = Mocha.Text) },
                onClick = onResetDefaults
            )
            if (showDeleteCustomConfig) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings_delete_custom_config), color = Mocha.Red) },
                    onClick = onDeleteCustomConfig
                )
            }
        }
    }
}

@Composable
private fun SettingsUnsavedChangesDialog(
    saving: Boolean,
    onCancel: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDiscard: () -> Unit
) {
    AppDialog(
        title = stringResource(R.string.settings_unsaved_title),
        onDismiss = onCancel,
        actions = listOf(
            DialogAction(stringResource(R.string.action_cancel), onCancel, enabled = !saving),
            DialogAction(stringResource(R.string.settings_discard), onDiscard, ButtonTone.Danger, enabled = !saving),
            DialogAction(stringResource(R.string.settings_save_and_exit), onSaveAndExit, ButtonTone.Accent, enabled = !saving)
        )
    ) {
        Text(stringResource(R.string.settings_unsaved_message))
    }
}
