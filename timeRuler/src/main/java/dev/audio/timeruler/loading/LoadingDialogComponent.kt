package dev.audio.timeruler.loading

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.android.player.framework.utils.dp
import dev.audio.timeruler.R
import dev.audio.timeruler.databinding.DialogLoadingComponentBinding

/**
 * 通用不可用取消Loading 弹窗
 */
class LoadingDialogComponent : DialogFragment() {


    companion object {

        private const val TAG = "LoadingDialogComponent"


        private const val EXTRA_MSG = "EXTRA_MSG"

        private const val EXTRA_IS_CANCEL = "EXTRA_IS_CANCEL"

        @JvmStatic
        private var dialog: LoadingDialogComponent? = null

        @JvmStatic
        @JvmOverloads
        fun show(activity: FragmentActivity?, isCancel: Boolean = false, msg: CharSequence? = null) {
            try {
                if (dialog?.activity != null) {
                    dialog?.dismiss()
                }
                dialog = LoadingDialogComponent()
                dialog?.arguments = bundleOf(EXTRA_IS_CANCEL to isCancel, EXTRA_MSG to msg)

                activity?.apply {
                    supportFragmentManager.beginTransaction()
                        .add(dialog!!, TAG)
                        .commitAllowingStateLoss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialog = null
            }

        }

        @JvmStatic
        fun dismissCompat() {
            try {
                dialog?.dismissAllowingStateLoss()
                dialog = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val isCancel by lazy {
        arguments?.getBoolean(EXTRA_IS_CANCEL) ?: false
    }

    private val msg by lazy {
        arguments?.getCharSequence(EXTRA_MSG) ?: ""
    }

    private var _binding: DialogLoadingComponentBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun getTheme(): Int {
        return R.style.LoadingProgressDialogComponent
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogLoadingComponentBinding.inflate(inflater, container, false)
        try {
            binding.root.background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(requireContext(), R.color.black_33))
                cornerRadius = 16f.dp
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!TextUtils.isEmpty(msg)) {
            binding.message.visibility = View.VISIBLE
            binding.message.text = msg
        } else {
            binding.message.visibility = View.GONE
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(isCancel)
        dialog?.setCanceledOnTouchOutside(isCancel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}