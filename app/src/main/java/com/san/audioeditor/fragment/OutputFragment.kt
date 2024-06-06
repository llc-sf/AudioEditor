package com.san.audioeditor.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.toast.ToastCompat
import androidx.lifecycle.ViewModelProvider
import com.san.audioeditor.R
import com.san.audioeditor.activity.MutiManagerActivity
import com.san.audioeditor.activity.SettingsActivity
import com.san.audioeditor.cell.CellAudioItemView
import com.san.audioeditor.databinding.FragmentOutputBinding
import com.san.audioeditor.sort.AudioSortAdapter
import com.san.audioeditor.sort.SortSelectDialogFragment
import com.san.audioeditor.view.AudioItemView
import com.san.audioeditor.viewmodel.AudioOutputViewModel
import com.san.audioeditor.viewmodel.AudioPickViewModel
import dev.android.player.framework.base.BaseMVVMRefreshFragment
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter

class OutputFragment : BaseMVVMRefreshFragment<FragmentOutputBinding>() {


    private val mAdapter by lazy {
        MultiTypeFastScrollAdapter()
    }

    private lateinit var mViewModel: AudioOutputViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onRefresh() {
        super.onRefresh()
        mViewModel.onRefresh(requireContext())
    }

    override fun initViewBinding(inflater: LayoutInflater): FragmentOutputBinding {
        return FragmentOutputBinding.inflate(inflater)
    }

    override fun initViewModel() {
        mViewModel = ViewModelProvider(this, AudioOutputViewModel.AudioOutputViewFactory())[AudioOutputViewModel::class.java]
    }


    override fun initView() {
        super.initView()
        mActivity?.let {
            mViewModel.initData(it, arguments)
        }
        showRefresh()
        mAdapter.register(CellAudioItemView({
                                                mViewModel.playingPosition = it
                                                mAdapter.notifyDataSetChanged()
                                            }, {
                                                mViewModel.playingPosition
                                            }, source = AudioItemView.Source.SOURCE_OUTPUT))
        viewBinding.recycleview.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        viewBinding.multiply.setOnClickListener {
//            mActivity?.let {
//                it.startActivity(Intent(it, MutiManagerActivity::class.java))
//            }
        }
        viewBinding.sort.setOnClickListener {
            SortSelectDialogFragment.show(childFragmentManager, mViewModel.mCurrentSort, AudioSortAdapter()) {
                mViewModel.mCurrentSort = it
                mViewModel.onRefresh(requireContext())
            }
        }
    }

    override fun startObserve() {
        mViewModel.outputState.observe(viewLifecycleOwner) {
            if (it.songs?.isNotEmpty() == true) {
                mAdapter.items = it.songs!!
                mAdapter.notifyDataSetChanged()
                viewBinding.audioCount.text = "${it.songs?.size} audio"
                stopRefresh()
            } else {
                ToastCompat.makeText(requireContext(), false, "没有找到音频文件").show()
                stopRefresh()
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

}