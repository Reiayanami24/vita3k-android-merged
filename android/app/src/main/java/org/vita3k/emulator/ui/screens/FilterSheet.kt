package org.vita3k.emulator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.data.SortOption
import org.vita3k.emulator.data.ViewMode
import org.vita3k.emulator.ui.components.ActionRow
import org.vita3k.emulator.ui.components.AppSheet
import org.vita3k.emulator.ui.components.EyebrowLabel
import org.vita3k.emulator.ui.components.Segmented
import org.vita3k.emulator.ui.theme.Mocha

@Composable
fun FilterSheet(
    sortOption: SortOption,
    viewMode: ViewMode,
    onSortChanged: (SortOption) -> Unit,
    onViewModeToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    AppSheet(onDismiss = onDismiss, title = stringResource(R.string.filter_title)) {
        EyebrowLabel(stringResource(R.string.filter_section_sort), modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
        SortOption.entries.forEach { option ->
            val selected = option == sortOption
            ActionRow(
                title = when (option) {
                    SortOption.TITLE -> stringResource(R.string.apps_list_sort_title)
                    SortOption.LAST_PLAYED -> stringResource(R.string.apps_list_sort_last_played)
                    SortOption.COMPATIBILITY -> stringResource(R.string.apps_list_sort_compatibility)
                },
                onClick = { onSortChanged(option) },
                icon = when (option) {
                    SortOption.TITLE -> Icons.Default.SortByAlpha
                    SortOption.LAST_PLAYED -> Icons.Default.History
                    SortOption.COMPATIBILITY -> Icons.Default.Verified
                },
                tint = if (selected) Mocha.Mauve else Mocha.Text,
                iconTint = if (selected) Mocha.Mauve else Mocha.Overlay2
            ) {
                if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Mocha.Mauve)
            }
        }

        EyebrowLabel(stringResource(R.string.filter_section_view), modifier = Modifier.padding(start = 8.dp, top = 18.dp, bottom = 10.dp))
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
