package com.masoudss.lib.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

object Utils {

    @JvmStatic
    fun dp(context: Context?, dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context!!.resources.displayMetrics
        )
    }


    fun getAudioUriFromPath(context: Context, filePath: String): Uri? {
        val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            mediaUri, arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ", arrayOf(filePath),
            null
        )
        var uri: Uri? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                uri = Uri.withAppendedPath(mediaUri, id.toString())
            }
            cursor.close()
        }
        return uri.apply {
            Log.i("path=", this.toString())
        }
    }

}

fun Context.uriToFile(uri: Uri) = with(contentResolver) {
    val data = readUriBytes(uri) ?: return@with null
    val extension = getUriExtension(uri)
    File(
        cacheDir.path,
        "${UUID.randomUUID()}.$extension"
    ).also { audio -> audio.writeBytes(data) }
}

fun ContentResolver.readUriBytes(uri: Uri) = openInputStream(uri)
    ?.buffered()?.use { it.readBytes() }

fun ContentResolver.getUriExtension(uri: Uri) = MimeTypeMap.getSingleton()
    .getMimeTypeFromExtension(getType(uri))


