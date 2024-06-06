package dev.audio.timeruler.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import dev.android.player.framework.utils.CircleOutline
import dev.audio.timeruler.R
import dev.audio.timeruler.databinding.DialogBaseTopIconContainerBinding

/**
 * 顶部带凸起一个Icon的Dialog
 */
abstract class TopIconTranslucentDialog : BaseTranslucentDialog() {


    private var _binding: DialogBaseTopIconContainerBinding? = null
    private val mBinging get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = DialogBaseTopIconContainerBinding.inflate(inflater, container, false)

        val content = onContentCreateView(inflater, mBinging.contentContainer, savedInstanceState)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mBinging.contentContainer.addView(content, params)
        mBinging.close.isVisible = isShowClose()
        mBinging.close.setOnClickListener {
            onCloseClick(it)
            dismiss()
        }

        mBinging.icon.setImageResource(getIcon())
        mBinging.iconContainer.CircleOutline()


        return mBinging.root
    }

    abstract fun onContentCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View


    /**
     * 子Fragment中根部布局ID
     */
    protected fun getChildContentId() = R.id.content_container

    /**
     * huo
     */
    @DrawableRes
    abstract fun getIcon(): Int

    protected open fun isShowClose() = false

    protected open fun onCloseClick(view: View) {}


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}