package com.dgtang.live.kuikly.core.ui.component

import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.gestures.stopScroll
import com.tencent.kuikly.compose.foundation.pager.HorizontalPager
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 与外部单向状态同步的跨端横向分页容器。
 *
 * 点击 Tab 时，调用方先更新 [selected]，分页器随后平滑滚动到目标页；用户直接左右滑动时，
 * 分页器只在页面停稳后调用 [onSelected]，避免拖动过程中重复触发搜索或列表刷新。
 * 页面顺序由 [pages] 唯一决定，适合状态 Tab、分类 Tab 等固定数量的页面。
 */
@Composable
fun <T> AppSyncedHorizontalPager(
    pages: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    pageContent: @Composable (T) -> Unit,
) {
    require(pages.isNotEmpty()) { "Pager pages must not be empty" }
    val selectedIndex = pages.indexOf(selected).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = pages::size,
    )
    val latestSelected by rememberUpdatedState(selected)
    val latestOnSelected by rememberUpdatedState(onSelected)

    LaunchedEffect(pagerState, pages, selectedIndex) {
        syncPagerSelection(
            selectedIndex = selectedIndex,
            scrollStates = snapshotFlow { pagerState.isScrollInProgress to pagerState.settledPage },
            stopScroll = { pagerState.stopScroll() },
            animateToPage = { page, offset -> pagerState.animateScrollToPage(page, pageOffsetFraction = offset) },
            onSettledPage = { pageIndex ->
                pages.getOrNull(pageIndex)
                    ?.takeIf { it != latestSelected }
                    ?.let(latestOnSelected)
            },
        )
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { pageIndex -> pages[pageIndex].hashCode() },
    ) { pageIndex ->
        pageContent(pages[pageIndex])
    }
}

internal suspend fun syncPagerSelection(
    selectedIndex: Int,
    scrollStates: Flow<Pair<Boolean, Int>>,
    stopScroll: suspend () -> Unit,
    animateToPage: suspend (Int, Float) -> Unit,
    onSettledPage: (Int) -> Unit,
) {
    val (scrolling, settledPage) = scrollStates.first()
    // Kuikly animate 只提交原生动画；先用公开 stopScroll 发出原生 abort，再动画到最新选择。
    if (scrolling || settledPage != selectedIndex) {
        stopScroll()
        // shortcut: Kuikly 2.29.1 同页 0 偏移会跳过动画；用正常非零的亚像素偏移重发，SDK 修复后移除。
        animateToPage(selectedIndex, 0.000001f)
    }

    scrollStates
        .filter { (inProgress, _) -> !inProgress }
        .map { (_, page) -> page }
        .distinctUntilChanged()
        .collect(onSettledPage)
}
