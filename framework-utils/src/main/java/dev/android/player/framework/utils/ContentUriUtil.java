package dev.android.player.framework.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.jetbrains.annotations.NotNull;

public class ContentUriUtil {


    /**
     * 通过Uri 获取文件的绝对路径
     */
    public static String getAbsolutePath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String path = null;
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Files.FileColumns.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    if (index > -1) {
                        path = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return path;
    }

    /**
     * 通过Uri 获取文件的名称
     */
    @NotNull
    public static String getFileName(@NotNull Activity activity, @NotNull Uri uri) {
        String fileName = null;
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            fileName = uri.getLastPathSegment();
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = activity.getContentResolver().query(uri, new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    if (index > -1) {
                        fileName = cursor.getString(index);
                    }
                }
                cursor.close();
            }
            if (fileName == null) {
                fileName = uri.getLastPathSegment();
            }
        }

        return fileName;
    }
}
