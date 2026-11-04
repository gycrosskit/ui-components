package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import com.tencent.kuikly.compose.extension.nativeRef
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clipToBounds
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.views.IScrollerViewEventObserver
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.ScrollerView

/** 瀑布流缺少框架刷新入口；由原生滚动事件驱动，框架支持后可替换内部实现。 */
@Composable
fun AppRefreshStaggeredGrid(
    columns: StaggeredGridCells,
    state: LazyStaggeredGridState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    content: LazyStaggeredGridScope.() -> Unit,
) {
    val threshold = 80f
    val session = remember(state) { StaggeredRefreshSession(threshold) }
    val refreshing by rememberUpdatedState(isRefreshing)
    val refresh by rememberUpdatedState(onRefresh)
    var scroller by remember(state) { mutableStateOf<ScrollerView<*, *>?>(null) }

    DisposableEffect(scroller, session) {
        val view = scroller
        val observer = object : IScrollerViewEventObserver {
            override fun subViewsDidLayout() = Unit
            override fun onContentOffsetDidChanged(contentOffsetX: Float, contentOffsetY: Float, params: ScrollParams) {
                if (!params.isDragging || view == null) return
                val wasArmed = session.armed
                session.drag(view, contentOffsetY, refreshing)
                if (session.armed != wasArmed) {
                    view?.setContentInsetWhenEndDrag(top = if (session.armed) threshold else 0f)
                }
            }
            override fun scrollerDragEnd(params: ScrollParams) {
                // shortcut: 当前 SDK 未公开系统取消标志；SDK 提供取消语义后需区分取消与松手。
                if (view != null && session.release(view, refreshing)) refresh()
            }
        }
        if (view != null) {
            session.attach(view)
            view.setHasPullToRefresh(true)
            view.addScrollerViewEventObserver(observer)
        }
        onDispose {
            if (view != null) session.detach(view)
            view?.removeScrollerViewEventObserver(observer)
            view?.setContentInsetWhenEndDrag(top = 0f)
            view?.setContentInset(top = 0f, animated = false)
            view?.setHasPullToRefresh(false)
        }
    }

    LaunchedEffect(scroller, isRefreshing, session.pending) {
        val view = scroller
        if (isRefreshing) {
            session.refreshStarted()
            view?.setContentInsetWhenEndDrag(top = 0f)
            view?.setContentInset(top = threshold, animated = true)
        } else if (session.pending) {
            // 同一帧内完成的请求可能不会产生可观察的 true 状态。
            withFrameNanos { }
            if (!refreshing) {
                session.finish()
                view?.setContentInsetWhenEndDrag(top = 0f)
                view?.setContentInset(top = 0f, animated = true)
            }
        }
    }

    Box(modifier.clipToBounds()) {
        LazyVerticalStaggeredGrid(
            columns = columns, state = state,
            modifier = Modifier.fillMaxSize().nativeRef { scroller = this as? ScrollerView<*, *> },
            contentPadding = contentPadding, verticalItemSpacing = verticalItemSpacing,
            horizontalArrangement = horizontalArrangement, content = content,
        )
        if (session.progress > 0f || isRefreshing || session.pending) {
            val visibleProgress = session.visibleProgress(isRefreshing)
            Box(
                Modifier.align(Alignment.TopCenter)
                    .offset(y = (threshold * visibleProgress - 40f).dp)
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) { AppRefreshIndicator(isRefreshing || session.pending, session.progress) }
        }
    }
}

/** 只持有当前 Grid 的刷新事务，已解绑的原生回调不能触发业务。 */
internal class StaggeredRefreshSession(private val threshold: Float) {
    var progress by mutableFloatStateOf(0f)
        private set
    var pending by mutableStateOf(false)
        private set
    var armed: Boolean = false
        private set
    private var owner: Any? = null

    fun attach(owner: Any) { this.owner = owner }
    fun detach(owner: Any) {
        if (this.owner !== owner) return
        this.owner = null
        armed = false
        pending = false
        progress = 0f
    }
    fun drag(owner: Any, offset: Float, refreshing: Boolean) {
        if (this.owner !== owner) return
        if (refreshing) { armed = false; return }
        if (pending) return
        progress = (-offset / threshold).coerceIn(0f, 1f)
        armed = -offset >= threshold
    }
    fun refreshStarted() {
        armed = false
        pending = true
    }
    fun visibleProgress(refreshing: Boolean): Float = if (refreshing || pending) 1f else progress
    fun release(owner: Any, refreshing: Boolean): Boolean {
        if (this.owner !== owner) return false
        val submit = armed && !pending && !refreshing
        armed = false
        if (submit) pending = true else progress = 0f
        return submit
    }
    fun finish() { pending = false; progress = 0f }
}
