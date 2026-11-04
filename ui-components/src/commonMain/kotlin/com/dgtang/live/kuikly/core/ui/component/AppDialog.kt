package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentCompositionLocalContext
import com.dgtang.live.kuikly.core.ui.DismissKeyboardOnEnter
import com.dgtang.live.kuikly.core.ui.LocalKeyboardHeight
import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import com.dgtang.live.kuikly.core.ui.appClickable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.WindowInsets
import com.tencent.kuikly.compose.foundation.layout.windowInsetsPadding
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.ColumnScope
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.dgtang.live.kuikly.core.ui.component.AppLazyColumn as LazyColumn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.HorizontalDivider
import com.dgtang.live.kuikly.core.ui.component.AppText as Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose.ui.window.Dialog
import com.tencent.kuikly.compose.ui.window.DialogProperties

/** Kuikly 插槽创建独立 Composition；捕获完整 Local 上下文，同时保留动态组件色、业务色和文案。 */
@Composable
fun AppThemeDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    consumeSafeArea: Boolean = true,
    content: @Composable () -> Unit,
) {
    val compositionLocals = currentCompositionLocalContext
    val configuration = LocalConfiguration.current
    val keyboardHeight = LocalKeyboardHeight.current
    val safeArea = configuration.safeAreaInsets
    // Dialog 不自动消费窗口安全区，按父页面当前环境计算一次，避免双重 Insets。
    val insets = WindowInsets(
        left = (if (consumeSafeArea) safeArea.left else 0f).dp,
        top = (if (consumeSafeArea) maxOf(safeArea.top, configuration.statusBarHeight) else 0f).dp,
        right = (if (consumeSafeArea) safeArea.right else 0f).dp,
        bottom = maxOf(
            if (consumeSafeArea) safeArea.bottom else 0f,
            keyboardHeight,
        ).dp,
    )
    // 必须在父 Composition 清理输入；Dialog 的独立内容树持有另一组焦点。
    if (!DismissKeyboardOnEnter(configuration.isIOS, keyboardHeight)) return
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        CompositionLocalProvider(compositionLocals) {
            Box(Modifier.fillMaxSize().windowInsetsPadding(insets), contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}

/** 居中业务弹窗；Kuikly 尚无 verticalScroll，正文改用 LazyColumn 保持小屏可滚动。 */
@Composable
fun AppDialog(
    title: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    cancelable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppThemeDialog(
        onDismissRequest = { if (cancelable) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = cancelable,
            dismissOnClickOutside = false,
        ),
    ) {
        val colors = KuiklyTheme.colors
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth(0.84f).widthIn(max = 336.dp)
                    .heightIn(max = maxHeight * 0.9f)
                    .background(colors.surface, RoundedCornerShape(10.dp)),
            ) {
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    item {
                        Column(Modifier.padding(
                            start = 30.dp,
                            top = if (title == null) 24.dp else 28.dp,
                            end = 30.dp,
                            bottom = 24.dp,
                        )) {
                            if (title != null) {
                                Text(
                                    title,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = colors.dialogTitle,
                                    fontSize = 18.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(18.dp))
                            }
                            content()
                        }
                    }
                }
                if (confirmText != null || dismissText != null) {
            HorizontalDivider(color = colors.dialogDivider)
                    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (dismissText != null) {
                            AppDialogAction(dismissText, colors.dialogText, onDismiss, Modifier.weight(1f))
                        }
                        if (dismissText != null && confirmText != null) {
                            Box(Modifier.height(28.dp).width(1.dp).background(colors.dialogDivider))
                        }
                        if (confirmText != null) {
                            AppDialogAction(confirmText, colors.dialogPrimary, { onConfirm?.invoke() }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDialogAction(text: String, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier.heightIn(min = 52.dp).appClickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
    }
}
