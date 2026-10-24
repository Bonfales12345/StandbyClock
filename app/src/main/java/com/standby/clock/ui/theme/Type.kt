package com.standby.clock.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.standby.clock.R

@OptIn(ExperimentalTextApi::class)
val SfCompactRoundedFamily = FontFamily(
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
    Font(
        resId = R.font.sf_compact_rounded,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    )
)

@OptIn(ExperimentalTextApi::class)
val SfNsRoundedFamily = FontFamily(
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
    Font(
        resId = R.font.sf_ns_rounded,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    )
)

@Composable
fun rememberRoundedTypeface(): Typeface {
    val context = LocalContext.current
    return remember(context) {
        try {
            ResourcesCompat.getFont(context, R.font.sf_ns_rounded) ?: Typeface.DEFAULT_BOLD
        } catch (e: Throwable) {
            Typeface.DEFAULT_BOLD
        }
    }
}

val StandByTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SfNsRoundedFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = SfNsRoundedFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SfNsRoundedFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SfNsRoundedFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SfNsRoundedFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)
