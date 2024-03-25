package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DBBean implements Parcelable {
    public int id;
    public String url;
    public String parentPath;
    public String name; // NOTICE: 一直以来，这个字段数据库中没有存入值。
    public int type;
    public long lastOpenTime;
    public long seenTime;
    public long duration;
    public ExInfo exInfo;

    public DBBean() {
        id = -1;
        duration = -1;
    }

    private DBBean(Parcel in) {
        id = in.readInt();
        url = in.readString();
        name = in.readString();
        type = in.readInt();
        lastOpenTime = in.readLong();
        seenTime = in.readLong();
        duration = in.readLong();
        exInfo = in.readParcelable(ExInfo.class.getClassLoader());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBBean dbBean = (DBBean) o;
        return id == dbBean.id
                && type == dbBean.type
                && lastOpenTime == dbBean.lastOpenTime
                && seenTime == dbBean.seenTime
                && duration == dbBean.duration
                && Objects.equals(url, dbBean.url)
                && Objects.equals(name, dbBean.name)
                && Objects.equals(exInfo, dbBean.exInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                url,
                name,
                type,
                lastOpenTime,
                seenTime,
                duration,
                exInfo);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeLong(lastOpenTime);
        dest.writeLong(seenTime);
        dest.writeLong(duration);
        dest.writeParcelable(exInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DBBean> CREATOR = new Creator<DBBean>() {
        @Override
        public DBBean createFromParcel(Parcel in) {
            return new DBBean(in);
        }

        @Override
        public DBBean[] newArray(int size) {
            return new DBBean[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "DBBean{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", lastOpenTime=" + lastOpenTime +
                ", seenTime=" + seenTime +
                ", duration=" + duration +
                ", exInfo=" + exInfo +
                '}';
    }
}
