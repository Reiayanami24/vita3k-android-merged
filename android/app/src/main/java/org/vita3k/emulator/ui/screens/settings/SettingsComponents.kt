package org.vita3k.emulator.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.ui.components.AppDialog
import org.vita3k.emulator.ui.components.ButtonTone
import org.vita3k.emulator.ui.components.DialogAction
import org.vita3k.emulator.ui.components.Panel
import org.vita3k.emulator.ui.components.Segmented
import org.vita3k.emulator.ui.components.Tag
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoSmall

private val RowShape = RoundedCornerShape(12.dp)

// up to this many short options read better as one segmented control than as chips
private const val SEGMENTED_MAX_OPTIONS = 4
private const val SEGMENTED_MAX_LABEL = 12

@Composable
internal fun settingsFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Mocha.Crust,
    labelColor = Mocha.Subtext0,
    iconColor = Mocha.Overlay1,
    selectedContainerColor = Mocha.Mauve.copy(alpha = 0.16f),
    selectedLabelColor = Mocha.Mauve,
    selectedLeadingIconColor = Mocha.Mauve
)

/** Category tabs for narrow screens: a scrolling row of pills, the current one filled. */
@Composable
internal fun SettingsCategoryStrip(
    categories: List<SettingsCategory>,
    selectedCategory: SettingsCategory,
    onCategorySelected: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(categories, key = { it.name }) { category ->
            val selected = category == selectedCategory
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) Mocha.Mauve else Mocha.Surface0.copy(alpha = 0.6f))
                    .clickable(role = Role.Tab) { onCategorySelected(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (selected) Mocha.Crust else Mocha.Overlay2,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    category.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Mocha.Crust else Mocha.Subtext1
                )
            }
        }
    }
}

/** Category list for wide screens, beside the content. */
@Composable
internal fun SettingsCategoryRail(
    categories: List<SettingsCategory>,
    selectedCategory: SettingsCategory,
    onCategorySelected: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RowShape)
                    .background(if (selected) Mocha.Mauve.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable(role = Role.Tab) { onCategorySelected(category) }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    tint = if (selected) Mocha.Mauve else Mocha.Overlay1,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    category.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Mocha.Mauve else Mocha.Subtext1
                )
            }
        }
    }
}

@Composable
internal fun SettingsSectionCard(
    title: String,
    summary: String? = null,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Panel(
        modifier = modifier,
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 16.dp, bottom = 8.dp),
        spacing = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Mocha.Text)
                if (!summary.isNullOrBlank()) {
                    Text(text = summary, style = MaterialTheme.typography.bodySmall, color = Mocha.Subtext0)
                }
            }
            help?.let { SettingsHelpButton(help = it, onShowHelp = onShowHelp) }
        }
        content()
    }
}

@Composable
internal fun SettingsSubsectionTitle(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = title.uppercase(), style = org.vita3k.emulator.ui.theme.Eyebrow, color = Mocha.Overlay1)
        if (!summary.isNullOrBlank()) {
            Text(text = summary, style = MaterialTheme.typography.bodySmall, color = Mocha.Subtext0)
        }
    }
}

@Composable
internal fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: String? = null,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    SettingsRowContainer(
        title = title,
        summary = summary,
        modifier = modifier,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        help = help,
        onShowHelp = onShowHelp
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Mocha.Crust,
                checkedTrackColor = Mocha.Mauve,
                checkedBorderColor = Mocha.Mauve,
                uncheckedThumbColor = Mocha.Overlay1,
                uncheckedTrackColor = Mocha.Crust,
                uncheckedBorderColor = Mocha.Surface1,
                disabledCheckedTrackColor = Mocha.Mauve.copy(alpha = 0.35f),
                disabledCheckedThumbColor = Mocha.Crust,
                disabledUncheckedTrackColor = Mocha.Crust,
                disabledUncheckedThumbColor = Mocha.Surface1,
                disabledUncheckedBorderColor = Mocha.Surface0
            )
        )
    }
}

