package com.dgtang.live.kuikly.core.ui.component

import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import androidx.compose.runtime.Composable
import com.dgtang.live.kuikly.core.ui.AppButton
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.dgtang.live.kuikly.core.ui.component.AppLazyColumn as LazyColumn
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.dgtang.live.kuikly.core.ui.component.AppText as Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp

/** 居中展示内容状态；加载指示器使用 Kuikly 已有控件。 */
@Composable
fun AppContentLoading(
    modifier: Modifier = Modifier.fillMaxSize(),
    message: String? = null,
    contentPadding: PaddingValues = PaddingValues(32.dp),
) {
    AppContentStateLayout(modifier, contentPadding) {
        AppLoadingIndicator()
        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                color = KuiklyTheme.colors.onSurfaceVariant,
                style = KuiklyTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun AppContentMessage(
    message: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(32.dp),
) {
    val colors = KuiklyTheme.colors
    val messageContent: @Composable () -> Unit = {
        Text(
            text = message,
            color = colors.onSurfaceVariant,
            style = KuiklyTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (!actionText.isNullOrBlank() && onAction != null) {
            AppButton(
                onClick = onAction,
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text(actionText, textAlign = TextAlign.Center) }
        }
    }
    BoxWithConstraints(modifier, propagateMinConstraints = true) {
        if (constraints.hasBoundedHeight) {
            // 提示与按钮按完整高度测量，空间不足时一起滚动，避免提示先耗尽按钮高度。
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                item {
                    AppContentStateLayout(Modifier, PaddingValues(0.dp), content = messageContent)
                }
            }
        } else {
            // 列表 item 的高度无界，由外层列表承接滚动，不能再嵌套同向滚动容器。
            AppContentStateLayout(Modifier, contentPadding, content = messageContent)
        }
    }
}

@Composable
private fun AppContentStateLayout(
    modifier: Modifier,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        content()
    }
}

/** 使用对应平台样式；iOS 与下拉刷新复用同一个 Cupertino 指示器。 */
@Composable
fun AppLoadingIndicator(modifier: Modifier = Modifier) {
    if (LocalConfiguration.current.isIOS) {
        CupertinoRefreshIndicator(animating = true, revealFraction = 1f, modifier = modifier)
    } else {
        CircularProgressIndicator(
            modifier = modifier.size(24.dp),
            color = KuiklyTheme.colors.primary,
            strokeWidth = 2.5.dp,
        )
    }
}
