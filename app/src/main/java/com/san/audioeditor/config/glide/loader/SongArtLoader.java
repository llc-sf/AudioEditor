package com.san.audioeditor.config.glide.loader;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.app.AppProvider;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import dev.android.player.framework.data.MusicContract;
import dev.android.player.framework.data.MusicDataContract;
import dev.android.player.framework.data.model.Song;
import dev.android.player.framework.utils.FileUtils;

/**
 * 歌曲封面加载
 */
public class SongArtLoader implements ModelLoader<Song, InputStream> {


    private static final String TAG = "SongArtLoader";

    /**
     * 优先级
     * 自定义的歌曲封面 > 歌曲专辑的自定义封面 > 歌曲内置的封面 > 歌曲专辑系统的封面
     */

    private ModelLoader<String, InputStream> mLoader;

    public SongArtLoader(ModelLoader<String, InputStream> loader) {
        this.mLoader = loader;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull Song song, int width, int height, @NonNull Options options) {
        Log.d(TAG, "buildLoadData() called with: song = [" + song + "], width = [" + width + "], height = [" + height + "], options = [" + options + "] " + Thread.currentThread());
        return new LoadData<InputStream>(new ModelHashKey(song, width, height), new SongDataFetcher(song));
    }


    @Override
    public boolean handles(@NonNull Song album) {
        return true;
    }


    public static class SongDataFetcher implements DataFetcher<InputStream> {

        private static final String TAG = "SongDataFetcher";

        private final Song song;

        private InputStream mInputStream;

        public SongDataFetcher(Song song) {
            this.song = song;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
            //先获取自定义的封面图
            if (mInputStream == null) {
                if (mInputStream == null) {
                    mInputStream = getMediaMetaCoverInputStream(song.path);
                }
            }
            if (mInputStream != null) {
                callback.onDataReady(mInputStream);
            } else {
                callback.onLoadFailed(new IllegalAccessException("Song Art Load Error"));
            }
        }

        @Override
        public void cleanup() {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void cancel() {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }

        /**
         * 获取歌曲的自定义的封面地址
         *
         * @param song
         * @return
         */
        private String getSongCoverPath(Song song) {
            if (song.cover == null) {
                String[] projection = new String[]{
                        MusicDataContract.MusicColumns.MUSIC_COVER
                };
                String selection = MusicDataContract.MusicColumns.ID + "=?";
                String[] args = new String[]{String.valueOf(song.id)};
                String cover = null;
                try (Cursor cursor = AppProvider.get().getContentResolver()
                        .query(MusicContract.Music.CONTENT_URI, projection, selection, args, null)) {
                    if (cursor.moveToFirst()) {
                        cover = cursor.getString(cursor.getColumnIndexOrThrow(MusicDataContract.MusicColumns.MUSIC_COVER));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                song.cover = cover;
            }
            return song.cover;
        }

        /**
         * 获取歌曲文件中封面图片
         *
         * @return
         */
        private InputStream getMediaMetaCoverInputStream(String path) {
            InputStream stream = null;
            MediaMetadataRetriever retriever = null;
            try {
                retriever = new MediaMetadataRetriever();
                retriever.setDataSource(path);
                byte[] bytes = retriever.getEmbeddedPicture();
                if (bytes != null) {
                    stream = new ByteArrayInputStream(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (retriever != null) {
                        retriever.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return stream;
        }

        /**
         * 获取封面文件
         *
         * @param path
         * @return
         */
        private InputStream getCoverFileInputStream(String path) {
            InputStream stream = null;
            try {
                stream = FileUtils.getFileInputStream(AppProvider.get(), path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stream;
        }
    }


    public static class Factory implements ModelLoaderFactory<Song, InputStream> {

        @NonNull
        @Override
        public ModelLoader<Song, InputStream> build(@NonNull MultiModelLoaderFactory factory) {
            return new SongArtLoader(factory.build(String.class, InputStream.class));
        }

        @Override
        public void teardown() {

        }
    }


}
