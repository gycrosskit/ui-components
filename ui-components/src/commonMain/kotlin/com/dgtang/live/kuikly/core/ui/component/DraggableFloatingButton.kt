package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.gestures.detectDragGestures
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.input.pointer.PointerInputScope
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.dp
import kotlin.math.roundToInt

/** 两种浮球共用可拖动区域与吸边；比例只表示位置，不负责业务持久化。 */
internal class FloatingButtonBounds(width: Int, height: Int, buttonSize: Int, margin: Float) {
    private val availableWidth = (width - buttonSize).coerceAtLeast(0).toFloat()
    private val availableHeight = (height - buttonSize).coerceAtLeast(0).toFloat()
    private val left = margin.coerceAtMost(availableWidth)
    private val top = margin.coerceAtMost(availableHeight)
    private val horizontal = (availableWidth - margin - left).coerceAtLeast(0f)
    private val vertical = (availableHeight - margin - top).coerceAtLeast(0f)

    fun offset(position: Offset): Offset = Offset(left + position.x * horizontal, top + position.y * vertical)

    fun position(offset: Offset): Offset = Offset(
        if (horizontal > 0f) ((offset.x - left) / horizontal).coerceIn(0f, 1f) else 1f,
        if (vertical > 0f) ((offset.y - top) / vertical).coerceIn(0f, 1f) else 1f,
    )

    fun snapToEdge(position: Offset): Offset = Offset(if (position.x < 0.5f) 0f else 1f, position.y)
}

/** 仅按钮挂手势；超过原生触摸阈值后消费移动，保留底层页面和按钮的轻触行为。 */
internal suspend fun PointerInputScope.detectFloatingButtonDrag(
    bounds: FloatingButtonBounds,
    position: () -> Offset,
    onDrag: (Offset) -> Unit,
    onFinished: (Offset) -> Unit,
) {
    val finish = {
        val snapped = bounds.snapToEdge(position())
        onDrag(snapped)
        onFinished(snapped)
    }
    detectDragGestures(onDragEnd = finish, onDragCancel = finish) { change, amount ->
        change.consume()
        onDrag(bounds.position(bounds.offset(position()) + amount))
    }
}

@Composable
internal fun rememberFloatingButtonPosition(initialPosition: Offset, bounds: FloatingButtonBounds): MutableState<Offset> =
    // 边界变化会取消旧手势，临时位置同步恢复，不能保存尚未完成的拖动。
    remember(initialPosition, bounds) { mutableStateOf(initialPosition) }

/** 共享拖拽、边界和吸边；反馈入口保存位置，Debug 仅保留组合期间的位置。 */
@Composable
fun DraggableFloatingButton(
    initialPosition: Offset,
    modifier: Modifier = Modifier,
    onDragFinished: ((Offset) -> Unit)? = null,
    content: @Composable (Modifier) -> Unit,
) {
    val density = LocalDensity.current
    val size = with(density) { 56.dp.roundToPx() }
    val margin = with(density) { 16.dp.toPx() }
    val finish by rememberUpdatedState(onDragFinished)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val bounds = remember(constraints.maxWidth, constraints.maxHeight, size, margin) {
            FloatingButtonBounds(constraints.maxWidth, constraints.maxHeight, size, margin)
        }
        var position by rememberFloatingButtonPosition(initialPosition, bounds)
        content(Modifier.offset {
            val offset = bounds.offset(position)
            IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
        }.size(56.dp).pointerInput(bounds, initialPosition) {
            detectFloatingButtonDrag(bounds, { position }, { position = it }) { finish?.invoke(it) }
        })
    }
}
