package org.vita3k.emulator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.ui.components.ActionRow
import org.vita3k.emulator.ui.components.AppDialog
import org.vita3k.emulator.ui.components.AppProgressBar
import org.vita3k.emulator.ui.components.AppSheet
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.DialogAction
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoMedium
import org.vita3k.emulator.ui.theme.MonoSmall
import org.vita3k.emulator.ui.viewmodel.DeleteSourceOption
import org.vita3k.emulator.ui.viewmodel.InstallResult
import org.vita3k.emulator.ui.viewmodel.InstallResultStatus
import org.vita3k.emulator.ui.viewmodel.InstallType

@Composable
fun InstallBottomSheet(
    onDismiss: () -> Unit,
    onSelectType: (InstallType) -> Unit
) {
    AppSheet(onDismiss = onDismiss, title = stringResource(R.string.install_sheet_title)) {
        ActionRow(
            title = stringResource(R.string.install_pkg_title),
            subtitle = stringResource(R.string.install_pkg_desc),
            onClick = { onSelectType(InstallType.PKG) },
            icon = Icons.Default.Inventory2,
            iconTint = Mocha.Mauve
        )
        ActionRow(
            title = stringResource(R.string.install_archive_title),
            subtitle = stringResource(R.string.install_archive_desc),
            onClick = { onSelectType(InstallType.ARCHIVE) },
            icon = Icons.Default.FolderZip,
            iconTint = Mocha.Blue
        )
        ActionRow(
            title = stringResource(R.string.install_license_title),
            subtitle = stringResource(R.string.install_license_desc),
            onClick = { onSelectType(InstallType.LICENSE) },
            icon = Icons.Default.Key,
            iconTint = Mocha.Yellow
        )
        ActionRow(
            title = stringResource(R.string.install_firmware_title),
            subtitle = stringResource(R.string.install_firmware_desc),
            onClick = { onSelectType(InstallType.FIRMWARE) },
            icon = Icons.Default.SystemUpdate,
            iconTint = Mocha.Green
        )
    }
}

@Composable
fun InstallProgressDialog(
    progress: Int,
    statusMessage: String
) {
    AppDialog(
        title = stringResource(R.string.install_progress_title),
        onDismiss = {},
        dismissible = false
    ) {
        Text(statusMessage)
        if (progress > 0) {
            AppProgressBar(progress = progress / 100f)
            Text("$progress%", style = MonoSmall, color = Mocha.Mauve)
        } else {
            AppProgressBar()
        }
    }
}

@Composable
fun InstallResultDialog(
    result: InstallResult,
    onConfirm: (List<DeleteSourceOption>) -> Unit
) {
    var selectedDeleteOptions by remember(result) {
        mutableStateOf(emptySet<Int>())
    }
    val confirm = {
        onConfirm(selectedDeleteOptions.sorted().map(result.deleteOptions::get))
    }

    AppDialog(
        title = when (result.status) {
            InstallResultStatus.SUCCESS -> stringResource(R.string.install_result_success_title)
            InstallResultStatus.PARTIAL -> stringResource(R.string.install_result_partial_title)
            InstallResultStatus.ERROR -> stringResource(R.string.install_result_failed_title)
        },
        onDismiss = { onConfirm(emptyList()) },
        actions = listOf(DialogAction(stringResource(R.string.action_ok), confirm, ButtonTone.Accent))
    ) {
        Text(result.message)
        result.deleteOptions.forEachIndexed { index, option ->
            val checked = index in selectedDeleteOptions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        selectedDeleteOptions = if (checked) selectedDeleteOptions - index else selectedDeleteOptions + index
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CheckBox(checked = checked)
                Text(option.label, color = Mocha.Text)
            }
        }
    }
}

@Composable
private fun CheckBox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) Mocha.Mauve else Mocha.Crust)
            .border(1.5.dp, if (checked) Mocha.Mauve else Mocha.Surface2, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(Icons.Default.Check, contentDescription = null, tint = Mocha.Crust, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun LicenseSourceDialog(
    title: String,
    message: String,
    onSelectLicenseFile: () -> Unit,
    onEnterZrif: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showManualEntry by remember { mutableStateOf(false) }
    var zrif by remember { mutableStateOf("") }

    if (showManualEntry) {
        AppDialog(
            title = stringResource(R.string.zrif_dialog_title),
            onDismiss = { showManualEntry = false },
            actions = listOf(
                DialogAction(stringResource(R.string.action_back), { showManualEntry = false }),
                DialogAction(stringResource(R.string.action_install), { onEnterZrif(zrif) }, ButtonTone.Accent, enabled = zrif.isNotEmpty())
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mocha.Crust)
                    .border(1.dp, Mocha.Surface1, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (zrif.isEmpty()) {
                    Text(stringResource(R.string.zrif_dialog_hint), style = MonoMedium, color = Mocha.Overlay0)
                }
                BasicTextField(
                    value = zrif,
                    onValueChange = { zrif = it.trim() },
                    singleLine = true,
                    textStyle = MonoMedium.copy(color = Mocha.Text),
                    cursorBrush = SolidColor(Mocha.Mauve),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        AppDialog(
            title = title,
            onDismiss = onDismiss,
            actions = listOf(DialogAction(stringResource(R.string.action_cancel), onDismiss))
        ) {
            Text(message)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionRow(stringResource(R.string.license_select_file), onSelectLicenseFile, icon = Icons.Default.UploadFile, iconTint = Mocha.Mauve)
                ActionRow(stringResource(R.string.license_enter_zrif), { showManualEntry = true }, icon = Icons.Default.Keyboard, iconTint = Mocha.Lavender)
            }
        }
    }
}

@Composable
fun ArchiveInstallSourceDialog(
    onSelectFile: () -> Unit,
    onSelectFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        title = stringResource(R.string.install_archive_source_title),
        onDismiss = onDismiss,
        actions = listOf(DialogAction(stringResource(R.string.action_cancel), onDismiss))
    ) {
        Text(stringResource(R.string.install_archive_source_message))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ActionRow(stringResource(R.string.install_archive_source_file), onSelectFile, icon = Icons.Default.FolderZip, iconTint = Mocha.Mauve)
            ActionRow(stringResource(R.string.install_archive_source_folder), onSelectFolder, icon = Icons.Default.CreateNewFolder, iconTint = Mocha.Blue)
        }
    }
}
