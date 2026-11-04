package com.dgtang.live.kuikly.core.ui

import com.dgtang.live.kuikly.core.ui.component.nativeImageColorFilter
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.graphics.ColorMatrixColorFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class ImageTintTest {
    @Test
    fun ohosTintReplacesRgbAndPreservesSourceTransparency() {
        val color = Color(0.2f, 0.4f, 0.8f, 0.5f)
        val tint = ColorFilter.tint(color)
        val filter = assertIs<ColorMatrixColorFilter>(nativeImageColorFilter(tint, isOhos = true))
        val matrix = filter.colorMatrix.values
        fun transform(red: Float, green: Float, blue: Float, alpha: Float): List<Float> {
            val input = floatArrayOf(red, green, blue, alpha, 1f)
            return (0..3).map { row -> (0..4).sumOf { col -> (matrix[row * 5 + col] * input[col]).toDouble() }.toFloat() }
        }
        assertEquals(listOf(color.red, color.green, color.blue, color.alpha), transform(1f, 0f, 0f, 1f))
        assertEquals(0f, transform(0f, 0f, 0f, 0f).last())
        assertEquals(color.alpha * 0.5f, transform(1f, 1f, 1f, 0.5f).last())
        assertSame(tint, nativeImageColorFilter(tint, isOhos = false))
        assertSame(filter, nativeImageColorFilter(filter, isOhos = true))
    }
}
