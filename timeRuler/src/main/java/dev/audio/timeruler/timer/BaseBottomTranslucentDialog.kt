package dev.audio.timeruler.timer

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import dev.audio.timeruler.R
import dev.audio.timeruler.databinding.DialogBaseBottomTranslucentBinding
import dev.audio.timeruler.utils.dp

/**
 * 底部透明弹窗 顶部带圆角
 */
abstract class BaseBottomTranslucentDialog : BaseTranslucentDialog() {

    private var _binding: DialogBaseBottomTranslucentBinding? = null
    private val mBinding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = DialogBaseBottomTranslucentBinding.inflate(inflater, container, false)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mBinding.contentContainer.addView(getContentView(inflater, container), params)
        return mBinding.root
    }

    abstract fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) //背景色
        view.background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(view.context, R.color.bottom_dialog_bg_color))
            cornerRadii = floatArrayOf(20f.dp, 20f.dp, 20f.dp, 20f.dp, 0f, 0f, 0f, 0f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}