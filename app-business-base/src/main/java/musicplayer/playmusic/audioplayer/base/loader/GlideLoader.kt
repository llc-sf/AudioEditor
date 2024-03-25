package musicplayer.playmusic.audioplayer.base.loader

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import dev.android.player.framework.data.model.*


/**
 * 通过指定数据源，获取对应的options
 */
fun getOptions(any: Any?): RequestOptions {
    return when (any) {
        is PlayList -> {
            GlideLoaderOptions.songOption
        }

        is Song -> {
            GlideLoaderOptions.songOption
        }

        is Album -> {
            GlideLoaderOptions.albumOption
        }

        is Genre -> {
            GlideLoaderOptions.genreOption
        }

        is Artist -> {
            GlideLoaderOptions.artistOption
        }

        else -> GlideLoaderOptions.defaultOption
    }
}

fun getOptionsAlpha1(any: Any?): RequestOptions {
    return when (any) {
        is PlayList -> {
            GlideLoaderOptions.playListOptionAlpha1
        }

        is Song -> {
            GlideLoaderOptions.songOption
        }

        is Album -> {
            GlideLoaderOptions.albumOptionAlpha1
        }

        is Genre -> {
            GlideLoaderOptions.genreOption
        }

        is Artist -> {
            GlideLoaderOptions.artistOptionAlpha1
        }

        else -> GlideLoaderOptions.defaultOption
    }
}


fun ImageView.load(any: Any?, listener: ImageLoadListener? = null) {
    val options = getOptions(any)
    onLoadImage(options, any, listener)
}

/**
 * 没有透明度
 */
fun ImageView.loadAlpha1(any: Any?, listener: ImageLoadListener? = null) {
    val options = getOptionsAlpha1(any)
    onLoadImage(options, any, listener)
}

/**
 * 非默认情况调用，指定RequestOptions
 */
fun ImageView.load(any: Any?, options: RequestOptions, listener: ImageLoadListener? = null) {
    onLoadImage(options, any, listener)
}

fun ImageView.loadCircle(any: Any?, listener: ImageLoadListener? = null) {
    val options = getOptions(any)
            .circleCrop()
    onLoadImage(options, any, listener)
}


private fun ImageView.onLoadImage(options: RequestOptions, any: Any?, listener: ImageLoadListener? = null) {
    //如果Activity 已经销毁，则不加载图片
    val ctx = context
    if (ctx is Activity && (ctx.isFinishing || ctx.isDestroyed)) {
        return
    }
    //防止View里Request中的model引用被提前改变，而导致图片加载错乱
    val clone = when (any) {
        is PlayList -> any.newInstance() as PlayList
        is Song -> any.newInstance() as Song
        is Album -> any.newInstance() as Album
        is Genre -> any.newInstance() as Genre
        is Artist -> any.newInstance() as Artist
        else -> any
    }
    if (listener != null) {
        Glide.with(ctx).load(clone).apply(options).listener(listener)
                .into(ImageViewTarget(this, listener))
    } else {
        Glide.with(ctx).load(clone).apply(options).into(ImageViewTarget(this))
    }

}


open class ImageLoadListener : RequestListener<Drawable> {

    private val TAG = "ImageLoadListener"
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
        Log.d(TAG, "onLoadFailed() called with: e = $e, model = $model, target = $target, isFirstResource = $isFirstResource")
        return false
    }

    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        Log.d(TAG, "onResourceReady() called with: resource = $resource, model = $model, target = $target, dataSource = $dataSource, isFirstResource = $isFirstResource")
        return false
    }

    open fun onImageDrawable() {

    }
}

private class ImageViewTarget(view: ImageView, var listener: ImageLoadListener? = null) : CustomViewTarget<ImageView, Drawable>(view) {
    private val TAG = "ImageViewTarget"

    override fun onLoadFailed(errorDrawable: Drawable?) {
        view.setImageDrawable(errorDrawable)
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        Log.d(TAG, "onResourceReady() called with: resource = $resource, transition = $transition")
        view.setImageDrawable(resource)
        listener?.onImageDrawable()
    }

    override fun onResourceLoading(placeholder: Drawable?) {
        view.setImageDrawable(placeholder)
    }

    override fun onResourceCleared(placeholder: Drawable?) {
        view.setImageDrawable(placeholder)
    }
}


