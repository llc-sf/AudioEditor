package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 歌曲文件信息
 */
public class Directory extends IndexModel implements Parcelable, Serializable {

    /**
     * 排序选项所所需要字段
     */
    public static final String NAME = "name";

    public String name;

    public String path;

    public int songCount;

    public long updateTime;

    public Directory() {

    }

    protected Directory(Parcel in) {
        name = in.readString();
        path = in.readString();
        songCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeInt(songCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Directory> CREATOR = new Creator<Directory>() {
        @Override
        public Directory createFromParcel(Parcel in) {
            return new Directory(in);
        }

        @Override
        public Directory[] newArray(int size) {
            return new Directory[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Directory && path.equals(((Directory) obj).path);
    }
}

