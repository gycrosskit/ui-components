package com.dgtang.live.kuikly.core.ui

const val MAX_APP_FONT_SCALE = 1.75f

/** 保留系统的小字和大字档位，仅限制放大上限。 */
fun appFontScale(scale: Float): Float =
    if (scale.isFinite() && scale > 0f) scale.coerceAtMost(MAX_APP_FONT_SCALE) else 1f
