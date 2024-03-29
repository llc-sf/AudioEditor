package com.san.audioeditor.activity

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.san.audioeditor.R
import dev.audio.ffmpeglib.listener.OnItemClickListener
import com.san.audioeditor.adapter.WaterfallAdapter
import com.san.audioeditor.handler.FFmpegHandler
import com.san.audioeditor.handler.FFmpegHandler.MSG_BEGIN
import com.san.audioeditor.handler.FFmpegHandler.MSG_FINISH
import com.san.audioeditor.handler.FFmpegHandler.MSG_INFO
import com.san.audioeditor.handler.FFmpegHandler.MSG_PROGRESS
import dev.audio.ffmpeglib.FFmpegApplication
import dev.audio.ffmpeglib.tool.FFmpegUtil
import dev.audio.ffmpeglib.tool.FFmpegUtil.fadeInOutAudio
import dev.audio.ffmpeglib.tool.FileUtil
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit


/**
 * Using ffmpeg command to handle audio
 * Created by frank on 2018/1/23.
 */

class AudioCutHandleActivity : BaseActivity() {
    //    private val appendFile = PATH + File.separator + "heart.m4a"
    private val appendFile = PATH + File.separator + "cutAudio.mp3"

    private var layoutAudioHandle: RecyclerView? = null
    private var layoutProgress: LinearLayout? = null
    private var txtProgress: TextView? = null
    private var currentPosition: Int = 0
    private var ffmpegHandler: FFmpegHandler? = null

    private val outputPath1 = PATH + File.separator + "output1.mp3"
    private val outputPath2 = PATH + File.separator + "output2.mp3"
    private var isJointing = false
    private var infoBuilder: StringBuilder? = null


    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_BEGIN -> {
                    layoutProgress!!.visibility = View.VISIBLE
                    layoutAudioHandle!!.visibility = View.GONE
                }

                MSG_FINISH -> {
                    layoutProgress!!.visibility = View.GONE
                    if (isJointing) {
                        isJointing = false
                        FileUtil.deleteFile(outputPath1)
                        FileUtil.deleteFile(outputPath2)
                    }
                    if (infoBuilder != null) {
                        Toast.makeText(
                            this@AudioCutHandleActivity,
                            infoBuilder.toString(), Toast.LENGTH_LONG
                        ).show()
                        infoBuilder = null
                    }
                    if (msg.obj is Int && msg.obj as Int == 0) {
                        findViewById<TextView>(R.id.path).text = "剪辑成功 路径: $outputPath"
//                        initCutPlayer()
                        initMediaPlayerCut()
                    } else {
                        findViewById<TextView>(R.id.path).text = "剪辑失败 路径: $outputPath"
                    }
                    if (!outputPath.isNullOrEmpty() && !this@AudioCutHandleActivity.isDestroyed) {
                        showToast("Save to:$outputPath")
                        outputPath = ""
                    }
                    // reset progress
                    txtProgress!!.text = String.format(Locale.getDefault(), "%d%%", 0)
                }

                MSG_PROGRESS -> {
                    findViewById<TextView>(R.id.path).text = "剪辑中"
                    val progress = msg.arg1
                    if (progress > 0) {
                        txtProgress!!.visibility = View.VISIBLE
                        txtProgress!!.text = String.format(Locale.getDefault(), "%d%%", progress)
                    } else {
                        txtProgress!!.visibility = View.GONE
                    }
                }

                MSG_INFO -> {
                    if (infoBuilder == null) infoBuilder = StringBuilder()
                    infoBuilder?.append(msg.obj)
                }

