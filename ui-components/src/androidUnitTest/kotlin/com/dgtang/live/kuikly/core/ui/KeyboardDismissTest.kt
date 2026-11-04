package com.dgtang.live.kuikly.core.ui

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import com.tencent.kuikly.compose.ui.focus.FocusDirection
import com.tencent.kuikly.compose.ui.focus.FocusManager
import com.tencent.kuikly.compose.ui.platform.LocalFocusManager
import com.tencent.kuikly.compose.ui.platform.LocalSoftwareKeyboardController
import com.tencent.kuikly.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class KeyboardDismissTest {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun dialogWaitsForParentKeyboardButKeepsItsOwnInput() = runTest {
        var dismissals = 0
        val focus = object : FocusManager {
            override fun clearFocus(force: Boolean) { dismissals++ }
            override fun moveFocus(focusDirection: FocusDirection) = false
        }
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(EmptyApplier(), recomposer)
        var ready = false
        fun show(visible: Boolean, height: Float, wait: Boolean = true) = composition.setContent {
            CompositionLocalProvider(LocalFocusManager provides focus) {
                if (visible) ready = DismissKeyboardOnEnter(waitForKeyboard = wait, keyboardHeight = height)
            }
        }
        try {
            show(true, 300f)
            runCurrent()
            assertEquals(false, ready, "iOS 旧键盘仍占用窗口时不能创建弹窗")
            show(true, 0f)
            runCurrent()
            show(true, 0f)
            assertTrue(ready)
            show(true, 260f)
            assertTrue(ready, "弹窗自己的输入不能重新关闭弹窗")
            assertEquals(1, dismissals)
            show(false, 260f)
            show(true, 260f)
            assertEquals(false, ready, "重新进入需要重新等待当前键盘")
            show(false, 260f)
            runCurrent()
            show(true, 0f)
            assertTrue(ready, "无键盘立即展示，不等待不存在的系统通知")
            show(false, 0f)
            show(true, 300f, wait = false)
            assertTrue(ready, "Android、鸿蒙保持即时展示")
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

    @Test
    fun enteringOverlayDismissesParentOnceAndKeepsOverlayInputOnRecomposition() = runTest {
        val calls = mutableListOf<String>()
        val focus = object : FocusManager {
            override fun clearFocus(force: Boolean) {
                assertTrue(force)
                calls += "parent-clear"
            }
            override fun moveFocus(focusDirection: FocusDirection) = false
        }
        val keyboard = object : SoftwareKeyboardController {
            override fun hide() { calls += "parent-hide" }
            override fun show() = Unit
        }
        val overlayFocus = object : FocusManager {
            override fun clearFocus(force: Boolean) { error("不能清理覆盖层自己的输入") }
            override fun moveFocus(focusDirection: FocusDirection) = false
        }
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(EmptyApplier(), recomposer)
        fun show(visible: Boolean, content: String) = composition.setContent {
            CompositionLocalProvider(
                LocalFocusManager provides focus,
                LocalSoftwareKeyboardController provides keyboard,
            ) {
                if (visible) {
                    DismissKeyboardOnEnter()
                    CompositionLocalProvider(LocalFocusManager provides overlayFocus) {
                        // 输入内容变化会重组，但不能再次结束输入。
                        androidx.compose.runtime.SideEffect { assertTrue(content.isNotEmpty()) }
                    }
                }
            }
        }
        try {
            show(false, "")
            assertTrue(calls.isEmpty())
            show(true, "输入")
            assertEquals(listOf("parent-hide", "parent-clear"), calls)
            show(true, "输入更多内容")
            assertEquals(2, calls.size, "覆盖层重组不能反复收键盘")
            show(false, "")
            assertEquals(2, calls.size, "关闭不能清理新页面输入")
            show(true, "重新打开")
            assertEquals(List(2) { listOf("parent-hide", "parent-clear") }.flatten(), calls)
            composition.dispose()
            assertEquals(4, calls.size)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

    @Test
    fun leavingEntryDismissesInputButRecompositionDoesNot() = runTest {
        val calls = mutableListOf<String>()
        val focus = object : FocusManager {
            override fun clearFocus(force: Boolean) {
                assertTrue(force)
                calls += "clear"
            }
            override fun moveFocus(focusDirection: FocusDirection) = false
        }
        val keyboard = object : SoftwareKeyboardController {
            override fun hide() { calls += "hide" }
            override fun show() = Unit
        }
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(EmptyApplier(), recomposer)
        fun show(entry: Any) = composition.setContent {
            CompositionLocalProvider(
                LocalFocusManager provides focus,
                LocalSoftwareKeyboardController provides keyboard,
            ) { DismissKeyboardOnLeave(entry) }
        }
        try {
            val first = Any()
            show(first)
            show(first)
            assertTrue(calls.isEmpty(), "同一条目重组不能打断输入")
            show(Any())
            assertEquals(listOf("hide", "clear"), calls)
            show(first)
            assertEquals(listOf("hide", "clear", "hide", "clear"), calls)
            composition.dispose()
            assertEquals(List(3) { listOf("hide", "clear") }.flatten(), calls)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

}
