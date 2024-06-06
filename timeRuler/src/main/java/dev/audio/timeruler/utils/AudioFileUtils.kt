package dev.audio.timeruler.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import com.android.app.AppProvider
import dev.audio.ffmpeglib.FFmpegApplication
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
    const val AUDIO_FOLDER_TEMP = "TempCut"
    const val AUDIO_TEMP_UN_COVER = "temp_un_cover_"
    const val AUDIO_CUT = "cut_"
    var APP_PATH = FFmpegApplication.instance?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + File.separator+ AUDIO_FOLDER + File.separator
    var PUBLIC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + File.separator + AUDIO_FOLDER + File.separator
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

    fun copyAudioToFileStore(src: File,
                             context: Context,
                             dir: String,
                             targetFileName: String): File? {
        if (!src.exists() || !src.canRead()) {
            Log.d("FileUtils", "Source file not found or not readable")
            return null
        }
        if (src.isDirectory()) {
            Log.d("FileUtils", "Source is a directory, expected a file.")
            return null
        }
        val realOutPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + File.separator + dir + File.separator + targetFileName
        val targetFile = File(realOutPath)
        if (targetFile.exists()) {
            targetFile.delete()
        } // Get MIME type based on file extension
        val mimeType = getMimeType(src.getName())

        // Read source file and write to MediaStore
        try {
            FileInputStream(src).use { inputStream ->
                saveAudioToPublicMusic(context, inputStream, dir, targetFileName, mimeType)
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

    private fun saveAudioToPublicMusic(context: Context,
                                       inputStream: InputStream,
                                       dir: String,
                                       fileName: String,
                                       mimeType: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + File.separator + dir)

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

    /**
     * 获取音频文件的比特率并带上单位
     *
     * @param filePath 音频文件路径
     * @return 带单位的比特率（例如：128 kbps），如果无法获取则返回"未知"
     */
    fun getAudioBitRate(filePath: String): String {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(filePath)
            val bitRateStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            mmr.release()
            if (bitRateStr != null) {
                val bitRateKbps = bitRateStr.toInt() / 1000
                "$bitRateKbps kbps"
            } else {
                "未知"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "未知"
        }
    }

    /**
     * 获取音频文件的采样率并带上单位
     *
     * @param filePath 音频文件路径
     * @return 带单位的采样率（例如：44.1 kHz），如果无法获取则返回"未知"
     */
    fun getAudioSampleRate(filePath: String): String {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(filePath)
            val sampleRateStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
            mmr.release()
            if (sampleRateStr != null) {
                val sampleRateKhz = sampleRateStr.toInt() / 1000.0
                "%.1f kHz".format(sampleRateKhz)
            } else {
                "未知"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "未知"
        }
    }


    private fun openRecentFolder(activity: Activity, path: String) {
        val uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:$path")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*") //想要展示的文件类型
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        activity.startActivityForResult(intent, 0)

    }

    /**
     * 删除指定文件夹下的所有文件
     *
     * @param directory 文件夹路径
     * @return 是否成功删除所有文件
     */
    fun deleteAllFilesInDirectory(directory: String): Boolean {
        return true
    }


    fun clearTempFiles() {
        GlobalScope.launch(Dispatchers.IO) {
            deleteAllFilesInDirectory(PUBLIC_PATH + AUDIO_FOLDER_TEMP)
            deleteAllFilesInDirectory(APP_PATH)
        }
    }

    fun openRecentFiles(openDocumentLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        openDocumentLauncher.launch(intent)
    }

    fun openMusicFolder(openDocumentLauncher: ActivityResultLauncher<Intent>) {
        val uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Music")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        }
        openDocumentLauncher.launch(intent)
    }

    fun openFolder(openDocumentLauncher: ActivityResultLauncher<Intent>, path: String) {
        val baseUri = "content://com.android.externalstorage.documents/document/primary:"
        val fullUri = baseUri + Uri.encode(("$path/").replace("/storage/emulated/0/", ""))
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("audio/*") //想要展示的文件类型
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(fullUri))
        openDocumentLauncher.launch(intent)
    }

    fun isAudioFile(uri: Uri): Boolean {
        val mimeType = AppProvider.get().contentResolver.getType(uri)
        return mimeType?.startsWith("audio/") == true
    }

}