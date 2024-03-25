package dev.android.player.framework.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.StringDef;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by robotinthesun on 16/11/2017.
 */

public class MediaUtils {

    public static Set<String> ALL_AUDIO_SUFFIX = new HashSet<>();

    static {
        ALL_AUDIO_SUFFIX.add("ogg");
        ALL_AUDIO_SUFFIX.add("oga");//三星S21 手机支持的格式
        ALL_AUDIO_SUFFIX.add("mp3");
        ALL_AUDIO_SUFFIX.add("wma");
        ALL_AUDIO_SUFFIX.add("wav");
        ALL_AUDIO_SUFFIX.add("mp2");
        ALL_AUDIO_SUFFIX.add("ape");
        ALL_AUDIO_SUFFIX.add("aac");
        ALL_AUDIO_SUFFIX.add("flac");
        ALL_AUDIO_SUFFIX.add("m4r");
        ALL_AUDIO_SUFFIX.add("mid");
        ALL_AUDIO_SUFFIX.add("midi");
        ALL_AUDIO_SUFFIX.add("m4a");
        ALL_AUDIO_SUFFIX.add("ac3");
        ALL_AUDIO_SUFFIX.add("amr");
    }

    public static Set<String> getSupportAudios() {
        return ALL_AUDIO_SUFFIX;
    }

    public static boolean isAudioFile(String fileName) {
        if (fileName == null) return false;
        int extPos = fileName.lastIndexOf('.');
        if (extPos >= 0) {
            String ext = fileName.substring(extPos + 1);
            if (MediaUtils.ALL_AUDIO_SUFFIX.contains(ext.toLowerCase(Locale.ENGLISH))) {
                // isVideo
                return true;
            }
        }
        return false;
    }

    /**
     * 通过路径获取文件类型
     *
     * @param filePath
     * @return fileType
     */
    public static String getMediaType(String filePath) {
        String type = MediaType.TYPE_OTHER;
        String end = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        if (ALL_AUDIO_SUFFIX.contains(end)) {
            type = MediaType.TYPE_AUDIO;
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = MediaType.TYPE_VIDEO;
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg")
                || end.equals("bmp")) {
            type = MediaType.TYPE_IMAGE;
        } else if (end.equals("apk")) {
            type = MediaType.TYPE_APK;
        } else {
            type = MediaType.TYPE_OTHER;
        }
        return type;
    }

    @StringDef({
            MediaType.TYPE_AUDIO,
            MediaType.TYPE_VIDEO,
            MediaType.TYPE_IMAGE,
            MediaType.TYPE_APK,
            MediaType.TYPE_OTHER
    })
    public @interface MediaType {
        public static final String TYPE_AUDIO = "audio";
        public static final String TYPE_VIDEO = "video";
        public static final String TYPE_IMAGE = "image";
        public static final String TYPE_APK = "application/vnd.android.package-archive";
        public static final String TYPE_OTHER = "*";
    }


    public static void scanMedia(Context context, String path) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
    }

