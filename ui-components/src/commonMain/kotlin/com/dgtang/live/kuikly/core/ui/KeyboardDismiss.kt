package com.dgtang.live.kuikly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.gestures.detectTapGestures
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.platform.LocalFocusManager
import com.tencent.kuikly.compose.ui.platform.LocalSoftwareKeyboardController

/** 只在子控件未消费的空白点击后清焦点、收键盘。 */
@Composable
fun Modifier.dismissKeyboardOnBlankTap(): Modifier {
    val dismissKeyboard = rememberDismissKeyboard()
    return pointerInput(dismissKeyboard) { detectTapGestures { dismissKeyboard() } }
}

/** 覆盖层进入时结束父页面输入；重组和退出不能打断覆盖层自己的输入框。 */
@Composable
fun DismissKeyboardOnEnter(
    waitForKeyboard: Boolean = false,
    keyboardHeight: Float = 0f,
): Boolean {
    val dismissKeyboard = rememberDismissKeyboard()
    var ready by remember { mutableStateOf(!waitForKeyboard || keyboardHeight <= 0f) }
    DisposableEffect(Unit) {
        dismissKeyboard()
        onDispose { }
    }
    LaunchedEffect(keyboardHeight) {
        if (keyboardHeight <= 0f) ready = true
    }
    // 只等待进入前的键盘；放行后弹窗自己的输入不能再次隐藏整个弹窗。
    return ready
}

/** 路由条目离开时结束输入；Kuikly 保留旧页时需要主动结束原生输入会话。 */
@Composable
fun DismissKeyboardOnLeave(pageKey: Any?) {
    val dismissKeyboard = rememberDismissKeyboard()
    DisposableEffect(pageKey, dismissKeyboard) {
        onDispose { dismissKeyboard() }
    }
}

@Composable
fun rememberDismissKeyboard(): () -> Unit {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember(focusManager, keyboardController) {
        {
            // 先 hide 再清焦点，使合并后的输入命令以 stopInput 清掉旧 TextArea 引用。
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
        }
    }
}
