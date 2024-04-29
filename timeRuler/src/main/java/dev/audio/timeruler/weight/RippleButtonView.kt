package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import dev.audio.timeruler.R
import dev.audio.timeruler.utils.dp
import dev.audio.timeruler.utils.sp

class RippleButtonView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val tv: TextView

    // 水波纹效果参数
    private var rippleX: Float = -1f
    private var rippleY: Float = -1f
    private var rippleRadiusX: Float = 0f
    private var rippleRadiusY: Float = 0f

    private var rippleStrokeWidth = 10f // 波纹圆环的宽度
    private var radius = 46.dp
    private var rippleDuration = 800L // 水波纹动画时长
    private var rippleMaxRadiusX: Float = 0f
    private var rippleMaxRadiusY: Float = 0f
    private val rippleEffectHandler = Handler(android.os.Looper.getMainLooper())

    private val ripplePaint = Paint().apply {
        color = Color.WHITE // 波纹颜色
        style = Paint.Style.STROKE // 描边
        strokeWidth = rippleStrokeWidth
        alpha = 0 // 初始透明度
    }

    private val rippleUpdateRunnable = object : Runnable {
        override fun run() {
            if (rippleRadiusX < rippleMaxRadiusX && rippleX >= 0 && rippleY >= 0) {
                rippleRadiusX += rippleMaxRadiusX / (rippleDuration / GAP_TIME_ANIMATION)
                rippleRadiusY += rippleMaxRadiusY / (rippleDuration / GAP_TIME_ANIMATION)
                ripplePaint.alpha = ((1 - rippleRadiusX / rippleMaxRadiusX) * 255).toInt()
                if (ripplePaint.alpha == 0) {
                    return
                }
                invalidate()
                rippleEffectHandler.postDelayed(this, GAP_TIME_ANIMATION)
            }
        }
    }

    private var middleAction: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_action_rapple_view, this, true)
        tv = findViewById(R.id.tv)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleButton, 0, 0)
        try {
            rippleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.RippleButton_ripplebutton_rippleStrokeWidth, rippleStrokeWidth.toInt())
                .toFloat()
            radius = typedArray.getDimensionPixelSize(R.styleable.RippleButton_ripplebutton_radius, radius)
            var tvBg = typedArray.getResourceId(R.styleable.RippleButton_ripplebutton_text_bg, R.drawable.rect_14ffffff_corner_46)
            tv.setBackgroundResource(tvBg)
            var padding = typedArray.getDimensionPixelSize(R.styleable.RippleButton_ripplebutton_padding, 4.dp)
            findViewById<ConstraintLayout>(R.id.container).setPadding(padding, padding, padding, padding)
            rippleDuration = typedArray.getString(R.styleable.RippleButton_ripplebutton_annimation_duration)
                ?.toLong() ?: rippleDuration
            var content = typedArray.getString(R.styleable.RippleButton_ripplebutton_text_content)
                ?: ""
            tv.text = content
            var size = typedArray.getFloat(R.styleable.RippleButton_ripplebutton_text_size, 12.sp.toFloat())
            tv.textSize = size
        } catch (e: Exception) {
            e.printStackTrace()
        }
        typedArray.recycle()
        ripplePaint.strokeWidth = rippleStrokeWidth

    }

    fun setActionListener(middleAction: () -> Unit) {
        this.middleAction = middleAction
    }

    fun setText(text: String) {
        tv.text = text
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rippleMaxRadiusX = this.width.toFloat() / 2 // 可以根据需要调整这个值
                rippleMaxRadiusY = this.height.toFloat() / 2 // 可以根据需要调整这个值
                rippleX = tv.x + tv.width / 2
                rippleY = tv.y + tv.height / 2 // 初始半径设置为rightIcon外部边界加上一个偏移量
                rippleRadiusX = this.tv.width / 2f + rippleStrokeWidth / 2
                rippleRadiusY = this.tv.height / 2f + rippleStrokeWidth / 2
                ripplePaint.alpha = 255
                rippleEffectHandler.post(rippleUpdateRunnable)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        val rippleEffectBounds = RectF(rippleX - rippleRadiusX, rippleY - rippleRadiusY, rippleX + rippleRadiusX, rippleY + rippleRadiusY)
        canvas?.drawRoundRect(rippleEffectBounds, radius.toFloat(), radius.toFloat(), ripplePaint)
    }


    companion object {
        private const val GAP_TIME_ANIMATION = 1L
    }

}
