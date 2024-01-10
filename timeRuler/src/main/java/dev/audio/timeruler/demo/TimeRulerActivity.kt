package dev.audio.timeruler.demo

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import dev.audio.timeruler.BaseScaleBar
import dev.audio.timeruler.R
import dev.audio.timeruler.TimeRulerBar
import dev.audio.timeruler.bean.TimeBean
import dev.audio.timeruler.bean.VideoBean
import dev.audio.timeruler.databinding.ActivityTimeRulerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class TimeRulerActivity : AppCompatActivity() {
    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss")
    var nowTime = 1
    private lateinit var binding: ActivityTimeRulerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeRulerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initTimeBar()
    }

    private fun initTimeBar() {
        val calendar = Calendar.getInstance()

        // 00:00:00 000
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var startTime = calendar.timeInMillis

        // 23:59:59 999
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 5
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var endTime = calendar.timeInMillis

        //一个手机宽度显示多长时间
//        binding.timeBar.setScreenSpanValue(TimeRulerBar.VALUE_1000_MS * 8)
        //
        binding.timeBar.setMode(TimeRulerBar.MODE_UINT_1000_MS)
        binding.timeBar.setRange(startTime, endTime)
        binding.timeBar.cursorValue = System.currentTimeMillis()

        binding.timeBar.setOnCursorListener(object : BaseScaleBar.OnCursorListener {
            override fun onStartTrackingTouch(cursorValue: Long) {

            }

            override fun onProgressChanged(cursorValue: Long, fromeUser: Boolean) {
                binding.tvData.text = cursorDateFormat.format(Date(cursorValue))
            }

            override fun onStopTrackingTouch(cursorValue: Long) {

            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.timeBar.setScale(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.seekAreaOffset.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.timeBar.setVideoAreaOffset(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.btnDir.setOnClickListener {
            binding.btnDir.isSelected = !binding.btnDir.isSelected
            binding.timeBar.setTickDirection(binding.btnDir.isSelected)
        }

        binding.btnShowCursor.setOnClickListener {
            binding.btnShowCursor.isSelected = !binding.btnShowCursor.isSelected
            binding.timeBar.setShowCursor(binding.btnShowCursor.isSelected);
        }

        binding.btnPlay.setOnClickListener {
            nowTime++
            binding.timeBar.cursorValue = System.currentTimeMillis() + 1000 * nowTime * 60
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb1 -> {
                    binding.timeBar.setMode(TimeRulerBar.MODE_UINT_100_MS)
                }

                R.id.rb2 -> {
                    binding.timeBar.setMode(TimeRulerBar.MODE_UINT_500_MS)
                }

                R.id.rb3 -> {
                    binding.timeBar.setMode(TimeRulerBar.MODE_UINT_1000_MS)
                }
            }
        }
        setData()

    }

    private fun setData() {
        var videos = mutableListOf<VideoBean>()
        var testTime = System.currentTimeMillis()
        for (i in 1..5) {
            val video = VideoBean(testTime, testTime + i * 10 * 60 * 1000, i % 2 == 0)
            videos.add(video)
            testTime += i * 15 * 60 * 1000
        }
        val timeBean = TimeBean(videos)
        binding.timeBar.setColorScale(timeBean)
    }
}