package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.text.InlineTextContent
import com.tencent.kuikly.compose.material3.LocalTextStyle
import com.tencent.kuikly.compose.material3.Text as NativeText
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.text.AnnotatedString
import com.tencent.kuikly.compose.ui.text.TextLayoutResult
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.TextUnit

/** Android/OHOS 原生 SP 已缩放；iOS 的绝对 UIFont 在唯一文本边界补偿。 */
@Composable
fun TextStyle.forNativeText(): TextStyle {
    val configuration = LocalConfiguration.current
    return scaledForNativeText(configuration.fontSizeScale, configuration.isIOS)
}

internal fun TextStyle.scaledForNativeText(fontScale: Float, isIos: Boolean): TextStyle =
    if (!isIos || fontScale == 1f) this else copy(
        fontSize = fontSize.scaledSp(fontScale),
        lineHeight = lineHeight.scaledSp(fontScale),
        letterSpacing = letterSpacing.scaledSp(fontScale),
    )

private fun TextUnit.scaledSp(scale: Float): TextUnit = if (isSp) this * scale else this

/** 追加字号覆盖保留原文、链接回调和 inline annotation；现有富文本没有段落字号。 */
internal fun AnnotatedString.scaledForNativeText(fontScale: Float, isIos: Boolean): AnnotatedString {
    if (!isIos || fontScale == 1f) return this
    return buildAnnotatedString {
        append(this@scaledForNativeText)
        this@scaledForNativeText.spanStyles.forEach { span ->
            if (span.item.fontSize.isSp || span.item.letterSpacing.isSp) {
                addStyle(span.item.copy(
                    fontSize = span.item.fontSize.scaledSp(fontScale),
                    letterSpacing = span.item.letterSpacing.scaledSp(fontScale),
                ), span.start, span.end)
            }
        }
    }
}

/** 保留 Kuikly Text 参数与语义，只补齐 iOS 系统字号。 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val configuration = LocalConfiguration.current
    val scale = if (configuration.isIOS) configuration.fontSizeScale else 1f
    NativeText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize.scaledSp(scale),
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing.scaledSp(scale),
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight.scaledSp(scale),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style.scaledForNativeText(scale, configuration.isIOS),
    )
}

/** 富文本复用标准样式、链接回调和行内内容；字号补偿与普通文字共用同一实现。 */
@Composable
fun AppRichText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val configuration = LocalConfiguration.current
    val scale = if (configuration.isIOS) configuration.fontSizeScale else 1f
    NativeText(
        text = text.scaledForNativeText(scale, configuration.isIOS),
        modifier = modifier,
        color = color,
        fontSize = fontSize.scaledSp(scale),
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing.scaledSp(scale),
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight.scaledSp(scale),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style.scaledForNativeText(scale, configuration.isIOS),
    )
}
