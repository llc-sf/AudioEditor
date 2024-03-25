package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class ExInfo implements Parcelable {
    public String subtitlePath = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_PATH; // external subtitle file path
    public int subtitleOffset = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_OFFSET; // only for external subtitle
    public boolean subtitleEnable = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_ENABLE; // is external subtitle turn on or not
    public int subtitleTrackId = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_TRACK_ID; // internal subtitle track ID. If is -1: means player will use external subtitle
    public int subtitleTextSize = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_TEXT_SIZE;
    public float subtitlePosition = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_POSITION;
    public int subtitleBackgroundAlpha = ExInfoJsonUtils.DEFAULT_VALUE_SUBTITLE_BG_ALPHA;
    public int audioTrackId = ExInfoJsonUtils.DEFAULT_VALUE_AUDIO_TRACK_ID; // internal audio track ID. If is -2: means use default audio track, don't need to change.
    public int defaultDecoder = ExInfoJsonUtils.DEFAULT_VALUE_CODEC_USE_SW; // -1: default; 0: hw; 1: sw.
    public float audioDelay = ExInfoJsonUtils.DEFAULT_VALUE_AUDIO_DELAY;
    public float assFontScale = ExInfoJsonUtils.DEFAULT_VALUE_ASS_FONT_SCALE;
    public boolean mirror = ExInfoJsonUtils.DEFAULT_VALUE_VIDEO_MIRROR;
    public int playListId = ExInfoJsonUtils.DEFAULT_VALUE_PLAYLIST_ID;
    public int musicSystemId = ExInfoJsonUtils.DEFAULT_VALUE_MUSIC_SYSTEM_ID;
    public String bookmarkList = ExInfoJsonUtils.DEFAULT_VALUE_VIDEO_BOOKMARK_LIST;
    public float lastSpeed = ExInfoJsonUtils.DEFAULT_VALUE_LAST_SPEED;

    public ExInfo() {
    }

    private ExInfo(Parcel in) {
        subtitlePath = in.readString();
        bookmarkList = in.readString();
        subtitleOffset = in.readInt();
        subtitleEnable = in.readByte() != 0;
        subtitleTrackId = in.readInt();
        subtitleTextSize = in.readInt();
        subtitlePosition = in.readFloat();
        audioTrackId = in.readInt();
        defaultDecoder = in.readByte();
        audioDelay = in.readFloat();
        assFontScale = in.readFloat();
        subtitleBackgroundAlpha = in.readInt();
        mirror = in.readByte() != 0;
        playListId = in.readInt();
        musicSystemId = in.readInt();
        lastSpeed = in.readFloat();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExInfo exInfo = (ExInfo) o;
        return subtitleOffset == exInfo.subtitleOffset
                && subtitleEnable == exInfo.subtitleEnable
                && subtitleTrackId == exInfo.subtitleTrackId
                && subtitleTextSize == exInfo.subtitleTextSize
                && Float.compare(exInfo.subtitlePosition, subtitlePosition) == 0
                && subtitleBackgroundAlpha == exInfo.subtitleBackgroundAlpha
                && audioTrackId == exInfo.audioTrackId
                && defaultDecoder == exInfo.defaultDecoder
                && Float.compare(exInfo.audioDelay, audioDelay) == 0
                && Float.compare(exInfo.assFontScale, assFontScale) == 0
                && mirror == exInfo.mirror
                && playListId == exInfo.playListId
                && musicSystemId == exInfo.musicSystemId
                && Objects.equals(subtitlePath, exInfo.subtitlePath)
                && Objects.equals(bookmarkList, exInfo.bookmarkList)
                && Float.compare(exInfo.lastSpeed, lastSpeed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subtitlePath,
                subtitleOffset,
                subtitleEnable,
                subtitleTrackId,
                subtitleTextSize,
                subtitlePosition,
                subtitleBackgroundAlpha,
                audioTrackId,
                defaultDecoder,
                audioDelay,
                assFontScale,
                mirror,
                playListId,
                bookmarkList,
                musicSystemId,
                lastSpeed);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subtitlePath);
        dest.writeString(bookmarkList);
        dest.writeInt(subtitleOffset);
        dest.writeByte((byte) (subtitleEnable ? 1 : 0));
        dest.writeInt(subtitleTrackId);
        dest.writeInt(subtitleTextSize);
        dest.writeFloat(subtitlePosition);
        dest.writeInt(audioTrackId);
        dest.writeByte((byte) defaultDecoder);
        dest.writeFloat(audioDelay);
        dest.writeFloat(assFontScale);
        dest.writeInt(subtitleBackgroundAlpha);
        dest.writeByte((byte) (mirror ? 1 : 0));
        dest.writeInt(playListId);
        dest.writeInt(musicSystemId);
        dest.writeFloat(lastSpeed);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExInfo> CREATOR = new Creator<ExInfo>() {
        @Override
        public ExInfo createFromParcel(Parcel in) {
            return new ExInfo(in);
        }

        @Override
        public ExInfo[] newArray(int size) {
            return new ExInfo[size];
        }
    };

    @Override
    public String toString() {
        return "ExInfo{" +
                "subtitlePath='" + subtitlePath + '\'' +
                ", subtitleOffset=" + subtitleOffset +
                ", subtitleEnable=" + subtitleEnable +
                ", subtitleTrackId=" + subtitleTrackId +
                ", subtitleTextSize=" + subtitleTextSize +
                ", subtitlePosition=" + subtitlePosition +
                ", subtitleBackgroundAlpha=" + subtitleBackgroundAlpha +
                ", audioTrackId=" + audioTrackId +
                ", defaultDecoder=" + defaultDecoder +
                ", audioDelay=" + audioDelay +
                ", assFontScale=" + assFontScale +
                ", mirror=" + mirror +
                ", playListId=" + playListId +
                ", musicSystemId=" + musicSystemId +
                ", bookmarkList='" + bookmarkList + '\'' +
                ", lastSpeed=" + lastSpeed +
                '}';
    }
}
