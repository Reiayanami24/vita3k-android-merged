package org.vita3k.emulator.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.data.AppInfo
import org.vita3k.emulator.ui.components.AppSheet
import org.vita3k.emulator.ui.components.EyebrowLabel
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoMedium
import org.vita3k.emulator.ui.theme.MonoSmall
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AppInfoSheet(
    app: AppInfo,
    installSizeBytes: Long,
    onDismiss: () -> Unit
) {
    val size = when {
        installSizeBytes == -1L -> stringResource(R.string.app_info_size_calculating)
        installSizeBytes == 0L -> stringResource(R.string.app_info_size_unknown)
        installSizeBytes < 1024L * 1024L ->
            stringResource(R.string.app_info_size_kb, (installSizeBytes / 1024L).toInt())
        installSizeBytes < 1024L * 1024L * 1024L ->
            stringResource(R.string.app_info_size_mb, (installSizeBytes / (1024.0 * 1024.0)).toFloat())
        else ->
            stringResource(R.string.app_info_size_gb, (installSizeBytes / (1024.0 * 1024.0 * 1024.0)).toFloat())
    }

    AppSheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app = app, size = 84, cornerRadius = 16.dp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EyebrowLabel(stringResource(R.string.app_info_title))
                Text(app.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                CompatBadge(app)
            }
        }

        // the numbers people compare at a glance, in a row of their own
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCell(stringResource(R.string.app_info_field_play_time), formatPlaytime(app.playtime), Modifier.weight(1f))
            StatCell(stringResource(R.string.app_info_field_install_size), size, Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .padding(top = 14.dp)
                .padding(horizontal = 8.dp)
        ) {
            InfoRow(label = stringResource(R.string.app_info_field_serial), value = app.titleId, mono = true)
            InfoRow(label = stringResource(R.string.app_info_field_app_version), value = app.appVer.ifEmpty { "—" }, mono = true)
            InfoRow(label = stringResource(R.string.app_info_field_category), value = app.category.ifEmpty { "—" }, mono = true)
            InfoRow(label = stringResource(R.string.app_info_field_last_played), value = formatLastPlayed(app.lastPlayed), mono = true)
            InfoRow(label = stringResource(R.string.app_info_field_title), value = app.title, divider = false)
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Mocha.Base)
            .border(1.dp, Mocha.Surface0, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EyebrowLabel(label)
        Text(value, style = MonoMedium, color = Mocha.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun formatPlaytime(seconds: Long): String {
    if (seconds <= 0) return stringResource(R.string.app_info_playtime_never)
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return when {
        h > 0 -> stringResource(R.string.app_info_playtime_hm, h.toInt(), m.toInt())
        m > 0 -> stringResource(R.string.app_info_playtime_ms, m.toInt(), s.toInt())
        else -> stringResource(R.string.app_info_playtime_s, s.toInt())
    }
}

@Composable
private fun formatLastPlayed(timestampSecs: Long): String {
    if (timestampSecs <= 0) return stringResource(R.string.app_info_last_played_never)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochSecond(timestampSecs))
}

// long-press copies the value
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InfoRow(label: String, value: String, mono: Boolean = false, divider: Boolean = true) {
    val clipboardManager = LocalClipboardManager.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = { clipboardManager.setText(AnnotatedString(value)) }
                )
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Mocha.Subtext0)
            Text(
                value,
                style = if (mono) MonoSmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize) else MaterialTheme.typography.bodyMedium,
                color = Mocha.Text,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (divider) HorizontalDivider(color = Mocha.Surface0, thickness = 1.dp)
    }
}
