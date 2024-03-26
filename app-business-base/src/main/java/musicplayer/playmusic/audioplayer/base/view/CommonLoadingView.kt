package musicplayer.playmusic.audioplayer.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import musicplayer.playmusic.audioplayer.base.R

class CommonLoadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.view_common_loading, this)
    }
}