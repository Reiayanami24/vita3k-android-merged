package org.vita3k.emulator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsControllerCompat

// Layers, darkest to lightest: crust (scrims, wells) < mantle (panels, sheets) < base (screens) < surface0..2 (controls)
private val AppColorScheme = darkColorScheme(
    primary = Mocha.Mauve,
    onPrimary = Mocha.Crust,
    primaryContainer = Color(0xFF3B3150),
    onPrimaryContainer = Mocha.Mauve,
    inversePrimary = Color(0xFF7A4FB0),
    secondary = Mocha.Lavender,
    onSecondary = Mocha.Crust,
    secondaryContainer = Mocha.Surface0,
    onSecondaryContainer = Mocha.Text,
    tertiary = Mocha.Teal,
    onTertiary = Mocha.Crust,
    tertiaryContainer = Color(0xFF233B3C),
    onTertiaryContainer = Mocha.Teal,
    background = Mocha.Base,
    onBackground = Mocha.Text,
    surface = Mocha.Base,
    onSurface = Mocha.Text,
    surfaceVariant = Mocha.Surface0,
    onSurfaceVariant = Mocha.Subtext0,
    surfaceTint = Color.Transparent,
    inverseSurface = Mocha.Text,
    inverseOnSurface = Mocha.Base,
    error = Mocha.Red,
    onError = Mocha.Crust,
    errorContainer = Color(0xFF45283A),
    onErrorContainer = Mocha.Red,
    outline = Mocha.Surface1,
    outlineVariant = Mocha.Surface0,
    scrim = Mocha.Crust,
    surfaceBright = Mocha.Surface0,
    surfaceDim = Mocha.Crust,
    surfaceContainerLowest = Mocha.Crust,
    surfaceContainerLow = Mocha.Mantle,
    surfaceContainer = Mocha.Mantle,
    surfaceContainerHigh = Mocha.Mantle,
    surfaceContainerHighest = Mocha.Surface0
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
@Suppress("DEPRECATION")
fun Vita3KTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val transparentSystemBar = Color.Transparent.toArgb()
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = transparentSystemBar
            window.navigationBarColor = transparentSystemBar
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

/**
 * A fraction in [0, 1] that is used uniformly for all modal scrim / dialog dim effects.
 * Apply it to:
 *   - ModalBottomSheet via `scrimColor = Color.Black.copy(alpha = SCRIM_ALPHA)`
 *   - Dialog / AlertDialog via [ApplyDialogDim]
 */
const val SCRIM_ALPHA = 0.55f

/**
 * Must be called inside a Dialog or AlertDialog composable scope to reduce the system dim amount
 * to the app-wide [SCRIM_ALPHA] value. Place it at the beginning of the dialog's content lambda.
 */
@Composable
fun ApplyDialogDim() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        // Check both the view itself and its parent — Compose's Dialog sets DialogWindowProvider
        // on the DialogLayout (the direct parent of the composable view tree), but the exact
        // position in the hierarchy can vary by API level and Material3 version.
        DisposableEffect(Unit) {
            val provider = (view as? DialogWindowProvider)
                ?: (view.parent as? DialogWindowProvider)
            provider?.window?.setDimAmount(SCRIM_ALPHA)
            onDispose {}
        }
    }
}
