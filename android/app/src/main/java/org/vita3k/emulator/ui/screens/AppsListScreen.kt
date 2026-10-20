package org.vita3k.emulator.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.vita3k.emulator.R
import org.vita3k.emulator.data.AppInfo
import org.vita3k.emulator.data.FirmwareComponent
import org.vita3k.emulator.data.FirmwareInstallState
import org.vita3k.emulator.data.SortOption
import org.vita3k.emulator.data.UpdateCheckResult
import org.vita3k.emulator.data.UpdateCheckStatus
import org.vita3k.emulator.data.ViewMode
import org.vita3k.emulator.ui.components.ActionRow
import org.vita3k.emulator.ui.components.AppButton
import org.vita3k.emulator.ui.components.AppDialog
import org.vita3k.emulator.ui.components.AppProgressBar
import org.vita3k.emulator.ui.components.AppSheet
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.CompatLabel
import org.vita3k.emulator.ui.components.DialogAction
import org.vita3k.emulator.ui.components.EmptyState
import org.vita3k.emulator.ui.components.EyebrowLabel
import org.vita3k.emulator.ui.components.HtmlText
import org.vita3k.emulator.ui.components.Panel
import org.vita3k.emulator.ui.components.SearchField
import org.vita3k.emulator.ui.components.Segmented
import org.vita3k.emulator.ui.components.Tag
import org.vita3k.emulator.ui.components.ToolbarButton
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoMedium
import org.vita3k.emulator.ui.theme.MonoSmall
import org.vita3k.emulator.ui.viewmodel.AppAction
import org.vita3k.emulator.ui.viewmodel.AppActionGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsListScreen(
    apps: List<AppInfo>,
    initialized: Boolean,
    loading: Boolean,
    appVersion: String,
    searchQuery: String,
    sortOption: SortOption,
    viewMode: ViewMode,
    updateCheckInProgress: Boolean,
    updateCheckResult: UpdateCheckResult?,
    firmwareInstallState: FirmwareInstallState,
    actionInProgress: Boolean,
    actionResultMessage: String?,
    selectionMode: Boolean,
    selectedAppIds: Set<String>,
    resolveAvailableActions: (AppInfo) -> Set<AppAction>,
    onAppSelected: (AppInfo) -> Unit,
    onRunAppAction: (AppInfo, AppAction) -> Unit,
    onPrepareAppActions: (AppInfo) -> Unit,
    onShowAppInfo: (AppInfo) -> Unit,
    onToggleAppSelection: (AppInfo) -> Unit,
    onSetSelectionMode: (Boolean) -> Unit,
    onSelectAllVisible: () -> Unit,
    onRunBatchDeleteSelected: () -> Unit,
    onDismissActionResult: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onSortChanged: (SortOption) -> Unit,
    onViewModeToggle: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onDismissUpdateCheckResult: () -> Unit,
    onRefresh: () -> Unit,
    onInstallClick: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenTrophyManager: () -> Unit = {},
    onOpenUserManagement: () -> Unit = {},
    onOpenWelcomeScreen: () -> Unit = {},
    onOpenCustomConfig: (AppInfo) -> Unit = {}
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    var selectedAppForActions by remember { mutableStateOf<AppInfo?>(null) }
    var actionTargetApp by remember { mutableStateOf<AppInfo?>(null) }
    var pendingAction by remember { mutableStateOf<AppAction?>(null) }
    var runningAction by remember { mutableStateOf<AppAction?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val openActions: (AppInfo) -> Unit = {
        selectedAppForActions = it
        onPrepareAppActions(it)
    }
    // the most recently played game gets a shortcut above the library while nothing is filtering it
    val resumeApp = remember(apps, searchQuery, selectionMode) {
        if (searchQuery.isNotEmpty() || selectionMode) null else apps.filter { it.lastPlayed > 0 }.maxByOrNull { it.lastPlayed }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            if (selectionMode) {
                SelectionHeader(
                    count = selectedAppIds.size,
                    onExit = { onSetSelectionMode(false) },
                    onSelectAll = onSelectAllVisible,
                    onDelete = { showBatchDeleteConfirm = true }
                )
            } else {
                LibraryHeader(
                    appVersion = appVersion,
                    appCount = apps.size,
                    searchQuery = searchQuery,
                    viewMode = viewMode,
                    onSearchChanged = onSearchChanged,
                    onOpenSort = { showFilterSheet = true },
                    onViewModeToggle = onViewModeToggle,
                    onOpenSettings = onOpenSettings,
                    onOpenMenu = { showMenuSheet = true }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    loading && apps.isEmpty() -> {
                        CircularProgressIndicator(
                            color = Mocha.Mauve,
                            trackColor = Mocha.Surface0,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    !initialized -> {
                        EmptyState(
                            icon = Icons.Default.Memory,
                            title = stringResource(R.string.apps_list_init_failed),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    apps.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            when {
                                searchQuery.isNotEmpty() -> EmptyState(
                                    icon = Icons.Default.SearchOff,
                                    title = stringResource(R.string.apps_list_empty_search, searchQuery)
                                )
                                !firmwareInstallState.hasRequiredComponents -> FirmwareMissingEmptyState(
                                    firmwareInstallState = firmwareInstallState,
                                    onInstallClick = onInstallClick
                                )
                                else -> EmptyState(
                                    icon = Icons.Default.Inventory2,
                                    title = stringResource(R.string.apps_list_empty_no_apps),
                                    body = stringResource(R.string.library_empty_body)
                                ) {
                                    AppButton(
                                        text = stringResource(R.string.apps_list_install_app),
                                        icon = Icons.Default.Add,
                                        onClick = onInstallClick
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        val onClick: (AppInfo) -> Unit = { app ->
                            if (selectionMode) onToggleAppSelection(app) else onAppSelected(app)
                        }
                        val onLongClick: (AppInfo) -> Unit = { app ->
                            if (selectionMode) onToggleAppSelection(app) else openActions(app)
                        }
                        when (viewMode) {
                            ViewMode.GRID -> LibraryGrid(
                                apps = apps,
                                resumeApp = resumeApp,
                                selectionMode = selectionMode,
                                selectedAppIds = selectedAppIds,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                onPlay = onAppSelected
                            )
                            ViewMode.LIST -> LibraryList(
                                apps = apps,
                                resumeApp = resumeApp,
                                selectionMode = selectionMode,
                                selectedAppIds = selectedAppIds,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                onPlay = onAppSelected
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !selectionMode,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            InstallButton(onClick = onInstallClick)
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            sortOption = sortOption,
            viewMode = viewMode,
            onSortChanged = onSortChanged,
            onViewModeToggle = onViewModeToggle,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showMenuSheet) {
        LibraryMenuSheet(
            onDismiss = { showMenuSheet = false },
            onSelect = { onSetSelectionMode(true) },
            onRefresh = onRefresh,
            onTrophyManager = onOpenTrophyManager,
            onUserManagement = onOpenUserManagement,
            onWelcomeScreen = onOpenWelcomeScreen,
            onCheckForUpdates = onCheckForUpdates,
            onAbout = { showAboutSheet = true }
        )
    }

    if (showAboutSheet) {
        AboutSheet(
            appVersion = appVersion,
            onDismiss = { showAboutSheet = false }
        )
    }

    selectedAppForActions?.let { app ->
        AppActionsSheet(
            app = app,
            availableActions = resolveAvailableActions(app),
            onDismiss = { selectedAppForActions = null },
            onPlay = {
                selectedAppForActions = null
                onAppSelected(app)
            },
            onActionSelected = { action ->
                actionTargetApp = app
                selectedAppForActions = null
                pendingAction = action
            },
            onShowInfo = {
                selectedAppForActions = null
                onShowAppInfo(app)
            },
            onCustomConfig = {
                selectedAppForActions = null
                onOpenCustomConfig(app)
            },
            onSelect = {
                selectedAppForActions = null
                onSetSelectionMode(true)
                onToggleAppSelection(app)
            }
        )
    }

    if (showBatchDeleteConfirm) {
        AppDialog(
            title = pluralStringResource(R.plurals.batch_delete_title, selectedAppIds.size, selectedAppIds.size),
            onDismiss = { showBatchDeleteConfirm = false },
            actions = listOf(
                DialogAction(stringResource(R.string.action_cancel), { showBatchDeleteConfirm = false }),
                DialogAction(stringResource(R.string.action_delete), {
                    onRunBatchDeleteSelected()
                    showBatchDeleteConfirm = false
                }, ButtonTone.Danger)
            )
        ) {
            Text(pluralStringResource(R.plurals.batch_delete_message, selectedAppIds.size, selectedAppIds.size))
        }
    }

    val action = pendingAction
    if (action != null) {
        val actionLabel = stringResource(action.labelResId)
        val dismiss = {
            pendingAction = null
            actionTargetApp = null
        }
        AppDialog(
            title = if (action.destructive) stringResource(R.string.confirm_delete_title, actionLabel)
            else stringResource(R.string.confirm_reset_playtime_title),
            onDismiss = dismiss,
            actions = listOf(
                DialogAction(
                    if (action.destructive) stringResource(R.string.action_cancel) else stringResource(R.string.action_no),
                    dismiss
                ),
                DialogAction(
                    if (action.destructive) stringResource(R.string.action_delete) else stringResource(R.string.action_yes),
                    {
                        actionTargetApp?.let { onRunAppAction(it, action) }
                        runningAction = action
                        dismiss()
                    },
                    if (action.destructive) ButtonTone.Danger else ButtonTone.Accent
                )
            )
        ) {
            Text(
                if (action.destructive) stringResource(R.string.confirm_delete_message, actionLabel)
                else stringResource(
                    R.string.confirm_reset_playtime_message,
                    actionTargetApp?.title ?: stringResource(R.string.confirm_reset_playtime_this_app)
                )
            )
        }
    }

    LaunchedEffect(actionInProgress) {
        if (!actionInProgress) runningAction = null
    }
    if (actionInProgress) {
        AppDialog(
            title = runningAction?.takeIf { it.destructive }?.let { stringResource(R.string.action_deleting, stringResource(it.labelResId)) }
                ?: stringResource(R.string.action_applying),
            onDismiss = {},
            dismissible = false
        ) {
            AppProgressBar()
        }
    }

    actionResultMessage?.let { message ->
        AppDialog(
            title = stringResource(R.string.action_done),
            onDismiss = onDismissActionResult,
            actions = listOf(DialogAction(stringResource(R.string.action_ok), onDismissActionResult, ButtonTone.Accent))
        ) {
            Text(message)
        }
    }

    updateCheckResult?.let { result ->
        UpdateCheckDialog(result = result, onDismiss = onDismissUpdateCheckResult)
    }

    if (updateCheckInProgress) {
        AppDialog(
            title = stringResource(R.string.updates_check_title),
            onDismiss = {},
            dismissible = false
        ) {
            Text(stringResource(R.string.updates_check_progress))
            AppProgressBar()
        }
    }
}

// ---- Header -------------------------------------------------------------------------------------

private data class ParsedAppVersion(
    val versionLabel: String,
    val buildNumber: String? = null,
    val revision: String? = null
)

private fun parseAppVersion(appVersion: String): ParsedAppVersion {
    if (appVersion.isBlank()) {
        return ParsedAppVersion(versionLabel = "")
    }

    val parts = appVersion.split("-", limit = 3)
    return ParsedAppVersion(
        versionLabel = parts.getOrNull(0).orEmpty().ifBlank { appVersion },
        buildNumber = parts.getOrNull(1)?.takeIf { it.isNotBlank() },
        revision = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
    )
}

@Composable
private fun LibraryHeader(
    appVersion: String,
    appCount: Int,
    searchQuery: String,
    viewMode: ViewMode,
    onSearchChanged: (String) -> Unit,
    onOpenSort: () -> Unit,
    onViewModeToggle: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMenu: () -> Unit
) {
    val version = remember(appVersion) { parseAppVersion(appVersion) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.apps_list_app_title),
                        style = MonoSmall,
                        color = Mocha.Mauve
                    )
                    if (version.versionLabel.isNotBlank()) {
                        Text(
                            listOfNotNull(version.versionLabel, version.buildNumber?.let { "#$it" }).joinToString(" "),
                            style = MonoSmall,
                            color = Mocha.Overlay1
                        )
                    }
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.library_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Mocha.Text
                    )
                    if (appCount > 0) {
                        Text(
                            appCount.toString(),
                            style = MonoMedium,
                            color = Mocha.Overlay1,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
            }
            ToolbarButton(Icons.Default.Settings, stringResource(R.string.settings_cd_open), onOpenSettings)
            ToolbarButton(Icons.Default.MoreHoriz, stringResource(R.string.apps_list_cd_more_options), onOpenMenu)
        }
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchField(
                query = searchQuery,
                onQueryChange = onSearchChanged,
                placeholder = stringResource(R.string.apps_list_search_placeholder),
                clearDescription = stringResource(R.string.apps_list_cd_close_search),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mocha.Surface0.copy(alpha = 0.7f))
            ) {
                ToolbarButton(
                    Icons.AutoMirrored.Filled.Sort,
                    stringResource(R.string.apps_list_cd_sort),
                    onOpenSort,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Segmented(
                options = listOf(
                    Icons.Default.GridView to stringResource(R.string.filter_view_grid),
                    Icons.AutoMirrored.Filled.List to stringResource(R.string.filter_view_list)
                ),
                selectedIndex = if (viewMode == ViewMode.GRID) 0 else 1,
                onSelect = { index ->
                    val wanted = if (index == 0) ViewMode.GRID else ViewMode.LIST
                    if (wanted != viewMode) onViewModeToggle()
                },
                iconsOnly = true
            )
        }
    }
}

@Composable
private fun SelectionHeader(
    count: Int,
    onExit: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Mocha.Mauve.copy(alpha = 0.12f))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(Icons.Default.Close, stringResource(R.string.apps_list_cd_exit_selection), onExit, tint = Mocha.Mauve)
        Text(
            pluralStringResource(R.plurals.apps_list_n_selected, count, count),
            style = MaterialTheme.typography.titleMedium,
            color = Mocha.Mauve,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        ToolbarButton(Icons.Default.DoneAll, stringResource(R.string.apps_list_cd_select_all), onSelectAll, tint = Mocha.Mauve)
        ToolbarButton(
            Icons.Default.Delete,
            stringResource(R.string.apps_list_cd_delete_selected),
            onDelete,
            enabled = count > 0,
            tint = Mocha.Red
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstallButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Mocha.Mauve)
            .combinedClickable(onClick = onClick)
            .padding(start = 16.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Mocha.Crust)
        Text(stringResource(R.string.apps_list_cd_install), style = MaterialTheme.typography.titleSmall, color = Mocha.Crust)
    }
}

// ---- Library ------------------------------------------------------------------------------------

private val LibraryBottomPadding = 104.dp

@Composable
private fun LibraryGrid(
    apps: List<AppInfo>,
    resumeApp: AppInfo?,
    selectionMode: Boolean,
    selectedAppIds: Set<String>,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
    onPlay: (AppInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 112.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = LibraryBottomPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (resumeApp != null) {
            item(key = "resume", span = { GridItemSpan(maxLineSpan) }) {
                ResumeCard(app = resumeApp, onPlay = { onPlay(resumeApp) }, onLongClick = { onLongClick(resumeApp) })
            }
        }
        items(apps, key = { it.titleId }) { app ->
            GameTile(
                app = app,
                selected = app.titleId in selectedAppIds,
                selectionMode = selectionMode,
                onClick = { onClick(app) },
                onLongClick = { onLongClick(app) }
            )
        }
    }
}

@Composable
private fun LibraryList(
    apps: List<AppInfo>,
    resumeApp: AppInfo?,
    selectionMode: Boolean,
    selectedAppIds: Set<String>,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
    onPlay: (AppInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = LibraryBottomPadding)
    ) {
        if (resumeApp != null) {
            item(key = "resume") {
                ResumeCard(
                    app = resumeApp,
                    onPlay = { onPlay(resumeApp) },
                    onLongClick = { onLongClick(resumeApp) },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
                )
            }
        }
        items(apps, key = { it.titleId }) { app ->
            GameRow(
                app = app,
                selected = app.titleId in selectedAppIds,
                selectionMode = selectionMode,
                onClick = { onClick(app) },
                onLongClick = { onLongClick(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResumeCard(
    app: AppInfo,
    onPlay: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Mocha.Mantle)
            .border(1.dp, Mocha.Surface0, RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onPlay,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppIcon(app = app, size = 76, cornerRadius = 14.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            EyebrowLabel(stringResource(R.string.library_resume), color = Mocha.Mauve)
            Text(
                app.title,
                style = MaterialTheme.typography.titleMedium,
                color = Mocha.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                listOfNotNull(app.titleId, formatPlaytimeShort(app.playtime)).joinToString("  ·  "),
                style = MonoSmall,
                color = Mocha.Overlay1,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Mocha.Mauve)
                .combinedClickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.library_play), tint = Mocha.Crust)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameTile(
    app: AppInfo,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(
                    if (selected) Modifier.border(2.dp, Mocha.Mauve, shape)
                    else Modifier.border(1.dp, Mocha.Surface0, shape)
                )
                .padding(if (selected) 4.dp else 0.dp)
        ) {
            AppIconFill(app = app, cornerRadius = if (selected) 12.dp else 16.dp)
            if (selectionMode) {
                SelectionMark(selected = selected, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            }
        }
        Text(
            app.title,
            style = MaterialTheme.typography.labelLarge,
            color = Mocha.Text,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Row(
            modifier = Modifier.padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompatLabel(
                state = app.compatibility,
                label = stringResource(app.compatibility.labelResId),
                modifier = Modifier.weight(1f, fill = false)
            )
            if (app.hasCustomConfig) CustomConfigTag()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameRow(
    app: AppInfo,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Mocha.Mauve.copy(alpha = 0.10f) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AppIcon(app = app, size = 52, cornerRadius = 12.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                app.title,
                style = MaterialTheme.typography.titleSmall,
                color = Mocha.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(app.titleId, style = MonoSmall, color = Mocha.Overlay1)
                CompatLabel(
                    state = app.compatibility,
                    label = stringResource(app.compatibility.labelResId),
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (app.hasCustomConfig) CustomConfigTag()
            }
        }
        if (selectionMode) {
            SelectionMark(selected = selected)
        } else {
            formatPlaytimeShort(app.playtime)?.let {
                Text(it, style = MonoSmall, color = Mocha.Overlay1)
            }
        }
    }
}

@Composable
private fun SelectionMark(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (selected) Mocha.Mauve else Mocha.Crust.copy(alpha = 0.7f))
            .border(1.5.dp, if (selected) Mocha.Mauve else Mocha.Overlay1, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.apps_list_cd_selected),
                tint = Mocha.Crust,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CustomConfigTag() {
    Tag(text = stringResource(R.string.apps_list_custom_config_badge), color = Mocha.Lavender)
}

private fun formatPlaytimeShort(seconds: Long): String? {
    if (seconds < 60) return null
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

// ---- Empty states -------------------------------------------------------------------------------

@Composable
private fun FirmwareMissingEmptyState(
    firmwareInstallState: FirmwareInstallState,
    onInstallClick: () -> Unit
) {
    val missingLabels = remember(firmwareInstallState) {
        firmwareInstallState.components.missingRequiredComponentsInOrder.map(::firmwareComponentLabelRes)
    }
    EmptyState(
        icon = Icons.Default.SystemUpdate,
        title = stringResource(R.string.apps_list_empty_firmware_title),
        body = stringResource(R.string.apps_list_empty_firmware_body)
    ) {
        Panel(
            modifier = Modifier.padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            spacing = 8.dp
        ) {
            EyebrowLabel(stringResource(R.string.apps_list_empty_firmware_missing_list_title))
            missingLabels.forEach { labelResId ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Mocha.Peach)
                    )
                    Text(stringResource(labelResId), style = MaterialTheme.typography.bodyMedium, color = Mocha.Text)
                }
            }
        }
        AppButton(
            text = stringResource(R.string.apps_list_install_firmware),
            icon = Icons.Default.SystemUpdate,
            onClick = onInstallClick
        )
    }
}

private fun firmwareComponentLabelRes(component: FirmwareComponent): Int {
    return when (component) {
        FirmwareComponent.Preinstalled -> R.string.initial_setup_preinstall_title
        FirmwareComponent.Main -> R.string.initial_setup_main_title
        FirmwareComponent.FontPackage -> R.string.initial_setup_font_title
    }
}

// ---- Sheets -------------------------------------------------------------------------------------

@Composable
private fun LibraryMenuSheet(
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
    onRefresh: () -> Unit,
    onTrophyManager: () -> Unit,
    onUserManagement: () -> Unit,
    onWelcomeScreen: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onAbout: () -> Unit
) {
    // close the sheet before the action so a screen change does not race the sheet animation
    fun pick(action: () -> Unit): () -> Unit = {
        onDismiss()
        action()
    }
    AppSheet(onDismiss = onDismiss) {
        ActionRow(stringResource(R.string.apps_list_menu_trophies), pick(onTrophyManager), icon = Icons.Default.EmojiEvents, iconTint = Mocha.Yellow)
        ActionRow(stringResource(R.string.apps_list_menu_users), pick(onUserManagement), icon = Icons.Default.People, iconTint = Mocha.Blue)
        ActionRow(stringResource(R.string.library_select), pick(onSelect), icon = Icons.Default.ChecklistRtl, iconTint = Mocha.Mauve)
        ActionRow(stringResource(R.string.apps_list_cd_refresh), pick(onRefresh), icon = Icons.Default.Refresh, iconTint = Mocha.Teal)
        ActionRow(stringResource(R.string.apps_list_menu_check_updates), pick(onCheckForUpdates), icon = Icons.Default.SystemUpdate, iconTint = Mocha.Green)
        ActionRow(stringResource(R.string.apps_list_menu_welcome), pick(onWelcomeScreen), icon = Icons.Default.WavingHand, iconTint = Mocha.Peach)
        ActionRow(stringResource(R.string.apps_list_menu_about), pick(onAbout), icon = Icons.Default.Info, iconTint = Mocha.Lavender)
    }
}

private const val ABOUT_DESCRIPTION_HTML =
    """Vita3K is the world's first functional PS Vita and PS TV emulator, open source and written in C++ for Windows, Linux, macOS, and Android. Visit <a href="https://vita3k.org/quickstart.html">vita3k.org</a> for more info, browse the project on <a href="https://github.com/Vita3K/Vita3K">GitHub</a> if you want to contribute, or support us on <a href="https://ko-fi.com/vita3k">Ko-fi</a>."""

private const val ABOUT_CREDIT_HTML =
    """Icon by <a href="https://gordonmackayillustration.blogspot.com">Gordon Mackay</a>. Type set in IBM Plex. Colours from <a href="https://catppuccin.com">Catppuccin</a>."""

private const val ABOUT_FOOTER_HTML =
    """<a href="https://vita3k.org">Website</a> &nbsp;·&nbsp; <a href="https://github.com/Vita3K/Vita3K">GitHub</a> &nbsp;·&nbsp; <a href="https://ko-fi.com/vita3k">Ko-fi</a> &nbsp;·&nbsp; <a href="https://discord.com/invite/6aGwQzh">Discord</a>"""

@Composable
private fun AboutSheet(
    appVersion: String,
    onDismiss: () -> Unit
) {
    val parsedVersion = remember(appVersion) { parseAppVersion(appVersion) }
    AppSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_plus),
                contentDescription = stringResource(R.string.apps_list_app_title),
                modifier = Modifier.size(96.dp)
            )
            Text(stringResource(R.string.apps_list_app_title), style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (parsedVersion.versionLabel.isNotBlank()) Tag(parsedVersion.versionLabel, Mocha.Mauve)
                parsedVersion.buildNumber?.let { Tag(stringResource(R.string.about_build, it), Mocha.Lavender) }
                parsedVersion.revision?.let { Tag(stringResource(R.string.about_revision, it), Mocha.Overlay2) }
            }
            HtmlText(
                html = ABOUT_DESCRIPTION_HTML,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                textColor = Mocha.Subtext1,
                linkColor = Mocha.Mauve,
                gravity = Gravity.CENTER
            )
            Text(
                text = stringResource(R.string.about_legal_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = Mocha.Peach,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mocha.Peach.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            HtmlText(
                html = ABOUT_CREDIT_HTML,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall,
                textColor = Mocha.Overlay1,
                linkColor = Mocha.Subtext1,
                gravity = Gravity.CENTER
            )
            HtmlText(
                html = ABOUT_FOOTER_HTML,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleSmall,
                textColor = Mocha.Overlay1,
                linkColor = Mocha.Mauve,
                gravity = Gravity.CENTER
            )
        }
    }
}

@Composable
private fun AppActionsSheet(
    app: AppInfo,
    availableActions: Set<AppAction>,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onActionSelected: (AppAction) -> Unit,
    onShowInfo: () -> Unit,
    onCustomConfig: () -> Unit,
    onSelect: () -> Unit
) {
    var showDeleteOptions by remember { mutableStateOf(false) }
    val availableDeletes = AppAction.entries.filter { it.group == AppActionGroup.DELETE && availableActions.contains(it) }
    val otherActions = AppAction.entries.filter { it.group == AppActionGroup.OTHER }

    AppSheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppIcon(app = app, size = 56, cornerRadius = 12.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(app.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(app.titleId, style = MonoSmall, color = Mocha.Overlay1)
                    CompatLabel(app.compatibility, stringResource(app.compatibility.labelResId))
                }
            }
            if (!showDeleteOptions) {
                AppButton(stringResource(R.string.library_play), onPlay, icon = Icons.Default.PlayArrow)
            }
        }

        if (showDeleteOptions) {
            availableDeletes.forEach { action ->
                ActionRow(
                    title = stringResource(action.labelResId),
                    onClick = { onActionSelected(action) },
                    icon = Icons.Default.Delete,
                    tint = Mocha.Red,
                    iconTint = Mocha.Red
                )
            }
            ActionRow(
                title = stringResource(R.string.action_back),
                onClick = { showDeleteOptions = false },
                icon = Icons.AutoMirrored.Filled.ArrowBack
            )
            return@AppSheet
        }

        ActionRow(stringResource(R.string.app_menu_information), onShowInfo, icon = Icons.Default.Info)
        ActionRow(stringResource(R.string.app_menu_custom_config), onCustomConfig, icon = Icons.Default.Tune)
        ActionRow(stringResource(R.string.library_select), onSelect, icon = Icons.Default.ChecklistRtl)
        otherActions.forEach { action ->
            ActionRow(
                title = stringResource(action.labelResId),
                onClick = { onActionSelected(action) },
                icon = if (action == AppAction.RESET_LAST_PLAYED) Icons.Default.History else Icons.Default.Refresh,
                enabled = availableActions.contains(action)
            )
        }
        if (availableDeletes.isNotEmpty()) {
            ActionRow(
                title = stringResource(R.string.app_menu_delete),
                onClick = { showDeleteOptions = true },
                icon = Icons.Default.Delete,
                tint = Mocha.Red,
                iconTint = Mocha.Red
            )
        }
    }
}

// ---- Updates ------------------------------------------------------------------------------------

@Composable
private fun UpdateCheckDialog(
    result: UpdateCheckResult,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val defaultDownloadUrl = "https://github.com/nckstwrt/Vita3K-Plus/releases/latest"
    val latestVersion = result.info.version.ifBlank {
        if (result.info.buildNumber > 0L) "Build ${result.info.buildNumber}" else stringResource(R.string.updates_latest_build)
    }
    val publishedAt = remember(result.info.publishedAt) { formatPublishedAt(result.info.publishedAt) }
    val downloadUrl = result.info.releaseUrl.ifBlank { defaultDownloadUrl }

    val titleRes = when (result.status) {
        UpdateCheckStatus.UpdateAvailable -> R.string.updates_available_title
        UpdateCheckStatus.CustomBuildCanUpdate -> R.string.updates_official_available_title
        else -> R.string.updates_check_title
    }

    val summary = remember(result, latestVersion, publishedAt) {
        when (result.status) {
            UpdateCheckStatus.UpdateAvailable ->
                if (publishedAt.isBlank()) {
                    context.getString(R.string.updates_summary_available, result.currentDisplayVersion.ifBlank { "Unknown" }, latestVersion)
                } else {
                    context.getString(R.string.updates_summary_available_with_date, result.currentDisplayVersion.ifBlank { "Unknown" }, latestVersion, publishedAt)
                }
            UpdateCheckStatus.CustomBuildCanUpdate ->
                if (publishedAt.isBlank()) {
                    context.getString(R.string.updates_summary_custom_build, latestVersion)
                } else {
                    context.getString(R.string.updates_summary_custom_build_with_date, latestVersion, publishedAt)
                }
            else -> result.message
        }
    }

    val showDownloadAction = result.status == UpdateCheckStatus.UpdateAvailable ||
        result.status == UpdateCheckStatus.CustomBuildCanUpdate

    AppDialog(
        title = stringResource(titleRes),
        onDismiss = onDismiss,
        actions = if (showDownloadAction) listOf(
            DialogAction(stringResource(R.string.action_cancel), onDismiss),
            DialogAction(stringResource(R.string.updates_open_download_page), {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)))
                onDismiss()
            }, ButtonTone.Accent)
        ) else listOf(DialogAction(stringResource(R.string.action_ok), onDismiss, ButtonTone.Accent))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(summary)
            if (result.info.notes.isNotBlank()) {
                Text(result.info.notes, style = MonoSmall, color = Mocha.Subtext0)
            }
        }
    }
}

private fun formatPublishedAt(publishedAt: String): String {
    if (publishedAt.isBlank()) {
        return ""
    }

    return runCatching {
        val instant = java.time.Instant.parse(publishedAt)
        java.time.format.DateTimeFormatter
            .ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)
            .withZone(java.time.ZoneId.systemDefault())
            .format(instant)
    }.getOrElse { publishedAt }
}

// ---- Shared pieces ------------------------------------------------------------------------------

@Composable
internal fun AppIcon(app: AppInfo, size: Int, modifier: Modifier = Modifier, cornerRadius: Dp = 12.dp) {
    Box(modifier = modifier.size(size.dp)) {
        AppIconFill(app = app, cornerRadius = cornerRadius)
    }
}

/** The app's icon filling its parent, or its initials when it has none. */
@Composable
private fun AppIconFill(app: AppInfo, cornerRadius: Dp) {
    val shape = RoundedCornerShape(cornerRadius)
    val iconFile = app.iconFile
    // Android Studio previews and screenshot tests run no image loader, so decode the icon in place there
    val previewBitmap = if (LocalInspectionMode.current && iconFile != null) {
        remember(iconFile) { runCatching { iconFile.inputStream().use { BitmapFactory.decodeStream(it) } }.getOrNull()?.asImageBitmap() }
    } else {
        null
    }
    if (previewBitmap != null) {
        Image(
            bitmap = previewBitmap,
            contentDescription = app.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        )
    } else if (iconFile != null) {
        AsyncImage(
            model = iconFile,
            contentDescription = app.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Mocha.Surface0)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Mocha.Surface0),
            contentAlignment = Alignment.Center
        ) {
            Text(
                app.title.filter { it.isLetterOrDigit() }.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Mocha.Overlay2
            )
        }
    }
}

@Composable
internal fun CompatBadge(app: AppInfo) {
    CompatLabel(app.compatibility, stringResource(app.compatibility.labelResId))
}
