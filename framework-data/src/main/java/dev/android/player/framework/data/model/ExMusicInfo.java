package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by inshot-user on 2017/8/2.
 */
public class ExMusicInfo implements Parcelable {
    private String artist;
    private String album;
    private int artistId;
    private int albumId;
    private int rate;
    private int id;

    public ExMusicInfo() {

    }

    public ExMusicInfo(Parcel in) {
        artist = in.readString();
        album = in.readString();
        artistId = in.readInt();
        albumId = in.readInt();
        rate = in.readInt();
        id = in.readInt();
    }

    public static final Creator<ExMusicInfo> CREATOR = new Creator<ExMusicInfo>() {
        @Override
        public ExMusicInfo createFromParcel(Parcel in) {
            return new ExMusicInfo(in);
        }

        @Override
        public ExMusicInfo[] newArray(int size) {
            return new ExMusicInfo[size];
        }
    };

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeInt(artistId);
        dest.writeInt(albumId);
        dest.writeInt(rate);
        dest.writeInt(id);
    }
}
