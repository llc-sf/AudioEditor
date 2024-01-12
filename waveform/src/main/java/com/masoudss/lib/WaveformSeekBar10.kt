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
import kotlin.math.max
import kotlin.math.roundToInt

open class WaveformSeekBar10 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
            field = value
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

    var waveBackgroundColor: Int = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val path = Path()
        // 起始点在视图的中心线上
        path.moveTo(paddingLeft.toFloat(), (height / 2).toFloat())

        sample?.let { waveSample ->
            if (waveSample.isEmpty()) return

            // 为了避免绘制过多的点，计算下采样间隔
            val sampleStep = max(1, (waveSample.size / getAvailableWidth()).toInt())
            var lastX = paddingLeft.toFloat()
            var lastY = (height / 2).toFloat()

            // 初始化path
            path.moveTo(lastX, lastY)

            // 使用贝塞尔曲线绘制平滑波形
            for (i in waveSample.indices step sampleStep) {
                val x = paddingLeft + i * waveWidth
                val y = (height / 2) + ((waveSample[i] / mMaxValue.toFloat()) * (height / 2))

                // 使用二次贝塞尔曲线平滑连接点
                val controlX = (lastX + x) / 2
                path.quadTo(controlX, lastY, x, y)

                lastX = x
                lastY = y
            }

            // 连接到视图右侧
            path.lineTo(width.toFloat(), (height / 2).toFloat())
        }

        // 完成路径回到起始点，形成一个封闭的形状
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(paddingLeft.toFloat(), height.toFloat())
        path.close()

        // 绘制波形背景
        mWavePaint.color = waveBackgroundColor
        mWavePaint.shader = null // 重置Shader
        canvas.drawPath(path, mWavePaint)

        // 创建进度Shader
        val shader = LinearGradient(
            0f, 0f, progressXPosition, 0f,
            waveProgressColor, waveBackgroundColor,
            Shader.TileMode.CLAMP
        )
        // 绘制波形进度
        mWavePaint.color = waveProgressColor
        mWavePaint.shader = shader
        canvas.drawPath(path, mWavePaint)
    }

    // 根据进度计算波形进度条的X位置
    val progressXPosition = paddingLeft + (progress / maxProgress) * getAvailableWidth()


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

    private fun getAvailableWidth(): Int {
        var width = mCanvasWidth - paddingLeft - paddingRight
        if (width <= 0) width = 1
        return width
    }

    private fun getAvailableHeight(): Int {
        var height = mCanvasHeight - paddingTop - paddingBottom
        if (height <= 0) height = 1
        return height
    }
}
