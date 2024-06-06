package dev.android.player.framework.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dnld on 26/05/16.
 */
public class ContentHelper {

    private static final String TAG = "ContentHelper";
    private static final String PRIMARY_VOLUME_NAME = "primary";

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    private static boolean isWritable(@NonNull final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (java.io.FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }

    private static void scanFile(Context context, String[] paths) {
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    /**
     * Create a folder. The folder may even be on external SD card for Kitkat.
     *
     * @param dir The folder to be created.
     * @return True if creation was successful.
     */
    public static boolean mkdir(Context context, @NonNull final File dir) {
        boolean success = dir.exists();
        // Try the normal way
        if (!success) success = dir.mkdir();

        // Try with Storage Access Framework.
        if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(context, dir, true, true);
            // getDocumentFile implicitly creates the directory.
            success = document != null && document.exists();
        }

        //let MediaStore know that a dir was created
        if (success) scanFile(context, new String[] { dir.getPath() });

        return success;
    }

    private static File getTargetFile(File source, File targetDir) {
        File file = new File(targetDir, source.getName());
        if (!source.getParentFile().equals(targetDir) && !file.exists())
            return file;


        return new File(targetDir, incrementFileNameSuffix(source.getName()));
    }

    private
    static String incrementFileNameSuffix(String name) {
        StringBuilder builder = new StringBuilder();

        int dot = name.lastIndexOf('.');
        String baseName = dot != -1 ? name.subSequence(0, dot).toString() : name;
        String nameWoSuffix = baseName;
        Matcher matcher = Pattern.compile("_\\d").matcher(baseName);
        if(matcher.find()) {
            int i = baseName.lastIndexOf("_");
            if (i != -1) nameWoSuffix = baseName.subSequence(0, i).toString();
        }
        builder.append(nameWoSuffix).append("_").append(new Date().getTime());
        builder.append(name.substring(dot));
        return builder.toString();
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    public static boolean copyFile(Context context, @NonNull final File source, @NonNull final File targetFile, int skipBytesCount, String mime) {
        InputStream inStream = null;
        OutputStream outStream = null;

        boolean success = false;
//        File target = getTargetFile(source, targetDir);

        try {
            inStream = new FileInputStream(source);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                DocumentFile dir = getDocumentFile(context, targetFile.getParentFile(), false, false);
                if (BuildConfig.DEBUG)
                    System.err.println("====== copy file, get dir success : " + (dir != null));
                if (dir != null) {
                    DocumentFile targetDocument = findFileIgnoreCase(dir, targetFile.getName());

                    if (targetDocument == null) {
                        targetDocument = dir.createFile(mime, targetFile.getName());

                        if (BuildConfig.DEBUG)
                            System.err.println("====== copy file, get document success : " + (targetDocument != null) + ", mime : " + mime);
                        if (targetDocument != null) {
                            outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
                            if (BuildConfig.DEBUG)
                                System.err.println("====== copy file, get document stream success : " + (outStream != null));
                        }
                    }
                }

            } else {
                outStream = new FileOutputStream(targetFile);
            }

            if (outStream != null) {
                if (skipBytesCount > 0)
                    inStream.skip(skipBytesCount);

                // Both for SAF and for Kitkat, write to output stream.
                byte[] buffer = new byte[IOUtils.BUFFER_SIZE]; // MAGIC_NUMBER
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) outStream.write(buffer, 0, bytesRead);
                success = true;
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "======= Error when copying file from " + source.getAbsolutePath() + " to " + targetFile.getAbsolutePath(), e);
            return false;
        }
        finally {
            if(inStream != null) try { inStream.close(); } catch (IOException ignored) { }
            if(outStream != null) try { outStream.close(); } catch (IOException ignored) { }
        }

        if (BuildConfig.DEBUG) System.err.println(String.format("====== copy file %s, [%s -> %s]", String.valueOf(success), source.getPath(), targetFile.getPath()));
        if (success) scanFile(context, new String[] { targetFile.getPath() });
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean isFileOnSdCard(Context context, File file) {
        String sdcardPath = getSdcardPath(context);
        return sdcardPath != null && file.getPath().startsWith(sdcardPath);
    }

    /**
     * Get an Uri from an file path.
     *
     * @param path The file path.
     * @return The Uri.
     */
    public static Uri getUriFromFile(Context context, final String path) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[] {BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[] {path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
        if (filecursor == null) {
            return null;
        }
        filecursor.moveToFirst();

        if (filecursor.isAfterLast()) {
            filecursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        }
        else {
            int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
            Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    Integer.toString(imageId)).build();
            filecursor.close();
            return uri;
        }
    }

    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */

    public static boolean deleteFilesInFolder(Context context, @NonNull final File folder) {
        boolean totalSuccess = true;

        String[] children = folder.list();
        if (children != null) {
            for (String child : children) {
                File file = new File(folder, child);
                if (!file.isDirectory()) {
                    boolean success = deleteFile(context, file);
                    if (!success) {
                        Log.w(TAG, "Failed to delete file" + child);
                        totalSuccess = false;
                    }
                }
            }
        }
        return totalSuccess;
    }



    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static boolean deleteFile(Context context, @NonNull final File file) {


        //W/DocumentFile: Failed getCursor: java.lang.IllegalArgumentException: Failed to determine if A613-F0E1:.android_secure is child of A613-F0E1:: java.io.FileNotFoundException: Missing file for A613-F0E1:.android_secure at /storage/sdcard1/.android_secure
        // First try the normal deletion.
        boolean success = file.delete();

        if (!success) {
            success = !file.exists();
            if (BuildConfig.DEBUG) System.err.println("====== delete failed but file not exist.");
        }

        // Try with Storage Access Framework.
        if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(context, file, false, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    success = document != null && document.delete();
                } catch (SecurityException ignore) {
                    // https://www.fabric.io/instashots-projects/android/apps/video.player.videoplayer/issues/5952963ebe077a4dccb0f731
                } catch (IllegalStateException ignore) {
                    // 8.0+ only : https://www.fabric.io/instashots-projects/android/apps/video.player.videoplayer/issues/5b512cec6007d59fcd199869
                }
            } else {
                try {
                    success = document != null && document.delete();
                } catch (SecurityException ignore) {
                    // https://www.fabric.io/instashots-projects/android/apps/video.player.videoplayer/issues/5952963ebe077a4dccb0f731
                }
            }
        }

//        // Try the Kitkat workaround.
//        if (!success && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            ContentResolver resolver = context.getContentResolver();
//
//            try {
//                Uri uri = getUriFromFile(context, file.getAbsolutePath());
//                if (uri != null) {
//                    resolver.delete(uri, null, null);
//                }
//                success = !file.exists();
//            }
//            catch (Exception e) {
//                Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
//                return false;
//            }
//        }

//        if(success) scanFile(context, new String[]{ file.getPath() });
        return success;
    }

    public static boolean renameFile(Context context, @NonNull final File file, String fileName) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(context, file, false, false);
            try {
                return document != null && document.renameTo(fileName);
            } catch (SecurityException ignore) {
                // https://www.fabric.io/instashots-projects/android/apps/video.player.videoplayer/issues/595a4897be077a4dccf9e9d0
                return false;
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static HashSet<File> getStorageRoots(Context context) {
        HashSet<File> paths = new HashSet<File>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                else
                    paths.add(new File(file.getAbsolutePath().substring(0, index)));
            }
        }
        return paths;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getSdcardPath(Context context) {
        for(File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                else
                    return new File(file.getAbsolutePath().substring(0, index)).getPath();
            }
        }
        return null;
    }

    /**
     * Delete a folder.
     *
     * @param file The folder name.
     * @return true if successful.
     */
    public static boolean rmdir(Context context, @NonNull final File file) {

        if(!file.exists() && !file.isDirectory()) return false;

        String[] fileList = file.list();

        if(fileList != null && fileList.length > 0)
            // Delete only empty folder.
            return false;

        // Try the normal way
        if (file.delete()) return true;


        // Try with Storage Access Framework.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(context, file, true, true);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Delete the created entry, such that content provider will delete the file.
            resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
                    new String[] {file.getAbsolutePath()});
        }

        return !file.exists();
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
     * @return The DocumentFile
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static DocumentFile getDocumentFile(Context context, @NonNull final File file, final boolean isDirectory, final boolean createDirectories) {

        Uri treeUri = getTreeUri(context);

        if (treeUri == null) return null;

        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        String sdcardPath = getSavedSdcardPath(context);
        String suffixPathPart = null;

        if (sdcardPath != null) {
            if((file.getPath().indexOf(sdcardPath)) != -1)
                suffixPathPart = file.getAbsolutePath().substring(sdcardPath.length());
        } else {
            HashSet<File> storageRoots = ContentHelper.getStorageRoots(context);
            for(File root : storageRoots) {
                if (root != null) {
                    if ((file.getPath().indexOf(root.getPath())) != -1)
                        suffixPathPart = file.getAbsolutePath().substring(file.getPath().length());
                }
            }
        }

        if (suffixPathPart == null) {
            Log.d(TAG, "unable to find the document file, filePath:"+ file.getPath()+ " root: " + ""+sdcardPath);
            return null;
        }

        if (suffixPathPart.startsWith(File.separator)) suffixPathPart = suffixPathPart.substring(1);

        String[] parts = suffixPathPart.split("/");

        for (int i = 0; i < parts.length; i++) { // 3 is the

            DocumentFile tmp = findFileIgnoreCase(document, parts[i]);
            if (tmp != null)
                document = tmp;
            else {
                if (i < parts.length - 1) {
                    if (createDirectories) document = document.createDirectory(parts[i]);
                    else return null;
                }
                else if (isDirectory) document = document.createDirectory(parts[i]);
                else {
                    if (createDirectories) {
                        try {
                            return document.createFile("image", parts[i]);
                        } catch (Exception ignore) {
                        }
                    }
                    else return null;
                }
            }
        }

        return document;
    }

    private static DocumentFile findFileIgnoreCase(DocumentFile document, String name) {
        if(name == null || document == null) return null;
        DocumentFile tmp = document.findFile(name);
        if (tmp != null)
            return tmp;
        DocumentFile[] child = document.listFiles();
        if (child != null) {
            for (DocumentFile d : child) {
                if (d != null && name.equalsIgnoreCase(d.getName()))
                    return d;
            }
        }
        return null;
    }

    /**
     * Get the stored tree URIs.
     *
     * @return The tree URIs.
     * @param context context
     */
    private static Uri getTreeUri(Context context) {
        String uriString = PreferenceManager.getDefaultSharedPreferences(context).getString("sdTreeUri", null);

        if (uriString == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                List<UriPermission> list = context.getContentResolver().getPersistedUriPermissions();
                if (!list.isEmpty()) return list.get(0).getUri();
            }
            return null;
        }
        return Uri.parse(uriString);
    }

    /**
     * Set a shared preference for an Uri.
     *
     * @param context context
     * @param uri          the target value of the preference.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void saveSdCardInfo(Context context, @Nullable final Uri uri) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("sdTreeUri",
                uri == null ? null : uri.toString());
        editor.putString("sdCardPath", ContentHelper.getSdcardPath(context));
        editor.apply();
    }

    private static String getSavedSdcardPath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("sdCardPath", null);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getMediaPath(final Context context, final Uri uri)
    {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("downloads".equals(uri.getAuthority())) { //download for chrome-dev workaround
            String[] seg = uri.toString().split("/");
            final String id = seg[seg.length - 1];
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                //easy way
                String a = getDataColumn(context, uri, null, null);
                if (a != null) return a;
            } catch (Exception ignored) { }


            // work around for general uri generated by FileProvider.getUriForFile()
            String[] split = uri.getPath().split("/");
            int z = -1, len = split.length;
            for (int i = 0; i < len; i++) {
                if (split[i].equals("external_files")) {
                    z = i;
                    break;
                }
            }

            if(z != -1) {
                StringBuilder partialPath = new StringBuilder();
                for (int i = z + 1; i < len; i++)
                    partialPath.append(split[i]).append('/');

                String p = partialPath.toString();
                for (File file : ContentHelper.getStorageRoots(context)) {
                    File f = new File(file, p);
                    if (f.exists()) return f.getPath();
                }
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to getCursor.
     * @param selection (Optional) Filter used in the getCursor.
     * @param selectionArgs (Optional) Selection arguments used in the getCursor.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
