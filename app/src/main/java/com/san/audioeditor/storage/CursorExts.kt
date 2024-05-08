package com.san.audioeditor.storage

import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import dev.android.player.framework.data.model.Song
import dev.audio.timeruler.utils.AudioFileUtils

/**
 * 转换单个数据
 */
fun Cursor.convertSong(): Song {
    val song = Song()
    song.id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
    song.path = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
    var fileName = AudioFileUtils.getFileNameWithoutExtension(song.path)
    song.title = if (!TextUtils.isEmpty(fileName)) fileName else getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
    song.albumId = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
    song.albumName = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
    song.artistId = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
    song.artistName = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
    song.track = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK))
    song.size = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
    song.duration = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
    song.dateAdded = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
    song.dateModified = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
    song.mimeType = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        song.genreId = getLong(getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.GENRE_ID))
        song.genre = getString(getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.GENRE))
    }
    return song

}

/**
 * 转换列表数据
 */
internal fun Cursor.convertSongs(): List<Song> {
    val result = mutableListOf<Song>()
    while (moveToNext()) {
        result.add(convertSong())
    }
    return result
}





