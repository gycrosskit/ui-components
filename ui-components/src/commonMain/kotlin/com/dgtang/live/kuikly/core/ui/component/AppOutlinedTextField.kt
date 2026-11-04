package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import com.tencent.kuikly.compose.animation.animateColorAsState
import com.tencent.kuikly.compose.animation.core.LinearEasing
import com.tencent.kuikly.compose.animation.core.animateDpAsState
import com.tencent.kuikly.compose.animation.core.animateFloat
import com.tencent.kuikly.compose.animation.core.spring
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.animation.core.updateTransition
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.interaction.collectIsFocusedAsState
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.defaultMinSize
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.text.KeyboardActions
import com.tencent.kuikly.compose.foundation.text.KeyboardOptions
import com.tencent.kuikly.compose.foundation.text.selection.LocalTextSelectionColors
import com.tencent.kuikly.compose.foundation.text.selection.TextSelectionColors
import com.tencent.kuikly.compose.material3.LocalContentColor
import com.tencent.kuikly.compose.material3.ProvideTextStyle
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.CornerRadius
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.ClipOp
import com.tencent.kuikly.compose.ui.graphics.SolidColor
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.graphics.drawscope.clipRect
import com.tencent.kuikly.compose.ui.graphics.graphicsLayer
import com.tencent.kuikly.compose.ui.layout.IntrinsicMeasurable
import com.tencent.kuikly.compose.ui.layout.IntrinsicMeasureScope
import com.tencent.kuikly.compose.ui.layout.Layout
import com.tencent.kuikly.compose.ui.layout.LayoutIdParentData
import com.tencent.kuikly.compose.ui.layout.Measurable
import com.tencent.kuikly.compose.ui.layout.MeasurePolicy
import com.tencent.kuikly.compose.ui.layout.MeasureResult
import com.tencent.kuikly.compose.ui.layout.MeasureScope
import com.tencent.kuikly.compose.ui.layout.layoutId
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.semantics.disabled
import com.tencent.kuikly.compose.ui.semantics.error
import com.tencent.kuikly.compose.ui.semantics.semantics
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.input.VisualTransformation
import com.tencent.kuikly.compose.ui.text.lerp
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.unit.constrainHeight
import com.tencent.kuikly.compose.ui.unit.constrainWidth
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.offset
import com.tencent.kuikly.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.roundToInt

private enum class FieldSlot { Container, Field, Label, Placeholder, Supporting }
private enum class InputPhase { Focused, Empty, NotEmpty }

/**
 * 2.28 的 Outlined DecorationBox 实际路由到 Filled，且默认 Container 使用背景色描边。
 * 这里只补当前无图标调用面的 Outlined 装饰，编辑、焦点与输入法仍由 BasicTextField 负责。
 */
