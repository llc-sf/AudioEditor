package com.san.audioeditor.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.san.audioeditor.databinding.ActivityFaqBinding
import com.san.audioeditor.fragment.FAQFragment
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.framework.utils.TrackerMultiple
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class FAQActivity: BaseFragmentActivity() {

    companion object {
        const val KEY_FAQ_OPEN_FROM = "key_faq_open_from"
        const val OPEN_FROM_SETTINGS = "FAQ_Settings"
        const val OPEN_FROM_ASK_RATE = "FAQ_AskRate"
        const val OPEN_FROM_RATE = "FAQ_Rate"
        const val OPEN_FROM_INDEX_HOME = "FAQ_Index_Home"
        const val OPEN_FROM_AUDIO_CUT = "FAQ_Audio_Cut"
        const val OPEN_FROM_SAVE = "FAQ_AUDIO_SAVE"

        fun open(context: Context, openFrom: String) {
            context.startActivity(Intent(context, FAQActivity::class.java).apply {
                putExtra(KEY_FAQ_OPEN_FROM, openFrom)
            })
        }
    }

    val binding by lazy { ActivityFaqBinding.inflate(layoutInflater) }
    //  从主动问询、评分邀请弹窗进入的 FAQ 页面，顶部展示一段安抚文案
    private var shouldShowHead = false
    private var openFrom = OPEN_FROM_SETTINGS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getIntentExtras()
        initView()
    }

    override fun generateFragment() = FAQFragment()

    override fun getFragmentTag() = FAQFragment::class.java.simpleName ?: ""

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntentExtras()
    }

    override fun onResume() {
        super.onResume()
        TrackerMultiple.onPagerPv("FAQ")
        TrackerMultiple.onEvent("FAQ", openFrom)
    }

    private fun getIntentExtras() {
        openFrom = intent.getStringExtra(KEY_FAQ_OPEN_FROM) ?: OPEN_FROM_SETTINGS
        shouldShowHead = openFrom in mutableListOf(OPEN_FROM_ASK_RATE, OPEN_FROM_RATE)
    }

    private fun initView() {
        binding.toolbar.run {
            ImmerseDesign()
            setSupportActionBar(this)
        }
    }

    override fun isEnableContentViewTransform() = false
}