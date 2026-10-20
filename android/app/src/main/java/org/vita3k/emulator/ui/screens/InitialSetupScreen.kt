package org.vita3k.emulator.ui.screens

import android.view.Gravity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.data.FirmwareInstallState
import org.vita3k.emulator.data.FirmwareLinks
import org.vita3k.emulator.ui.components.AppButton
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.EyebrowLabel
import org.vita3k.emulator.ui.components.HtmlText
import org.vita3k.emulator.ui.components.Panel
import org.vita3k.emulator.ui.components.Tag
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoSmall

private const val INITIAL_SETUP_INFO_HTML =
    """To get started, please install all PS Vita firmware files.<br><br>A comprehensive guide is available on the <a href="https://vita3k.org/quickstart.html">Quickstart</a> page. Check the <a href="https://vita3k.org/compatibility.html">commercial</a> and <a href="https://vita3k.org/compatibility-homebrew.html">homebrew</a> compatibility lists to see what currently runs.<br><br>Contributions are welcome on <a href="https://github.com/Vita3K/Vita3K">GitHub</a>, and additional help is available on <a href="https://discord.gg/6aGwQzh">Discord</a>."""

private const val PAGE_COUNT = 2

@Composable
fun InitialSetupScreen(
    firmwareInstallState: FirmwareInstallState,
    preferredLanguageIndex: Int,
    onInstallFirmware: () -> Unit,
    dismissLabel: String,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var firmwareLocaleIndex by rememberSaveable {
        mutableIntStateOf(FirmwareLinks.coerceLocaleIndex(preferredLanguageIndex))
    }
    val stepTitles = listOf(
        stringResource(R.string.initial_setup_welcome_title),
        stringResource(R.string.initial_setup_firmware_title)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val wide = maxWidth >= 720.dp
        val content: @Composable (Modifier) -> Unit = { modifier ->
            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                label = "initial-setup-page",
                modifier = modifier
            ) { currentPage ->
                when (currentPage) {
                    0 -> WelcomePage()
                    else -> FirmwareSetupPage(
                        firmwareInstallState = firmwareInstallState,
                        firmwareLocaleIndex = firmwareLocaleIndex,
                        onFirmwareLocaleSelected = { firmwareLocaleIndex = it },
                        onInstallFirmware = onInstallFirmware
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.initial_setup_step, page + 1, PAGE_COUNT),
                    style = MonoSmall,
                    color = Mocha.Overlay1,
                    modifier = Modifier.weight(1f)
                )
                AppButton(text = dismissLabel, onClick = onSkip, tone = ButtonTone.Quiet)
            }

            if (wide) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    StepRail(
                        titles = stepTitles,
                        current = page,
                        onSelect = { page = it },
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .padding(start = 24.dp, top = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        content(Modifier.widthIn(max = 640.dp).fillMaxSize())
                    }
                }
            } else {
                StepBar(current = page, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                content(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (page > 0) {
                    AppButton(
                        text = stringResource(R.string.initial_setup_back),
                        onClick = { page-- },
                        tone = ButtonTone.Neutral,
                        icon = Icons.AutoMirrored.Filled.ArrowBack
                    )
                }
                Spacer(Modifier.weight(1f))
                if (page < PAGE_COUNT - 1) {
                    AppButton(
                        text = stringResource(R.string.initial_setup_next),
                        onClick = { page++ },
                        icon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                } else {
                    AppButton(text = stringResource(R.string.initial_setup_open_library), onClick = onFinish)
                }
            }
        }
    }
}

@Composable
private fun StepBar(current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(PAGE_COUNT) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(if (index <= current) Mocha.Mauve else Mocha.Surface0)
            )
        }
    }
}

