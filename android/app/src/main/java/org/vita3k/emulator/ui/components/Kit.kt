package org.vita3k.emulator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.vita3k.emulator.data.CompatibilityState
import org.vita3k.emulator.ui.theme.ApplyDialogDim
import org.vita3k.emulator.ui.theme.Eyebrow
import org.vita3k.emulator.ui.theme.Mocha
import org.vita3k.emulator.ui.theme.MonoSmall
import org.vita3k.emulator.ui.theme.SCRIM_ALPHA
import org.vita3k.emulator.ui.theme.tint

val PanelShape = RoundedCornerShape(18.dp)
val ControlShape = RoundedCornerShape(12.dp)

/** A grouped block of content: mantle on base, with a hairline edge instead of a shadow. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    spacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Mocha.Mantle)
            .border(1.dp, Mocha.Surface0, PanelShape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

@Composable
fun EyebrowLabel(text: String, modifier: Modifier = Modifier, color: Color = Mocha.Overlay1) {
    Text(text = text.uppercase(), style = Eyebrow, color = color, modifier = modifier)
}

/** A small tinted label, e.g. "CFG" on games with a custom config. */
@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MonoSmall,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun StatusDot(color: Color, size: Dp = 7.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun CompatLabel(state: CompatibilityState, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatusDot(state.tint)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = state.tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

enum class ButtonTone { Accent, Neutral, Danger, Quiet }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Accent,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val (container, content) = when (tone) {
        ButtonTone.Accent -> Mocha.Mauve to Mocha.Crust
        ButtonTone.Neutral -> Mocha.Surface0 to Mocha.Text
        ButtonTone.Danger -> Mocha.Red.copy(alpha = 0.16f) to Mocha.Red
        ButtonTone.Quiet -> Color.Transparent to Mocha.Subtext1
    }
    Row(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(ControlShape)
            .background(if (enabled) container else Mocha.Surface0.copy(alpha = 0.5f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        val color = if (enabled) content else Mocha.Overlay0
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1)
    }
}

/** A round icon button; [active] fills it to show a toggled state. */
@Composable
fun ToolbarButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true,
    tint: Color = Mocha.Subtext1
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (active) Mocha.Mauve.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = when {
                !enabled -> Mocha.Surface2
                active -> Mocha.Mauve
                else -> tint
            },
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    clearDescription: String? = null
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    if (autoFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(ControlShape)
            .background(Mocha.Surface0.copy(alpha = 0.7f))
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Mocha.Overlay1, modifier = Modifier.size(20.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Mocha.Overlay1, maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Mocha.Text),
                cursorBrush = SolidColor(Mocha.Mauve),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = clearDescription, tint = Mocha.Overlay2, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Connected options where exactly one is on. Icons-only when every label is null. */
@Composable
fun Segmented(
    options: List<Pair<ImageVector?, String>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    iconsOnly: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(ControlShape)
            .background(Mocha.Crust)
            .border(1.dp, Mocha.Surface0, ControlShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, (icon, label) ->
            val selected = index == selectedIndex
            Row(
                modifier = Modifier
                    .then(if (iconsOnly) Modifier else Modifier.weight(1f, fill = true))
                    .heightIn(min = 34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) Mocha.Surface0 else Color.Transparent)
                    .clickable(enabled = enabled, role = Role.RadioButton) { onSelect(index) }
                    .padding(horizontal = if (iconsOnly) 9.dp else 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                val color = when {
                    !enabled -> Mocha.Overlay0
                    selected -> Mocha.Mauve
                    else -> Mocha.Subtext0
                }
                if (icon != null) {
                    Icon(icon, contentDescription = if (iconsOnly) label else null, tint = color, modifier = Modifier.size(18.dp))
                }
                if (!iconsOnly) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** One tappable line in a sheet or menu: icon, title and an optional explanation. */
@Composable
fun ActionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    tint: Color = Mocha.Text,
    iconTint: Color = Mocha.Overlay2,
    enabled: Boolean = true,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Mocha.Surface0.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (enabled) iconTint else Mocha.Surface2, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (enabled) tint else Mocha.Overlay0)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mocha.Subtext0)
            }
        }
        trailing()
    }
}

@Composable
fun AppProgressBar(modifier: Modifier = Modifier, progress: Float? = null) {
    if (progress == null) {
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = Mocha.Mauve,
            trackColor = Mocha.Surface0,
            strokeCap = StrokeCap.Round
        )
    } else {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = Mocha.Mauve,
            trackColor = Mocha.Surface0,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

data class DialogAction(
    val text: String,
    val onClick: () -> Unit,
    val tone: ButtonTone = ButtonTone.Quiet,
    val enabled: Boolean = true
)

/** The one dialog shape in the app: title, free content and right-aligned actions. */
@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    actions: List<DialogAction> = emptyList(),
    dismissible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
            usePlatformDefaultWidth = false
        )
    ) {
        ApplyDialogDim()
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 460.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = Mocha.Mantle,
            contentColor = Mocha.Text,
            border = BorderStroke(1.dp, Mocha.Surface0)
        ) {
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Mocha.Text)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides Mocha.Subtext1,
                        androidx.compose.material3.LocalTextStyle provides MaterialTheme.typography.bodyMedium
                    ) {
                        content()
                    }
                }
                if (actions.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        actions.forEach { action ->
                            AppButton(
                                text = action.text,
                                onClick = action.onClick,
                                tone = action.tone,
                                enabled = action.enabled
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Mocha.Mantle,
        contentColor = Mocha.Text,
        scrimColor = Mocha.Crust.copy(alpha = SCRIM_ALPHA),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Mocha.Surface2)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 12.dp)
                )
            }
            content()
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .widthIn(max = 420.dp)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Mocha.Mantle)
                .border(1.dp, Mocha.Surface0, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Mocha.Mauve, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = Mocha.Text,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!body.isNullOrBlank()) {
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = Mocha.Subtext0,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        content()
    }
}
