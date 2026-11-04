package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.text.BasicTextField
import com.tencent.kuikly.compose.foundation.text.KeyboardActions
import com.tencent.kuikly.compose.foundation.text.KeyboardOptions
import com.tencent.kuikly.compose.extension.setProp
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.SolidColor
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.input.KeyboardType
import com.tencent.kuikly.compose.ui.text.input.VisualTransformation

/**
 * 登录、搜索和 Outlined 输入共用的编辑入口，外观由调用方的 decorationBox 决定。
 * 原生 SP 与 iOS 文本共用统一平台补偿，placeholder/label 由 AppText 保持同一比例。
 */
@Composable
fun AppBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = KuiklyTheme.typography.bodyLarge,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(KuiklyTheme.colors.primary),
    hideNativePasswordIcon: Boolean = keyboardOptions.keyboardType == KeyboardType.Password,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { it() },
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = if (hideNativePasswordIcon) modifier.setProp("skillcurePasswordIconVisible", 0) else modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle.forNativeText(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        decorationBox = decorationBox,
    )
}
