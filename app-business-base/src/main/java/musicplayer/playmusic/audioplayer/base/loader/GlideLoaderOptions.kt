package musicplayer.playmusic.audioplayer.base.loader

import android.graphics.Color
import com.bumptech.glide.request.RequestOptions
import musicplayer.playmusic.audioplayer.base.R
import musicplayer.playmusic.audioplayer.themes.utils.ThemeUtils
import kotlin.properties.Delegates

object GlideLoaderOptions {
    private fun getDefaultSongDrawable(): Int {
        return ThemeUtils.getAttrValueResourceIdByCurrentThemeId(R.attr.drawable_default_song)
    }

    private fun getDefaultGenreDrawable(): Int {
        return ThemeUtils.getAttrValueResourceIdByCurrentThemeId(R.attr.drawable_default_genre)
    }

    fun getDefaultBottomPlayerDefaultCoverDrawable(): Int {
        return ThemeUtils.getAttrValueResourceIdByCurrentThemeId(R.attr.drawable_default_bottom_player_cover)
    }

    /**
     * 不透明
     */
    private fun getDefaultSongAlpha1Drawable(): Int {
        return ThemeUtils.getAttrValueResourceIdByCurrentThemeId(R.attr.drawable_default_song_alpha1)
    }


    fun onThemeChanged() {
        songOption = generateSongOption()
        genreOption = generateGenreOption()
        playListOptionAlpha1 = generateDefaultSongAlpha1Option()
    }

    var songOption: RequestOptions by Delegates.observable(
            initialValue = generateSongOption()
    ) { _, _, _ ->

    }

    var playListOptionAlpha1: RequestOptions by Delegates.observable(
            initialValue = generateDefaultSongAlpha1Option()
    ) { _, _, _ ->

    }

    var genreOption: RequestOptions by Delegates.observable(
            initialValue = generateGenreOption()
    ) { _, _, _ ->
        // 在这里可以添加更多的逻辑，例如日志输出
    }

    private fun generateSongOption(): RequestOptions {
        return RequestOptions()
                .fallback(getDefaultSongDrawable())
                .placeholder(getDefaultSongDrawable())
                .error(getDefaultSongDrawable())
    }

    private fun generateDefaultSongAlpha1Option(): RequestOptions {
        return RequestOptions()
                .fallback(getDefaultSongAlpha1Drawable())
                .placeholder(getDefaultSongAlpha1Drawable())
                .error(getDefaultSongAlpha1Drawable())
    }


    val songOptionAlpha1 by lazy {
        RequestOptions()
                .fallback(getDefaultSongAlpha1Drawable())
                .placeholder(getDefaultSongAlpha1Drawable())
                .error(getDefaultSongAlpha1Drawable())
    }

    //播放页默认封面图
    val nowPlayingSongOption by lazy {
        RequestOptions()
                .fallback(getDefaultSongDrawable())
                .placeholder(getDefaultSongDrawable())
                .error(getDefaultSongDrawable())
    }
    val albumOption by lazy {
        Color.TRANSPARENT
        RequestOptions()
                .fallback(R.drawable.ic_default_album)
                .placeholder(R.drawable.ic_default_album)
                .error(R.drawable.ic_default_album)
    }
    val albumOptionAlpha1 by lazy {
        Color.TRANSPARENT
        RequestOptions()
                .fallback(R.drawable.ic_default_album_alpha1)
                .placeholder(R.drawable.ic_default_album_alpha1)
                .error(R.drawable.ic_default_album_alpha1)
    }

    private fun generateGenreOption(): RequestOptions {
        return RequestOptions()
                .fallback(getDefaultGenreDrawable())
                .placeholder(getDefaultGenreDrawable())
                .error(getDefaultGenreDrawable())
    }


    val artistOption by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_default_artist)
                .placeholder(R.drawable.ic_default_artist)
                .error(R.drawable.ic_default_artist)
    }

    val artistOptionAlpha1 by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_default_artists_alpha1)
                .placeholder(R.drawable.ic_default_artists_alpha1)
                .error(R.drawable.ic_default_artists_alpha1)
    }
    val defaultOption by lazy { RequestOptions() }

}