                else -> {
                }
            }
        }
    }

    override val layoutId: Int
        get() = R.layout.activity_audio_cut_handle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        ffmpegHandler = FFmpegHandler(mHandler)
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            PATH = cacheDir.absolutePath
//        }
    }

    private fun initCutPlayer() {
        if (!TextUtils.isEmpty(cutAudioOutPutPath)) {
            mediaPlayerCut.reset()
            mediaPlayerCut.setDataSource(cutAudioOutPutPath)
            mediaPlayerCut.prepare() // 可能需要一些时间来缓冲
        } else {
//            Toast.makeText(this, "请先剪辑音频", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initOriPlayer() {
        if (!TextUtils.isEmpty(oriPath)) {
            mediaPlayerOri.reset()
            mediaPlayerOri.setDataSource(oriPath)
            mediaPlayerOri.prepare() // 可能需要一些时间来缓冲
        } else {
//            Toast.makeText(this, "请先选择音频原文件", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        layoutProgress = getView(R.id.layout_progress)
        txtProgress = getView(R.id.txt_progress)
        val list = listOf(
//            getString(R.string.audio_transform),
            getString(R.string.audio_cut),
//            getString(R.string.audio_concat),
//            getString(R.string.audio_mix),
//            getString(R.string.audio_play),
//            getString(R.string.audio_speed),
//            getString(R.string.audio_echo),
//            getString(R.string.audio_tremolo),
//            getString(R.string.audio_denoise),
//            getString(R.string.audio_add_equalizer),
//            getString(R.string.audio_silence),
//            getString(R.string.audio_volume),
//            getString(R.string.audio_waveform),
//            getString(R.string.audio_encode),
//            getString(R.string.audio_surround),
//            getString(R.string.audio_reverb), "淡入淡出","多轨道"
        )

        layoutAudioHandle = findViewById(R.id.list_audio_item)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutAudioHandle?.layoutManager = layoutManager

        val adapter = WaterfallAdapter(list)
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                currentPosition = position
                selectFile()
            }
        })
        layoutAudioHandle?.adapter = adapter

        findViewById<View>(R.id.oriFile).setOnClickListener {
            currentPosition = 1
            selectFile()
        }


        findViewById<View>(R.id.btn_cut).setOnClickListener {
            if (TextUtils.isEmpty(oriPath)) {
                Toast.makeText(this, "请选择文件", Toast.LENGTH_SHORT).show()
            } else {
                doHandleAudio(oriPath!!)
            }

        }

        //监测EditText变化
        findViewById<EditText>(R.id.startTime).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    startTime = s.toString().toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        findViewById<EditText>(R.id.durationTime).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    durationTime = s.toString().toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        findViewById<EditText>(R.id.outName).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
