package com.standby.clock.data

import androidx.compose.ui.graphics.Color
import com.standby.clock.ui.theme.*

enum class ColorTheme(
    val title: String,
    val previewColor: Color,
    val isDynamic: Boolean = false
) {
    MATERIAL_YOU("Dynamic (Material You)", StandByBlue, isDynamic = true),
    BLUE("Sky Blue", StandByBlue),
    CORAL("Coral", StandByCoral),
    ORANGE("Amber Sunset", StandByOrange),
    GREEN("Mint Green", StandByMint),
    PURPLE("Lavender", StandByPurple),
    WHITE("Mono White", StandByWhite),
    NIGHT_RED("Night Red", StandByNightRed);

    fun getPrimaryColor(dynamicPrimary: Color): Color {
        return when {
            isDynamic -> dynamicPrimary
            else -> previewColor
        }
    }
}
