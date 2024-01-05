package com.san.audioeditor

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaProgressListener
import linc.com.amplituda.AmplitudaResult
import linc.com.amplituda.Cache
import linc.com.amplituda.Compress
import linc.com.amplituda.InputAudio
import linc.com.amplituda.ProgressOperation
import linc.com.amplituda.exceptions.AmplitudaException
import java.io.File
import java.util.Arrays
import java.util.Locale


class AmplitudaActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_amplituda

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val amplituda = Amplituda(this)
        amplituda.setLogConfig(Log.ERROR, true)
        amplituda.processAudio(
            "/data/hw_init/product/media/Pre-loaded/Music/Last_Stop.mp3",
            Compress.withParams(Compress.AVERAGE, 1),
            Cache.withParams(Cache.REUSE),
            object : AmplitudaProgressListener() {
                override fun onStartProgress() {
                    super.onStartProgress()
                    println("Start Progress")
                }

                override fun onStopProgress() {
                    super.onStopProgress()
                    println("Stop Progress")
                }

                override fun onProgress(operation: ProgressOperation, progress: Int) {
                    var currentOperation = ""
                    currentOperation = when (operation) {
                        ProgressOperation.PROCESSING -> "Process audio"
                        ProgressOperation.DECODING -> "Decode audio"
                        ProgressOperation.DOWNLOADING -> "Download audio from url"
                    }
                    System.out.printf("%s: %d%% %n", currentOperation, progress)
                }
            }
        )[{ result: AmplitudaResult<String?>? ->
            printResult(
                result!!
            )
        }, { exception: AmplitudaException -> exception.printStackTrace() }]
    }

    override fun onViewClick(view: View) {
    }

    override fun onSelectedFile(filePath: String) {
    }

    private fun printResult(result: AmplitudaResult<*>) {
        val resultStr = String.format(
            Locale.US,
            """
             Audio info:
             millis = %d
             seconds = %d
             
             source = %s
             source type = %s
             
             Amplitudes:
             size: = %d
             list: = %s
             amplitudes for second 1: = %s
             json: = %s
             single line sequence = %s
             new line sequence = %s
             custom delimiter sequence = %s
             %n
             """.trimIndent(),
            result.getAudioDuration(AmplitudaResult.DurationUnit.MILLIS),
            result.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS),
            if (result.inputAudioType == InputAudio.Type.FILE) (result.audioSource as File).absolutePath else result.audioSource,
            result.inputAudioType.name,
            result.amplitudesAsList().size,
            Arrays.toString(result.amplitudesAsList().toTypedArray()),
            Arrays.toString(result.amplitudesForSecond(1).toTypedArray()),
            result.amplitudesAsJson(),
            result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE),
            result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.NEW_LINE),
            result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE, " * ")
        )
        if (!TextUtils.isEmpty(resultStr)) {
            findViewById<TextView>(R.id.desc).text = resultStr
        }
        Log.i("llc_audio", resultStr)
    }


}
