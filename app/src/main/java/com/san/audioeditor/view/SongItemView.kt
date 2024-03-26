package com.san.audioeditor.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Pair
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.san.audioeditor.databinding.ItemSongViewBinding
import com.san.audioeditor.fragment.MediaPickFragment
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.StringsUtils
import java.io.Serializable

/**
 * 歌曲Item
 */
class SongItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private val mBinding =
        ItemSongViewBinding.inflate(LayoutInflater.from(getContext()), this, true)

    private var mCurrentSong: Song? = null

    private var isShowTrackNumber: Boolean = false

    private var isPlaying: Pair<Long, Boolean>? = null

    private var mFromSource: Serializable? = null


    init {

        onCreateDisposeIfNeed()
        mBinding.more.setOnClickListener {


        }
    }

    private fun onCreateDisposeIfNeed() {
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    /**
     * 设置是否显示拖动排序图标
     * 播放列表里可用
     */
    fun setIsCanDrag(isCanDrag: Boolean) {
        mBinding.drag.isVisible = isCanDrag
    }

    /**
     * 设置拖动按钮的Touch事件
     */
    fun setDragTouchListener(listener: OnTouchListener) {
        mBinding.drag.setOnTouchListener(listener)
    }

    /**
     * 设置是否显示TrackNumber 数字
     */
    fun setShowTrackNumber(isShow: Boolean) {
        isShowTrackNumber = isShow
    }

    @JvmOverloads
    fun setData(song: Song, key: String? = null) {
        try {
            this.mCurrentSong = song
            mBinding.title.text = song.title
            mBinding.description.text = song.artistName

            mBinding.track.isInvisible = !isShowTrackNumber
            mBinding.cover.isVisible = true
//            mBinding.cover.load(song)
            setShowExtensionInfo(song, key)
            mBinding.root.setOnClickListener {
                (mBinding.root.context as? Activity)?.setResult(Activity.RESULT_OK, Intent().apply {
                    putExtras(Bundle().apply {
                        putParcelable(MediaPickFragment.PARAM_SONG, song)
                    })
                })
                (mBinding.root.context as? Activity)?.finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 更新播放状态
     */

    fun setFromSource(source: Serializable?) {
        this.mFromSource = source
    }

    private fun setShowExtensionInfo(song: Song, key: String?) {
        when (key) {
            Song.DURATION -> {
                mBinding.extentsInfo.visibility = VISIBLE
                //歌曲时长
                mBinding.extentsInfo.text = DateUtil.formatTime((song.duration).toLong())
            }

            Song.SIZE -> {
                mBinding.extentsInfo.visibility = VISIBLE
                //文件大小
                mBinding.extentsInfo.text = FileUtils.getFileSize(song.size)
            }

            Song.TIME_ADD -> {
                mBinding.extentsInfo.visibility = VISIBLE
                //添加时间
                mBinding.extentsInfo.text = DateUtil.makeDateString(song.dateAdded)
            }

            Song.TIME_ADD_PLAYLIST -> {
                mBinding.extentsInfo.visibility = VISIBLE
                //添加到播放列表时间
                mBinding.extentsInfo.text =
                    DateUtil.makeDateString(song.add_time / 1000)//这里数据库保存的是毫秒，需要转换为秒
            }

            Song.ORDER, Song.ARTIST_NAME, Song.TITLE, Song.ALBUM_NAME -> {
                mBinding.extentsInfo.visibility = GONE
            }

            Song.COUNT -> {   //播放次数
                mBinding.count.visibility = VISIBLE
                mBinding.count.text = StringsUtils.formatNumber(song.count)
            }

            else -> {
                mBinding.count.visibility = GONE
                mBinding.extentsInfo.visibility = GONE
            }
        }
    }

}