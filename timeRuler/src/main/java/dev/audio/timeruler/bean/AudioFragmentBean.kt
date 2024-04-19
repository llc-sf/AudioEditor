package dev.audio.timeruler.bean

import android.os.Parcel
import android.os.Parcelable

class AudioFragmentBean() : Parcelable {
    var path: String? = null
    var playingLine: Long = 0
    var cursorValue: Long = 0
    var cutPieces: MutableList<CutPieceBean> = mutableListOf()

    constructor(parcel: Parcel) : this() {
        path = parcel.readString()
        playingLine = parcel.readLong()
        cursorValue = parcel.readLong()
    }


    override fun equals(other: Any?): Boolean {
        return if (other is AudioFragmentBean) {
            other.path == path
        } else {
            false
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeLong(playingLine)
        parcel.writeLong(cursorValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioFragmentBean> {
        override fun createFromParcel(parcel: Parcel): AudioFragmentBean {
            return AudioFragmentBean(parcel)
        }

        override fun newArray(size: Int): Array<AudioFragmentBean?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "AudioFragmentBean(path=$path, playingLine=$playingLine, cursorValue=$cursorValue, cutPieces=$cutPieces)"
    }
}