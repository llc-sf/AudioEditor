package com.san.audioeditor.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.san.audioeditor.activity.AudioPickActivity
import com.san.audioeditor.databinding.ViewOutputEmptyBinding

/**
 * AudioItem
 */
class OutputEmptyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private val mBinding = ViewOutputEmptyBinding.inflate(LayoutInflater.from(getContext()), this, true)


    init {
        mBinding.create.setOnClickListener {
            val intent = Intent(context, AudioPickActivity::class.java) //            pickAudioResult.launch(intent)
            getContext()?.startActivity(intent)
        }
    }

}