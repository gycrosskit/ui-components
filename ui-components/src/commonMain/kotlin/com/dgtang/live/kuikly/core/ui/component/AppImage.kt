package com.dgtang.live.kuikly.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.io.encoding.Base64
import com.tencent.kuikly.compose.coil3.AsyncImagePainter
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.graphics.BlendMode
import com.tencent.kuikly.compose.ui.graphics.BlendModeColorFilter
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.core.base.attr.ColorMatrix
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.core.base.attr.ImageUri

/** Kuikly 的 common assets 由各端宿主打包，并转换为 Painter。 */
@OptIn(InternalResourceApi::class)
@Composable
fun assetPainter(name: String): Painter =
    painterResource(DrawableResource(ImageUri.commonAssets(name).toUrl("")))

@Composable
fun AppAssetImage(
    name: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
) {
    AppImage(assetPainter(name), contentDescription, modifier, contentScale, colorFilter)
}

/** 图片和图标共用绘制入口，调用方通过 ColorFilter.tint 着色。 */
@Composable
fun AppImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    alignment: Alignment = Alignment.Center,
) {
    val isOhos = LocalConfiguration.current.pageData.isOhOs
    val filter = remember(colorFilter, isOhos) { nativeImageColorFilter(colorFilter, isOhos) }
    Image(painter, contentDescription, modifier, contentScale = contentScale, colorFilter = filter, alignment = alignment)
}

/**
 * OHOS 2.28 的 tintColor 与 colorFilter 共用原生属性；Compose 先着色再清空 filter 会擦除着色。
 * 用同一矩阵完成 SrcIn，保留源图透明度；Android/iOS 保留原生 tint 路径。
 */
internal fun nativeImageColorFilter(filter: ColorFilter?, isOhos: Boolean): ColorFilter? {
    if (!isOhos || filter !is BlendModeColorFilter || filter.blendMode != BlendMode.SrcIn) return filter
    val color = filter.color
    return ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0f, 0f, 0f, 0f, color.red,
        0f, 0f, 0f, 0f, color.green,
        0f, 0f, 0f, 0f, color.blue,
        0f, 0f, 0f, color.alpha, 0f,
    )))
}

/** Kuikly 使用原生图片加载器，绘制入口不创建第二份应用级缓存。 */
@Composable
fun AppImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
) {
    val painter = rememberAppImagePainter(model, placeholder)
    Image(painter, contentDescription, modifier, contentScale = contentScale)
}

/** 原始字节与宿主给出的有界缩略图共用原生 ImageView，业务不必为预览读取原图。 */
@Composable
fun AppImage(
    model: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    previewDataUri: String? = null,
) {
    val uri = remember(model, previewDataUri) { previewDataUri ?: ("data:image/png;base64," + Base64.encode(model)) }
    AppImage(uri, contentDescription, modifier, contentScale, placeholder)
}

/** 欢迎图需要预取及成功状态；复用同一加载入口，计时与跳过仍归 Splash。 */
@Composable
fun rememberAppImagePainter(model: String?, placeholder: Painter? = null): AsyncImagePainter =
    rememberAsyncImagePainter(
        model = model?.takeIf(String::isNotBlank),
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
    )
