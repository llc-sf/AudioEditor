package com.san.audioeditor.business;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.san.audioeditor.R;
import com.san.audioeditor.config.ShareProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//todo  链接更换
public class ShareBusiness {
    /**
     * 分享歌曲文件
     *
     * @param context
     * @param path
     */
    public static void shareTrack(final Context context, String path) {
        try {
            File file = new File(path);
            Uri uri;
            Intent share = new Intent(Intent.ACTION_SEND);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = ShareProvider.getShareUri(context, file);
                share.setDataAndType(uri, "audio/*");
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
                share.setType("audio/*");
            }

            share.putExtra(Intent.EXTRA_STREAM, uri);
            String args = context.getString(R.string.app_name_place_holder) + "  " + context.getString(R.string.audio_download_link) + " ";
            share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.music_player_share_text, args));
            context.startActivity(Intent.createChooser(share, context.getString(R.string.share)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 分享多个歌曲文件
     *
     * @param context
     * @param paths
     */
    public static void shareTrackList(final Context context, List<String> paths) {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            for (String path : paths) {
                File file = new File(path);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = ShareProvider.getShareUri(context, file);
                } else {
                    uri = Uri.fromFile(file);
                }
                uris.add(uri);
            }

            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.setType("audio/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            String args = context.getString(R.string.app_name_place_holder) + "  " + context.getString(R.string.audio_download_link) + " ";
            share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.music_player_share_text, args));
            context.startActivity(Intent.createChooser(share, context.getString(R.string.share)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
