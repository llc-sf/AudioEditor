package dev.android.player.framework.utils

import android.graphics.Outline
import android.graphics.Rect
import android.graphics.Typeface
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

/**
 * View 剪裁设置。
 * @param radius 圆角半径
 * @param tl 左上角圆角半径
 * @param tr 右上角圆角半径
 * @param bl 左下角圆角半径
 * @param br 右下角圆角半径
 * 不支持设置三个角的圆角半径，这种情况这个方法不支持
 *
 * 1.支持同时设置四个角的圆角半径
 * 2.支持设置单个圆角半径
 * 3.支持设置 单独上 下 左 右 圆角半径
 */
private class RoundRadiusOutlineProvider(
        private val radius: Float? = null,
        private val tl: Float? = null,//左上
        private val tr: Float? = null,//右上
        private val bl: Float? = null,//左下
        private val br: Float? = null//右下
) : ViewOutlineProvider() {

    private val isAll = tl != null && tr != null && bl != null && br != null//是否全部圆角
    private val isTop = tl != null && tr != null//上方是否有圆角
    private val isBottom = bl != null && br != null//下方是否有圆角
    private val isLeft = tl != null && bl != null//左方是否有圆角
    private val isRight = tr != null && br != null//右方是否有圆角
    override fun getOutline(view: View, outline: Outline) {
        if (radius != null || isAll) {
            val r = radius ?: tl ?: tr ?: bl ?: br ?: 0f
            outline.setRoundRect(0, 0, view.width, view.height, r)
            return
        } else {
            val r = (tl ?: tr ?: bl ?: br ?: 0).toInt()
            when {
                isTop -> outline.setRoundRect(0, 0, view.width, view.height + r, r.toFloat())
                isBottom -> outline.setRoundRect(0, -r, view.width, view.height, r.toFloat())
                isLeft -> outline.setRoundRect(0, 0, view.width + r, view.height, r.toFloat())
                isRight -> outline.setRoundRect(-r, 0, view.width, view.height, r.toFloat())
                tl != null -> outline.setRoundRect(0, 0, view.width + r, view.height + r, r.toFloat())
                tr != null -> outline.setRoundRect(-r, 0, view.width, view.height + r, r.toFloat())
                bl != null -> outline.setRoundRect(0, -r, view.width + r, view.height, r.toFloat())
                br != null -> outline.setRoundRect(-r, -r, view.width, view.height, r.toFloat())
            }
        }
    }
}

/**
 * 将View设置成圆角矩形
 */
fun View.CornerOutline(tl: Float? = null, tr: Float? = null, bl: Float? = null, br: Float? = null) {
    outlineProvider = RoundRadiusOutlineProvider(tl = tl, tr = tr, bl = bl, br = br)
    clipToOutline = true
}

/**
 * 将View设置成圆角矩形
 */
fun View.CornerOutline(radius: Float) {
    val provider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
    outlineProvider = provider
    clipToOutline = true
}

fun View.CircleOutline() {
    val provider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setOval(0, 0, view.width, view.height)
        }
    }
    outlineProvider = provider
    clipToOutline = true
}

/**
 * 设置View点击默认点击动画
 */
inline fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

/**
 * 设置View 默认点击动画，可超出边界
 */
inline fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}

/**
 * View 扩大点区域
 * @param offset 偏移量
 */
inline fun View.outTouchDelegate(offset: Int) {
    outTouchDelegate(offset, offset, offset, offset)
}

/**
 * View 扩大点区域
 * @param horizontalOffset 左右偏移量
 * @param verticalOffset 上下偏移量
 */
inline fun View.outTouchDelegate(horizontalOffset: Int, verticalOffset: Int) {
    outTouchDelegate(horizontalOffset, verticalOffset, horizontalOffset, verticalOffset)
}

/**
 * View 扩大点区域
 * @param leftOffset 左偏移量
 * @param topOffset 上偏移量
 * @param rightOffset 右偏移量
 * @param bottomOffset 下偏移量
 */
inline fun View.outTouchDelegate(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    post {
        touchDelegate = TouchDelegate(Rect(
                left,
                top,
                right,
                bottom
        ), this)
    }
}

/**
 * View 测量结束后回调
 */
inline fun View.doOnGlobalLayout(crossinline action: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action()
        }
    })
}


/**
 * 设置TextView 高亮文字设置
 * @param pattern 要高亮的文字
 * @param color 高亮颜色
 * @param isBold 是否设置加粗
 * @param ignoreCase 是否忽略大小写匹配
 */
inline fun TextView.bindHighLightColor(pattern: String, @ColorInt color: Int, isBold: Boolean = false, ignoreCase: Boolean = true) {


    val s = SpannableString(text)

    //删除*号的匹配。
    val p = StringsUtils.getPattern(pattern, ignoreCase)
    val m = p.matcher(s)
    while (m.find()) {
        val start = m.start()
        val end = m.end()
        s.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    if (isBold) {
        s.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    setText(s)
}


@JvmOverloads
inline fun TextView.bindHighLightColorRes(pattern: String, @ColorRes res: Int, isBold: Boolean = false, ignoreCase: Boolean = true) {
    bindHighLightColor(pattern, ContextCompat.getColor(context, res), isBold, ignoreCase)
}

/**
 * 沉浸式状态栏体验
 */
inline fun View.ImmerseDesign() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        //获取状态栏高度
        val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        //设置padding
        v.updateLayoutParams<MarginLayoutParams> {
            topMargin = status.top
        }
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * 沉浸式状态栏体验
 */
inline fun View.ImmerseDesignPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        //获取状态栏高度
        val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        //设置padding
        v.setPadding(0, status.top, 0, 0)
        WindowInsetsCompat.CONSUMED
    }
}

const val CLICK_INTERVAL = 800 // 800毫秒内只允许点击一次

/**
 * 防止view连续点击，默认点击间隔控制在800毫秒
 */
fun View.setSingleClickListener(onClick: (View) -> Unit) {
    var lastClickTime: Long = 0

    setOnClickListener {
        System.currentTimeMillis()
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= CLICK_INTERVAL) {
            lastClickTime = currentTime
            onClick(it)
        }
    }
}