package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.dgtang.live.kuikly.core.ui.component.AppLazyColumn as LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier

/** 空/非空共用真实列表位置，使空态也能由同一个刷新容器判断顶部。 */
@Composable
fun AppRefreshableEmptyContent(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    LazyColumn(state = scrollState, modifier = modifier, isRefreshing = isRefreshing, onRefresh = onRefresh) {
        item {
            Box(
                Modifier.fillParentMaxWidth().fillParentMaxHeight(),
                contentAlignment = contentAlignment,
                content = content,
            )
        }
    }
}
