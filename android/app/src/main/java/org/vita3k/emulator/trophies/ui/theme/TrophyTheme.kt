package org.vita3k.emulator.trophies.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.vita3k.emulator.R
import org.vita3k.emulator.trophies.domain.model.TrophyGrade
import org.vita3k.emulator.ui.theme.Mocha

internal object TrophyColors {
    private val MauveDeep = Color(0xFF8C6BB8)

    val BackgroundTop = Mocha.Base
    val BackgroundMid = Mocha.Mantle
    val BackgroundBottom = Mocha.Crust
    val AtmosphereBlue = Mocha.Mauve
    val AtmosphereGold = Mocha.Yellow
    val Panel = Mocha.Surface0.copy(alpha = 0.32f)
    val PanelStrong = Mocha.Surface0.copy(alpha = 0.6f)
    val PanelBorder = Mocha.Surface1.copy(alpha = 0.7f)
    val PanelHighlight = Mocha.Overlay0
    val TextPrimary = Mocha.Text
    val TextSecondary = Mocha.Subtext0
    val TextMuted = Mocha.Overlay1
    val LockedScrim = Mocha.Crust.copy(alpha = 0.6f)
    val Earned = Mocha.Green
    val Error = Mocha.Red
    val FilterOrbTop = Mocha.Mauve
    val FilterOrbMid = MauveDeep
    val FilterOrbBottom = Mocha.Surface1
    val FilterOrbShadow = Mocha.Crust
    val FilterMenuTop = Mocha.Surface1
    val FilterMenuMid = Mocha.Surface0
    val FilterMenuBottom = Mocha.Mantle
    val FilterMenuBorder = Mocha.Surface2
    val FilterMenuDivider = Mocha.Surface0
    val FilterPillTop = Mocha.Surface1
    val FilterPillBottom = Mocha.Surface0
    val FilterPillSelectedTop = Mocha.Mauve
    val FilterPillSelectedBottom = MauveDeep

    fun gradeAccent(grade: TrophyGrade): Color = when (grade) {
        TrophyGrade.PLATINUM -> Mocha.Lavender
        TrophyGrade.GOLD -> Mocha.Yellow
        TrophyGrade.SILVER -> Mocha.Subtext1
        TrophyGrade.BRONZE -> Mocha.Peach
        TrophyGrade.UNKNOWN -> Mocha.Overlay1
    }

    fun gradeGlow(grade: TrophyGrade): Color = gradeAccent(grade).copy(alpha = 0.2f)
}

internal object TrophyDimensions {
    val ScreenPadding = 20.dp
    val HeaderSpacing = 14.dp
    val SectionSpacing = 12.dp
    val PanelPadding = 16.dp
    val CompactPanelPadding = 12.dp
    val RowIconSize = 58.dp
    val CollectionRowIconSize = 62.dp
    val CollectionRowMinHeight = 78.dp
    val CollectionLegendHeight = 34.dp
    val CollectionCountsPanelWidth = 136.dp
    val LargeIconSize = 88.dp
}

internal object TrophyShapes {
    val Panel = RoundedCornerShape(16.dp)
    val Band = RoundedCornerShape(6.dp)
    val CompactPanel = RoundedCornerShape(12.dp)
    val Pill = RoundedCornerShape(10.dp)
    val Badge = RoundedCornerShape(999.dp)
    val IconFrame = RoundedCornerShape(10.dp)
}

@Composable
internal fun TrophyGrade.displayName(): String = stringResource(
    when (this) {
        TrophyGrade.PLATINUM -> R.string.trophy_grade_platinum
        TrophyGrade.GOLD -> R.string.trophy_grade_gold
        TrophyGrade.SILVER -> R.string.trophy_grade_silver
        TrophyGrade.BRONZE -> R.string.trophy_grade_bronze
        TrophyGrade.UNKNOWN -> R.string.trophy_grade_unknown
    }
)
