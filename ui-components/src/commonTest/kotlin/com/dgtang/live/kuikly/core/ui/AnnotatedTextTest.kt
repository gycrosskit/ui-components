package com.dgtang.live.kuikly.core.ui

import com.tencent.kuikly.compose.ui.text.LinkAnnotation
import com.tencent.kuikly.compose.ui.text.SpanStyle
import com.tencent.kuikly.compose.ui.text.TextLinkStyles
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.withLink
import com.tencent.kuikly.compose.ui.text.withStyle
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotatedTextTest {
    @Test
    fun explicitSpacingCoversPlainAndLinkSegmentsWithoutLosingClicksOrOverrides() {
        var clicked = false
        val original = buildAnnotatedString {
            append("请阅读")
            withLink(LinkAnnotation.Clickable(
                tag = "privacy",
                styles = TextLinkStyles(style = SpanStyle()),
                linkInteractionListener = { clicked = true },
            )) { append("隐私政策") }
            withStyle(SpanStyle(letterSpacing = 1.sp)) { append("。") }
        }
        val result = original.withSpanLetterSpacing(TextStyle(letterSpacing = 0.2.sp))
        assertEquals(original.text, result.text)
        assertEquals(0, result.spanStyles.first().start)
        assertEquals(result.length, result.spanStyles.first().end)
        assertEquals(0.2.sp, result.spanStyles.first().item.letterSpacing)
        assertEquals(original.spanStyles, result.spanStyles.drop(1))
        assertEquals(original.getLinkAnnotations(0, original.length), result.getLinkAnnotations(0, result.length))
        val link = result.getLinkAnnotations(0, result.length).single().item
        link.linkInteractionListener!!.onClick(link)
        assertEquals(true, clicked)
        assertEquals(0.sp, original.withSpanLetterSpacing(TextStyle()).spanStyles.first().item.letterSpacing)
    }
}
