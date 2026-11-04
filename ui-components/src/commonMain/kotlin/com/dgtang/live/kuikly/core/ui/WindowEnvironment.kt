package com.dgtang.live.kuikly.core.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** 宿主只订阅一次键盘高度，页面和 Dialog 消费同一快照。 */
val LocalKeyboardHeight = staticCompositionLocalOf { 0f }

/** 原生窗口策略由宿主提供，返回值负责恢复本次弹层的系统栏。 */
val LocalAppDialogSystemBars = staticCompositionLocalOf<(Boolean, Boolean) -> (() -> Unit)> {
    { _, _ -> {} }
}
