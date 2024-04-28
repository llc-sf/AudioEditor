package dev.audio.timeruler.weight

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import dev.audio.timeruler.R

class CustomActionView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    // 定义一个常量作为放大消息的标识
    companion object {
        private const val MESSAGE_LEFT = 1
        private const val MESSAGE_RIGHT = 2

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


    init { // 从XML中加载布局
        LayoutInflater.from(context).inflate(R.layout.custom_action_view, this, true)

        // 初始化视图元素
        leftIcon = findViewById(R.id.left)
        rightIcon = findViewById(R.id.right)

        // 读取并应用自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSearchView, 0, 0)
        leftIcon.setImageDrawable(typedArray.getDrawable(R.styleable.CustomSearchView_leftDrawable))
        rightIcon.setImageDrawable(typedArray.getDrawable(R.styleable.CustomSearchView_rightDrawable))
        leftIconLeftPadding = typedArray.getDimensionPixelSize(R.styleable.CustomSearchView_left_icon_left_padding, 0)
        leftIconRightPadding = typedArray.getDimensionPixelSize(R.styleable.CustomSearchView_left_icon_right_padding, 0)
        rightIconLeftPadding = typedArray.getDimensionPixelSize(R.styleable.CustomSearchView_right_icon_left_padding, 0)
        rightIconRightPadding = typedArray.getDimensionPixelSize(R.styleable.CustomSearchView_right_icon_right_padding, 0)
        topPadding = typedArray.getDimensionPixelSize(R.styleable.CustomSearchView_top_padding, 0)
        bottomPadding = typedArray.getDimensionPixelSize(R.styleable.CustomSearchView_bottom_padding, 0)
        typedArray.recycle()
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
                if (x < width / 2) {
                    if (!leftIcon.isEnabled) {
                        setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
                        return true
                    }
                    setBackgroundResource(R.drawable.bg_left_click)
                    leftDown = true
                    zoomHandler.sendEmptyMessageDelayed(MESSAGE_LEFT, GAP_TIME)
                } else {
                    if (!rightIcon.isEnabled) {
                        setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
                        return true
                    }
                    setBackgroundResource(R.drawable.bg_right_click)
                    rightDown = true
                    zoomHandler.sendEmptyMessageDelayed(MESSAGE_RIGHT, GAP_TIME)
                }

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                clearMsg()
                if (leftDown) {
                    if (!leftIcon.isEnabled) {
                        setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
                        return true
                    }
                } else {
                    if (!rightIcon.isEnabled) {
                        setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
                        return true
                    }
                }
                setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
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
    fun setActionListener(leftAction: () -> Unit, rightAction: () -> Unit) {
        this.leftAction = leftAction
        this.rightAction = rightAction
    }

    fun freshLeftIconEnable(enable: Boolean) {
        leftIcon.isEnabled = enable
        leftIcon.alpha = if (enable) 1.0f else 0.5f
        if (!enable) {
            setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
        }
    }

    fun freshRightIconEnable(enable: Boolean) {
        rightIcon.isEnabled = enable
        rightIcon.alpha = if (enable) 1.0f else 0.5f
        if (!enable) {
            setBackgroundResource(R.drawable.rect_14ffffff_corner_46)
        }
    }


    private val zoomHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_LEFT -> {
                    leftAction?.invoke()
                    sendEmptyMessageDelayed(MESSAGE_LEFT, GAP_TIME)
                }

                MESSAGE_RIGHT -> {
                    rightAction?.invoke()
                    sendEmptyMessageDelayed(MESSAGE_RIGHT, GAP_TIME)
                }
            }
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec) // 获取Drawable对象并计算尺寸
        val leftIconDrawable = leftIcon.drawable
        val rightIconDrawable = rightIcon.drawable

        val leftIconWidth = leftIconDrawable?.intrinsicWidth ?: 0
        val leftIconHeight = leftIconDrawable?.intrinsicHeight ?: 0
        val rightIconWidth = rightIconDrawable?.intrinsicWidth ?: 0
        val rightIconHeight = rightIconDrawable?.intrinsicHeight ?: 0

        // 使用Drawable的尺寸加上内边距计算宽度和高度
        val desiredWidth = leftIconWidth + leftIconLeftPadding + leftIconRightPadding + rightIconWidth + rightIconLeftPadding + rightIconRightPadding
        val desiredHeight = maxOf(leftIconHeight, rightIconHeight) + topPadding + bottomPadding

        setMeasuredDimension(desiredWidth, desiredHeight)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearMsg()
    }

    private fun clearMsg() {
        zoomHandler.removeMessages(MESSAGE_LEFT)
        zoomHandler.removeMessages(MESSAGE_RIGHT)
    }

}
