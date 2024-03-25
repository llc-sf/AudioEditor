package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Objects;

import kotlinx.android.parcel.Parcelize;

/**
 * 歌曲专辑
 */
@Parcelize
public class Album extends IndexModel implements Serializable, Parcelable {
    public long id;

    public String name;

    public long artistId;

    public String artistName;

    public String cover;

    public int songCount = 0;

    public long recent;//最近播放的时间

    public static final String TITLE = "name";
    public static final String ARTIST = "artistName";


    public Album() {
    }

    public Album(long id, String name, long artistId, String artistName, String cover, int songCount) {
        this.id = id;
        this.name = name;
        this.artistId = artistId;
        this.artistName = artistName;
        this.cover = cover;
        this.songCount = songCount;
    }

    protected Album(Parcel in) {
        id = in.readLong();
        name = in.readString();
        artistId = in.readLong();
        artistName = in.readString();
        cover = in.readString();
        songCount = in.readInt();
        recent = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(artistId);
        dest.writeString(artistName);
        dest.writeString(cover);
        dest.writeInt(songCount);
        dest.writeLong(recent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", cover='" + cover + '\'' +
                ", songCount=" + songCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return id == album.id &&
                artistId == album.artistId &&
                songCount == album.songCount &&
                Objects.equals(name, album.name) &&
                Objects.equals(artistName, album.artistName) &&
                Objects.equals(cover, album.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, artistId, artistName, cover, songCount);
    }

    public Album newInstance() {
        return new Album(id, name, artistId, artistName, cover, songCount);
    }
}
