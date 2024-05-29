package dev.audio.timeruler.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Locale

object AudioFileUtils {

    const val AUDIO_FOLDER = "AudioEditor338"
    val OUTPUT_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + File.separator + AUDIO_FOLDER


    fun copyAudioToFileStore(src: File, context: Context, targetFileName: String): File? {
        if (!src.exists() || !src.canRead()) {
            Log.d("FileUtils", "Source file not found or not readable")
            return null
        }
        if (src.isDirectory()) {
            Log.d("FileUtils", "Source is a directory, expected a file.")
            return null
        }
        val realOutPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + File.separator + AUDIO_FOLDER + File.separator + targetFileName
        val targetFile = File(realOutPath)
        if (targetFile.exists()) {
            targetFile.delete()
        } // Get MIME type based on file extension
        val mimeType = getMimeType(src.getName())

        // Read source file and write to MediaStore
        try {
            FileInputStream(src).use { inputStream ->
                saveAudioToPublicMusic(context, inputStream, targetFileName, mimeType)
                return targetFile
            }
        } catch (e: FileNotFoundException) {
            Log.e("FileUtils", "File not found: " + e.message)
        } catch (e: Exception) {
            Log.e("FileUtils", "IO Exception: " + e.message)
        }
        return null
    }

    private fun saveAudioToPublicMusic(context: Context,
                                       inputStream: InputStream,
                                       fileName: String,
                                       mimeType: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + File.separator + AUDIO_FOLDER)

        val audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(audioCollection, contentValues)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream!!.write(buffer, 0, bytesRead)
                    }
                }
            } catch (e: IOException) {
                Log.e("FileUtils", "Error writing to MediaStore", e)
            }
        } else {
            Log.e("FileUtils", "Could not insert audio file into MediaStore")
        }
    }

    private fun getMimeType(fileName: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
        if (extension != null) {
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
            if (mimeType != null) {
                return mimeType
            }
        }
        return "audio/*" // Fallback generic audio MIME type
    }


    fun notifyMediaScanner(context: Context,
                           filePath: String,
                           callback: MediaScannerConnection.OnScanCompletedListener) {
        MediaScannerConnection.scanFile(context, arrayOf(filePath), null, callback)
    }


    /**
     * Generates a new file path with an incremented suffix if the file already exists.
     *
     * @param originalPath The original file path as a string.
     * @return The new file path with an incremented suffix.
     */
    fun generateNewFilePath(originalPath: String): String {
        val file = File(originalPath)
        val parentPath = file.parent ?: ""
        val fileNameWithoutExtension = file.nameWithoutExtension
        val extension = file.extension
        var counter = 1

        var newPath: String
        do {
            newPath = "$parentPath/$fileNameWithoutExtension" + "_$counter.$extension"
            counter++
        } while (File(newPath).exists())

        return newPath
    }


    fun getFileName(absolutePath: String): String {
        val file = File(absolutePath)
        return file.name // This returns the file name with extension.
    }

    fun getFileNameWithoutExtension(absolutePath: String): String {
        val file = File(absolutePath)
        return file.nameWithoutExtension // This returns the file name with extension.
    }

    fun getExtension(absolutePath: String): String {
        val file = File(absolutePath)
        return file.extension // This returns the file name with extension.
    }


    fun deleteFile(path: String) {
        if (path.isEmpty()) {
            return
        }
        val file = File(path)
        if (!file.exists()) {
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            file.delete()
        }

    }


}