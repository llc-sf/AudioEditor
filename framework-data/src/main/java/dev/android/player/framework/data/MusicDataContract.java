package dev.android.player.framework.data;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by robotinthesun on 20/10/2017.
 */

public interface MusicDataContract {

    public interface Tables {
        /* Search history table name */
        String SEARCH = "searchhistory";

        /* Playback queue table name*/
        String PLAYBACK_QUEUE = "playbackqueue";

        //播放记录
        /* recent history table name */
        String RECENT = "recent";

        /* Song play count table name */
        String SONG_PLAY_COUNT = "songplaycount";
        /**
         * Whitelist & Black list table name
         */
        String INC_EXC = "incexc";
        /**
         * Playlist table name
         */
        String PLAYLIST = "playlist";

        /**
         * music of playlist table
         */
        String PLAYLIST_MUSIC = "playlist_music";
        /**
         * musics table
         */
        String MUSICS = "musics";
        /**
         * Albums table
         */
        String ALBUMS = "albums";
        /**
         * Artists table
         */
        String ARTISTS = "artists";
        /**
         * Audio genre table
         */
        String GENRES = "audio_genres";

        String AUDIO_GENRE_MAP = "audio_genres_map";
    }

//    interface Views {
//
//        String ARTIST_INFO = "artist_info";
//
//        String ALBUM_INFO = "album_info";
//    }

    public interface SearchHistoryColumns {
        /* What was searched */
        String SEARCH_STRING = "searchstring";

        /* Time of search */
        String TIMESEARCHED = "timesearched";
    }

    public interface RecentColumns {
        /* Album IDs column */
        String ID = "songid";

        /* Time played column */
        String TIMEPLAYED = "timeplayed";

        String TOTAL_COUNT = "total_count";
    }

    public interface SongPlayCountColumns {
        /* Song IDs column */
        String ID = "songid";

        /* Week Play Count */
        String WEEK_PLAY_COUNT = "week";

        /* Weeks since Epoch */
        String LAST_UPDATED_WEEK_INDEX = "weekindex";

        /* Play count */
        String PLAYCOUNTSCORE = "playcountscore";

        String TOTAL_COUNT = "total_count";//播放总次数
    }

    public interface PlaybackQueueColumns {
        String TRACK_ID = "trackid";
        String SOURCE_ID = "sourceid";
        String SOURCE_TYPE = "sourcetype";
        String SOURCE_POSITION = "sourceposition";
    }

    public interface IncludeExcludeColumns {
        String ID = "_id";
        String DATA = "data";
        String TYPE = "type";
    }

    public interface PlaylistColumns {
        String ID = "_id";
        String NAME = "name";
        String ORDER = "_order";
        String ADD_TIME = "add_time";
        String MODIFY_TIME = "modify_time";
        /**
         * playlist type 0:local 1: local youtube 2: online youtube
         */
        String TYPE = "type";
    }

    public interface PlaylistMusicColumns {
        String ID = "_id";
        String PLAYLIST_ID = "playlist_id";
        String ORDER = "_order";
        String ADD_TIME = "add_time";//添加时间
        String DATA = "_data";
    }

    interface MusicColumns {
        String ID = "_id";
        String DATA = "_data";
        String SIZE = "_size";
        String DATE_MODIFIED = "data_modified";
        String DATE_ADDED = "data_added";
        String DURATION = "duration";
        String TITLE = "title";
        String ARTIST_ID = "artist_id";
        String ARTIST = "artist";
        String ALBUM_ID = "album_id";
        String ALBUM = "album";

        String GENRE_ID = "genre_id";
        String GENRE = "genre";

        String MIME_TYPE = "mime_type";

        String MUSIC_COVER = "cover_url";
        String TRACK = "track";
        //是否隐藏掉
        String IS_HIDDEN = "is_hidden";
    }

    interface AlbumColumns {
        String ID = "_id";
        String ALBUM = "album";
        /**
         * The artist whose songs appear on this album
         * <P>Type: TEXT</P>
         */
        String ARTIST = "artist";
        String ARTIST_ID = "artist_id";
        String ALBUM_ART = "album_art";
        String MODIFIED_ALBUM_ART = "modified_album_art";

        String NUMBER_OF_TRACKS = "number_of_tracks";

    }

    interface ArtistColumns {
        String ID = "_id";
        String ARTIST = "artist";
        String ARTIST_ART = "artist_art";

        String NUMBER_OF_ALBUMS = "number_of_albums";

        String NUMBER_OF_TRACKS = "number_of_tracks";
    }

    interface GenreColumns {
        String ID = "_id";
        String NAME = "name";

        String GENRE_COVER = "genre_cover";//曲风的封面
    }

    interface AudioGenreMapColumns {
        String ID = "_id";
        String AUDIO_ID = "audio_id";
        String GENRE_ID = "genre_id";
    }

    int PLAYLIST_LOCAL_MUSIC = 0;
    int PLAYLIST_LOCAL_YOUTUBE = 1;
    int PLAYLIST_ONLINE_YOUTUBE = 2;

    @IntDef({
            PLAYLIST_LOCAL_MUSIC,
            PLAYLIST_LOCAL_YOUTUBE,
            PLAYLIST_ONLINE_YOUTUBE,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlaylistType {
    }

}
