package dev.android.player.framework.data.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ExInfoJsonUtils {
    public static final String NAME_SUBTITLE_PATH = "a";
    public static final String NAME_SUBTITLE_OFFSET = "b";
    public static final String NAME_SUBTITLE_ENABLE = "c";
    public static final String NAME_SUBTITLE_TRACK_ID = "d";
    public static final String NAME_AUDIO_TRACK_ID = "e";
    public static final String NAME_SUBTITLE_POSITION = "f";
    public static final String NAME_SUBTITLE_TEXT_SIZE = "g";
    public static final String NAME_CODEC_USE_SW = "h";
    public static final String NAME_AUDIO_DELAY = "i";
    public static final String NAME_ASS_FONT_SCALE = "j";
    public static final String NAME_SUBTITLE_BG_ALPHA = "k";
    public static final String NAME_VIDEO_MIRROR = "l";
    public static final String NAME_PLAYLIST_ID = "m";
    public static final String NAME_MUSIC_SYSTEM_ID = "n";
    public static final String NAME_VIDEO_BOOKMARK_LIST = "o";
    public static final String NAME_LAST_SPEED = "p";

    public static final String DEFAULT_VALUE_SUBTITLE_PATH = null;
    public static final int DEFAULT_VALUE_SUBTITLE_OFFSET = 0;
    public static final boolean DEFAULT_VALUE_SUBTITLE_ENABLE = false;
    public static final int DEFAULT_VALUE_SUBTITLE_TRACK_ID = -1;
    public static final int DEFAULT_VALUE_AUDIO_TRACK_ID = -1;
    public static final float DEFAULT_VALUE_SUBTITLE_POSITION = -1f;
    public static final int DEFAULT_VALUE_SUBTITLE_TEXT_SIZE = -1;
    public static final int DEFAULT_VALUE_CODEC_USE_SW = -1;
    public static final float DEFAULT_VALUE_AUDIO_DELAY = 0f;
    public static final float DEFAULT_VALUE_ASS_FONT_SCALE = 1f;
    public static final int DEFAULT_VALUE_SUBTITLE_BG_ALPHA = 0;
    public static final boolean DEFAULT_VALUE_VIDEO_MIRROR = false;
    public static final int DEFAULT_VALUE_PLAYLIST_ID = DatabaseConst.PLAYLIST_ID_NONE;
    public static final int DEFAULT_VALUE_MUSIC_SYSTEM_ID = 0;
    public static final String DEFAULT_VALUE_VIDEO_BOOKMARK_LIST = null;
    public static final float DEFAULT_VALUE_LAST_SPEED = -1;

    public static String exInfoToJson(ExInfo exInfo) {
        if (exInfo != null) {
            JSONObject json = new JSONObject();
            // 和默认值相等的字段不存入数据库，提供性能
            try {
                if (!Objects.equals(exInfo.subtitlePath, DEFAULT_VALUE_SUBTITLE_PATH))
                    json.put(NAME_SUBTITLE_PATH, exInfo.subtitlePath);
                if (exInfo.subtitleOffset != DEFAULT_VALUE_SUBTITLE_OFFSET)
                    json.put(NAME_SUBTITLE_OFFSET, exInfo.subtitleOffset);
                if (exInfo.subtitleEnable != DEFAULT_VALUE_SUBTITLE_ENABLE)
                    json.put(NAME_SUBTITLE_ENABLE, exInfo.subtitleEnable ? 1 : 0);
                if (exInfo.subtitleTrackId != DEFAULT_VALUE_SUBTITLE_TRACK_ID)
                    json.put(NAME_SUBTITLE_TRACK_ID, exInfo.subtitleTrackId);
                if (exInfo.subtitlePosition != DEFAULT_VALUE_SUBTITLE_POSITION)
                    json.put(NAME_SUBTITLE_POSITION, exInfo.subtitlePosition);
                if (exInfo.subtitleTextSize != DEFAULT_VALUE_SUBTITLE_TEXT_SIZE)
                    json.put(NAME_SUBTITLE_TEXT_SIZE, exInfo.subtitleTextSize);
                if (exInfo.subtitleBackgroundAlpha != DEFAULT_VALUE_SUBTITLE_BG_ALPHA)
                    json.put(NAME_SUBTITLE_BG_ALPHA, exInfo.subtitleBackgroundAlpha);
                if (exInfo.audioTrackId != DEFAULT_VALUE_AUDIO_TRACK_ID)
                    json.put(NAME_AUDIO_TRACK_ID, exInfo.audioTrackId);
                if (exInfo.defaultDecoder != DEFAULT_VALUE_CODEC_USE_SW)
                    json.put(NAME_CODEC_USE_SW, exInfo.defaultDecoder);
                if (exInfo.audioDelay != DEFAULT_VALUE_AUDIO_DELAY)
                    json.put(NAME_AUDIO_DELAY, exInfo.audioDelay);
                if (exInfo.assFontScale != DEFAULT_VALUE_ASS_FONT_SCALE)
                    json.put(NAME_ASS_FONT_SCALE, exInfo.assFontScale);
                if (exInfo.mirror != DEFAULT_VALUE_VIDEO_MIRROR)
                    json.put(NAME_VIDEO_MIRROR, exInfo.mirror ? 1 : 0);
                if (exInfo.playListId != DEFAULT_VALUE_PLAYLIST_ID)
                    json.put(NAME_PLAYLIST_ID, exInfo.playListId);
                if (exInfo.musicSystemId != DEFAULT_VALUE_MUSIC_SYSTEM_ID)
                    json.put(NAME_MUSIC_SYSTEM_ID, exInfo.musicSystemId);
                if (!Objects.equals(exInfo.bookmarkList, DEFAULT_VALUE_VIDEO_BOOKMARK_LIST))
                    json.put(NAME_VIDEO_BOOKMARK_LIST, exInfo.bookmarkList);
                if (exInfo.lastSpeed != DEFAULT_VALUE_LAST_SPEED)
                    json.put(NAME_LAST_SPEED, exInfo.lastSpeed);
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ExInfo jsonToExInfo(String jsonStr) {
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                JSONObject json = new JSONObject(jsonStr);
                ExInfo info = new ExInfo();
                info.subtitlePath = json.optString(NAME_SUBTITLE_PATH, DEFAULT_VALUE_SUBTITLE_PATH);
                info.subtitleOffset = json.optInt(NAME_SUBTITLE_OFFSET, DEFAULT_VALUE_SUBTITLE_OFFSET);
                info.subtitleEnable = 1 == json.optInt(NAME_SUBTITLE_ENABLE, DEFAULT_VALUE_SUBTITLE_ENABLE ? 1 : 0);
                info.subtitleTrackId = json.optInt(NAME_SUBTITLE_TRACK_ID, DEFAULT_VALUE_SUBTITLE_TRACK_ID);
                info.subtitlePosition = (float) json.optDouble(NAME_SUBTITLE_POSITION, DEFAULT_VALUE_SUBTITLE_POSITION);
                info.subtitleTextSize = json.optInt(NAME_SUBTITLE_TEXT_SIZE, DEFAULT_VALUE_SUBTITLE_TEXT_SIZE);
                info.subtitleBackgroundAlpha = json.optInt(NAME_SUBTITLE_BG_ALPHA, DEFAULT_VALUE_SUBTITLE_BG_ALPHA);
                info.audioTrackId = json.optInt(NAME_AUDIO_TRACK_ID, DEFAULT_VALUE_AUDIO_TRACK_ID);
                info.defaultDecoder = json.optInt(NAME_CODEC_USE_SW, DEFAULT_VALUE_CODEC_USE_SW);
                info.audioDelay = (float) json.optDouble(NAME_AUDIO_DELAY, DEFAULT_VALUE_AUDIO_DELAY);
                info.assFontScale = (float) json.optDouble(NAME_ASS_FONT_SCALE, DEFAULT_VALUE_ASS_FONT_SCALE);
                info.mirror = 1 == json.optInt(NAME_VIDEO_MIRROR, DEFAULT_VALUE_VIDEO_MIRROR ? 1 : 0);
                info.playListId = json.optInt(NAME_PLAYLIST_ID, DEFAULT_VALUE_PLAYLIST_ID);
                info.musicSystemId = json.optInt(NAME_MUSIC_SYSTEM_ID, DEFAULT_VALUE_MUSIC_SYSTEM_ID);
                info.bookmarkList = json.optString(NAME_VIDEO_BOOKMARK_LIST, DEFAULT_VALUE_VIDEO_BOOKMARK_LIST);
                info.lastSpeed = (float) json.optDouble(NAME_LAST_SPEED, DEFAULT_VALUE_LAST_SPEED);
                return info;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
