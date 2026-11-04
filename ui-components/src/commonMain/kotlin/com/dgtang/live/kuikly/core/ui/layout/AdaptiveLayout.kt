package com.dgtang.live.kuikly.core.ui.layout

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.platform.LocalDensity

/** 大字体下的布局切换阈值。 */
const val EXPANDED_TEXT_FONT_SCALE = 1.3f

fun requiresExpandedTextLayout(fontScale: Float): Boolean =
    fontScale >= EXPANDED_TEXT_FONT_SCALE

@Composable
fun requiresExpandedTextLayout(): Boolean =
    requiresExpandedTextLayout(LocalDensity.current.fontScale)
