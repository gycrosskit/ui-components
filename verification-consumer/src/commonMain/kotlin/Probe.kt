import androidx.compose.runtime.Composable
import com.dgtang.live.core.ui.ClickDebounceGate
import com.dgtang.live.kuikly.core.ui.*
import com.dgtang.live.kuikly.core.ui.component.*
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.AnnotatedString

// 此消费者只从 Maven 解析组件；主题、素材和输入状态仍由宿主提供。
@Composable
fun ComponentProbe(colors: KuiklyAppColors, text: String, onTextChanged: (String) -> Unit) {
    ProvideKuiklyTheme(colors) {
        AppText(text)
        AppRichText(AnnotatedString(text))
        AppBasicTextField(text, onTextChanged)
        AppImage("https://example.com/image.png", "图片")
        AppCheckbox(checked = true, onCheckedChange = {})
        AppSyncedHorizontalPager(listOf("a", "b"), "a", {}) { AppText(it) }
        AppThemeDialog(onDismissRequest = {}) { AppText(text, Modifier) }
    }
}
fun fontAndClickProbe() = appFontScale(2f) to ClickDebounceGate(500).tryAcquire()
