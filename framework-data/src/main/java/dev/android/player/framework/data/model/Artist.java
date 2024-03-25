package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Objects;

import kotlinx.android.parcel.Parcelize;

/**
 * 歌手信息
 */
@Parcelize
public class Artist extends IndexModel implements Serializable, Parcelable {

    public long id;

    public String name;

    public String cover;

    public int albumCount;

    public int songCount;

    public long recent;// 播放时间


    public static final String NAME = "name";
    public static final String NUMBER_OF_SONGS = "songCount";
    public static final String NUMBER_OF_ALBUMS = "albumCount";

    public Artist() {
    }

    public Artist(long id, String name, String cover, int albumCount, int songCount) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.albumCount = albumCount;
        this.songCount = songCount;
    }



    protected Artist(Parcel in) {
        id = in.readLong();
        name = in.readString();
        cover = in.readString();
        albumCount = in.readInt();
        songCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(cover);
        dest.writeInt(albumCount);
        dest.writeInt(songCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", albumCount=" + albumCount +
                ", songCount=" + songCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(id, artist.id)
                && Objects.equals(name, artist.name)
                && Objects.equals(songCount, artist.songCount)
                && Objects.equals(recent, artist.recent)
                && Objects.equals(albumCount, artist.albumCount)
                && Objects.equals(cover, artist.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cover, albumCount, songCount, recent);
    }

    /**
     * 重新搞 一个新的对象
     */
    public Artist newInstance() {
        return new Artist(id, name, cover, albumCount, songCount);
    }
}
