package com.dgtang.live.kuikly.core.ui

import com.dgtang.live.core.ui.APP_CLICK_INTERVAL_MILLIS
import com.dgtang.live.core.ui.ClickDebounceGate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.composed
import com.tencent.kuikly.compose.ui.semantics.Role

/** 连续点击以当前控件为单位拦截；高频操作继续使用原始回调。 */
@Composable
fun rememberAppClick(
    intervalMillis: Long = APP_CLICK_INTERVAL_MILLIS,
    onClick: () -> Unit,
): () -> Unit {
    require(intervalMillis >= 0L) { "Click interval must not be negative" }
    val latestOnClick by rememberUpdatedState(onClick)
    val gate = remember(intervalMillis) { ClickDebounceGate(intervalMillis) }
    return remember(gate) {
        {
            if (gate.tryAcquire()) latestOnClick()
        }
    }
}

/** Checkbox 等离散选择使用防重规则；文本输入不使用。 */
@Composable
fun <T> rememberAppChange(
    intervalMillis: Long = APP_CLICK_INTERVAL_MILLIS,
    onChange: (T) -> Unit,
): (T) -> Unit {
    require(intervalMillis >= 0L) { "Click interval must not be negative" }
    val latestOnChange by rememberUpdatedState(onChange)
    val gate = remember(intervalMillis) { ClickDebounceGate(intervalMillis) }
    return remember(gate) {
        { value -> if (gate.tryAcquire()) latestOnChange(value) }
    }
}

/** 统一应用点击修饰符，接入防重规则。 */
fun Modifier.appClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    intervalMillis: Long = APP_CLICK_INTERVAL_MILLIS,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = rememberAppClick(intervalMillis, onClick),
    )
}
