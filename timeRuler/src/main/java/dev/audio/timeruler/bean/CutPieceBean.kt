package dev.audio.timeruler.bean

import android.os.Parcel
import android.os.Parcelable

class CutPieceBean() : android.os.Parcelable {
    var startTimestampTimeInSelf = 0L
    var endTimestampTimeInSelf = 0L

    constructor(parcel: Parcel) : this() {
        startTimestampTimeInSelf = parcel.readLong()
        endTimestampTimeInSelf = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(startTimestampTimeInSelf)
        parcel.writeLong(endTimestampTimeInSelf)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CutPieceBean> {
        override fun createFromParcel(parcel: Parcel): CutPieceBean {
            return CutPieceBean(parcel)
        }

        override fun newArray(size: Int): Array<CutPieceBean?> {
            return arrayOfNulls(size)
        }
    }
}