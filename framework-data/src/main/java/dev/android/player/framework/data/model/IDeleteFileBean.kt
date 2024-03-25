package dev.android.player.framework.data.model

import android.os.Parcelable

interface IDeleteFileBean : Parcelable {

    companion object{
        const val TYPE_SONG = 1
        const val TYPE_VIDEO = 2
    }

    fun getDeleteFilePath(): String

    fun getDeleteId(): Long

    fun getDeleteFileName(): String

    fun getFileType(): Int
}