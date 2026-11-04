package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import com.tencent.kuikly.compose.material3.Checkbox
import com.tencent.kuikly.compose.material3.CheckboxColors
import com.tencent.kuikly.compose.material3.CheckboxDefaults
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color

/** 状态归调用方；整行消费点击时传 null，避免复选框再次处理同一次操作。 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = appCheckboxColors(),
) {
    Checkbox(checked, onCheckedChange, modifier = modifier, enabled = enabled, colors = colors)
}

/** 品牌可覆盖正常配色，禁用色共用主题语义。 */
@Composable
fun appCheckboxColors(
    checkedColor: Color = KuiklyTheme.colors.primary,
    uncheckedColor: Color = KuiklyTheme.colors.onSurfaceVariant,
    checkmarkColor: Color = KuiklyTheme.colors.onPrimary,
): CheckboxColors {
    val disabledColor = KuiklyTheme.colors.onSurface.copy(alpha = 0.38f)
    return CheckboxDefaults.colors(
        checkedColor = checkedColor,
        uncheckedColor = uncheckedColor,
        checkmarkColor = checkmarkColor,
        disabledCheckedColor = disabledColor,
        disabledUncheckedColor = disabledColor,
        disabledIndeterminateColor = disabledColor,
    )
}
