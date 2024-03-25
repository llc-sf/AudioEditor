package musicplayer.playmusic.audioplayer.base.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dev.android.player.framework.utils.dimen
import dev.android.player.framework.utils.dp
import musicplayer.playmusic.audioplayer.base.R
import musicplayer.playmusic.audioplayer.base.databinding.ViewCommonEmptyBuilderBinding
import musicplayer.playmusic.audioplayer.base.databinding.ViewCommonEmptyButtonBinding

/**
 * Create Business Empty View
 */
class EmptyViewBuilder(val context: Context) {

    private val binding: ViewCommonEmptyBuilderBinding = ViewCommonEmptyBuilderBinding.inflate(LayoutInflater.from(context))

    init {
//        val radio = ScreenUtil.getScreenRatio(context)
//        if (radio >= ScreenUtil.RATIO_18_9) {
//            binding.container.updatePaddingRelative(top = context.dimen(R.dimen.dp_98))
//        } else if (radio >= ScreenUtil.RATIO_16_9) {
//            binding.container.updatePaddingRelative(top = context.dimen(R.dimen.dp_50))
//        } else if (radio >= ScreenUtil.RATIO_4_3) {
//            binding.container.updatePaddingRelative(top = context.dimen(R.dimen.dp_30))
//        } else {
//            binding.container.updatePaddingRelative(top = context.dimen(R.dimen.dp_20))
//        }
    }

    fun setStateImage(@DrawableRes image: Int): EmptyViewBuilder {
        binding.state.visibility = View.VISIBLE
        binding.state.setImageResource(image)
        return this
    }

    fun setStateImage(drawable: Drawable?): EmptyViewBuilder {
        binding.state.visibility = View.VISIBLE
        binding.state.setImageDrawable(drawable)
        return this
    }

    /**
     * 设置EmptyView上提示文字
     */
    fun setEmptyText(empty: CharSequence?): EmptyViewBuilder {
        binding.title.text = empty
        return this
    }

    fun setEmptyText(@StringRes id: Int): EmptyViewBuilder {
        return setEmptyText(context.getString(id))
    }

    /**
     * 添加按钮文字
     */
    fun addBusinessButton(@DrawableRes icon: Int, text: CharSequence, margin: Int? = null, onClick: (v: View) -> Unit): EmptyViewBuilder {
        val button = ViewCommonEmptyButtonBinding.inflate(LayoutInflater.from(context))
        button.icon.setImageResource(icon)
        button.description.text = text
        button.root.setOnClickListener {
            onClick(it)
        }
        button.root.minimumWidth = context.dimen(R.dimen.dp_220)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.topMargin = margin ?: context.dimen(R.dimen.dp_20)
        binding.container.addView(button.root, params)
        return this
    }

    fun addBusinessButton(@DrawableRes icon: Int, @StringRes text: Int, margin: Int? = null, onClick: (v: View) -> Unit): EmptyViewBuilder {
        return addBusinessButton(icon, context.getString(text), margin, onClick)
    }

    fun build(): View {
        return binding.root
    }

    fun adapterMusicControllerView(isControllerViewVisible: Boolean) {
        try {
            var gapView = binding.container.findViewById<View>(R.id.view_gap)
            if (isControllerViewVisible) {
                if (gapView == null) {
                    binding.container.addView(View(binding.container.context).apply {
                        id = R.id.view_gap
                        minimumHeight = 56.dp
                    })
                    binding.container.requestLayout()
                }
            } else {
                if (gapView != null) {
                    binding.container.removeView(gapView)
                    binding.container.requestLayout()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}