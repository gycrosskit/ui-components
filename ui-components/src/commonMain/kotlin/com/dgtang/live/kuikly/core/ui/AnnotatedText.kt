package com.dgtang.live.kuikly.core.ui

import com.tencent.kuikly.compose.ui.text.AnnotatedString
import com.tencent.kuikly.compose.ui.text.SpanStyle
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.withStyle
import com.tencent.kuikly.compose.ui.unit.isSpecified
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * Kuikly 2.28 Android 富文本把继承的绝对字距直接当成 em；显式片段字距才会除以字号。
 * 为整段提供底层样式，保留原有链接及局部样式，避免协议文字被拉宽。
 */
fun AnnotatedString.withSpanLetterSpacing(style: TextStyle): AnnotatedString = buildAnnotatedString {
    val spacing = if (style.letterSpacing.isSpecified) style.letterSpacing else 0.sp
    withStyle(SpanStyle(letterSpacing = spacing)) {
        append(this@withSpanLetterSpacing)
    }
}