//
//    public static List<Album> songsToAlbums(List<Song> songs) {
//
//        HashMap<Long, Album> albumMap = new HashMap<>();
//
//        for (Song song : songs) {
//
//            //Create an album representing the album this song belongs to
//
//
//            //Now check if there's already an equivalent album in our albumMap
//            Album oldAlbum = albumMap.get(song.albumId);
//
//            if (oldAlbum != null) {
//
//                //Increment the number of songs.
//                oldAlbum.songCount++;
//
//            } else {
//                if (song.albumName == null) {
//                    song.albumName = "<unknown>";
//                }
//                Album album = new Album(song.albumId, song.albumName, song.artistName, song.artistId, 1, 0);
//                //Couldn't find an existing entry for this album. Add a new one.
//                albumMap.put(album.id, album);
//            }
//        }
//
//        return new ArrayList<>(albumMap.values());
//    }
//
//    public static List<Artist> songsToArtists(List<Song> songs) {
//
//        HashMap<String, Artist> artistMap = new HashMap<>();
//        HashMap<String, List<Long>> artistAlbums = new HashMap<>();
//        LogUtils logger = LogUtils.getInstance(AndroidContext.instance().get());
//        logger.log("Start map song to artist");
//        for (Song song : songs) {
//
//            //Create an album-artist representing the album-artist this album belongs to
//
//            try {
//                //Check if there's already an equivalent album-artist in our albumArtistMap
//                Artist oldArtist = artistMap.get(song.artistName);
//                if (oldArtist != null) {
//
//                    //Add this album to the album artist's albums
//                    oldArtist.albumCount++;
//                    oldArtist.songCount += 1;
//                } else {
//                    if (song.artistName == null) {
//                        song.artistName = "<unknown>";
//                    }
//                    Artist artist = new Artist(song.artistId, song.artistName, 1, 1);
//                    artistMap.put(artist.name, artist);
//                    logger.log("map song to artist:" + song.title + "-->" + song.artistName);
//                }
//                List<Long> albums = artistAlbums.get(song.artistName);
//                long albumId = song.albumId;
//                if (albums == null) {
//                    albums = new ArrayList<>();
//                    albums.add(albumId);
//                    artistAlbums.put(song.artistName, albums);
//                } else {
//                    if (!albums.contains(albumId)) {
//                        albums.add(albumId);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.logException(e, false);
//            }
//        }
//        for (Map.Entry<String, Artist> entry : artistMap.entrySet()) {
//            if (artistAlbums.containsKey(entry.getKey())) {
//                Artist artist = entry.getValue();
//                artist.albumCount = artistAlbums.get(entry.getKey()).size();
//            }
//        }
//        return new ArrayList<>(artistMap.values());
//    }
//
//    public static String getUnknownArtistID(ContentResolver cr) {
//        String unknownArtistId = null;
//        String[] artistProj = {MediaStore.Audio.AudioColumns.ARTIST_ID};
//        String[] artistValue = {MediaStore.UNKNOWN_STRING};
//        String artistWhere = MediaStore.Audio.AudioColumns.ARTIST + "=?";
//        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, artistProj, artistWhere, artistValue, null);
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                unknownArtistId = cursor.getString(0);
//            }
//            cursor.close();
//        }
//        return unknownArtistId;
//    }
//
//    static String getDirAlbumID(ContentResolver cr, String dir) {
//        String albumID = null;
//        String[] albumProj = {MediaStore.Audio.AudioColumns.ALBUM_ID};
//        String[] albumValue = {dir};
//        String albumWhere = MediaStore.Audio.AudioColumns.ALBUM + "=?";
//        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, albumProj, albumWhere, albumValue, null);
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                albumID = cursor.getString(0);
//            }
//            cursor.close();
//        }
//        return albumID;
//    }
//
//    public static void markMusicSupported(Context context, String ext) {
//        try {
//            ContentResolver cr = context.getContentResolver();
//
//            ContentValues contentValues = new ContentValues(5);
//            contentValues.put("is_music", 1);
//            contentValues.put("media_type", 2);
//            contentValues.put("mime_type", "audio/" + ext);
//            String selection = "_data like ? and _data not like '%/.%/%' and _data not like '%/.%' and media_type != ?";
//            String[] selectionArgs = {"%." + ext, "2"};
//            String[] albumProj = {"bucket_display_name"};
//            String selectionGroup = "_data like ? and _data not like '%/.%/%' and _data not like '%/.%' and media_type != ?) group by (?";
//            String[] selectionArgsGroup = {"%." + ext, "2", "bucket_display_name"};
//            Cursor cursor = cr.query(MediaStore.Files.getContentUri("external"), albumProj, selectionGroup, selectionArgsGroup, null);
//            if (cursor != null) {
//                if (cursor.moveToFirst()) {
//                    String unknownArtistId = getUnknownArtistID(cr);
//                    if (unknownArtistId != null) {
//                        contentValues.put(MediaStore.Audio.AudioColumns.ARTIST_ID, unknownArtistId);
//                    }
//                    do {
//                        ContentValues newValues = new ContentValues(contentValues);
//                        String dir = cursor.getString(0);
//                        String albumID = getDirAlbumID(cr, dir);
//                        if (albumID != null) {
//                            newValues.put(MediaStore.Audio.AudioColumns.ALBUM_ID, albumID);
//                        }
//                        cr.update(MediaStore.Files.getContentUri("external"), newValues, selection, selectionArgs);
//                    } while (cursor.moveToNext());
//                }
//                cursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void markFileSupported(Context context, String path) {
//        try {
//            if (path == null) return;
//            int extPos = path.lastIndexOf('.');
//            if (extPos < 0) {
//                return;
//            }
//
//            String ext = path.substring(extPos + 1);
//            ContentResolver cr = context.getContentResolver();
//
//            ContentValues contentValues = new ContentValues(5);
//            contentValues.put("is_music", 1);
//            contentValues.put("media_type", 2);
//            contentValues.put("mime_type", "audio/" + ext);
//            String selection = "_data == ? and media_type != ?";
//            String[] selectionArgs = {path, "2"};
//            String[] albumProj = {"bucket_display_name"};
//            String selectionGroup = selection + ") group by (?";
//            String[] selectionArgsGroup = {path, "2", "bucket_display_name"};
//            Cursor cursor = cr.query(MediaStore.Files.getContentUri("external"), albumProj, selectionGroup, selectionArgsGroup, null);
//            if (cursor != null) {
//                if (cursor.moveToFirst()) {
//                    String unknownArtistId = getUnknownArtistID(cr);
//                    if (unknownArtistId != null) {
//                        contentValues.put(MediaStore.Audio.AudioColumns.ARTIST_ID, unknownArtistId);
//                    }
//                    do {
//                        ContentValues newValues = new ContentValues(contentValues);
//                        String dir = cursor.getString(0);
//                        String albumID = getDirAlbumID(cr, dir);
//                        if (albumID != null) {
//                            newValues.put(MediaStore.Audio.AudioColumns.ALBUM_ID, albumID);
//                        }
//                        cr.update(MediaStore.Files.getContentUri("external"), newValues, selection, selectionArgs);
//                    } while (cursor.moveToNext());
//                }
//                cursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
