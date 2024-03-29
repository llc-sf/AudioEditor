package com.masoudss.lib

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.RawRes
import com.masoudss.lib.utils.ThreadBlocking
import com.masoudss.lib.utils.Utils
import com.masoudss.lib.utils.WaveGravity
import com.masoudss.lib.utils.WaveformOptions
import java.io.File
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * 毛刺效果 带阴影
 */
open class WaveformSeekBar9 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.RED
        this.style = Paint.Style.FILL
    }
    private val mWaveRect = RectF()
    private val mMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMarkerRect = RectF()
    private val mProgressCanvas = Canvas()
    private var mMaxValue = Utils.dp(context, 2).toInt()
    private var mTouchDownX = 0F
    private var mProgress = 0f
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var mAlreadyMoved = false
    private lateinit var progressBitmap: Bitmap
    private lateinit var progressShader: Shader

    var onProgressChanged: SeekBarOnProgressChanged? = null

    var sample: IntArray? = null
        set(value) {
            val downSampleFactor = 400
            val downSampledSamples = downSample(value!!, downSampleFactor)
            field = downSampledSamples
            setMaxValue()
            refreshPosition()
            invalidate()
        }

    var progress: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    var maxProgress: Float = 100F
        set(value) {
            field = value
            invalidate()
        }

    var waveBackgroundColor: Int = Color.RED

    var waveProgressColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var waveGap: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingTop: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingBottom: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingLeft: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingRight: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var waveWidth: Float = Utils.dp(context, 5)
        set(value) {
            field = value
            invalidate()
        }

    var waveMinHeight: Float = waveWidth
        set(value) {
            field = value
            invalidate()
        }

    var waveCornerRadius: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var waveGravity: WaveGravity = WaveGravity.CENTER
        set(value) {
            field = value
            invalidate()
        }

    var marker: HashMap<Float, String>? = null
        set(value) {
            field = value
            invalidate()
        }

    var markerWidth: Float = Utils.dp(context, 1)
        set(value) {
            field = value
            invalidate()
        }

    var markerColor: Int = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }

    var markerTextColor: Int = Color.RED
        set(value) {
            field = value
            invalidate()
        }

    var markerTextSize: Float = Utils.dp(context, 12)
        set(value) {
            field = value
            invalidate()
        }

    var markerTextPadding: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var visibleProgress: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveformSeekBar)
        waveWidth = ta.getDimension(R.styleable.WaveformSeekBar_wave_width, waveWidth)
        waveGap = ta.getDimension(R.styleable.WaveformSeekBar_wave_gap, waveGap)
        wavePaddingTop = ta.getDimension(R.styleable.WaveformSeekBar_wave_padding_top, 0F).toInt()
        wavePaddingBottom =
            ta.getDimension(R.styleable.WaveformSeekBar_wave_padding_Bottom, 0F).toInt()
        wavePaddingLeft = ta.getDimension(R.styleable.WaveformSeekBar_wave_padding_left, 0F).toInt()
        wavePaddingRight =
            ta.getDimension(R.styleable.WaveformSeekBar_wave_padding_right, 0F).toInt()
        waveCornerRadius =
            ta.getDimension(R.styleable.WaveformSeekBar_wave_corner_radius, waveCornerRadius)
        waveMinHeight = ta.getDimension(R.styleable.WaveformSeekBar_wave_min_height, waveMinHeight)
        waveBackgroundColor =
            ta.getColor(R.styleable.WaveformSeekBar_wave_background_color, waveBackgroundColor)
        waveProgressColor =
            ta.getColor(R.styleable.WaveformSeekBar_wave_progress_color, waveProgressColor)
        progress = ta.getFloat(R.styleable.WaveformSeekBar_wave_progress, progress)
        maxProgress = ta.getFloat(R.styleable.WaveformSeekBar_wave_max_progress, maxProgress)
        visibleProgress =
            ta.getFloat(R.styleable.WaveformSeekBar_wave_visible_progress, visibleProgress)
        val gravity = ta.getString(R.styleable.WaveformSeekBar_wave_gravity)?.toInt()
            ?: WaveGravity.CENTER.ordinal
        waveGravity = WaveGravity.values()[gravity]
        markerWidth = ta.getDimension(R.styleable.WaveformSeekBar_marker_width, markerWidth)
        markerColor = ta.getColor(R.styleable.WaveformSeekBar_marker_color, markerColor)
        markerTextColor =
            ta.getColor(R.styleable.WaveformSeekBar_marker_text_color, markerTextColor)
        markerTextSize =
            ta.getDimension(R.styleable.WaveformSeekBar_marker_text_size, markerTextSize)
        markerTextPadding =
            ta.getDimension(R.styleable.WaveformSeekBar_marker_text_padding, markerTextPadding)
        ta.recycle()
    }

    open fun refreshPosition() {

    }

    private fun setMaxValue() {
        mMaxValue = sample?.maxOrNull() ?: 0
    }

    @ThreadBlocking
    fun setSampleFrom(samples: IntArray) {
        this.sample = samples
    }

    @ThreadBlocking
    fun setSampleFrom(audio: File) {
        setSampleFrom(audio.path)
    }

    @ThreadBlocking
    fun setSampleFrom(audio: String) {
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    @ThreadBlocking
    fun setSampleFrom(@RawRes audio: Int) {
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    @ThreadBlocking
    fun setSampleFrom(audio: Uri) {
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
        progressBitmap =
            Bitmap.createBitmap(getAvailableWidth(), getAvailableHeight(), Bitmap.Config.ARGB_8888)
        progressShader = BitmapShader(progressBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

   private var waveHeightScale: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f) // 确保值在0到1之间
            invalidate()
        }

    // 新增：用于绘制平滑波形的Path
    private val wavePath = Path()

    // 新增：控制波形平滑度的属性
    private var smoothFactor = 100

    // 添加一个成员变量来控制贝塞尔曲线的平滑度
    var smoothness = 0.5f  // 取值范围通常是0（直线）到0.5（最大平滑度）
    fun smooth(samples: IntArray, windowSize: Int): IntArray {
        val smoothed = IntArray(samples.size)
        for (i in samples.indices) {
            var sum = 0
            var count = 0
            for (j in i - windowSize..i + windowSize) {
                if (j >= 0 && j < samples.size) {
                    sum += samples[j]
                    count++
                }
            }
            smoothed[i] = sum / count
        }
        return smoothed
    }

    // 在onDraw之前调用这个方法来平滑数据
    val windowSize = 5 // 窗口大小可以根据你的需求进行调整
//    val smoothedSamples = smooth(samples, windowSize)


    fun downSample(samples: IntArray, factor: Int): IntArray {
        return samples.filterIndexed { index, _ ->
            index % factor == 0
        }.toIntArray()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val path = Path()
        sample?.let { samples ->
            if (samples.isNotEmpty()) {
                val widthScale = width.toFloat() / (samples.size - 1)
                val heightScale = height.toFloat() / mMaxValue

                // 初始点设置在第一个样本的位置
                path.moveTo(0f, height / 2f - (samples[0] * heightScale / 2))

                // 使用二次贝塞尔曲线绘制平滑波形
                for (i in 1 until samples.size) {
                    val x1 = (i - 1) * widthScale
                    val y1 = height / 2f - (samples[i - 1] * heightScale / 2)
                    val x2 = i * widthScale
                    val y2 = height / 2f - (samples[i] * heightScale / 2)

                    // 控制点为前一个点和当前点的中点
                    val controlX = (x1 + x2) / 2
                    val controlY = (y1 + y2) / 2

                    // 二次贝塞尔曲线
                    path.quadTo(controlX, controlY, x2, y2)
                }

                // 绘制路径
                canvas.drawPath(path, mWavePaint)
            }
        }
    }





    // 辅助方法：获取波形中心Y坐标
    private fun getWaveCenterY(): Float {
        return paddingTop + (height - paddingTop - paddingBottom) / 2f
    }


    // 辅助方法：根据样本值计算波形高度
    private fun getWaveHeight(sampleValue: Int): Float {
        return if (mMaxValue != 0)
            getAvailableHeight() * (sampleValue.toFloat() / mMaxValue) * waveHeightScale
        else 0F
    }

    // 辅助方法：根据波形高度和重力设置获取顶部位置
    private fun getWaveTopPosition(waveHeight: Float): Float {
        return when (waveGravity) {
            WaveGravity.TOP -> paddingTop.toFloat()
            WaveGravity.CENTER -> (paddingTop + getAvailableHeight()) / 2F - waveHeight / 2F
            WaveGravity.BOTTOM -> mCanvasHeight - paddingBottom - waveHeight
        }
    }

    // 辅助方法：获取可用的宽度
    private fun getAvailableWidth(): Int {
        return mCanvasWidth - paddingLeft - paddingRight
    }

    // 辅助方法：获取可用的高度
    private fun getAvailableHeight(): Int {
        return mCanvasHeight - paddingTop - paddingBottom
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled)
            return false
        if (visibleProgress > 0) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchDownX = event.x
                    mProgress = progress
                    mAlreadyMoved = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop || mAlreadyMoved) {
                        updateProgress(event)
                        mAlreadyMoved = true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    performClick()
                }
            }
        } else {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isParentScrolling())
                        mTouchDownX = event.x
                    else
                        updateProgress(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    updateProgress(event)
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop)
                        updateProgress(event)
                    performClick()
                }
            }
        }
        return true
    }

    private fun isParentScrolling(): Boolean {
        var parent = parent as View
        val root = rootView
        while (true) {
            when {
                parent.canScrollHorizontally(1) -> return true
                parent.canScrollHorizontally(-1) -> return true
                parent.canScrollVertically(1) -> return true
                parent.canScrollVertically(-1) -> return true
            }
            if (parent == root)
                return false
            parent = parent.parent as View
        }
    }

    private fun updateProgress(event: MotionEvent) {
        progress = getProgress(event);
    }

    private fun getProgress(event: MotionEvent): Float {
        return if (visibleProgress > 0) {
            (mProgress - visibleProgress * (event.x - mTouchDownX) / getAvailableWidth()).coerceIn(
                0F,
                maxProgress
            )
        } else {
            maxProgress * event.x / getAvailableWidth()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

}
