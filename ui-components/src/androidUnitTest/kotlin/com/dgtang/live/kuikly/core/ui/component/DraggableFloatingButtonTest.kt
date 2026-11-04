package com.dgtang.live.kuikly.core.ui.component

import com.dgtang.live.kuikly.core.ui.EmptyApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.input.pointer.AwaitPointerEventScope
import com.tencent.kuikly.compose.ui.input.pointer.PointerEvent
import com.tencent.kuikly.compose.ui.input.pointer.PointerEventPass
import com.tencent.kuikly.compose.ui.input.pointer.PointerId
import com.tencent.kuikly.compose.ui.input.pointer.PointerInputChange
import com.tencent.kuikly.compose.ui.input.pointer.PointerInputScope
import com.tencent.kuikly.compose.ui.node.LayoutNode
import com.tencent.kuikly.compose.ui.platform.ViewConfiguration
import com.tencent.kuikly.compose.ui.unit.IntSize
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class DraggableFloatingButtonTest {
    private val bounds = FloatingButtonBounds(360, 640, 56, 16f)

    @Test
    fun boundsKeepMarginsAndRestoreFractionsAfterResize() {
        assertEquals(Offset(16f, 16f), bounds.offset(Offset.Zero))
        assertEquals(Offset(288f, 568f), bounds.offset(Offset(1f, 1f)))
        assertEquals(Offset.Zero, bounds.position(Offset(-100f, -100f)))
        assertEquals(Offset(1f, 1f), bounds.position(Offset(999f, 999f)))
        val saved = Offset(0f, 0.25f)
        assertEquals(saved, bounds.position(bounds.offset(saved)))
        val resized = FloatingButtonBounds(640, 360, 56, 16f)
        assertEquals(Offset(16f, 84f), resized.offset(saved))
        val tiny = FloatingButtonBounds(40, 70, 56, 16f)
        assertEquals(Offset(0f, 14f), tiny.offset(saved))
        assertEquals(Offset(1f, 1f), tiny.position(Offset.Zero))
    }

    @Test
    fun tapAndJitterDoNotConsumeOrCommit() {
        for (distance in listOf(0f, 4f)) {
            val changes = stream(listOf(Offset(distance, 0f)))
            val result = drag(changes)
            assertEquals(Offset(1f, 0.5f), result.first)
            assertEquals(0, result.second)
            assertTrue(changes.none { it.isConsumed }, "轻触仍交给按钮点击处理")
        }
    }

    @Test
    fun finishedDragSnapsToNearestEdgeAndKeepsHeight() {
        assertEquals(Offset(0f, 0.25f), bounds.snapToEdge(Offset(0.49f, 0.25f)))
        assertEquals(Offset(1f, 0.25f), bounds.snapToEdge(Offset(0.5f, 0.25f)))
        for ((distance, edge) in listOf(-220f to 0f, -60f to 1f)) {
            val changes = stream(listOf(Offset(distance, 50f)))
            val (position, finishes) = drag(changes)
            assertEquals(edge, position.x)
            assertTrue(position.y > 0.5f, "吸边保留拖动后的高度")
            assertEquals(1, finishes)
            assertTrue(changes[1].isConsumed, "拖动吸边不能触发点击")
        }
    }

    @Test
    fun dragClampsAndConsumesEvenWhenReturningToStart() {
        val changes = stream(listOf(Offset(-1000f, -1000f), Offset(0f, 0f)))
        val (position, finishes) = drag(changes)
        assertEquals(Offset(1f, 1f), position)
        assertEquals(1, finishes)
        assertFalse(changes.first().isConsumed, "按下不抢占手势")
        assertTrue(changes.drop(1).dropLast(1).all { it.isConsumed }, "拖回原点仍不能触发点击")
    }

    @Test
    fun cancelledDragFinishesOnceAndDoesNotConsumeFollowingTap() {
        val cancelled = stream(listOf(Offset(-100f, 0f), Offset(-150f, 0f)))
        cancelled[2].consume() // 模拟原生取消/其他手势接管。
        val tap = stream(listOf(Offset.Zero))
        val (position, finishes) = drag(cancelled + tap)
        assertEquals(1f, position.x, "取消后仍吸附到较近的右边缘")
        assertEquals(1, finishes, "反馈入口取消后仍沿用原有吸边与保存回调")
        assertTrue(tap.none { it.isConsumed }, "下一次轻触不能继承拖动状态")
    }

    @Test
    fun resizeDuringDragRestoresSavedEdgeWithoutSavingInterruptedPosition() = runTest {
        val saved = Offset(0f, 0.25f)
        val currentBounds = mutableStateOf(bounds)
        lateinit var position: MutableState<Offset>
        var saves = 0
        val recomposer = Recomposer(backgroundScope.coroutineContext)
        val composition = Composition(EmptyApplier(), recomposer)
        val clock = BroadcastFrameClock()
        val runner = backgroundScope.launch(clock) { recomposer.runRecomposeAndApplyChanges() }
        try {
            composition.setContent { position = rememberFloatingButtonPosition(saved, currentBounds.value) }
            val changes = stream(listOf(Offset(100f, 80f))).dropLast(1)
            val drag = launch {
                FloatingPointerScope(changes, waitForNextEvent = true).detectFloatingButtonDrag(
                    bounds, { position.value }, { position.value = it }, { saves++ },
                )
            }
            runCurrent()
            assertTrue(changes.last().isConsumed)
            assertNotEquals(saved, position.value, "确认旧窗口中的拖动已经开始")

            currentBounds.value = FloatingButtonBounds(640, 360, 56, 16f)
            Snapshot.sendApplyNotifications()
            runCurrent()
            clock.sendFrame(16_000_000L)
            runCurrent()
            // 边界 key 更新会取消旧 pointerInput 协程，不能把它当作用户松手保存。
            drag.cancelAndJoin()
            assertEquals(saved, position.value, "窗口变化后恢复已保存的边缘与高度比例")
            assertEquals(Offset(16f, 84f), currentBounds.value.offset(position.value))
            assertEquals(0, saves, "中断拖动不能写入反馈持久位置")
        } finally {
            composition.dispose()
            recomposer.cancel()
            runner.cancel()
        }
    }

    private fun drag(changes: List<PointerInputChange>): Pair<Offset, Int> = runBlocking {
        var position = Offset(1f, 0.5f)
        var finishes = 0
        try {
            FloatingPointerScope(changes).detectFloatingButtonDrag(bounds, { position }, { position = it }) {
                finishes++
            }
        } catch (_: PointerStreamFinished) {
            // detectDragGestures 持续等待下一次手势，队列结束只终止测试。
        }
        position to finishes
    }

    private fun stream(moves: List<Offset>): List<PointerInputChange> {
        val points = listOf(Offset(100f, 100f)) + moves.map { it + Offset(100f, 100f) }
        return points.mapIndexed { index, point ->
            change(index * 16L, point, true, points.getOrElse(index - 1) { point }, index > 0)
        } + change(points.size * 16L, points.last(), false, points.last(), true)
    }

    private fun change(time: Long, point: Offset, pressed: Boolean, previous: Offset, wasPressed: Boolean) =
        PointerInputChange(PointerId(1), time, point, pressed, time - 16L, previous, wasPressed, false)
}

