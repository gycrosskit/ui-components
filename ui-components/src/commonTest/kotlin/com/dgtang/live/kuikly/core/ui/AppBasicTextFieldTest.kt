package com.dgtang.live.kuikly.core.ui

import com.dgtang.live.kuikly.core.ui.component.scaledForNativeText
import com.tencent.kuikly.compose.ui.text.LinkAnnotation
import com.tencent.kuikly.compose.ui.text.SpanStyle
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.withLink
import com.tencent.kuikly.compose.ui.text.withStyle
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.em
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AppBasicTextFieldTest {
    @Test
    fun nativeSpPlatformsDoNotReceiveASecondFontScale() {
        val style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp)
        assertSame(style, style.scaledForNativeText(1.5f, isIos = false))
        assertSame(style, style.scaledForNativeText(2f, isIos = false))
        val ios = style.scaledForNativeText(1.5f, isIos = true)
        assertEquals(21.sp, ios.fontSize)
        assertEquals(30.sp, ios.lineHeight)
        assertEquals(0.3f, ios.letterSpacing.value, 0.0001f)
    }

    @Test
    fun iosScalePreservesUnspecifiedAndRelativeUnits() {
        // 只指定字号的表单不能拿 Unspecified 行高参与乘法。
        val form = TextStyle(fontSize = 14.sp).scaledForNativeText(1.5f, isIos = true)
        assertEquals(21.sp, form.fontSize)
        assertEquals(TextUnit.Unspecified, form.lineHeight)
        assertEquals(TextStyle(), TextStyle().scaledForNativeText(1.5f, isIos = true))
        val relative = TextStyle(fontSize = 14.sp, lineHeight = 1.5.em, letterSpacing = 0.1.em)
            .scaledForNativeText(2f, isIos = true)
        assertEquals(28.sp, relative.fontSize)
        assertEquals(1.5.em, relative.lineHeight)
        assertEquals(0.1.em, relative.letterSpacing)
    }

    @Test
    fun iosRichTextScaleRetainsLinksAndInlineAnnotations() {
        var clicked = false
        val original = buildAnnotatedString {
            withLink(LinkAnnotation.Clickable("privacy", linkInteractionListener = { clicked = true })) {
                withStyle(SpanStyle(fontSize = 14.sp, letterSpacing = 0.2.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) { append("隐私政策") }
            }
            addStringAnnotation("androidx.compose.foundation.text.inlineContent", "emoji", 0, 1)
        }
        val scaled = original.scaledForNativeText(1.5f, isIos = true)
        assertEquals(original.text, scaled.text)
        assertEquals(original.getStringAnnotations(0, original.length), scaled.getStringAnnotations(0, scaled.length))
        assertEquals(original.getLinkAnnotations(0, original.length), scaled.getLinkAnnotations(0, scaled.length))
        assertEquals(21.sp, scaled.spanStyles.last().item.fontSize)
        assertEquals(FontWeight.Bold, scaled.spanStyles.last().item.fontWeight)
        assertEquals(FontStyle.Italic, scaled.spanStyles.last().item.fontStyle)
        val link = scaled.getLinkAnnotations(0, scaled.length).single().item
        link.linkInteractionListener!!.onClick(link)
        assertEquals(true, clicked)
        assertSame(original, original.scaledForNativeText(2f, isIos = false))
    }
}
