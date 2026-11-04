package com.dgtang.live.kuikly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.material3.LocalContentColor
import com.tencent.kuikly.compose.material3.ProvideTextStyle
import com.tencent.kuikly.compose.material3.Shapes
import com.tencent.kuikly.compose.material3.Typography
import com.tencent.kuikly.compose.ui.graphics.Color

/** 当前 Kuikly MaterialTheme 不能注入品牌配色和排版；由应用主题统一提供。 */
data class KuiklyAppColors(
    val primary: Color,
    val onPrimary: Color,
    val headerBackground: Color,
    val headerContent: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurfaceVariant: Color,
    val contentDivider: Color,
    val outline: Color,
    val dialogTitle: Color,
    val isDark: Boolean,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val onSurface: Color,
    val surfaceContainerHighest: Color,
    val secondaryContainer: Color,
    val error: Color,
    val tertiary: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceVariant: Color,
    val onSecondaryContainer: Color,
    val outlineVariant: Color,
    val dialogDivider: Color,
    val dialogText: Color,
    val dialogPrimary: Color,
)

internal val LocalKuiklyAppColors = staticCompositionLocalOf<KuiklyAppColors> {
    error("KuiklyAppTheme must wrap the page")
}
internal val LocalKuiklyAppTypography = staticCompositionLocalOf<Typography> {
    error("KuiklyAppTheme must wrap the page")
}

object KuiklyTheme {
    val colors: KuiklyAppColors
        @Composable get() = LocalKuiklyAppColors.current

    val typography: Typography
        @Composable get() = LocalKuiklyAppTypography.current

    // 使用 Material3 默认五档圆角，不另建形状表。
    val shapes = Shapes()
}

private val defaultTypography = Typography()
/** Dialog 有独立 Composition，根页面与弹窗复用同一主题值。 */
@Composable
fun ProvideKuiklyTheme(
    colors: KuiklyAppColors,
    typography: Typography = defaultTypography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalKuiklyAppColors provides colors,
        LocalKuiklyAppTypography provides typography,
        LocalContentColor provides colors.onBackground,
    ) {
        ProvideTextStyle(typography.bodyLarge, content)
    }
}
