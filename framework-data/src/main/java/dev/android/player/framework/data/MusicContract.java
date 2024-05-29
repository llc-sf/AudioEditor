package dev.android.player.framework.data;

import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by robotinthesun on 20/10/2017.
 */

public class MusicContract {

    public static final String SCHEMA = "content://";
//    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".data";
    public static final String AUTHORITY = "com.san.audioeditor" + ".data";
    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEMA + AUTHORITY);

    public static Uri addLimit(Uri paramUri, int limit) {
        if (limit <= 0) {
            return paramUri;
        }
        return paramUri.buildUpon().appendQueryParameter("limit", limit + "").build();
    }

    public static int getLimit(Uri paramUri) {
        int limit = 0;
        try {
            String params = paramUri.getQueryParameter("limit");
            if (params != null) {
                limit = Integer.parseInt(params);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return limit;
    }

    public static Uri buildIdUri(Uri paramUri, String paramString) {
        return paramUri.buildUpon().appendPath(paramString).build();
    }

    public static String getId(Uri paramUri) {
        return paramUri.getPathSegments().get(1);
    }

    //    public static class Recent implements BaseColumns, MusicDataContract.RecentColumns {
//        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.recent";
//        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.recent";
//        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
//                .appendPath(MusicDataContract.Tables.RECENT).build();
//
//
//    }
    public static class Recent implements BaseColumns, MusicDataContract.RecentColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.recent";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.recent";
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.RECENT).build();
    }

    /**
     * Music1的逻辑部分，已经不适用现在的逻辑了
     */
    @Deprecated
    public static class SongPlayCount implements BaseColumns, MusicDataContract.SongPlayCountColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.song_play_count";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.song_play_count";
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.SONG_PLAY_COUNT).build();
    }

    public static class Search implements BaseColumns, MusicDataContract.SearchHistoryColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.search_history";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.search_history";
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.SEARCH).build();
    }

    public static class PlaybackQueue implements BaseColumns, MusicDataContract.PlaybackQueueColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.playback_queue";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.playback_queue";
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.PLAYBACK_QUEUE).build();
    }

//    public static class PlaybackHistory implements BaseColumns, MusicDataContract.PlaybackHistoryColumns {
//        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.playback_history";
//        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.playback_history";
//        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
//                .appendPath(MusicDataContract.Tables.PLAYBACK_HISTORY).build();
//    }


    public static class IncludeExclude implements BaseColumns, MusicDataContract.IncludeExcludeColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.include_exclude";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.include_exclude";
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.INC_EXC).build();
    }

    public static class Music implements MusicDataContract.MusicColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.musicplayer.music";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.musicplayer.music";
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.MUSICS).build();
    }

    public static class Playlist implements BaseColumns, MusicDataContract.PlaylistColumns {
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.PLAYLIST).build();

    }

    public static class PlaylistMusic implements BaseColumns, MusicDataContract.PlaylistMusicColumns {
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.PLAYLIST_MUSIC).build();
    }

    public static class Album implements MusicDataContract.AlbumColumns {
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.ALBUMS).build();
    }

    public static class Artist implements MusicDataContract.ArtistColumns {
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.ARTISTS).build();
    }

    public static class Genre implements MusicDataContract.GenreColumns {
        public static final Uri CONTENT_URI = MusicContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(MusicDataContract.Tables.GENRES).build();
    }

}