@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: String? = null,
    textStyle: TextStyle = KuiklyTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    require(minLines >= 1 && maxLines >= minLines)
    val colors = KuiklyTheme.colors
    val typography = KuiklyTheme.typography
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val phase = when {
        focused -> InputPhase.Focused
        value.isEmpty() -> InputPhase.Empty
        else -> InputPhase.NotEmpty
    }
    val transition = updateTransition(phase, label = "OutlinedTextFieldInput")
    val labelProgress = transition.animateFloat(
        transitionSpec = { tween(150) }, label = "LabelProgress",
    ) { if (it == InputPhase.Empty) 0f else 1f }
    val placeholderAlpha = transition.animateFloat(
        transitionSpec = {
            when {
                InputPhase.Focused isTransitioningTo InputPhase.Empty -> tween(67, easing = LinearEasing)
                InputPhase.Empty isTransitioningTo InputPhase.Focused ||
                    InputPhase.NotEmpty isTransitioningTo InputPhase.Empty ->
                    tween(83, delayMillis = 67, easing = LinearEasing)
                else -> spring()
            }
        }, label = "PlaceholderOpacity",
    ) {
        when (it) {
            InputPhase.Focused -> 1f
            InputPhase.Empty -> if (label == null) 1f else 0f
            InputPhase.NotEmpty -> 0f
        }
    }
    val disabledColor = colors.onBackground.copy(alpha = 0.38f)
    val targetBorderColor = when {
        !enabled -> colors.onBackground.copy(alpha = 0.12f)
        isError -> colors.error
        focused -> colors.primary
        else -> colors.outline
    }
    val borderColor by if (enabled) animateColorAsState(targetBorderColor, tween(150))
        else rememberUpdatedState(targetBorderColor)
    val borderWidth by if (enabled) animateDpAsState(if (focused) 2.dp else 1.dp, tween(150))
        else rememberUpdatedState(1.dp)
    val targetLabelColor = when {
        !enabled -> disabledColor
        isError -> colors.error
        focused -> colors.primary
        else -> colors.onSurfaceVariant
    }
    val labelColor by animateColorAsState(targetLabelColor, tween(150))
    var labelSize by remember { mutableStateOf(Size.Zero) }
    val measurePolicy = remember(singleLine) {
        OutlinedFieldMeasurePolicy(singleLine, { labelProgress.value }) {
            if (labelSize != it) labelSize = it
        }
    }
    val lineHeight = with(density) {
        val fontSize = if (textStyle.fontSize.isSp) textStyle.fontSize else typography.bodyLarge.fontSize
        when {
            textStyle.lineHeight.isSp -> textStyle.lineHeight.toDp()
            textStyle.lineHeight.isEm -> (fontSize * textStyle.lineHeight.value).toDp()
            else -> (fontSize * (typography.bodyLarge.lineHeight.value / typography.bodyLarge.fontSize.value)).toDp()
        }
    }
    // 2.28 的 String BasicTextField 没有把 minLines 传给 CoreTextField，必须在输入 slot 约束高度。
    val fieldModifier = Modifier.padding(horizontal = 16.dp).heightIn(
        min = lineHeight * if (singleLine) 1 else minLines,
        max = if (singleLine || maxLines == Int.MAX_VALUE) Dp.Unspecified else lineHeight * maxLines,
    )
    val placeholderModifier = Modifier.padding(horizontal = 16.dp)
        .heightIn(min = with(density) { 24.sp.toDp() })
    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = colors.primary,
            backgroundColor = colors.primary.copy(alpha = 0.4f),
        ),
    ) {
        AppBasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.semantics(mergeDescendants = label != null) {
                if (!enabled) disabled()
                if (isError && errorMessage != null) error(errorMessage)
            }.then(if (label != null) Modifier.padding(top = with(density) { 8.sp.toDp() }) else Modifier)
                .defaultMinSize(minWidth = 280.dp, minHeight = 56.dp),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = textStyle.copy(color = if (enabled) colors.onBackground else disabledColor),
            cursorBrush = SolidColor(if (isError) colors.error else colors.primary),
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Layout(
                    content = {
                        // 2.28 的 drawBehind 只在 CanvasView 执行；普通 Box 不会执行描边回调。
                        Canvas(Modifier.layoutId(FieldSlot.Container)) {
                            val stroke = borderWidth.toPx()
                            val half = stroke / 2f
                            fun drawBorder() = drawRoundRect(
                                color = borderColor,
                                topLeft = Offset(half, half),
                                size = Size((size.width - stroke).coerceAtLeast(0f), (size.height - stroke).coerceAtLeast(0f)),
                                cornerRadius = CornerRadius((4.dp.toPx() - half).coerceAtLeast(0f)),
                                style = Stroke(stroke),
                            )
                            val cutoutWidth = labelSize.width * labelProgress.value
                            val cutoutHeight = labelSize.height * labelProgress.value
                            if (cutoutWidth > 0f) {
                                val start = 16.dp.toPx()
                                val left = if (layoutDirection == LayoutDirection.Ltr) start
                                    else size.width - start - cutoutWidth
                                // 直接裁描边，不用背景色遮盖，表单放在页面或卡片中都保留透明背景。
                                clipRect(
                                    left = (left - 4.dp.toPx()).coerceAtLeast(0f),
                                    top = -cutoutHeight / 2f,
                                    right = (left + cutoutWidth + 4.dp.toPx()).coerceAtMost(size.width),
                                    bottom = cutoutHeight / 2f,
                                    clipOp = ClipOp.Difference,
                                ) { drawBorder() }
                            } else drawBorder()
                        }
                        Box(Modifier.layoutId(FieldSlot.Field).then(fieldModifier), propagateMinConstraints = true) {
                            innerTextField()
                        }
                        if (label != null) {
                            Box(Modifier.layoutId(FieldSlot.Label).heightIn(
                                min = with(density) {
                                    (24.sp.toPx() + (16.sp.toPx() - 24.sp.toPx()) * labelProgress.value).toDp()
                                },
                            )) {
                                CompositionLocalProvider(LocalContentColor provides labelColor) {
                                    ProvideTextStyle(lerp(typography.bodyLarge, typography.bodySmall, labelProgress.value)) { label() }
                                }
                            }
                        }
                        if (placeholder != null && value.isEmpty() && placeholderAlpha.value > 0f) {
                            Box(Modifier.layoutId(FieldSlot.Placeholder).then(placeholderModifier)
                                .graphicsLayer { alpha = placeholderAlpha.value }) {
                                CompositionLocalProvider(LocalContentColor provides if (enabled) colors.onSurfaceVariant else disabledColor) {
                                    ProvideTextStyle(typography.bodyLarge) { placeholder() }
                                }
                            }
                        }
                        if (supportingText != null) {
                            Box(Modifier.layoutId(FieldSlot.Supporting)
                                .padding(start = 16.dp, top = 4.dp, end = 16.dp)
                                .heightIn(min = with(density) { 16.sp.toDp() })) {
                                CompositionLocalProvider(LocalContentColor provides when {
                                    !enabled -> disabledColor
                                    isError -> colors.error
                                    else -> colors.onSurfaceVariant
                                }) {
                                    ProvideTextStyle(typography.bodySmall) { supportingText() }
                                }
                            }
                        }
                    },
                    measurePolicy = measurePolicy,
                )
            },
        )
    }
}

