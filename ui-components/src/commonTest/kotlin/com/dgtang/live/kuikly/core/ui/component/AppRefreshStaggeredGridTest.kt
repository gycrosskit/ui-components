package com.dgtang.live.kuikly.core.ui.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppRefreshStaggeredGridTest {
    @Test
    fun releaseRequiresThresholdAndReverseCancelsIt() {
        val session = StaggeredRefreshSession(80f)
        val owner = Any()
        session.attach(owner)
        session.drag(owner, -100f, refreshing = false)
        assertTrue(session.armed)
        session.drag(owner, -30f, refreshing = false)
        assertFalse(session.armed)
        assertFalse(session.release(owner, refreshing = false))
        assertEquals(0f, session.progress)
    }

    @Test
    fun pendingRefreshDoesNotRepeatAndCanRefreshAgainAfterCompletion() {
        val session = StaggeredRefreshSession(80f)
        val owner = Any()
        session.attach(owner)
        session.drag(owner, -100f, refreshing = false)
        assertTrue(session.release(owner, refreshing = false))
        assertEquals(1f, session.visibleProgress(refreshing = false))
        session.drag(owner, -100f, refreshing = false)
        assertFalse(session.release(owner, refreshing = false))
        session.finish()
        session.drag(owner, -100f, refreshing = false)
        assertTrue(session.release(owner, refreshing = false))
    }

    @Test
    fun leavingOrReplacingGridRejectsLateCallbacks() {
        val session = StaggeredRefreshSession(80f)
        val old = Any()
        val current = Any()
        session.attach(old)
        session.drag(old, -100f, refreshing = false)
        session.detach(old)
        assertFalse(session.release(old, refreshing = false))
        session.attach(current)
        session.drag(current, -100f, refreshing = false)
        session.detach(old)
        assertFalse(session.release(old, refreshing = false), "旧视图回调不能占用新视图的刷新")
        assertTrue(session.release(current, refreshing = false))
    }

    @Test
    fun externalRefreshShowsIndicatorAndClearsPreviousPull() {
        val session = StaggeredRefreshSession(80f)
        val owner = Any()
        session.attach(owner)
        assertEquals(1f, session.visibleProgress(refreshing = true))
        session.drag(owner, -100f, refreshing = false)
        session.refreshStarted()
        assertTrue(session.pending, "外部刷新结束后必须走统一的 inset 收口")
        assertFalse(session.release(owner, refreshing = false))
        session.finish()
        assertFalse(session.pending)
        session.drag(owner, -100f, refreshing = true)
        assertFalse(session.release(owner, refreshing = false))
    }
}
