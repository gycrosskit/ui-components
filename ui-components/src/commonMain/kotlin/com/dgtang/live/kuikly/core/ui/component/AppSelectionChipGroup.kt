package com.dgtang.live.kuikly.core.ui.component

import com.dgtang.live.kuikly.core.ui.KuiklyTheme
import androidx.compose.runtime.Composable
import com.dgtang.live.kuikly.core.ui.appClickable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.ExperimentalLayoutApi
import com.tencent.kuikly.compose.foundation.layout.FlowRow
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.heightIn
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.widthIn
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.dgtang.live.kuikly.core.ui.component.AppText as Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.semantics.Role
import com.tencent.kuikly.compose.ui.semantics.selected
import com.tencent.kuikly.compose.ui.semantics.semantics
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

/** 可换行单选标签组；品牌色由公共主题提供。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> AppSelectionChipGroup(
    items: List<T>,
    selectedItem: T?,
    itemText: @Composable (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = KuiklyTheme.colors
    val itemAlpha = if (enabled) 1f else 0.45f
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            val selected = selectedItem == item
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .widthIn(min = 88.dp)
                    .heightIn(min = 48.dp)
                    .background(
                        if (selected) colors.primary.copy(alpha = if (enabled) 0.12f else 0.06f)
                        else colors.surface,
                        shape,
                    )
                    .border(
                        width = 1.dp,
                        color = (if (selected) colors.primary else colors.outline).copy(alpha = itemAlpha),
                        shape = shape,
                    )
                    .semantics { this.selected = selected }
                    .appClickable(enabled = enabled, role = Role.RadioButton) { onItemSelected(item) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = itemText(item),
                    color = (if (selected) colors.primary else colors.onSurface).copy(alpha = itemAlpha),
                    style = KuiklyTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
