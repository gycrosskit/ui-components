package com.dgtang.live.core.ui

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** App 普通按钮默认的重复点击拦截间隔。 */
const val APP_CLICK_INTERVAL_MILLIS = 500L

/**
 * 单个点击控件的跨端时间门闩。
 *
 * 使用 Kotlin 单调时钟，不读取 Android `SystemClock`，系统时间校准不会影响点击间隔。
 */
class ClickDebounceGate(
    private val intervalMillis: Long,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private var lastClickedAt: TimeMark? = null

    /** 首次点击立即放行；后续每次点击都重新开始静默计时，连续点击期间不再放行。 */
    fun tryAcquire(): Boolean {
        val previous = lastClickedAt
        val accepted = previous == null || previous.elapsedNow() >= intervalMillis.milliseconds
        lastClickedAt = timeSource.markNow()
        return accepted
    }
}
