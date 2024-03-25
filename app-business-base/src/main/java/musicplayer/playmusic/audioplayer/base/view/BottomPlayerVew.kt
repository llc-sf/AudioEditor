package musicplayer.playmusic.audioplayer.base.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.request.RequestOptions
import dev.android.player.business.service.MusicPlayer
import dev.android.player.business.service.PlaybackMonitor
import dev.android.player.business.service.Session
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.rx.RxUtils
import dev.android.player.framework.rx.add
import dev.android.player.framework.utils.CornerOutline
import dev.android.player.framework.utils.TrackerMultiple
import dev.android.player.framework.utils.dimenF
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.schedulers.Schedulers
import musicplayer.playmusic.audioplayer.base.BuildConfig
import musicplayer.playmusic.audioplayer.base.R
import musicplayer.playmusic.audioplayer.base.SlideTrackSwitcher
import musicplayer.playmusic.audioplayer.base.databinding.ViewBottomPlayerBinding
import musicplayer.playmusic.audioplayer.base.loader.GlideLoaderOptions
import musicplayer.playmusic.audioplayer.base.loader.load
import musicplayer.playmusic.audioplayer.themes.IApplyTheme

/**
 * 底部小播放器视图
 */
class BottomPlayerVew @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), IApplyTheme {

    private var mLocationAction: Runnable? = null//定位执行动作
    private var mIsQueue: Boolean

    private val mBinding: ViewBottomPlayerBinding

    private var mCurrentSong: Song? = null

    private var mProgress = 0


    /**
     *歌曲手势检测
     */
    private val mDetectorSwitcher by lazy {
        object : SlideTrackSwitcher() {
            override fun onClick() {
                if (Session.currentAudioId <= 0) return
                //跳转到播放页
                val intent = Intent("${BuildConfig.APPLICATION_ID}.action.NOW_PLAYING")
                intent.setPackage(BuildConfig.APPLICATION_ID)
                getContext().startActivity(intent)
                TrackerMultiple.onEvent("BottomPlayer", "GoFulllscreen_Click")
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                TrackerMultiple.onEvent("BottomPlayer", "Slide")
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                TrackerMultiple.onEvent("BottomPlayer", "Slide")
            }
        }
    }


    init {
        //获取自定义属性
        val array = context.obtainStyledAttributes(attrs, R.styleable.BottomPlayerVew)
        mIsQueue = array.getBoolean(R.styleable.BottomPlayerVew_bp_isqueue, false)
        array.recycle()

        clipChildren = false
        clipToPadding = false
        mBinding = ViewBottomPlayerBinding.inflate(LayoutInflater.from(getContext()), this)
        mBinding.container.CornerOutline(dimenF(R.dimen.dp_30))
        mBinding.play.setOnClickListener {
            if (Session.currentAudioId <= 0) return@setOnClickListener
            if (Session.playing) {
                TrackerMultiple.onEvent("BottomPlayer", "Pause")
                mBinding.play.setImageResource(R.drawable.ic_pause_bottom)
            } else {
                TrackerMultiple.onEvent("BottomPlayer", "Start")
                mBinding.play.setImageResource(R.drawable.ic_play_bottom)
            }
            RxUtils.completableOnSingle { MusicPlayer.playOrPause() }
        }

        if (mIsQueue) {//已经在队列中的，添加定位按钮
            mBinding.queue.setImageResource(R.drawable.ic_bottom_postion)
            //队列按钮
            mBinding.queue.setOnClickListener {
                mLocationAction?.run()
                TrackerMultiple.onEvent("BottomPlayer", "Located")
            }
        } else {
            mBinding.queue.setImageResource(R.drawable.ic_bottom_queue)
            //队列按钮
            mBinding.queue.setOnClickListener {
                //跳转到播放页
                val intent = Intent("${BuildConfig.APPLICATION_ID}.action.QUEUE")
                intent.setPackage(BuildConfig.APPLICATION_ID)
                getContext().startActivity(intent)
                TrackerMultiple.onEvent("BottomPlayer", "Queue")
            }
        }


        //更新歌曲播放状态
        Session.playStatePublisher.toFlowable(BackpressureStrategy.LATEST)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onUpdatePlayState()
                }, { it.printStackTrace() }).add(getContext())

        Session.songPublisher
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ song: Song ->
                    onUpdateSongInfo(song)
                    onUpdateMaxDuration(song.duration)
                    onLoaderAlbumArt(song)
                    onShowVisible(song)
                }) { e: Throwable -> e.printStackTrace() }.add(getContext())

        //播放位置更新
        PlaybackMonitor.getInstance().currentPositionObservable
                .subscribeOn(Schedulers.io())
                .subscribe({ pos: Long -> onUpdateProgress(pos.toInt()) }
                ) { it.printStackTrace() }.add(getContext())

        onUpdatePlayState()
        applyTheme()
    }

    private fun onShowVisible(song: Song) {
        if (song.id <= 0) {
            mBinding.root.visibility = View.GONE
        } else {
            mBinding.root.visibility = View.VISIBLE
        }
    }


    override fun onAttachedToWindow() {
        if (!mIsQueue) {
            mDetectorSwitcher.attach(this)
        }
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        if (!mIsQueue) {
            mDetectorSwitcher.detach()
        }
        super.onDetachedFromWindow()
    }


    /**
     * 更新歌曲播放状态按钮
     */
    private fun onUpdatePlayState() {
        mBinding.play.setImageResource(if (Session.playing) {
            R.drawable.ic_pause_bottom
        } else {
            R.drawable.ic_play_bottom
        })
    }


    /**
     * 更新歌曲的信息
     */
    private fun onUpdateSongInfo(song: Song) {
        mCurrentSong = song
        mBinding.title.setText(song.title)
//        mBinding.artist.setSelected(true)
//        mBinding.artist.setText(song.artistName)
    }

    /**
     * 更新歌曲时长
     */
    private fun onUpdateMaxDuration(duration: Int) {
        mBinding.progress.max = duration
        mBinding.progress.secondaryProgress = duration
        mBinding.progress.progress = mProgress + 1//这里手动+1 当设置为0时，进度条可能会占满整个播放进度。
    }

    /**
     *更新歌曲播放进度
     */
    private fun onUpdateProgress(progress: Int) {
        this.mProgress = progress
        mBinding.progress.progress = progress
    }


    /**
     * 更新歌曲封面
     */
    private fun onLoaderAlbumArt(song: Song) {
        val options = RequestOptions()
                .placeholder(GlideLoaderOptions.getDefaultBottomPlayerDefaultCoverDrawable())
                .error(GlideLoaderOptions.getDefaultBottomPlayerDefaultCoverDrawable())
                .dontAnimate()
        mBinding.cover.load(song, options)
    }

    fun onAttachLocationAction(action: Runnable?) {
        mLocationAction = action
    }

    override fun applyTheme() {

    }

}