package com.san.audioeditor.config;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alan
 */
public class ShareProvider extends ContentProvider {
    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
    private static Map<String, String> mCache = new HashMap<>();

    /**
     * Copied from ContentResolver.java
     */
    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }

    private static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Uri getUriForFile(Context context, File file) {
        if (file != null) {
            byte[] encode = Base64.encode(file.getAbsolutePath().getBytes(), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);

            String path = new String(encode);

            return new Uri.Builder().scheme("content").authority(context.getPackageName() + ".file").encodedPath(path).build();
        }
        return null;
    }

    private static File getFileForUri(Uri uri) {
        if (uri != null) {
            String path = uri.getPath();

            if (path != null) {

                String filePath = mCache.get(path);

                if (filePath == null) {
                    if (path.startsWith("/"))
                        path = path.substring(1);

                    byte[] result = Base64.decode(path, Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);

                    filePath = new String(result);
                }

                return new File(filePath);
            }
        }
        return null;
    }

    private static Uri getUriAndAddToCache(Context context, File file) {
        Uri uri = getUriForFile(context, file);
        if (uri != null)
            mCache.put(uri.getPath(), file.getAbsolutePath());
        return uri;
    }

    public static Uri getShareUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= 23) {
            // 6.0 以上用自己的provider，防止第三方应用权限不足
            return getUriAndAddToCache(context, file);
        } else return Uri.fromFile(file);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // ContentProvider has already checked granted permissions
        final File file = getFileForUri(uri);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = file.getName();
            } else if (OpenableColumns.SIZE.equals(col)) {
                cols[i] = OpenableColumns.SIZE;
                values[i++] = file.length();
            } else if ("_data".equalsIgnoreCase(col)) {
                cols[i] = "_data";
                values[i++] = file.getAbsolutePath();
            }
        }

        cols = copyOf(cols, i);
        values = copyOf(values, i);

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        // ContentProvider has already checked granted permissions
        final File file = getFileForUri(uri);

        final int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = file.getName().substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external inserts");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // ContentProvider has already checked granted permissions
        final File file = getFileForUri(uri);
        return file.delete() ? 1 : 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        // ContentProvider has already checked granted permissions
        final File file = getFileForUri(uri);
        final int fileMode = modeToMode(mode);
        return ParcelFileDescriptor.open(file, fileMode);
    }
}