//                startTime = s.toString().toFloat()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                startTime = s.toString().toFloat()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    outName = s.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        findViewById<View>(R.id.btn_play_both).setOnClickListener {
            try {
                mediaPlayerCut.start()
                mediaPlayerOri.start()

                findViewById<TextView>(R.id.pauseOri).text = "暂停播放"
                findViewById<TextView>(R.id.pauseCut).text = "暂停播放"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        findViewById<View>(R.id.pauseOri).setOnClickListener {
            if (mediaPlayerOri.isPlaying) {
                mediaPlayerOri.pause()
                findViewById<TextView>(R.id.pauseOri).text = "继续播放"
                setStartTime()

            } else {
                mediaPlayerOri.start()
                findViewById<TextView>(R.id.pauseOri).text = "暂停"
            }
        }


        findViewById<View>(R.id.pauseCut).setOnClickListener {
            if (mediaPlayerCut.isPlaying) {
                mediaPlayerCut.pause()
                findViewById<TextView>(R.id.pauseCut).text = "继续播放"
            } else {
                mediaPlayerCut.start()
                findViewById<TextView>(R.id.pauseCut).text = "暂停"
            }
        }


        findViewById<View>(R.id.playOri).setOnClickListener {
            initOriPlayer()
            mediaPlayerOri.start() // 开始播放
        }


        findViewById<View>(R.id.play).setOnClickListener {
            initCutPlayer()
            mediaPlayerCut.start() // 开始播放
        }
    }

    private fun initMediaPlayerCut() {
        findViewById<TextView>(R.id.pauseCut).text = "暂停"
        if (TextUtils.isEmpty(cutAudioOutPutPath)) {
            Toast.makeText(this, "请先剪辑", Toast.LENGTH_SHORT).show()
            return
        }

        //懒加载 mediaPlayer
        mediaPlayerCut.reset()

        try {
            mediaPlayerCut.setDataSource(cutAudioOutPutPath)
            mediaPlayerCut.prepare() // 可能需要一些时间来缓冲



            // 格式化并设置总时间
            val totalTime = String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(mediaPlayerCut.duration.toLong()),
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayerCut.duration.toLong()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mediaPlayerCut.duration.toLong())),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayerCut.duration.toLong()) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                mediaPlayerCut.duration.toLong()
                            )
                        )
            )
            totalTimeTextView.text = totalTime
            currentTimeTextView.text = "00:00:00"


            seekBar.max = mediaPlayerCut.duration
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayerCut.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })


            mHandler?.postDelayed(object : Runnable {
                override fun run() {
                    val currentProgress = mediaPlayerCut.currentPosition
                    val seconds = currentProgress / 1000
                    val milliseconds = currentProgress % 1000
                    val currentTime = "$seconds.$milliseconds"
                    findViewById<TextView>(R.id.currentCut).text = currentTime
                    mHandler?.postDelayed(this, 50)
                }

            }, 50)

            // 定期更新进度条的进度
            mHandler?.post(object : Runnable {
                override fun run() {
                    val currentProgress = mediaPlayerCut.currentPosition
                    seekBar.progress = currentProgress


                    // 格式化并设置当前时间
                    val currentTime = String.format(
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(currentProgress.toLong()),
                        TimeUnit.MILLISECONDS.toMinutes(currentProgress.toLong()) -
                                TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(
                                        currentProgress.toLong()
                                    )
                                ),
                        TimeUnit.MILLISECONDS.toSeconds(currentProgress.toLong()) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        currentProgress.toLong()
                                    )
                                )
                    )
                    currentTimeTextView.text = currentTime



                    mHandler?.postDelayed(this, 200)
                }
            })


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initMediaPlayerOri() {
        findViewById<TextView>(R.id.pauseOri).text = "暂停"
        if (TextUtils.isEmpty(oriPath)) {
            Toast.makeText(this, "请先选择源文件", Toast.LENGTH_SHORT).show()
            return
        }

        //懒加载 mediaPlayer
        mediaPlayerOri.reset()

        try {
            mediaPlayerOri.setDataSource(oriPath)
            mediaPlayerOri.prepare() // 可能需要一些时间来缓冲

            // 格式化并设置总时间
            val totalTime = String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(mediaPlayerOri.duration.toLong()),
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayerOri.duration.toLong()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mediaPlayerOri.duration.toLong())),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayerOri.duration.toLong()) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                mediaPlayerOri.duration.toLong()
                            )
                        )
            )
            totalTimeOriTextView.text = totalTime
            currentTimeOriTextView.text = "00:00:00"


            seekBarOri.max = mediaPlayerOri.duration
            seekBarOri.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayerOri.seekTo(progress)
                    }
                    setStartTime()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })


            mHandler?.postDelayed(object : Runnable {
                override fun run() {
                    val currentProgress = mediaPlayerOri.currentPosition
                    val seconds = currentProgress / 1000
                    val milliseconds = currentProgress % 1000
                    val currentTime = "$seconds.$milliseconds"
                    findViewById<TextView>(R.id.currentOri).text = currentTime
                    mHandler?.postDelayed(this, 5)
                }

            }, 5)

            // 定期更新进度条的进度
            mHandler?.post(object : Runnable {
                override fun run() {
                    val currentProgress = mediaPlayerOri.currentPosition
                    seekBarOri.progress = currentProgress


                    // 格式化并设置当前时间
                    val currentTime = String.format(
                        "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(currentProgress.toLong()),
                        TimeUnit.MILLISECONDS.toMinutes(currentProgress.toLong()) -
                                TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(
                                        currentProgress.toLong()
                                    )
                                ),
                        TimeUnit.MILLISECONDS.toSeconds(currentProgress.toLong()) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        currentProgress.toLong()
                                    )
                                )
                    )
                    currentTimeOriTextView.text = currentTime



                    mHandler?.postDelayed(this, 200)
                }
            })


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setStartTime() {
        if (!mediaPlayerOri.isPlaying) {
            val currentProgress = mediaPlayerOri.currentPosition
            val seconds = currentProgress / 1000
            val milliseconds = currentProgress % 1000
            val currentTime = "$seconds.$milliseconds"
            findViewById<EditText>(R.id.startTime).setText(currentTime)
            startTime = currentTime.toFloat()
        }
    }


    val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }
    val seekBarOri: SeekBar by lazy {
        findViewById(R.id.seekBarOri)
    }
    val currentTimeTextView: TextView by lazy {
        findViewById(R.id.currentTime)
    }
    val totalTimeTextView: TextView by lazy {
        findViewById(R.id.totalTime)
    }
    val currentTimeOriTextView: TextView by lazy {
        findViewById(R.id.currentTimeOri)
    }
    val totalTimeOriTextView: TextView by lazy {
        findViewById(R.id.totalTimeOri)
    }
    val mediaPlayerCut: MediaPlayer by lazy {
        MediaPlayer()
    }
    val mediaPlayerOri: MediaPlayer by lazy {
        MediaPlayer()
    }

    var startTime: Float = 0f
    var durationTime: Float = 0f
    var outName: String? = null

    override fun onViewClick(view: View) {

    }

    var oriPath: String? = null
    override fun onSelectedFile(filePath: String) {
        oriPath = filePath
        findViewById<TextView>(R.id.oriPath).text = "源文件路径：$filePath"
//        initOriPlayer()
        initMediaPlayerOri()
//        doHandleAudio(filePath)
    }

    /**
     * Using ffmpeg cmd to handle audio
     *
     * @param srcFile srcFile
     */
    private fun doHandleAudio(srcFile: String) {
        var commandLine: Array<String>? = null
        if (!FileUtil.checkFileExist(srcFile)) {
            return
        }
        if (!FileUtil.isAudio(srcFile)) {
            showToast(getString(R.string.wrong_audio_format))
            return
        }
        when (currentPosition) {
            0 -> if (useFFmpeg) { //use FFmpeg to transform
                outputPath = PATH + File.separator + "transformAudio.mp3"
                commandLine = FFmpegUtil.transformAudio(srcFile, outputPath)
            } else { //use MediaCodec and libmp3lame to transform
                Thread {
                    outputPath = PATH + File.separator + "transformAudio.mp3"
                    try {
                        mHandler.obtainMessage(MSG_BEGIN).sendToTarget()
                        val clazz = Class.forName("com.frank.mp3.Mp3Converter")
                        val instance = clazz.newInstance()
                        val method = clazz.getDeclaredMethod(
                            "convertToMp3",
                            String::class.java,
                            String::class.java
                        )
                        method.invoke(instance, srcFile, outputPath)
                        mHandler.obtainMessage(MSG_FINISH).sendToTarget()
                    } catch (e: Exception) {
                        Log.e("AudioHandleActivity", "convert mp3 error=" + e.message)
                    }
                }.start()
            }

            1 -> { //cut audio, it's best not include special characters
                val suffix = FileUtil.getFileSuffix(srcFile)
                if (suffix == null || suffix.isEmpty()) {
                    return
                }

                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(srcFile)
                val durationStr =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val totalDuration = (durationStr?.toFloat() ?: 0F) / 1000 // 转换为秒

                if (startTime < 0) {
                    Toast.makeText(this, "开始时间不能小于0", Toast.LENGTH_SHORT).show()
                    return
                }
                if (startTime >= totalDuration) {
                    Toast.makeText(this, "开始时间不能大于歌曲总时长", Toast.LENGTH_SHORT).show()
                    return
                }
                if (startTime + durationTime > totalDuration) {
                    Toast.makeText(this, "开始时间+截取时长不能大于歌曲总时长", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                if (durationTime == 0f) {
                    Toast.makeText(this, "截取时长不能为0", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                outputPath =
                    PATH + File.separator + "${if (outName?.isNotEmpty() == true) outName else "cutAudio_${startTime.toInt()}_${durationTime.toInt()}"}" + suffix
                cutAudioOutPutPath = outputPath
                commandLine = FFmpegUtil.cutAudio(srcFile, startTime, durationTime, outputPath)
            }

            16 -> { //cut audio, it's best not include special characters
                val suffix = FileUtil.getFileSuffix(srcFile)
                if (suffix == null || suffix.isEmpty()) {
                    return
                }
                val fadeInTime = 5.0f // 淡入时间，单位为秒

                val fadeOutTime = 5.0f // 淡出时间，单位为秒


                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(srcFile)
                val durationStr =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val totalDuration = (durationStr?.toFloat() ?: 0F) / 1000 // 转换为秒


                outputPath =
                    PATH + File.separator + "jianbianAudio_${System.currentTimeMillis()}" + suffix

                commandLine =
                    fadeInOutAudio(srcFile, fadeInTime, fadeOutTime, totalDuration, outputPath)

            }

            17 -> {
                //cut audio, it's best not include special characters
                var srcFiles = mutableListOf<String>().apply {
                    add("/storage/emulated/0/Android/data/com.san.audioeditor/files/Music/1.mp3")
                    add("/storage/emulated/0/Android/data/com.san.audioeditor/files/Music/2.mp3")
                }
                val suffix = FileUtil.getFileSuffix(srcFiles.get(0));
                if (suffix == null || suffix.isEmpty()) {
                    return;
                }
                outputPath =
                    PATH + File.separator + "mutiAudio_${System.currentTimeMillis()}" + suffix;
                commandLine = FFmpegUtil.mutiAudio(srcFiles, 5.5f, 15.0f, outputPath);
            }


            2 -> { //concat audio
                if (!FileUtil.checkFileExist(appendFile)) {
                    return
                }
                concatAudio(srcFile)
                return
            }

            3 -> { //mix audio
                if (!FileUtil.checkFileExist(appendFile)) {
                    return
                }
                val mixSuffix = FileUtil.getFileSuffix(srcFile)
                if (mixSuffix == null || mixSuffix.isEmpty()) {
                    return
                }
                commandLine = if (mixAudio) {
                    outputPath = PATH + File.separator + "mix" + mixSuffix
                    FFmpegUtil.mixAudio(srcFile, appendFile, outputPath)
                } else {
                    outputPath = PATH + File.separator + "merge" + mixSuffix
                    FFmpegUtil.mergeAudio(srcFile, appendFile, outputPath)
                }
            }

            4 -> { //use AudioTrack to play audio
//                val audioIntent = Intent(this@AudioHandleActivity, AudioPlayActivity::class.java)
//                audioIntent.data = Uri.parse(srcFile)
//                startActivity(audioIntent)
                return
            }

            5 -> { //change audio speed
                val speed = 2.0f // funny effect, range from 0.5 to 100.0
                outputPath = PATH + File.separator + "speed.mp3"
                commandLine = FFmpegUtil.changeAudioSpeed(srcFile, outputPath, speed)
            }

            6 -> { //echo effect
                val echo = 1000 // echo effect, range from 0 to 90000
                outputPath = PATH + File.separator + "echo.mp3"
                commandLine = FFmpegUtil.audioEcho(srcFile, echo, outputPath)
            }

            7 -> { //tremolo effect
                val frequency = 5 // range from 0.1 to 20000.0
                val depth = 0.9f // range from 0 to 1
                outputPath = PATH + File.separator + "tremolo.mp3"
                commandLine = FFmpegUtil.audioTremolo(srcFile, frequency, depth, outputPath)
            }

            8 -> { //audio denoise
                outputPath = PATH + File.separator + "denoise.mp3"
                commandLine = FFmpegUtil.audioDenoise(srcFile, outputPath)
            }

            9 -> { // equalizer plus
                // key:band  value:gain=[0-20]
                val bandList = arrayListOf<String>()
                bandList.add("6b=5")
                bandList.add("8b=4")
                bandList.add("10b=3")
                bandList.add("12b=2")
                bandList.add("14b=1")
                bandList.add("16b=0")
                outputPath = PATH + File.separator + "equalize.mp3"
                commandLine = FFmpegUtil.audioEqualizer(srcFile, bandList, outputPath)
            }

            10 -> { //silence detect
                commandLine = FFmpegUtil.audioSilenceDetect(srcFile)
            }

            11 -> { // modify volume
                val volume = 0.5f // 0.0-1.0
                outputPath = PATH + File.separator + "volume.mp3"
                commandLine = FFmpegUtil.audioVolume(srcFile, volume, outputPath)
            }

            12 -> { // audio waveform
                outputPath = PATH + File.separator + "waveform.png"
                val resolution = "1280x720"
                commandLine = FFmpegUtil.showAudioWaveform(srcFile, resolution, 0, outputPath)
            }

            13 -> { //audio encode
                val pcmFile = PATH + File.separator + "raw.pcm"
                outputPath = PATH + File.separator + "convert.mp3"
                //sample rate, normal is 8000/16000/44100
                val sampleRate = 44100
                //channel num of pcm
                val channel = 2
                commandLine = FFmpegUtil.encodeAudio(pcmFile, outputPath, sampleRate, channel)
            }

            14 -> { // change to surround sound
                outputPath = PATH + File.separator + "surround.mp3"
                commandLine = FFmpegUtil.audioSurround(srcFile, outputPath)
            }

            15 -> {
                outputPath = PATH + File.separator + "reverb.mp3"
                commandLine = FFmpegUtil.audioReverb(srcFile, outputPath)
            }

            else -> {
            }
        }
        Log.i("llc_fuck", "outputPath=$outputPath")
        //打印 commandLine
        var sb = StringBuilder()
        commandLine?.forEachIndexed { index, s ->
            sb.append("$s ")
            Log.i("llc_fuck", "s=$s")
        }
        Log.i("llc_fuck", "sb=$sb")



        if (ffmpegHandler != null && commandLine != null) {
            ffmpegHandler!!.executeFFmpegCmd(commandLine)
        }
    }

    private fun concatAudio(selectedPath: String) {
        if (ffmpegHandler == null || selectedPath.isEmpty() || appendFile.isEmpty()) {
            return
        }
        isJointing = true
        val targetPath = PATH + File.separator + "concatAudio.mp3"
        val transformCmd1 = FFmpegUtil.transformAudio(selectedPath, "libmp3lame", outputPath1)
        val transformCmd2 = FFmpegUtil.transformAudio(appendFile, "libmp3lame", outputPath2)
        val fileList = ArrayList<String>()
        fileList.add(outputPath1)
        fileList.add(outputPath2)
        val jointVideoCmd = FFmpegUtil.concatAudio(fileList, targetPath)
        val commandList = ArrayList<Array<String>>()
        commandList.add(transformCmd1)
        commandList.add(transformCmd2)
        commandList.add(jointVideoCmd)
        ffmpegHandler!!.executeFFmpegCmds(commandList)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        mediaPlayerCut.stop()
        mediaPlayerCut.release()
        mediaPlayerOri.stop()
        mediaPlayerOri.release()
    }

    companion object {

        private val PATH =
            FFmpegApplication.instance?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
                ?: ""
//        private val PATH = Environment.getExternalStorageDirectory().path

        private const val useFFmpeg = true

        private const val mixAudio = true

        private var outputPath: String? = null
        private var cutAudioOutPutPath: String? = null

        init {
            System.loadLibrary("media-handle")
        }
    }


}
