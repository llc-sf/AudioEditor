package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * 曲风信息
 */
public class Genre extends IndexModel implements Serializable, Parcelable {
    public long id;

    public String name;

    public String cover;

    public int songCount;

    @NotNull
    public static final String NAME = "name";


    public Genre() {
    }


    public Genre(long id, String name, String cover, int count) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.songCount = count;
    }


    protected Genre(Parcel in) {
        id = in.readLong();
        name = in.readString();
        cover = in.readString();
        songCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(cover);
        dest.writeInt(songCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", songCount=" + songCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return id == genre.id && songCount == genre.songCount && Objects.equals(name, genre.name) && Objects.equals(cover, genre.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cover, songCount);
    }

    /**
     * 重新搞 一个新的对象
     */
    public Genre newInstance() {
        Genre genre = new Genre();
        genre.id = id;
        genre.name = name;
        genre.cover = cover;
        genre.songCount = songCount;
        return genre;
    }
}
