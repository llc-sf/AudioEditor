package dev.audio.timeruler.weight

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import dev.audio.timeruler.R
import org.w3c.dom.Text

class CustomActionTextView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    // 定义一个常量作为放大消息的标识
    companion object {
        private const val MESSAGE_LEFT = 1
        private const val MESSAGE_RIGHT = 2

        private const val GAP_TIME_ANIMATION = 5L
        private const val GAP_TIME = 500L
    }

    // 初始化内边距属性
    private val leftIconLeftPadding: Int
    private val leftIconRightPadding: Int
    private val rightIconLeftPadding: Int
    private val rightIconRightPadding: Int
    private val topPadding: Int
    private val bottomPadding: Int


    private val leftIcon: ImageView
    private val rightIcon: ImageView
    private val tv: TextView

    // 新增水波纹效果的成员变量
    private var rippleX: Float = -1f
    private var rippleY: Float = -1f
    private var rippleRadius: Float = 0f

    private val rippleStrokeWidth = 10f // 波纹圆环的宽度
    private val initialRippleRadiusOffset = 0f // 波纹开始扩散时的初始偏移量

    // 修改画笔设置为描边
    private val ripplePaint = Paint().apply {
        color = Color.LTGRAY // 波纹颜色
        style = Paint.Style.STROKE // 描边
        strokeWidth = rippleStrokeWidth
        alpha = 0 // 初始透明度
    }

    private var rippleMaxRadius = 30f // 水波纹最大半径
    private val rippleDuration = 600L // 水波纹动画时长
    private val rippleEffectHandler = Handler(Looper.getMainLooper())

    private var time_gap: Long


    init { // 从XML中加载布局
        LayoutInflater.from(context).inflate(R.layout.custom_action_text_view, this, true)

        // 初始化视图元素
        leftIcon = findViewById(R.id.left)
        rightIcon = findViewById(R.id.right)
        tv = findViewById(R.id.middle_tv)

        // 读取并应用自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomActionTextView, 0, 0)
        leftIcon.setImageDrawable(typedArray.getDrawable(R.styleable.CustomActionTextView_text_leftDrawable))
        rightIcon.setImageDrawable(typedArray.getDrawable(R.styleable.CustomActionTextView_text_rightDrawable))
        leftIconLeftPadding = typedArray.getDimensionPixelSize(R.styleable.CustomActionTextView_text_left_icon_left_padding, 0)
        leftIconRightPadding = typedArray.getDimensionPixelSize(R.styleable.CustomActionTextView_text_left_icon_right_padding, 0)
        rightIconLeftPadding = typedArray.getDimensionPixelSize(R.styleable.CustomActionTextView_text_right_icon_left_padding, 0)
        rightIconRightPadding = typedArray.getDimensionPixelSize(R.styleable.CustomActionTextView_text_right_icon_right_padding, 0)
        topPadding = typedArray.getDimensionPixelSize(R.styleable.CustomActionTextView_text_top_padding, 0)
        bottomPadding = typedArray.getDimensionPixelSize(R.styleable.CustomActionTextView_text_bottom_padding, 0)
        try {
            time_gap = typedArray.getString(R.styleable.CustomActionTextView_text_click_time_gap)
                ?.toLong() ?: GAP_TIME
        } catch (e: Exception) {
            e.printStackTrace()
            time_gap = GAP_TIME
        }
        typedArray.recycle()

        rippleMaxRadius = leftIcon.drawable.intrinsicWidth / 2 + 10f
        setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
        tv.setOnClickListener {
            middleAction?.invoke()
        }
    }

    private var lastTouchDown: Long = 0 // 记录按下的时间
    private var leftDown = false
    private var rightDown = false


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchDown = System.currentTimeMillis()
                val width = width
                val x = event.x
                Log.i("CustomActionView", "x: $x, width: $width , tv.width: ${tv.width}")
                if (x < (width / 2 + tv.width / 2) && x > (width / 2 - tv.width / 2)) {
                    Log.i("CustomActionView", "middleAction")
                    return true
                } else {
                    Log.i("CustomActionView", " un middleAction")
                }
                if (x < width / 2) {
                    if (!leftIcon.isEnabled) {
                        return true
                    } //                    setBackgroundResource(R.drawable.bg_left_click)
                    leftDown = true
                    zoomHandler.sendEmptyMessageDelayed(MESSAGE_LEFT, time_gap)


                    // 将波纹的中心点设置为 `rightIcon` 的中心
                    rippleX = leftIcon.x + leftIcon.width / 2
                    rippleY = leftIcon.y + leftIcon.height / 2 // 初始半径设置为rightIcon外部边界加上一个偏移量
                    rippleRadius = maxOf(leftIcon.width, leftIcon.height) / 2f + initialRippleRadiusOffset
                    ripplePaint.alpha = 255 // 初始透明度为完全不透明
                    rippleEffectHandler.removeCallbacks(rippleUpdateRunnable)
                    rippleEffectHandler.post(rippleUpdateRunnable)

                } else {
                    if (!rightIcon.isEnabled) {
                        return true
                    } //                    setBackgroundResource(R.drawable.bg_right_click)
                    rightDown = true
                    zoomHandler.sendEmptyMessageDelayed(MESSAGE_RIGHT, time_gap)


                    // 将波纹的中心点设置为 `rightIcon` 的中心
                    rippleX = rightIcon.x + rightIcon.width / 2
                    rippleY = rightIcon.y + rightIcon.height / 2 // 初始半径设置为rightIcon外部边界加上一个偏移量
                    rippleRadius = maxOf(rightIcon.width, rightIcon.height) / 2f + initialRippleRadiusOffset
                    ripplePaint.alpha = 255 // 初始透明度为完全不透明
                    rippleEffectHandler.removeCallbacks(rippleUpdateRunnable)
                    rippleEffectHandler.post(rippleUpdateRunnable)
                }

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                clearMsg()
                if (leftDown) {
                    if (!leftIcon.isEnabled) {
                        return true
                    }
                } else {
                    if (!rightIcon.isEnabled) {
                        return true
                    }
                }
                if (System.currentTimeMillis() - lastTouchDown < ViewConfiguration.getTapTimeout() * 2) {
                    if (leftDown) {
                        Log.i("CustomActionView", "leftAction")
                        leftAction?.invoke()
                    } else if (rightDown) {
                        Log.i("CustomActionView", "rightAction")
                        rightAction?.invoke()
                    }
                }
                rightDown = false
                leftDown = false
            }
        }
        return true
    }


    var leftAction: (() -> Unit)? = null
    var rightAction: (() -> Unit)? = null
    var middleAction: (() -> Unit)? = null
    fun setActionListener(
        leftAction: () -> Unit,
        middleAction: () -> Unit,
        rightAction: () -> Unit,
    ) {
        this.leftAction = leftAction
        this.rightAction = rightAction
        this.middleAction = middleAction
    }

    fun freshLeftIconEnable(enable: Boolean) {
        leftIcon.isEnabled = enable
        leftIcon.alpha = if (enable) 1.0f else 0.5f
    }

    fun freshRightIconEnable(enable: Boolean) {
        rightIcon.isEnabled = enable
        rightIcon.alpha = if (enable) 1.0f else 0.5f
    }


    private val zoomHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_LEFT -> {
                    leftAction?.invoke()
                    sendEmptyMessageDelayed(MESSAGE_LEFT, time_gap)
                }

                MESSAGE_RIGHT -> {
                    rightAction?.invoke()
                    sendEmptyMessageDelayed(MESSAGE_RIGHT, time_gap)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearMsg()
    }

    private fun clearMsg() {
        zoomHandler.removeMessages(MESSAGE_LEFT)
        zoomHandler.removeMessages(MESSAGE_RIGHT)
    }

    fun setText(text: String) {
        tv.text = text
    }


    private val rippleUpdateRunnable = object : Runnable {
        override fun run() {
            if (rippleRadius < rippleMaxRadius && rippleX >= 0 && rippleY >= 0) { // 更新水波纹半径
                rippleRadius += rippleMaxRadius / (rippleDuration / GAP_TIME_ANIMATION) // 更新透明度
                ripplePaint.alpha = ((1 - rippleRadius / rippleMaxRadius) * 255).toInt()
                invalidate()
                rippleEffectHandler.postDelayed(this, GAP_TIME_ANIMATION)
            }
        }
    }

    // 重写onDraw方法绘制波纹效果
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas) // 确定波纹圆环的绘制区域
        val rippleEffectBounds = RectF(rippleX - rippleRadius, rippleY - rippleRadius, rippleX + rippleRadius, rippleY + rippleRadius) // 只有当圆环半径大于初始半径偏移量时才绘制
        if (rippleRadius > initialRippleRadiusOffset) {
            canvas.drawOval(rippleEffectBounds, ripplePaint)
        }
    }

}
