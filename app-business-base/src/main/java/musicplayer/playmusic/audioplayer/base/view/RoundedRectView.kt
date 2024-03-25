package musicplayer.playmusic.audioplayer.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import musicplayer.playmusic.audioplayer.base.R

class RoundedRectView : View {
    private val rect: RectF = RectF()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cornerRadius: Float = 0f
    private val path: Path = Path()

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Update Paint and text measurements from attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundedRectView, defStyle, 0)
        cornerRadius = a.getDimension(R.styleable.RoundedRectView_cornerRadius, 0f)
        paint.color = a.getColor(R.styleable.RoundedRectView_rectColor, 0)
        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        rect.set(0f, 0f, width.toFloat(), height.toFloat())
//        val radii = floatArrayOf(cornerRadius, cornerRadius, // Top left radius in px
//                cornerRadius, cornerRadius, // Top right radius in px
//                0f, 0f, // Bottom right radius in px
//                0f, 0f) // Bottom left radius in px
//        path.addRoundRect(rect, radii, Path.Direction.CW)
//        canvas.drawPath(path, paint)
    }

    fun refresh(alpha: Int) {
        paint.alpha = alpha
        invalidate()
    }
}