private val IntrinsicMeasurable.slotId: Any?
    get() = (parentData as? LayoutIdParentData)?.layoutId

/** 使用无图标 Attached label 测量公式，supportingText 不属于描边区域。 */
private class OutlinedFieldMeasurePolicy(
    private val singleLine: Boolean,
    private val labelProgress: () -> Float,
    private val onLabelMeasured: (Size) -> Unit,
) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        val padding = 16.dp.roundToPx()
        val relaxed = constraints.copy(minWidth = 0, minHeight = 0)
        val label = measurables.firstOrNull { it.slotId == FieldSlot.Label }
            ?.measure(relaxed.offset(horizontal = -2 * padding, vertical = -padding))
        onLabelMeasured(label?.let { Size(it.width.toFloat(), it.height.toFloat()) } ?: Size.Zero)
        val supporting = measurables.firstOrNull { it.slotId == FieldSlot.Supporting }
        val supportingIntrinsicHeight = supporting?.minIntrinsicHeight(constraints.minWidth) ?: 0
        val textConstraints = constraints.offset(
            vertical = -padding - max(padding, (label?.height ?: 0) / 2) - supportingIntrinsicHeight,
        ).copy(minHeight = 0)
        val field = measurables.first { it.slotId == FieldSlot.Field }.measure(textConstraints)
        val placeholder = measurables.firstOrNull { it.slotId == FieldSlot.Placeholder }
            ?.measure(textConstraints.copy(minWidth = 0))
        val p = labelProgress()
        val width = constraints.constrainWidth(calculateWidth(
            field.width, placeholder?.width ?: 0, label?.width ?: 0, padding, p,
        ))
        val support = supporting?.measure(relaxed.copy(maxWidth = width).offset(
            vertical = -max(field.height, placeholder?.height ?: 0) - max(padding, (label?.height ?: 0) / 2) - padding,
        ))
        val supportHeight = support?.height ?: 0
        val height = constraints.constrainHeight(calculateHeight(
            field.height, placeholder?.height ?: 0, label?.height ?: 0, supportHeight, padding, p,
        ))
        val containerHeight = (height - supportHeight).coerceAtLeast(0)
        val container = measurables.first { it.slotId == FieldSlot.Container }.measure(Constraints(
            minWidth = if (width == Constraints.Infinity) 0 else width,
            maxWidth = width,
            minHeight = if (containerHeight == Constraints.Infinity) 0 else containerHeight,
            maxHeight = containerHeight,
        ))
        return layout(width, height) {
            container.placeRelative(0, 0)
            fun fieldY(childHeight: Int): Int = max(
                if (singleLine) (containerHeight - childHeight) / 2 else padding,
                (label?.height ?: 0) / 2,
            )
            field.placeRelative(0, fieldY(field.height))
            placeholder?.placeRelative(0, fieldY(placeholder.height))
            label?.let {
                val startY = if (singleLine) (containerHeight - it.height) / 2 else padding
                it.placeRelative(padding, (startY + (-it.height / 2 - startY) * p).roundToInt())
            }
            support?.placeRelative(0, containerHeight)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        intrinsicWidth(measurables, height) { child, h -> child.minIntrinsicWidth(h) }
    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int =
        intrinsicWidth(measurables, height) { child, h -> child.maxIntrinsicWidth(h) }
    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        intrinsicHeight(measurables, width) { child, w -> child.minIntrinsicHeight(w) }
    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int =
        intrinsicHeight(measurables, width) { child, w -> child.maxIntrinsicHeight(w) }

    private fun IntrinsicMeasureScope.intrinsicWidth(
        children: List<IntrinsicMeasurable>, height: Int, measure: (IntrinsicMeasurable, Int) -> Int,
    ): Int {
        fun width(slot: FieldSlot) = children.firstOrNull { it.slotId == slot }?.let { measure(it, height) } ?: 0
        return calculateWidth(width(FieldSlot.Field), width(FieldSlot.Placeholder), width(FieldSlot.Label), 16.dp.roundToPx(), labelProgress())
    }
    private fun IntrinsicMeasureScope.intrinsicHeight(
        children: List<IntrinsicMeasurable>, width: Int, measure: (IntrinsicMeasurable, Int) -> Int,
    ): Int {
        fun height(slot: FieldSlot) = children.firstOrNull { it.slotId == slot }?.let { measure(it, width) } ?: 0
        return calculateHeight(height(FieldSlot.Field), height(FieldSlot.Placeholder), height(FieldSlot.Label), height(FieldSlot.Supporting), 16.dp.roundToPx(), labelProgress())
    }
    private fun calculateWidth(field: Int, placeholder: Int, label: Int, padding: Int, p: Float): Int = max(
        maxOf(field, placeholder, (label * (1f - p)).roundToInt()),
        ((label + 2 * padding) * p).roundToInt(),
    )
    private fun calculateHeight(field: Int, placeholder: Int, label: Int, supporting: Int, padding: Int, p: Float): Int =
        (maxOf(field, placeholder, (label * (1f - p)).roundToInt()) +
            padding + (max(padding.toFloat(), label / 2f) - padding) * p + padding).roundToInt() + supporting
}
