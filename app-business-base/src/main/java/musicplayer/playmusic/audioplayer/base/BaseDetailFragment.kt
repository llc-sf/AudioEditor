package musicplayer.playmusic.audioplayer.base

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.rx.add
import dev.android.player.framework.rx.ioMain
import dev.android.player.framework.utils.ImageUtils
import dev.android.player.framework.utils.ScreenUtil
import dev.android.player.framework.utils.dp
import io.reactivex.rxjava3.core.Single
import musicplayer.playmusic.audioplayer.base.databinding.FragmentBaseDetailBinding
import kotlin.math.abs

/**
 * 用户自建播放列表，专辑，歌手，曲风 详情页
 */
open class BaseDetailFragment : BaseFragment() {

    private var _binding: FragmentBaseDetailBinding? = null
    private val mBinding get() = _binding!!


    private var mTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBaseDetailBinding.inflate(inflater, container, false)
        mBinding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = ScreenUtil.getStatusBarHeight(context)
        }
        //顶部圆角占位的背景色
        mBinding.topHolder.background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.window_bg_color))
            cornerRadii = floatArrayOf(16f.dp, 16f.dp, 16f.dp, 16f.dp, 0f, 0f, 0f, 0f)
        }
        (activity as? AppCompatActivity)?.setSupportActionBar(mBinding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.collapsing.title = ""
        mBinding.appbar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {

            override fun onStateChange(state: Int, offset: Int, total: Int) {
                mBinding.topHolder.isInvisible = state == CollapsingToolbarLayoutState.COLLAPSED
            }


            override fun onOffsetChange(offset: Int, total: Int) {
                val alpha = abs(offset) / total.toFloat()
                mBinding.topContainer.alpha = 1 - alpha
                mBinding.toolbarTitle.alpha = alpha
            }
        })
        return mBinding.root
    }

    /**
     * 设置RecyclerView Adapter
     */
    protected fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        mBinding.recyclerview.adapter = adapter
    }

    protected fun getRecyclerView(): RecyclerView {
        return mBinding.recyclerview
    }

    protected fun setTitle(title: String) {
        this.mTitle = title;
        mBinding.detailTitle.setText(title)
        mBinding.toolbarTitle.text = title
    }

    /**
     *
     * 控制Description的显示与隐藏
     */
    protected fun setDescription(description: String) {
        mBinding.detailDescription.text = description
        mBinding.detailDescription.isVisible = !TextUtils.isEmpty(description)
    }

    /**
     * 子类设置图片进行加载Target
     * @param icon icon资源
     * @param cover 大图的资源
     */
    protected fun getTarget(@DrawableRes cover: Int = -1, @DrawableRes icon: Int = -1): Target<Bitmap> {
        return IconTarget(mBinding.coverIcon, mBinding.blur, cover, icon)
    }


    /**
     * 设置 Icon And 背景模糊图
     * 自定义Target
     */
    protected inner class IconTarget(val view: ImageView, val blur: ImageView, val cover: Int, val icon: Int) : CustomViewTarget<ImageView, Bitmap>(view) {

        override fun onLoadFailed(drawable: Drawable?) {
            if (icon != -1) {
                view.setImageResource(icon)
            } else {
                view.setImageDrawable(drawable)
            }

            if (cover != -1) {
                blur.setImageResource(cover)
            } else if (drawable != null) {
                onLoadBlurBitmap(drawable)
            }
        }

        override fun onResourceLoading(drawable: Drawable?) {
            super.onResourceLoading(drawable)
            if (icon != -1) {
                view.setImageResource(icon)
            } else {
                view.setImageDrawable(drawable)
            }
        }

        override fun onResourceCleared(placeholder: Drawable?) {

        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            view.setImageBitmap(resource)
            //设置封面的模糊背景
            onLoadBlurBitmap(resource)
        }
    }


    /**
     * 加载模糊背景图
     */
    private fun onLoadBlurBitmap(bitmap: Bitmap) {
        Single.fromCallable { ImageUtils.createBlurredScaleImageWithRadius(bitmap, activity, 2, 25, 0x44000000) }
                .ioMain()
                .subscribe({ image: Drawable? ->
                    mBinding.blur.setImageDrawable(image ?: ColorDrawable(Color.TRANSPARENT))
                }) { e: Throwable -> e.printStackTrace() }.add(this)
    }

    /**
     * @see onLoadBlurBitmap
     */
    private fun onLoadBlurBitmap(drawable: Drawable) {
        //Drawable draw bitmap
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        onLoadBlurBitmap(bitmap)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.setSupportActionBar(null)
        _binding = null
    }

}