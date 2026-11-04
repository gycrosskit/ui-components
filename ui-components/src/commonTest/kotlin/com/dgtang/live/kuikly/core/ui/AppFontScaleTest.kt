package com.dgtang.live.kuikly.core.ui

import com.dgtang.live.kuikly.core.ui.component.scaledForNativeText
import com.tencent.kuikly.compose.material3.Typography
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppFontScaleTest {
    @Test
    fun defaultTypographyKeepsItsFamiliesAndWeightsWhenScaled() {
        val typography = Typography()
        val styles = with(typography) {
            listOf(displayLarge, displayMedium, displaySmall, headlineLarge, headlineMedium, headlineSmall,
                titleLarge, titleMedium, titleSmall, bodyLarge, bodyMedium, bodySmall, labelLarge, labelMedium, labelSmall)
        }
        assertEquals(15, styles.size)
        assertEquals(FontWeight.Medium, typography.titleMedium.fontWeight)
        assertEquals(FontWeight.Normal, typography.bodyLarge.fontWeight)
        for (style in styles) {
            assertEquals(FontFamily.SansSerif, style.fontFamily)
            val italic = style.copy(fontStyle = FontStyle.Italic)
            val scaled = italic.scaledForNativeText(appFontScale(3.2f), isIos = true)
            assertEquals(style.fontFamily, scaled.fontFamily)
            assertEquals(style.fontWeight, scaled.fontWeight)
            assertEquals(FontStyle.Italic, scaled.fontStyle)
            assertEquals(style.fontSize.value * 1.75f, scaled.fontSize.value, 0.001f)
            assertEquals(style.lineHeight.value * 1.75f, scaled.lineHeight.value, 0.001f)
        }
    }

    @Test
    fun followsSystemBelowLimitAndCapsTextAndLayoutTogether() {
        for (system in listOf(0.85f, 1f, 1.3f, 1.5f, 1.75f, 1.8f, 2f, 3.2f, 1.2f)) {
            val effective = appFontScale(system)
            assertEquals(minOf(system, 1.75f), effective)
            val text = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
                .scaledForNativeText(effective, isIos = true)
            val layout = Density(2f, effective)
            assertEquals(with(layout) { 16.sp.toDp().value }, text.fontSize.value, 0.001f)
            assertEquals(with(layout) { 24.sp.toDp().value }, text.lineHeight.value, 0.001f)
        }
    }

    @Test
    fun invalidScaleKeepsExistingDefault() {
        for (scale in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertEquals(1f, appFontScale(scale))
        }
    }
}
