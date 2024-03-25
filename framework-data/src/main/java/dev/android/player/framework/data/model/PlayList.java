package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class PlayList implements Serializable, Parcelable {
    public long id;

    public String name;

    public int songCount;

    public int order;

    public List<String> paths;//播放列表中包含的歌曲路径

    public Song cover;//第一首歌歌曲

    public static final Song DEFAULT_COVER = new Song();

    public PlayList() {

    }

    private PlayList(long id) {
        this.id = id;
    }


    public static long SPECIAL_ID_FAVORITE = 1;
    public static long SPECIAL_ID_LAST_ADDED = -1;
    public static long SPECIAL_ID_RECENTLY_PLAYED = -2;
    public static long SPECIAL_ID_TOP_TRACKS = -3;
    public static long SPECIAL_ID_CREATE_NEW = -10010;

    public static PlayList LAST_ADDED = new PlayList(SPECIAL_ID_LAST_ADDED);

    public static PlayList RECENTLY_PLAYED = new PlayList(SPECIAL_ID_RECENTLY_PLAYED);

    public static PlayList TOP_TRACKS = new PlayList(SPECIAL_ID_TOP_TRACKS);

    public static PlayList CREATE_NEW = new PlayList(SPECIAL_ID_CREATE_NEW);

    protected PlayList(Parcel in) {
        id = in.readLong();
        name = in.readString();
        songCount = in.readInt();
        order = in.readInt();
        paths = in.createStringArrayList();
    }

    public PlayList(long id, String name, int songCount, int order, List<String> paths, Song cover) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.order = order;
        this.paths = paths;
        this.cover = cover;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(songCount);
        dest.writeInt(order);
        dest.writeStringList(paths);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PlayList> CREATOR = new Creator<PlayList>() {
        @Override
        public PlayList createFromParcel(Parcel in) {
            return new PlayList(in);
        }

        @Override
        public PlayList[] newArray(int size) {
            return new PlayList[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayList playList = (PlayList) o;
        return id == playList.id &&
                songCount == playList.songCount &&
                Objects.equals(cover, playList.cover) &&
                Objects.equals(name, playList.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, songCount, cover);
    }

    //判断是特殊的一种播放列表
    public boolean isSpecial() {
        return id == SPECIAL_ID_FAVORITE ||
                id == SPECIAL_ID_LAST_ADDED ||
                id == SPECIAL_ID_RECENTLY_PLAYED ||
                id == SPECIAL_ID_TOP_TRACKS ||
                id == SPECIAL_ID_CREATE_NEW;
    }

    @Override
    public String toString() {
        return "PlayList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cover=" + cover +
                '}';
    }

    public PlayList newInstance() {
        return new PlayList(id, name, songCount, order, paths, cover);
    }


}
