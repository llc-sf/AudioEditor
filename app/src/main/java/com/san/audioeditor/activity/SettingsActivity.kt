package com.san.audioeditor.activity

import android.graphics.Color
import android.os.Bundle
import com.san.audioeditor.databinding.ActivitySettingsBinding
import com.san.audioeditor.fragment.SettingsFragment
import dev.android.player.framework.utils.ImmerseDesign
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class SettingsActivity : BaseFragmentActivity() {

    private val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root.rootView)
        binding.toolbar.run {
            ImmerseDesign()
            setNavigationOnClickListener { finish() }
        }
        // TODO: 暂时设置颜色 调试使用
        binding.root.setBackgroundColor(Color.parseColor( "#FF101010"))
    }

    override fun generateFragment() = SettingsFragment().apply { arguments = intent.extras }

    override fun getFragmentTag() = SettingsFragment::class.java.simpleName ?: ""
}