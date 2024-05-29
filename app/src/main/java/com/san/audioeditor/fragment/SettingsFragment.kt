package com.san.audioeditor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.toast.ToastCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.audioeditor.BuildConfig
import com.san.audioeditor.R
import com.san.audioeditor.cell.CellSettingsCategoryItemView
import com.san.audioeditor.cell.CellSettingsItemView
import com.san.audioeditor.cell.CellSettingsVersionItemView
import com.san.audioeditor.databinding.FragmentSettingsBinding
import com.san.audioeditor.view.SettingsCommonItemView
import com.san.audioeditor.view.SettingsVersionItemView
import dev.android.player.framework.base.BaseFragment
import dev.android.player.framework.base.viewBinding
import dev.android.player.framework.data.model.SettingsCategoryItemData
import dev.android.player.framework.data.model.SettingsCommonItemData
import dev.android.player.framework.data.model.SettingsVersionItemData
import dev.android.player.framework.utils.LogUtils
import dev.android.player.framework.utils.dimen
import dev.android.player.widget.RecyclerViewItemDecorationHelperV3
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter

class SettingsFragment : BaseFragment() {

    private val binding by viewBinding(FragmentSettingsBinding::inflate)
    private val adapter by lazy { MultiTypeFastScrollAdapter() }
    private val items by lazy { configItemsData() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSettings.run {
            adapter = this@SettingsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
            RecyclerViewItemDecorationHelperV3.SpaceDividerBuilder.create()
                .setSpace(dimen(R.dimen.dp_2))
                .setBorder(0, dimen(R.dimen.dp_28), 0, 0)
                .add(this)
        }
    }

    override fun onLazyLoad() {
        super.onLazyLoad()
        setItems()
    }

    private fun setItems() {
        registerItemBinders()
        adapter.items = items
    }

    /**
     * 注册 adapter binder 类型
     */
    private fun registerItemBinders() {
        adapter.register(CellSettingsItemView().apply {
            itemClick = this@SettingsFragment::itemClick
        })
        adapter.register(CellSettingsVersionItemView().apply {
            itemClick = this@SettingsFragment::itemClick
        })
        adapter.register(CellSettingsCategoryItemView())
    }

    /**
     * 配置 列表数据
     */
    private fun configItemsData() = mutableListOf(
        SettingsCommonItemData(R.drawable.ic_settings_storage_path, getString(R.string.settings_save_path), "/storage/emulated/0/Music/品牌名/"),
        SettingsCategoryItemData(getString(R.string.help)),
        SettingsCommonItemData(R.drawable.ic_settings_help, getString(R.string.settings_faq), ""),
        SettingsCommonItemData(R.drawable.ic_settings_policy, getString(R.string.privacy_policy), ""),
        SettingsCommonItemData(R.drawable.ic_settings_terms, getString(R.string.terms_of_service), ""),
        SettingsVersionItemData("${getString(R.string.version)}: ${BuildConfig.VERSION_NAME} ${BuildConfig.VERSION_CODE}")
    )

    private fun itemClick(view: View, date: Any?) {
        when (view) {
            is SettingsCommonItemView -> {
                if (date == null || date !is SettingsCommonItemData) return
                when (date.icon) {
                    R.drawable.ic_settings_storage_path -> {
                        ToastCompat.makeText(context, true, getString(R.string.settings_save_path)).show()
                        LogUtils.getInstance(context).log("settings_save_path====>")
                    }
                    R.drawable.ic_settings_help -> {
                        ToastCompat.makeText(context, true, getString(R.string.settings_faq)).show()
                        LogUtils.getInstance(context).log("settings_faq====>")
                    }
                    R.drawable.ic_settings_policy -> {
                        ToastCompat.makeText(context, true, getString(R.string.privacy_policy)).show()
                        LogUtils.getInstance(context).log("privacy_policy====>")
                    }
                    R.drawable.ic_settings_terms -> {
                        ToastCompat.makeText(context, true, getString(R.string.terms_of_service)).show()
                        LogUtils.getInstance(context).log("terms_of_service====>")
                    }
                }
            }
            is SettingsVersionItemView -> {
                val versionData = date as? SettingsVersionItemData ?: return
                ToastCompat.makeText(context, true, getString(R.string.version)).show()
                LogUtils.getInstance(context).log("version====>${versionData.version}")
            }
        }
    }
}