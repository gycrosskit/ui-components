package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyListScope
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.material3.pullToRefreshItem
import com.tencent.kuikly.compose.material3.rememberPullToRefreshState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp

/** 页面、表单、空态和弹窗共用，继续沿用调用方的滚动状态与布局参数。 */
@Composable
fun AppLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val refreshState = if (onRefresh != null) rememberPullToRefreshState(isRefreshing) else null
    LazyColumn(
        modifier = modifier, state = state, contentPadding = contentPadding,
        verticalArrangement = verticalArrangement, horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled,
    ) {
        if (refreshState != null && onRefresh != null) {
            pullToRefreshItem(refreshState, onRefresh, state) { progress, refreshing, _ ->
                AppRefreshIndicator(refreshing, progress)
            }
        }
        content()
    }
}