@Composable
internal fun SettingsChoiceSelector(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: String? = null,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    val clampedSelectedIndex = if (options.isEmpty()) 0 else selectedIndex.coerceIn(0, options.lastIndex)
    SettingsBlock(title = title, summary = summary, enabled = enabled, help = help, onShowHelp = onShowHelp, modifier = modifier) {
        if (options.isNotEmpty()) {
            val segmented = options.size <= SEGMENTED_MAX_OPTIONS && options.all { it.length <= SEGMENTED_MAX_LABEL }
            if (segmented) {
                Segmented(
                    options = options.map { null to it },
                    selectedIndex = clampedSelectedIndex,
                    onSelect = onSelected,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SettingsChoiceChips(
                    options = options,
                    selectedIndex = clampedSelectedIndex,
                    onSelected = { if (enabled) onSelected(it) }
                )
            }
        }
    }
}

@Composable
internal fun SettingsScrollableChoiceSelector(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: String? = null,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    val clampedSelectedIndex = if (options.isEmpty()) 0 else selectedIndex.coerceIn(0, options.lastIndex)
    var expanded by remember { mutableStateOf(false) }
    SettingsBlock(title = title, summary = summary, enabled = enabled, help = help, onShowHelp = onShowHelp, modifier = modifier) {
        if (options.isNotEmpty()) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clip(RowShape)
                        .background(Mocha.Crust)
                        .border(1.dp, if (expanded) Mocha.Mauve else Mocha.Surface0, RowShape)
                        .clickable(enabled = enabled) { expanded = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = options[clampedSelectedIndex],
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) Mocha.Text else Mocha.Overlay0,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = Mocha.Overlay1, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Mocha.Mantle,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Mocha.Surface0),
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        val selected = index == clampedSelectedIndex
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = if (selected) Mocha.Mauve else Mocha.Text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = Mocha.Mauve, modifier = Modifier.size(18.dp)) }
                            } else null,
                            onClick = {
                                expanded = false
                                onSelected(index)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsActionRow(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionEnabled: Boolean = true,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    SettingsRowContainer(
        title = title,
        summary = value,
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        help = help,
        onShowHelp = onShowHelp
    ) {
        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            val on = enabled && actionEnabled
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (on) Mocha.Mauve else Mocha.Overlay0,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Mocha.Mauve.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable(enabled = on, onClick = onActionClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Mocha.Overlay0
        )
    }
}

@Composable
internal fun SettingsSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Mocha.Text,
                modifier = Modifier.weight(1f)
            )
            help?.let { SettingsHelpButton(help = it, onShowHelp = onShowHelp) }
            Tag(text = valueLabel, color = Mocha.Mauve)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Mocha.Mauve,
                activeTrackColor = Mocha.Mauve,
                inactiveTrackColor = Mocha.Surface0,
                activeTickColor = Mocha.Crust.copy(alpha = 0.5f),
                inactiveTickColor = Mocha.Surface2,
                disabledThumbColor = Mocha.Overlay0,
                disabledActiveTrackColor = Mocha.Overlay0,
                disabledInactiveTrackColor = Mocha.Surface0
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        )
    }
}

@Composable
internal fun SettingsNote(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Mocha.Subtext0
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(width = 2.dp, height = 14.dp)
                .background(color.copy(alpha = 0.6f))
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsChoiceChips(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Text(
                text = option,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Mocha.Mauve else Mocha.Subtext1,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Mocha.Mauve.copy(alpha = 0.14f) else Mocha.Crust)
                    .border(1.dp, if (selected) Mocha.Mauve.copy(alpha = 0.6f) else Mocha.Surface0, RoundedCornerShape(10.dp))
                    .clickable(role = Role.RadioButton) { onSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
internal fun SettingsSearchResultRow(
    entry: SettingsSearchEntry,
    onOpen: (SettingsCategory) -> Unit,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    SettingsRowContainer(
        title = entry.title,
        summary = entry.summary,
        onClick = { onOpen(entry.category) },
        help = entry.help,
        onShowHelp = onShowHelp
    ) {
        Tag(entry.category.label(), Mocha.Overlay2)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Mocha.Overlay0)
    }
}

@Composable
internal fun SettingsHelpSheet(
    help: SettingsHelpEntry,
    onDismiss: () -> Unit
) {
    AppDialog(
        title = help.title,
        onDismiss = onDismiss,
        actions = listOf(DialogAction(stringResource(R.string.action_ok), onDismiss, ButtonTone.Accent))
    ) {
        help.scope.labelRes?.let { labelRes ->
            Tag(stringResource(labelRes), Mocha.Lavender)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = help.body, style = MaterialTheme.typography.bodyMedium, color = Mocha.Subtext1)
        }
    }
}

@Composable
internal fun SettingsLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Mocha.Mauve, trackColor = Mocha.Surface0)
    }
}

@Composable
internal fun SettingsHelpButton(
    help: SettingsHelpEntry,
    onShowHelp: (SettingsHelpEntry) -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button) { onShowHelp(help) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = stringResource(R.string.settings_cd_show_info),
            tint = Mocha.Overlay1,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** A setting whose control sits under its title: choices, dropdowns. */
@Composable
private fun SettingsBlock(
    title: String,
    summary: String?,
    enabled: Boolean,
    help: SettingsHelpEntry?,
    onShowHelp: (SettingsHelpEntry) -> Unit,
    modifier: Modifier = Modifier,
    control: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Mocha.Text)
                if (!summary.isNullOrBlank()) {
                    Text(text = summary, style = MaterialTheme.typography.bodySmall, color = Mocha.Subtext0)
                }
            }
            help?.let { SettingsHelpButton(help = it, onShowHelp = onShowHelp) }
        }
        control()
    }
}

/** A setting on one line: title and value on the left, its control on the right. */
@Composable
private fun SettingsRowContainer(
    title: String,
    summary: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    help: SettingsHelpEntry? = null,
    onShowHelp: (SettingsHelpEntry) -> Unit,
    trailing: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RowShape)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Mocha.Text)
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = if (summary.looksLikeAPath()) MonoSmall else MaterialTheme.typography.bodySmall,
                    color = Mocha.Subtext0
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            help?.let { SettingsHelpButton(help = it, onShowHelp = onShowHelp) }
            trailing()
        }
    }
}

private fun String.looksLikeAPath(): Boolean = startsWith("/") || startsWith("content://")
