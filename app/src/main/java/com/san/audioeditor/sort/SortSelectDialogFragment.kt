package com.san.audioeditor.sort

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogSortSelectionBinding
import dev.android.player.app.business.SortBusiness
import dev.android.player.app.business.data.OrderType
import dev.android.player.app.business.data.SortStatus
import dev.android.player.framework.data.model.Album
import dev.android.player.framework.data.model.Artist
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.TrackerMultiple
import dev.android.player.framework.utils.dimen
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by lisao on 3/4/21
 * 排序选择
 */
class SortSelectDialogFragment : BaseBottomTranslucentDialog() {


    companion object {
        private const val TAG = "SortSelectFragment"

        private const val EXTRA_ARG_SELECTED = "EXTRA_ARG_SELECTED"

        @JvmStatic
        @JvmOverloads
        fun show(manager: FragmentManager,
                 action: SortStatus? = null, //当前选择的排序依据
                 adapter: ISortAdapter? = null,//排序的选项列表
                 callback: ISortSelectionCallback? = null//排序完成后回调
        ) {
            try {
                val fragment = SortSelectDialogFragment()
                fragment.apply {
                    arguments = bundleOf(EXTRA_ARG_SELECTED to action)
                }
                fragment.mSelectionCallback = callback
                fragment.mAdapter = adapter
                BottomDialogManager.show(manager, fragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


    //是否是反向选项
    private var isReverse = false

    /**
     * 排序选择
     */
    private val mDefaultStatus: SortStatus = SortStatus("NONE", OrderType.ASC)

    /**
     * 点击排序后回调
     */
    private var mSelectionCallback: ISortSelectionCallback? = null

    /**
     * 排序选项列表
     */
    private var mAdapter: ISortAdapter? = null

    private val mArgStatus by lazy {
        arguments?.getParcelable(EXTRA_ARG_SELECTED) as SortStatus?
    }


    private var _binding: DialogSortSelectionBinding? = null
    private val binding get() = _binding!!
    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSortSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mArgStatus?.let {
            mDefaultStatus.key = it.key
            mDefaultStatus.order = it.order
        }

        //设置分割Space
        val divider = GradientDrawable().apply {
            setSize(0, dimen(R.dimen.dp_20))
        }
        binding.sortBy.let {
            it.dividerDrawable = divider
            it.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        }
        binding.sortOrder.let {
            it.dividerDrawable = divider
            it.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        }

        initSortData(view)
        initSortOrder(view)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        //恢复播放主题适配
        binding.btnOk.setOnClickListener {
            dismiss()
            //默认的不管
            if (mArgStatus?.key != mDefaultStatus.key || mArgStatus?.order != mDefaultStatus.order) {
                onTrackEvent(mDefaultStatus)//统计埋点
                mSelectionCallback?.onSortSelection(mDefaultStatus)
                GlobalScope.launch(Dispatchers.IO) {
                    SortBusiness.setAllSongsSortStatusSync(mDefaultStatus)
                }
            }
        }

    }


    /**
     * 初始化排序数据
     */
    private fun initSortData(view: View) {
        //设置排序数据
        val adapter = mAdapter ?: EmptyAdapter()
        var flag = false//标记是否有选中的排序标记
        binding.sortBy.apply {
            removeAllViews()
            adapter.getSortItems().forEach { it ->
                val radio = onCreateRadioButton(context, it, onKeySelected)
                addView(radio)
                //标记是否有默认选中
                if (it.getKey() == mDefaultStatus.key) {
                    radio.isSelected = true
                    flag = true
                }
            }
        }
        //如果没有一个选中，那么排序选项隐藏掉
        if (!flag) {
            binding.sortOrder.visibility = View.GONE
            binding.divider.visibility = View.GONE
            setBtnEnable(false, binding.btnOk)
        }
    }

    /**
     * 初始化排序Order
     */
    private fun initSortOrder(view: View) {
        //排序规则
        binding.sortOrderAsc.setSelectedListener {
            mDefaultStatus.order = getOrderCompat(OrderType.ASC)
            binding.sortOrderDesc.isSelected = false
        }
        binding.sortOrderDesc.setSelectedListener {
            mDefaultStatus.order = getOrderCompat(OrderType.DESC)
            binding.sortOrderAsc.isSelected = false
        }
        setSortOrderData(getOrderCompat(mDefaultStatus.order))
    }

    /**
     * 设置排序数据
     */
    private fun setSortOrderData(@OrderType order: Int) {
        binding.sortOrderAsc.isSelected = OrderType.ASC == order
        binding.sortOrderDesc.isSelected = OrderType.DESC == order
        mDefaultStatus.order = getOrderCompat(order)
    }

    /**
     * 设置按钮是否可以点击
     */
    private fun setBtnEnable(isEnable: Boolean, view: View?) {
        view?.isEnabled = isEnable
        if (isEnable) {
            view?.alpha = 1f
        } else {
            view?.alpha = 0.4f
        }
    }

    /**
     * 排序选项选择
     */
    private val onKeySelected = fun(view: SortSelectionItemView, sort: SortType) {
        try {
            //如果有选中的，清除其他的选中状态
            binding.sortBy.children.forEach {
                if (it != view) {
                    it.isSelected = false
                }
            }
            //如果排序发生变化就重置默认排序顺序
            isReverse = sort.isReverse()
            if (mDefaultStatus.key != sort.getKey()) {
                mDefaultStatus.key = sort.getKey()
                setSortOrderData(OrderType.ASC)//默认按照升序排序
            }
            setBtnEnable(true, binding.btnOk)//设置按钮是否可用
            binding.sortOrder.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
            binding.sortOrderAsc.setTitle(sort.getOrderOptionASC(requireContext()))
            binding.sortOrderDesc.setTitle(sort.getOrderOptionDESC(requireContext()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 返回选择的兼容处理
     * 如果选择的type为反向操作，那么升序就是降序，降序就是升序。UI页面上显示也不一样
     */
    private fun getOrderCompat(origin: Int): Int {
        return if (isReverse) {//判断
            return origin * -1
        } else {
            origin
        }
    }

    /**
     * 创建一个排序选项
     */
    private fun onCreateRadioButton(context: Context, sort: SortType, onSelected: (view: SortSelectionItemView, sort: SortType) -> Unit): SortSelectionItemView {
        return SortSelectionItemView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setTitle(sort.getTitle(context))
            setSelectedListener {
                onSelected(this, sort)
            }
        }
    }

    /**
     * 歌曲排序埋点
     */
    private fun onTrackEvent(status: SortStatus) {
        var mPrefix = ""
        var mType = ""
        var mOrder = ""
        when (mAdapter) {
            is SongSortAdapter, is AlbumSongSortAdapter, is ArtistSongSortAdapter, is PlayListSongSortAdapter -> {
                mPrefix = if (mAdapter is SongSortAdapter)
                    "SortSongs"
                else if (mAdapter is AlbumSongSortAdapter)
                    "SortAlbumDetails"
                else if (mAdapter is ArtistSongSortAdapter)
                    "SortArtistDetails"
                else if (mAdapter is PlayListSongSortAdapter)
                    "SortCPlaylistDetails"
                else
                    "Unknown"
                mType = when (status.key) {
                    Song.TITLE -> "Name"//按照歌曲名称排序
                    Song.ARTIST_NAME -> "Artist"//按照歌手排序
                    Song.ALBUM_NAME -> "Album"//按照专辑排序
                    Song.TIME_ADD -> "Time"//按照添加时间排序
                    Song.DURATION -> "Duration"//按照时长排序
                    Song.SIZE -> "Size"//按照大小排序
                    Song.TIME_ADD_PLAYLIST -> "PlaylistTime"//按照播放列表添加时间排序
                    Song.TRACK -> "TrackOrder"//按照歌曲序号顺序排序
                    Song.ORDER -> "Order"//按照播放列表顺序排序
                    else -> "Unknown"
                }
            }

            is ArtistSortAdapter -> {
                mPrefix = "SortArtists"
                mType = when (status.key) {
                    Artist.NAME -> "Artist"
                    Artist.NUMBER_OF_ALBUMS -> "NumberAlbums"
                    Artist.NUMBER_OF_SONGS -> "NumberSongs"
                    else -> "Unknown"
                }
            }

            is AlbumSortAdapter -> {
                mPrefix = "SortAlbums"
                mType = when (status.key) {
                    Album.TITLE -> "Album"
                    Album.ARTIST -> "Artist"
                    else -> "Unknown"
                }
            }
        }
        mOrder = when (status.order) {
            OrderType.ASC -> "ASC"
            OrderType.DESC -> "DESC"
            else -> "Unknown"
        }
        TrackerMultiple.onEvent("Sort_Songs", "${mPrefix}_${mType}_${mOrder}")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * 排序后回调接口
     */
    fun interface ISortSelectionCallback {
        /**
         * @param status 选择的排序内容
         *
         */
        fun onSortSelection(status: SortStatus)
    }

}
