package musicplayer.playmusic.audioplayer.base.loader

import com.bumptech.glide.request.RequestOptions
import musicplayer.playmusic.audioplayer.base.R

object GlideLoaderOptions {
    val playListOption by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_default_song)
                .placeholder(R.drawable.ic_default_song)
                .error(R.drawable.ic_default_song)
    }
    val songOption by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_cover_default)
                .placeholder(R.drawable.ic_cover_default)
                .error(R.drawable.ic_cover_default)
    }
    val albumOption by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_default_album)
                .placeholder(R.drawable.ic_default_album)
                .error(R.drawable.ic_default_album)
    }
    val genreOption by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_default_genre)
                .placeholder(R.drawable.ic_default_genre)
                .error(R.drawable.ic_default_genre)
    }
    val artistOption by lazy {
        RequestOptions()
                .fallback(R.drawable.ic_default_artist)
                .placeholder(R.drawable.ic_default_artist)
                .error(R.drawable.ic_default_artist)
    }
    val defaultOption by lazy { RequestOptions() }
}