private class PointerStreamFinished : RuntimeException()

/** 同一事件按 Initial/Main/Final 派发，直接检查生产 SDK 对事件的消费。 */
private class FloatingPointerScope(
    changes: List<PointerInputChange>,
    private val waitForNextEvent: Boolean = false,
) : PointerInputScope {
    private val events = changes.map { PointerEvent(listOf(it)) }.iterator()
    private var pendingEvent: Continuation<PointerEvent>? = null
    override val density = 1f
    override val fontScale = 1f
    override val size = IntSize(360, 640)
    override val layoutNode: LayoutNode get() = error("浮球手势不访问 LayoutNode 内部")
    override val viewConfiguration = object : ViewConfiguration {
        override val longPressTimeoutMillis = 500L
        override val doubleTapTimeoutMillis = 300L
        override val doubleTapMinTimeMillis = 40L
        override val touchSlop = 8f
    }
    override suspend fun <R> awaitPointerEventScope(block: suspend AwaitPointerEventScope.() -> R): R =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cause ->
                pendingEvent?.resumeWith(Result.failure(checkNotNull(cause)))
                pendingEvent = null
            }
            block.startCoroutine(eventScope, object : Continuation<R> {
                override val context = EmptyCoroutineContext
                override fun resumeWith(result: Result<R>) = continuation.resumeWith(result)
            })
        }

    private val eventScope = object : AwaitPointerEventScope {
        private var previousPass = PointerEventPass.Final
        override var currentEvent = PointerEvent(emptyList())
            private set
        override val density get() = this@FloatingPointerScope.density
        override val fontScale get() = this@FloatingPointerScope.fontScale
        override val size get() = this@FloatingPointerScope.size
        override val layoutNode get() = this@FloatingPointerScope.layoutNode
        override val viewConfiguration get() = this@FloatingPointerScope.viewConfiguration
        override suspend fun awaitPointerEvent(pass: PointerEventPass): PointerEvent {
            if (pass.ordinal <= previousPass.ordinal) {
                if (!events.hasNext()) {
                    if (waitForNextEvent) return suspendCoroutine { pendingEvent = it }
                    throw PointerStreamFinished()
                }
                currentEvent = events.next()
            }
            previousPass = pass
            return currentEvent
        }
    }
}
