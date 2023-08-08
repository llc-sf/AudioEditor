package com.san.audioeditor

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.san.audioeditor.adapter.WaterfallAdapter
import com.frank.ffmpeg.listener.OnItemClickListener
import com.san.audioeditor.handler.FFmpegHandler
import com.san.audioeditor.handler.FFmpegHandler.MSG_BEGIN
import com.san.audioeditor.handler.FFmpegHandler.MSG_FINISH
import com.san.audioeditor.handler.FFmpegHandler.MSG_INFO
import com.san.audioeditor.handler.FFmpegHandler.MSG_PROGRESS
import com.san.audioeditor.tool.AndroidUtil
import com.san.audioeditor.tool.FFmpegUtil
import com.san.audioeditor.tool.FFmpegUtil.fadeInOutAudio
import com.san.audioeditor.tool.FileUtil
import com.san.audioeditor.tool.StoragePermissionManager
import java.io.File
import java.util.Locale


/**
 * Using ffmpeg command to handle audio
 * Created by frank on 2018/1/23.
 */

class AudioHandleActivity : BaseActivity() {
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
                    layoutAudioHandle!!.visibility = View.VISIBLE
                    if (isJointing) {
                        isJointing = false
                        FileUtil.deleteFile(outputPath1)
                        FileUtil.deleteFile(outputPath2)
                    }
                    if (infoBuilder != null) {
                        Toast.makeText(
                            this@AudioHandleActivity,
                            infoBuilder.toString(), Toast.LENGTH_LONG
                        ).show()
                        infoBuilder = null
                    }
                    if (!outputPath.isNullOrEmpty() && !this@AudioHandleActivity.isDestroyed) {
                        showToast("Save to:$outputPath")
                        outputPath = ""
                    }
                    // reset progress
                    txtProgress!!.text = String.format(Locale.getDefault(), "%d%%", 0)
                }

                MSG_PROGRESS -> {
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
        get() = R.layout.activity_audio_handle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        ffmpegHandler = FFmpegHandler(mHandler)
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            PATH = cacheDir.absolutePath
//        }

    }

    override fun onResume() {
        super.onResume()
        adapterPermission()
    }

    private var isPermissionGranted = false
    private fun adapterPermission() {
        isPermissionGranted = AndroidUtil.hasStoragePermissionAdapter33(this)
        if (!isPermissionGranted) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        StoragePermissionManager.onAfterRequestPermission(this) {
            isPermissionGranted = true
        }
    }

    private fun initView() {
        layoutProgress = getView(R.id.layout_progress)
        txtProgress = getView(R.id.txt_progress)
        val list = listOf(
            getString(R.string.audio_cut),
            getString(R.string.audio_concat),
            getString(R.string.audio_waveform),
            "淡入淡出", "多轨道"
        )

        layoutAudioHandle = findViewById(R.id.list_audio_item)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutAudioHandle?.layoutManager = layoutManager

        val adapter = WaterfallAdapter(list)
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                when (position) {

                    0 -> {
                        startActivity(
                            Intent(
                                this@AudioHandleActivity,
                                AudioCutHandleActivity::class.java
                            )
                        )
                    }

                    else -> {
                        Toast.makeText(this@AudioHandleActivity, "Coming soon", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
//                currentPosition = position
//                selectFile()
            }
        })
        layoutAudioHandle?.adapter = adapter
    }

    override fun onViewClick(view: View) {

    }

    override fun onSelectedFile(filePath: String) {
        doHandleAudio(filePath)
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
                outputPath =
                    PATH + File.separator + "cutAudio_${System.currentTimeMillis()}" + suffix
                commandLine = FFmpegUtil.cutAudio(srcFile, 10.5f, 25.0f, outputPath)
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
    }

    companion object {

        private val PATH =
            FFmpegApplication.context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
                ?: ""
//        private val PATH = Environment.getExternalStorageDirectory().path

        private const val useFFmpeg = true

        private const val mixAudio = true

        private var outputPath: String? = null

        init {
            System.loadLibrary("media-handle")
        }
    }

}
