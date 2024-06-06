package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.san.audioeditor.R
import com.san.audioeditor.databinding.ViewFaqAnswerBatteryBinding
import dev.android.player.framework.utils.TrackerMultiple
import dev.android.player.framework.utils.createGradientRoundedBorderDrawable
import dev.android.player.framework.utils.dimenF
import dev.android.player.framework.utils.toSpannableString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 *  description :音乐中断-回答
 */
class FaqAnswerBattery @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FaqAnswer(context, attributeSet) {

    private var binding: ViewFaqAnswerBatteryBinding

    private var isReqBattery = false//是否请求电池优化

    private var batteryJob: Job? = null

    init {
        binding = ViewFaqAnswerBatteryBinding.inflate(LayoutInflater.from(context), null, false)
        binding.faqJumpContent.text = context.getString(R.string.allow_background_playback_gpt)
        binding.faqJumpEntrance.createGradientRoundedBorderDrawable(
                ContextCompat.getColor(context, R.color.widget_text_start_color),
                ContextCompat.getColor(context, R.color.widget_text_end_color),
                context.dimenF(R.dimen.dp_32), context.dimenF(R.dimen.dp_1))
//        val batteryShouldSetting = SystemBatteryOptimization.isShouldSetting(context)
        val batteryShouldSetting = false
        binding.faqAnswer1.isVisible = batteryShouldSetting
        binding.faqJumpEntrance.isVisible = batteryShouldSetting
        setSystemBatteryOptimizationStatus()
//        binding.faqJumpEntrance.setOnClickListener {
//            if (SystemBatteryOptimization.isShouldSetting(context)) {
//                TrackerMultiple.onEvent("FAQ", "Battery_PV")
//                //打开电池优化
//                SystemBatteryOptimization.startBatteryOptimization(context)
//                isReqBattery = true
//            }
//        }
        if (batteryShouldSetting) {
            binding.faqAnswer1.text = context.getString(R.string.music_stop_faq_ans1_gpt, context.getString(R.string.faq_text_app_name)).toSpannableString(context, R.color.colorAccent)
            binding.faqAnswer2.text = context.getString(R.string.music_stop_faq_ans2_gpt)
            binding.faqAnswer3.text = context.getString(R.string.music_stop_faq_ans3_gpt, context.getString(R.string.pause_detach)).toSpannableString(context, R.color.colorAccent)
        } else {
            binding.faqAnswer2.text = context.getString(R.string.music_stop_faq_ans4_gpt).toSpannableString(context, R.color.colorAccent)
            binding.faqAnswer3.text = context.getString(R.string.music_stop_faq_ans3_gpt, context.getString(R.string.pause_detach))
        }
//        setClickNoListener {
//            if (!SystemBatteryOptimization.isContainsIgnoreDevices()) {
//                if (SystemBatteryOptimization.isBatteryOptIgnored(context)) {
//                    //No_True_系统_机型 支持保活 且 开启了保活，点击 No，上报系统和机型
//                    TrackerMultiple.onEvent("FAQ", "No_True_${Build.VERSION.SDK_INT}_${Build.BRAND}_${Build.MODEL}")
//                }
//            } else {
//                // No_False_系统_机型 不支持保活的机型，点击 No，上报系统和机型
//                TrackerMultiple.onEvent("FAQ", "No_False_${Build.VERSION.SDK_INT}_${Build.BRAND}_${Build.MODEL}")
//            }
//        }
        bindChildViews(binding.root)
    }

    /**
     * 根据系统电池优化状态改变图标
     */
    private fun setSystemBatteryOptimizationStatus() {
//        if (SystemBatteryOptimization.isShouldSetting(context)) {
        val shouldSetting = false
        if (shouldSetting) {
            binding.batteryStatusYes.isVisible = false
            binding.batteryStatusNot.isVisible = true
            if (isReqBattery) {
                TrackerMultiple.onEvent("FAQ", "Battery_Deny")
            }
        } else {
            binding.batteryStatusYes.isVisible = true
            binding.batteryStatusNot.isVisible = false
            if (isReqBattery) {
                TrackerMultiple.onEvent("FAQ", "Battery_Allow")
            }
        }
        isReqBattery = false
    }

    /**
     * 在保活弹窗启动后，View会进入WindowFocusChanged状态，在此时检测保活是否开启
     * 如果开启，说明弹窗选择了Allow，保活弹窗入口的图标需要替换
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (batteryJob?.isActive == true) {
            batteryJob?.cancel()
        }
        batteryJob = (context as? AppCompatActivity)?.lifecycleScope?.launchWhenResumed {
            delay(100)//防止重复回调导致频繁触发
            setSystemBatteryOptimizationStatus()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        batteryJob?.cancel()
    }
}