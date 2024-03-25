package musicplayer.playmusic.audioplayer.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.CornerOutline
import dev.android.player.framework.utils.dimenF
import musicplayer.playmusic.audioplayer.base.R
import musicplayer.playmusic.audioplayer.base.databinding.ViewBottomPlayerPreBinding
import musicplayer.playmusic.audioplayer.base.loader.GlideLoaderOptions
import musicplayer.playmusic.audioplayer.themes.ThemeManager
import musicplayer.playmusic.audioplayer.themes.bean.Background
import musicplayer.playmusic.audioplayer.themes.utils.ThemeUtils


/**
 * 底部小播放器视图
 */
class BottomPlayerVewFake @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val mBinding: ViewBottomPlayerPreBinding

    init {
        clipChildren = false
        clipToPadding = false
        mBinding = ViewBottomPlayerPreBinding.inflate(LayoutInflater.from(getContext()), this)
        mBinding.container.CornerOutline(dimenF(R.dimen.dp_30))
        mBinding.root.visibility = View.VISIBLE
    }


    /**
     * 更新歌曲封面
     */
    fun onLoaderAlbumArt(bg: Background) {
        var song = Song().apply {
            title = "Following The Sun - GAY"
            cover = "https://resource.leap.app/appself/musicplayer.musicapp.playmusic.mp3player/theme/cover/img_playbar_cover"
        }
        mBinding.title.text = song.title
        bg?.let {
            var cover = GlideLoaderOptions.getDefaultBottomPlayerDefaultCoverDrawable()
            ThemeManager.loadUrl(context, song.cover)?.placeholder(cover)?.error(cover)?.into(mBinding.cover)
        }
    }

    fun applyTheme(bg: Background) {
        var themeId = ThemeManager.getThemeIdByBg(bg)
        val drawable = resources.getDrawable(ThemeUtils.getAttrValueResourceIdByTheme(themeId, R.attr.bg_bottom_player))
        mBinding.baseline.background = drawable
    }

}