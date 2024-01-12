package com.masoudss.lib.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.masoudss.lib.databinding.ActivityAmplitudaValueBinding
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


class AmplitudaValueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmplitudaValueBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmplitudaValueBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val amplituda = Amplituda(this)
        amplituda.setLogConfig(Log.ERROR, true)
        amplituda.processAudio(
            "/storage/emulated/0/Music/网易云音乐/Kina Grannis,Imaginary Future - I Will Spend My Whole Life Loving You.mp3",
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
            result?.let {
                printResult(
                    it
                )
            }
        }, { exception: AmplitudaException -> exception.printStackTrace() }]
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
        Log.i("llc_audio", resultStr)
        binding.value.text = resultStr
    }

}
