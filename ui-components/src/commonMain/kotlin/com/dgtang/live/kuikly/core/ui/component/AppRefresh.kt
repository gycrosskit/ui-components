package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import com.tencent.kuikly.compose.animation.Crossfade
import com.tencent.kuikly.compose.animation.core.LinearEasing
import com.tencent.kuikly.compose.animation.core.RepeatMode
import com.tencent.kuikly.compose.animation.core.animateFloat
import com.tencent.kuikly.compose.animation.core.animateFloatAsState
import com.tencent.kuikly.compose.animation.core.infiniteRepeatable
import com.tencent.kuikly.compose.animation.core.rememberInfiniteTransition
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.progressSemantics
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.requiredSize
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.shadow
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.graphics.drawscope.inset
import com.tencent.kuikly.compose.ui.graphics.drawscope.rotate
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

/** 指示器只负责显示；拖动与刷新状态由 Kuikly 管理。 */
@Composable
internal fun AppRefreshIndicator(isRefreshing: Boolean, progress: Float) {
    if (LocalConfiguration.current.isIOS) {
        CupertinoRefreshIndicator(isRefreshing, progress)
    } else {
        val colors = KuiklyTheme.colors
        Box(
            Modifier.size(40.dp).shadow(3.dp, CircleShape)
                .background(colors.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = isRefreshing) { active ->
                if (active) CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), strokeWidth = 2.5.dp,
                    color = colors.onSurfaceVariant,
                ) else MaterialRefreshArrow(progress)
            }
        }
    }
}

@Composable
private fun MaterialRefreshArrow(progress: Float) {
    val color = KuiklyTheme.colors.onSurfaceVariant
    val arrow = remember { Path() }
    val alpha by animateFloatAsState(if (progress >= 1f) 1f else 0.3f)
    // 保留 16dp 布局和 Material3 指示器路径；32dp 原生 frame 容纳旋转箭头及描边，不缩小图形。
    Canvas(Modifier.size(16.dp).requiredSize(32.dp).progressSemantics(progress.coerceIn(0f, 1f))) {
        val iconSize = 16.dp.roundToPx()
        val padding = ((size.minDimension.toInt() - iconSize) / 2).toFloat()
        inset(padding, padding, size.width - iconSize - padding, size.height - iconSize - padding) {
            val adjusted = (progress.coerceAtMost(1f) - 0.4f).coerceAtLeast(0f) * 5f / 3f
            val tension = (abs(progress) - 1f).coerceIn(0f, 2f)
            val rotation = (-0.25f + 0.4f * adjusted + tension - tension * tension / 4f) * 0.5f
            val start = rotation * 360f
            val end = (rotation + adjusted * 0.8f) * 360f
            val bounds = Rect(center, 5.5.dp.toPx() + 2.5.dp.toPx() / 2f)
            // 角度、三角形 Stroke 与末端 strokePx 修正沿用 Material3 1.9 PullToRefresh.kt 的公式。
            rotate(rotation) {
                drawArc(color, start, end - start, false, bounds.topLeft, bounds.size,
                    alpha = alpha, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Butt))
                arrow.reset()
                arrow.moveTo(0f, 0f)
                arrow.lineTo(10.dp.toPx() * adjusted / 2f, 5.dp.toPx() * adjusted)
                arrow.lineTo(10.dp.toPx() * adjusted, 0f)
                arrow.translate(Offset(bounds.width / 2f + bounds.center.x - 10.dp.toPx() * adjusted / 2f,
                    bounds.center.y - 2.5.dp.toPx()))
                rotate(end - 2.5.dp.toPx()) {
                    drawPath(arrow, color, alpha = alpha, style = Stroke(2.5.dp.toPx()))
                }
            }
        }
    }
}

/** iOS 使用十二段、20dp、900ms 的 Cupertino 指示器。 */
@Composable
internal fun CupertinoRefreshIndicator(
    animating: Boolean,
    revealFraction: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(0f, 12f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart))
    val color = KuiklyTheme.colors.onSurfaceVariant
    Canvas(modifier.size(20.dp)) {
        val visibleSpokes = (revealFraction.coerceIn(0f, 1f) * 12).roundToInt()
        repeat(12) { index ->
            val age = (phase.toInt() - index + 12) % 12
            val alpha = when {
                animating -> 1f - age.toFloat() / 14f
                index < visibleSpokes -> 0.72f
                else -> 0f
            }
            if (alpha > 0f) rotate(index * 30f) {
                drawLine(color.copy(alpha = alpha.coerceIn(0.18f, 1f)),
                    Offset(center.x, size.height * 0.08f), Offset(center.x, size.height * 0.30f),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}