@Composable
private fun StepRail(
    titles: List<String>,
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        titles.forEachIndexed { index, title ->
            val done = index < current
            val active = index == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) Mocha.Mauve.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (done || active) Mocha.Mauve else Mocha.Surface0),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Mocha.Crust, modifier = Modifier.size(16.dp))
                    } else {
                        Text("${index + 1}", style = MonoSmall, color = if (active) Mocha.Crust else Mocha.Subtext0)
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) Mocha.Text else Mocha.Subtext0
                )
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_plus),
                contentDescription = stringResource(R.string.apps_list_app_title),
                modifier = Modifier.size(72.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EyebrowLabel(stringResource(R.string.apps_list_app_title), color = Mocha.Mauve)
                Text(
                    text = stringResource(R.string.initial_setup_welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Mocha.Text
                )
            }
        }
        Text(
            text = stringResource(R.string.initial_setup_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = Mocha.Subtext1
        )
        HtmlText(
            html = INITIAL_SETUP_INFO_HTML,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            textColor = Mocha.Subtext0,
            linkColor = Mocha.Mauve,
            gravity = Gravity.START
        )
        Text(
            text = stringResource(R.string.initial_setup_piracy_notice),
            style = MaterialTheme.typography.bodyMedium,
            color = Mocha.Peach,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Mocha.Peach.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirmwareSetupPage(
    firmwareInstallState: FirmwareInstallState,
    firmwareLocaleIndex: Int,
    onFirmwareLocaleSelected: (Int) -> Unit,
    onInstallFirmware: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val components = firmwareInstallState.components

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.initial_setup_firmware_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Mocha.Text,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = stringResource(R.string.initial_setup_firmware_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Mocha.Subtext0,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 6.dp)
        )

        FirmwareCard(
            number = 1,
            title = stringResource(R.string.initial_setup_preinstall_title),
            installed = components.preinstalled,
            optional = true
        ) {
            FirmwareActions(
                onDownload = { uriHandler.openUri(FirmwareLinks.PREINSTALL_URL) },
                onInstall = onInstallFirmware
            )
        }

        FirmwareCard(
            number = 2,
            title = stringResource(R.string.initial_setup_main_title),
            installed = components.main
        ) {
            FirmwareLanguagePicker(selectedIndex = firmwareLocaleIndex, onSelected = onFirmwareLocaleSelected)
            FirmwareActions(
                onDownload = { uriHandler.openUri(FirmwareLinks.firmwareDownloadUrl(firmwareLocaleIndex)) },
                onInstall = onInstallFirmware
            )
        }

        FirmwareCard(
            number = 3,
            title = stringResource(R.string.initial_setup_font_title),
            installed = components.fontPackage
        ) {
            FirmwareActions(
                onDownload = { uriHandler.openUri(FirmwareLinks.FONT_PACKAGE_URL) },
                onInstall = onInstallFirmware
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirmwareActions(onDownload: () -> Unit, onInstall: () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppButton(
            text = stringResource(R.string.initial_setup_download),
            onClick = onDownload,
            tone = ButtonTone.Neutral,
            icon = Icons.Default.Download
        )
        AppButton(
            text = stringResource(R.string.initial_setup_install_pup),
            onClick = onInstall,
            icon = Icons.Default.FileOpen
        )
    }
}

@Composable
private fun FirmwareLanguagePicker(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = FirmwareLinks.coerceLocaleIndex(selectedIndex)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EyebrowLabel(stringResource(R.string.initial_setup_select_firmware_language))
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mocha.Crust)
                    .border(1.dp, Mocha.Surface0, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Mocha.Overlay1, modifier = Modifier.size(18.dp))
                Text(stringResource(FirmwareLinks.locales[current].nameResId), style = MaterialTheme.typography.bodyMedium, color = Mocha.Text)
                Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = Mocha.Overlay1, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Mocha.Mantle,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Mocha.Surface0)
            ) {
                FirmwareLinks.locales.forEachIndexed { index, locale ->
                    DropdownMenuItem(
                        text = { Text(stringResource(locale.nameResId), color = if (index == current) Mocha.Mauve else Mocha.Text) },
                        onClick = {
                            onSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FirmwareCard(
    number: Int,
    title: String,
    installed: Boolean,
    optional: Boolean = false,
    actions: @Composable ColumnScope.() -> Unit
) {
    Panel(spacing = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (installed) Mocha.Green.copy(alpha = 0.16f) else Mocha.Surface0),
                contentAlignment = Alignment.Center
            ) {
                if (installed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Mocha.Green, modifier = Modifier.size(18.dp))
                } else {
                    Text("$number", style = MonoSmall, color = Mocha.Subtext1)
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = Mocha.Text, modifier = Modifier.weight(1f))
            when {
                installed -> Tag(stringResource(R.string.initial_setup_status_installed), Mocha.Green)
                optional -> Tag(stringResource(R.string.initial_setup_status_optional), Mocha.Overlay2)
                else -> Tag(stringResource(R.string.initial_setup_status_missing), Mocha.Peach)
            }
        }
        if (!installed) actions()
    }
}
