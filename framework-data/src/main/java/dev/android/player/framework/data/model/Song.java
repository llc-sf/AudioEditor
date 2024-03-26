package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

import kotlinx.android.parcel.Parcelize;

/**
 * 歌曲信息
 */
@Parcelize
public class Song extends IndexModel implements Serializable, Parcelable, IDeleteFileBean {

    public long id = 0;
    public long albumId;
    public long artistId;
    public int track;
    public String path;
    public long size;
    public int duration;
    public String albumName;
    public String artistName;
    public String title;
    public long dateAdded;
    public long dateModified;
    //曲风信息
    public long genreId;
    public String genre;
    public String mimeType;

    public String cover;


    public int order;//播放列表的位置
    public long add_time;//添加到播放列表的时间

    public int count;//播放的次数

    public long recent;//最近播放的时间


    /**
     * 排序选项所所需要字段
     */
    public static final String TITLE = "title";
    public static final String ALBUM_NAME = "albumName";
    public static final String ARTIST_NAME = "artistName";
    public static final String TIME_ADD = "dateAdded";
    public static final String DURATION = "duration";
    public static final String SIZE = "size";
    public static final String TRACK = "track";

    public static final String ORDER = "order";
    public static final String TIME_ADD_PLAYLIST = "add_time";
    public static final String COUNT = "count";//播放的次数


    public Song() {
    }

    public Song(long _id, long _albumId, long _artistId, String _title, String _artistName, String _albumName, int _duration, int _trackNumber, int size, String data) {
        this.id = _id;
        this.albumId = _albumId;
        this.artistId = _artistId;
        this.title = _title;
        this.artistName = _artistName;
        this.albumName = _albumName;
        this.duration = _duration;
        this.track = _trackNumber;
        this.size = size;
        this.path = data;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        albumId = in.readLong();
        artistId = in.readLong();
        track = in.readInt();
        path = in.readString();
        size = in.readLong();
        duration = in.readInt();
        albumName = in.readString();
        artistName = in.readString();
        title = in.readString();
        dateAdded = in.readLong();
        dateModified = in.readLong();
        genreId = in.readLong();
        genre = in.readString();
        mimeType = in.readString();
        cover = in.readString();
        order = in.readInt();
        count = in.readInt();
        recent = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(albumId);
        dest.writeLong(artistId);
        dest.writeInt(track);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeInt(duration);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeString(title);
        dest.writeLong(dateAdded);
        dest.writeLong(dateModified);
        dest.writeLong(genreId);
        dest.writeString(genre);
        dest.writeString(mimeType);
        dest.writeString(cover);
        dest.writeInt(order);
        dest.writeInt(count);
        dest.writeLong(recent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    /**
     * 获取比特率
     *
     * @return
     */
    public int getBitRate() {
        return (int) (duration > 0 ? size * 8 / duration : -1);
    }

    /**
     * 获取是否是高清音频（测试方法不准确）
     *
     * @return
     */
    public boolean isHQ() {
        if (path != null && path.length() > 0) {
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex >= 0) {
                String suffix = path.substring(dotIndex).toLowerCase();
                switch (suffix) {
                    case ".flac":
                    case ".ape":
                    case ".wav":
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id
                && albumId == song.albumId
                && artistId == song.artistId
                && genreId == song.genreId
                && Objects.equals(path, song.path)
                && Objects.equals(title, song.title)
                && Objects.equals(albumName, song.albumName)
                && Objects.equals(artistName, song.artistName)
                && Objects.equals(genre, song.genre)
                && Objects.equals(cover, song.cover)
                && Objects.equals(dateModified, song.dateModified)
                && Objects.equals(track, song.track);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, albumId, artistId, path, duration, albumName, artistName, title, dateAdded, dateModified, genreId, genre, mimeType, cover);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", albumId=" + albumId +
                ", artistId=" + artistId +
                ", track=" + track +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                ", albumName='" + albumName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", title='" + title + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                ", genreId=" + genreId +
                ", genre='" + genre + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", cover='" + cover + '\'' +
                '}';
    }

    /**
     * 重新创建一个队形
     *
     * @return
     */
    @Nullable
    public Song newInstance() {
        Song song = new Song();
        song.id = id;
        song.albumId = albumId;
        song.artistId = artistId;
        song.track = track;
        song.path = path;
        song.size = size;
        song.duration = duration;
        song.albumName = albumName;
        song.artistName = artistName;
        song.title = title;
        song.dateAdded = dateAdded;
        song.dateModified = dateModified;
        song.genreId = genreId;
        song.genre = genre;
        song.mimeType = mimeType;
        song.cover = cover;
        return song;
    }

    @NonNull
    @Override
    public String getDeleteFilePath() {
        return path;
    }

    @Override
    public long getDeleteId() {
        return id;
    }

    @NonNull
    @Override
    public String getDeleteFileName() {
        return title;
    }

    @Override
    public int getFileType() {
        return IDeleteFileBean.TYPE_SONG;
    }


}


