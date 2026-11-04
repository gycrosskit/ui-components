package com.dgtang.live.kuikly.core.ui.component

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AppSyncedHorizontalPagerTest {
    @Test
    fun initialSelectionDoesNotScrollAndGesturesOnlyNotifyWhenIdle() = runTest {
        val pager = PagerSelectionFixture()
        val observer = launch { pager.sync(0) }
        runCurrent()
        assertEquals(emptyList(), pager.commands)
        pager.states.value = true to 0
        runCurrent()
        assertEquals(emptyList(), pager.selections)
        pager.states.value = false to 1
        runCurrent()
        pager.states.value = true to 1
        runCurrent()
        pager.states.value = false to 1
        runCurrent()
        assertEquals(listOf(1), pager.selections, "同页拖动不能重复触发业务")
        observer.cancelAndJoin()
    }

    @Test
    fun ordinarySelectionKeepsAnimationAndWaitsForNativeIdle() = runTest {
        val pager = PagerSelectionFixture()
        val observer = launch { pager.sync(1) }
        runCurrent()
        assertEquals(listOf("stop", "animate:1"), pager.commands)
        assertEquals(emptyList(), pager.selections, "animate 返回不代表原生已停稳")
        pager.states.value = false to 1
        runCurrent()
        assertEquals(emptyList(), pager.selections, "程序目标不重复通知业务")
        observer.cancelAndJoin()
    }

    @Test
    fun latestSelectionStopsOldAnimationBeforeStartingItsOwnAnimation() = runTest {
        val pager = PagerSelectionFixture()
        val first = launch { pager.sync(1) }
        runCurrent()
        first.cancelAndJoin()
        val latest = launch { pager.sync(2) }
        runCurrent()
        assertEquals(listOf("stop", "animate:1", "stop", "animate:2"), pager.commands)
        pager.states.value = true to 1
        runCurrent()
        assertEquals(emptyList(), pager.selections, "最新动画未停稳时不能回写旧页")
        pager.states.value = false to 2
        runCurrent()
        assertEquals(emptyList(), pager.selections)
        pager.states.value = true to 2
        runCurrent()
        pager.states.value = false to 0
        runCurrent()
        assertEquals(listOf(0), pager.selections, "重选完成后仍可手势切页")
        latest.cancelAndJoin()
    }

    @Test
    fun returningToOriginStillStopsOldAnimationAndSubmitsTheLatestAnimation() = runTest {
        val pager = PagerSelectionFixture()
        val first = launch { pager.sync(1) }
        runCurrent()
        first.cancelAndJoin()
        val origin = launch { pager.sync(0) }
        runCurrent()
        assertEquals(listOf("stop", "animate:1", "stop", "animate:0"), pager.commands)
        assertEquals(listOf(0.000001f, 0.000001f), pager.animationOffsets,
            "最新同页动画仍须发出有效命令，不能被 SDK 的 0 偏移早退吞掉")
        pager.states.value = true to 1
        runCurrent()
        assertEquals(emptyList(), pager.selections)
        pager.states.value = false to 0
        runCurrent()
        assertEquals(emptyList(), pager.selections)
        origin.cancelAndJoin()
    }

    @Test
    fun thirdSelectionCancelsAnUnfinishedStopWithoutStartingTheSupersededAnimation() = runTest {
        val pager = PagerSelectionFixture()
        val first = launch { pager.sync(1) }
        runCurrent()
        first.cancelAndJoin()
        val unfinishedStop = CompletableDeferred<Unit>()
        pager.stopCompletion = unfinishedStop
        val second = launch { pager.sync(2) }
        runCurrent()
        second.cancelAndJoin()
        pager.stopCompletion = null
        val latest = launch { pager.sync(0) }
        runCurrent()
        assertEquals(listOf("stop", "animate:1", "stop", "stop", "animate:0"), pager.commands)
        unfinishedStop.complete(Unit)
        runCurrent()
        assertEquals(emptyList(), pager.selections, "取消的 stop 不能恢复旧动画或旧订阅")
        pager.states.value = false to 0
        runCurrent()
        assertEquals(emptyList(), pager.selections)
        latest.cancelAndJoin()
    }

    @Test
    fun gesturesCanTakeOverBeforeProgrammaticTargetIsReached() = runTest {
        val pager = PagerSelectionFixture()
        val animation = launch { pager.sync(1) }
        runCurrent()
        pager.states.value = false to 2
        runCurrent()
        assertEquals(listOf(2), pager.selections, "手势打断动画后不要求先到原目标")
        animation.cancelAndJoin()

        pager.states.value = true to 2
        val replacement = launch { pager.sync(0) }
        runCurrent()
        pager.states.value = false to 1
        runCurrent()
        assertEquals(listOf(2, 1), pager.selections, "覆盖旧动画后用户手势也可以接管")
        replacement.cancelAndJoin()
    }

    @Test
    fun leavingCancelsBothAnimationObservationAndAnUnfinishedStop() = runTest {
        val pager = PagerSelectionFixture()
        val animation = launch { pager.sync(1) }
        runCurrent()
        animation.cancelAndJoin()
        pager.states.value = false to 2
        runCurrent()
        assertEquals(emptyList(), pager.selections)

        val unfinishedStop = CompletableDeferred<Unit>()
        pager.stopCompletion = unfinishedStop
        val replacement = launch { pager.sync(0) }
        runCurrent()
        replacement.cancelAndJoin()
        unfinishedStop.complete(Unit)
        runCurrent()
        assertEquals(emptyList(), pager.selections, "离页后迟到的停页不能回写业务")
        assertEquals(listOf("stop", "animate:1", "stop"), pager.commands)
    }
}

/** 仅向宿主同步函数交付滚动状态与命令完成信号，不模拟或断言 SDK 内部实现。 */
private class PagerSelectionFixture {
    val states = MutableStateFlow(false to 0)
    val commands = mutableListOf<String>()
    val animationOffsets = mutableListOf<Float>()
    val selections = mutableListOf<Int>()
    var stopCompletion: CompletableDeferred<Unit>? = null

    suspend fun sync(selected: Int) = syncPagerSelection(
        selectedIndex = selected,
        scrollStates = states,
        stopScroll = {
            commands += "stop"
            stopCompletion?.await()
            states.value = false to states.value.second
        },
        animateToPage = { page, offset ->
            commands += "animate:$page"
            animationOffsets += offset
            states.value = true to states.value.second
        },
        onSettledPage = { page -> if (page != selected) selections += page },
    )
}
