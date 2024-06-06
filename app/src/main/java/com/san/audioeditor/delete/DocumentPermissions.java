package com.san.audioeditor.delete;

import android.annotation.TargetApi;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.android.app.AppProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.android.player.framework.utils.PreferencesUtility;
import dev.android.player.framework.utils.TrackerMultiple;


public class DocumentPermissions {

    private static final String TAG = "DocumentPermissions";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getDocumentTree() {
        String treeUri = PreferencesUtility.getInstance(AppProvider.get()).getDocumentTreeUri();
        List<UriPermission> perms = AppProvider.get().getContentResolver().getPersistedUriPermissions();
        for (UriPermission perm : perms) {
            if (perm.getUri().toString().equals(treeUri) && perm.isWritePermission())
                return treeUri;
        }
        return null;
    }


    static DocumentFile getDocumentFile(Uri treeUri, final File file) {
        String baseFolder = getExtSdCardFolder(file);

        if (baseFolder == null) {
            return null;
        }

        if (treeUri == null) {
            return null;
        }

        String relativePath;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(AppProvider.get(), treeUri);

        String[] parts = relativePath.split("/");
        for (String part : parts) {
            DocumentFile nextDocument = document.findFile(part);
            if (nextDocument != null) {
                document = nextDocument;
            }
        }
        if (document.isFile()) {
            return document;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        try {
            File[] externalFilesDirs = AppProvider.get().getExternalFilesDirs("external");
            if (externalFilesDirs != null && externalFilesDirs.length > 0) {
                for (File file : externalFilesDirs) {
                    if (file != null && !file.equals(AppProvider.get().getExternalFilesDir("external"))) {
                        int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                        if (index < 0) {
                            Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                        } else {
                            String path = file.getAbsolutePath().substring(0, index);
                            try {
                                path = new File(path).getCanonicalPath();
                            } catch (IOException e) {
                                // Keep non-canonical path.
                            }
                            paths.add(path);
                        }
                    }
                }
            }
        } catch (NoSuchMethodError e) {
            TrackerMultiple.onRecordLog("getExtSdCardPaths() failed. " + e.getMessage());
        }
        return paths.toArray(new String[paths.size()]);
    }
}
