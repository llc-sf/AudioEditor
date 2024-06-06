package com.san.audioeditor.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.san.audioeditor.databinding.ActivitySearchBinding
import com.san.audioeditor.fragment.SearchFragment
import com.san.audioeditor.viewmodel.AudioSearchViewModel
import dev.android.player.framework.utils.ImmerseDesign
import dev.android.player.framework.utils.KeyboardUtil
import musicplayer.playmusic.audioplayer.base.BaseFragmentActivity

class SearchActivity : BaseFragmentActivity() {

    companion object {
        const val KEY_FROM = "key_from"
        const val KEY_SONG_DIRECTORY_PATH = "key_song_directory_path"
        const val FROM_PICK = "from_pick"
        const val FROM_HOME = "from_home"

        fun open(context: Context, from: String, directoryPath: String) {
            context.startActivity(Intent(context, SearchActivity::class.java).apply {
                putExtra(KEY_FROM, from)
                putExtra(KEY_SONG_DIRECTORY_PATH, directoryPath)
            })
        }
    }

    private val viewBinding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private val searchModel by lazy {
        ViewModelProvider(this)[AudioSearchViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initView()
    }

    private fun initView() {
        viewBinding.toolbar.run {
            ImmerseDesign()
            setNavigationOnClickListener { finish() }
        }
        viewBinding.query.run {
            postDelayed({ KeyboardUtil.showKeyBoard(this) }, 500)
            doAfterTextChanged {
                doEditTextChange(it?.trim()?.toString() ?: "")
            }
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtil.hideKeyboardFrom(v)
                    false
                } else {
                    true
                }
            }
        }
        viewBinding.ivClear.setOnClickListener {
            KeyboardUtil.hideKeyboardFrom(viewBinding.query)
            viewBinding.query.setText("")
        }
        updateEditBar("")
    }

    private fun doEditTextChange(text: String) {
        updateEditBar(text)
        searchModel.sendKeyword(text)
    }

    private fun updateEditBar(keyword: String) {
        viewBinding.ivSearch.isVisible = keyword.isEmpty()
        viewBinding.ivClear.isVisible = keyword.isNotEmpty()
    }

    override fun generateFragment() = SearchFragment().apply { arguments = intent.extras }

    override fun getFragmentTag() = SearchFragment::class.java.simpleName ?: ""
}