package com.dgtang.live.kuikly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.tencent.kuikly.compose.foundation.BorderStroke
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.defaultMinSize
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.RowScope
import com.tencent.kuikly.compose.material3.Button as MaterialButton
import com.tencent.kuikly.compose.material3.OutlinedButton as MaterialOutlinedButton
import com.tencent.kuikly.compose.material3.TextButton as MaterialTextButton
import com.tencent.kuikly.compose.material3.LocalContentColor
import com.tencent.kuikly.compose.material3.ProvideTextStyle
import com.tencent.kuikly.compose.material3.LocalMinimumInteractiveComponentEnforcement
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.minimumInteractiveComponentSize
import com.tencent.kuikly.compose.material3.ButtonColors
import com.tencent.kuikly.compose.material3.ButtonDefaults
import com.tencent.kuikly.compose.material3.ButtonElevation
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Shape
import com.tencent.kuikly.compose.ui.semantics.Role
import com.tencent.kuikly.compose.ui.unit.dp

/** 防重按钮的默认颜色取品牌主题，避免 Kuikly 默认浅色 ColorScheme。 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val buttonColors = if (colors != null) colors else {
        val themeColors = KuiklyTheme.colors
        ButtonDefaults.buttonColors(
            containerColor = themeColors.primary,
            contentColor = themeColors.onPrimary,
            disabledContainerColor = themeColors.onBackground.copy(alpha = 0.12f),
            disabledContentColor = themeColors.onBackground.copy(alpha = 0.38f),
        )
    }
    AppButtonBounds(modifier, border, shape) { visualModifier ->
        MaterialButton(
            onClick = rememberAppClick(onClick = onClick),
            modifier = visualModifier,
            enabled = enabled,
            shape = shape,
            colors = buttonColors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            // MaterialButton 会覆盖根文字样式，在其内容作用域恢复品牌主题。
            content = { ProvideTextStyle(KuiklyTheme.typography.labelLarge) { content() } },
        )
    }
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    AppButtonBounds(modifier, border, shape) { visualModifier ->
        MaterialTextButton(
            onClick = rememberAppClick(onClick = onClick),
            modifier = visualModifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.textButtonColors(
                contentColor = KuiklyTheme.colors.primary,
                disabledContentColor = KuiklyTheme.colors.onBackground.copy(alpha = 0.38f),
            ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = { ProvideTextStyle(KuiklyTheme.typography.labelLarge) { content() } },
        )
    }
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = if (colors != null) ButtonDefaults.outlinedButtonBorder(enabled) else BorderStroke(
        1.dp,
        if (enabled) KuiklyTheme.colors.outline else KuiklyTheme.colors.onBackground.copy(alpha = 0.12f),
    ),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    AppButtonBounds(modifier, border, shape) { visualModifier ->
        MaterialOutlinedButton(
            onClick = rememberAppClick(onClick = onClick),
            modifier = visualModifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.outlinedButtonColors(
                contentColor = KuiklyTheme.colors.primary,
                disabledContentColor = KuiklyTheme.colors.onBackground.copy(alpha = 0.38f),
            ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = { ProvideTextStyle(KuiklyTheme.typography.labelLarge) { content() } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppButtonBounds(
    modifier: Modifier,
    border: BorderStroke?,
    shape: Shape,
    content: @Composable (Modifier) -> Unit,
) {
    val visualModifier = Modifier.defaultMinSize(ButtonDefaults.MinWidth, ButtonDefaults.MinHeight)
    if (border == null) {
        content(modifier.then(visualModifier))
    } else {
        // 2.28 Surface 的 border 未实现。描边必须与背景同层，不能画在 48dp 点击预留区上。
        Box(modifier.minimumInteractiveComponentSize(), propagateMinConstraints = true) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                content(visualModifier.border(border, shape))
            }
        }
    }
}

/** 当前 Kuikly 版本没有 Material IconButton，用同尺寸可点击容器承载图标。 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val contentColor = LocalContentColor.current
    CompositionLocalProvider(
        LocalContentColor provides if (enabled) contentColor else contentColor.copy(alpha = 0.38f),
    ) {
        Box(
            modifier = modifier.minimumInteractiveComponentSize()
                .appClickable(enabled = enabled, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}
