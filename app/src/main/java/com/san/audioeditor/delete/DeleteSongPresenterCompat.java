package com.san.audioeditor.delete;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.toast.ToastCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.android.app.AppProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.audio.timeruler.loading.LoadingDialogComponent;
import dev.android.player.framework.data.model.Song;
import dev.android.player.framework.utils.LogUtils;
import dev.android.player.framework.utils.MediaUtils;
import dev.android.player.framework.utils.PreferencesUtility;
import dev.android.player.framework.utils.ResourceUtils;
import dev.android.player.framework.utils.TrackerMultiple;
import dev.audio.timeruler.player.PlayerManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import kotlin.jvm.JvmStatic;

import com.san.audioeditor.R;
import com.san.audioeditor.delete.dialog.DeleteFailedDialog;
import com.san.audioeditor.delete.dialog.DeleteResultDialog;
import com.san.audioeditor.delete.dialog.DeleteSongDialog;
import com.san.audioeditor.delete.dialog.ISAFPermissionRequest;
import com.san.audioeditor.delete.dialog.SDCardPermissionDialog;


/**
 * 删除歌曲逻辑
 * 兼容Android 5.0以上SAF
 * 兼容Android 11 分区存储
 */
public class DeleteSongPresenterCompat {
    private ActivityResultLauncher<IntentSenderRequest> mDeleteLauncher = null;
    private final ActivityResultLauncher<Uri> mRequestDocumentLauncher;


    public static final String TAG = "DeleteSongPresenterCompat";

    private static final PublishSubject<DeleteAction> DeletePublish = PublishSubject.create();

    public static void onDelete(DeleteAction action) {
        DeletePublish.onNext(action);
    }

    private static PublishSubject<DeleteActionSuccess> DeleteSuccessPublish = null;

    @JvmStatic
    public static Flowable<DeleteActionSuccess> getDeleteActionSuccess() {
        if (DeleteSuccessPublish == null || DeleteSuccessPublish.hasComplete()) {
            DeleteSuccessPublish = PublishSubject.create();
        }
        return DeleteSuccessPublish.toFlowable(BackpressureStrategy.LATEST);
    }

    private AppCompatActivity mActivity;

    private List<Song> mSongs;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    /**
     * Before Activity onCreated
     *
     * @param ac
     */
    public DeleteSongPresenterCompat(AppCompatActivity ac) {
        this.mActivity = ac;
        this.mActivity.getLifecycle().addObserver(new DeleteLifecycleObserver(mDisposable));
        mDisposable.add(DeletePublish.observeOn(AndroidSchedulers.mainThread())
                .subscribe(action -> {
                    if (action.getActivity() == null || action.getActivity() != ac) {
                        return;
                    }
                    List<Song> songs = action.getSongsToDelete();
                    if (songs == null || songs.isEmpty()) {
                        if (DeleteSuccessPublish != null) {
                            DeleteSuccessPublish.onComplete();
                        }
                    } else {
                        onDeleteSong((AppCompatActivity) action.getActivity(), songs);
                    }
                }, e -> {
                    e.printStackTrace();
                    onDeleteException(e);
                })
        );
        //Android 10以后分区存储，请求权限
        mDeleteLauncher = mActivity.registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                onRealDelete();
            } else {
                onDeleteCancel();
            }
        });
        //SAF 适配，请求权限
        mRequestDocumentLauncher = mActivity.registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), result -> {
            if (result != null) {//成功
                Context context = AppProvider.get();
                PreferencesUtility.getInstance(context).setDocumentTreeUri(result.toString());
                context.getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            //不管成功与否，都调用删除方法
            onShowDeleteDialog(mSongs);
        });
    }

    //判断APP的Song数据是否是和系统id相同
    /**
     * 由于Music 使用的是自建数据库，那么可能存在id与系统id不匹配的情况
     * 如果存在不匹配的情况，那么就需要通过路径查询系统数据库中的id，并返回Uri
     */
    private final boolean isAppKeepSongId = true;

    /**
     * 获取Content Uris
     *
     * @param context
     * @param items
     * @return
     */
    private List<Uri> getCompatUris(Context context, List<Song> items) {
        List<Uri> uris = new ArrayList<>();
        if (isAppKeepSongId) {
            for (Song song : items) {
                uris.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id));
            }
        } else {
            String[] projection = new String[]{MediaStore.Audio.Media._ID,};
            String selection = MediaStore.Audio.Media.DATA + "=?";
            for (Song song : items) {
                try (Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        new String[]{String.valueOf(song.path)}, null)) {
                    if (cursor.moveToFirst()) {
                        long id = cursor.getLong(0);
                        uris.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return uris;
    }

    /**
     * 开始删除歌曲
     *
     * @param activity
     * @param songs
     */
    public void onDeleteSong(AppCompatActivity activity, List<Song> songs) {
        this.mSongs = songs;
        this.mActivity = activity;
        onPreRequestPermission(songs);
    }


    /**
     * 预处理权限
     *
     * @param songs
     */
    private void onPreRequestPermission(List<Song> songs) {
        LoadingDialogComponent.show(mActivity);
        mDisposable.add(Flowable.fromCallable(() -> getCompatUris(mActivity, songs))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uris -> {
                    //如果大于Android 5.0先判断是否存在SAF权限问题。
                    boolean hasPermission = true;
                    for (Song song : songs) {
                        File file = new File(song.path);
                        if (!isFileHadPermission(file)) {//如果不存在权限启动SAF权限弹窗
                            hasPermission = false;
                            String name = DocumentPermissions.getExtSdCardFolder(file);
                            onRequestDocumentPermission(mActivity, name);
                            break;
                        }
                    }
                    LoadingDialogComponent.dismissCompat();
                    if (hasPermission) {
                        onShowDeleteDialog(mSongs);
                    }
                }));
    }

    /**
     * 判断文件是否有读写权限
     *
     * @param file
     * @return
     */
    private boolean isFileHadPermission(File file) {
        if (!file.exists()) return true;
        //Android 11 使用 MediaStore去处理默认处理成 只要文件存在都有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return file.exists();
        //内置SD卡文件
        if (DocumentPermissions.getExtSdCardFolder(file) == null) {
            return file.exists() && file.canWrite();
        } else {//SD卡中的文件
            if (DocumentPermissions.getDocumentTree() != null) {
                Uri docUri = Uri.parse(DocumentPermissions.getDocumentTree());
                DocumentFile document = DocumentPermissions.getDocumentFile(docUri, file);
                return document != null && document.canWrite();
            } else {
                return false;
            }
        }
    }

    public void onRequestDocumentPermission(AppCompatActivity context, String extFolder) {
        try {
            SDCardPermissionDialog.show(context, new SAFDeletePermissionRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 展示删除弹窗
     *
     * @param songs
     */
    private void onShowDeleteDialog(List<Song> songs) {
        try {
            TrackerMultiple.onEvent("Delete", "PV");
            DeleteSongDialog.show(mActivity, songs, new DeleteSongDialog.Callback() {
                @Override
                public void onDelete() {
                    onDeleteMediaStore(mActivity, getCompatUris(mActivity, songs));
                }

                @Override
                public void onCancel() {
                    onDeleteCancel();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 从媒体库删除文件
     *
     * @param context
     * @param uris
     */
    private void onDeleteMediaStore(Context context, List<Uri> uris) {
        mDisposable.add(Completable.fromAction(() -> {
                    ContentResolver resolver = context.getContentResolver();
                    ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                    if (uris != null && uris.size() > 0) {
                        for (Uri uri : uris) {
                            operations.add(ContentProviderOperation.newDelete(uri).build());
                        }
                    }
                    //这个地方可能出现异常
                    try {
                        ContentProviderResult[] results = resolver.applyBatch(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.getAuthority(), operations);
                        for (ContentProviderResult result : results) {
                            LogUtils.getInstance(AppProvider.get()).log("onDeleteMediaStore isSuccess = " + (result.count > 0) + " Uri = " + result.uri);
                        }
                        onRealDelete();
                        resolver.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
                    } catch (Exception e) {
                        onDeleteMediaStoreException(context, uris, e);
                        e.printStackTrace();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> onDeleteMediaStoreException(context, uris, throwable))
                .subscribe());
    }

    private void onDeleteMediaStoreException(Context context, List<Uri> uris, Throwable e) {
        PendingIntent intent = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                intent = MediaStore.createDeleteRequest(resolver, uris);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (e instanceof RecoverableSecurityException) {
                    intent = ((RecoverableSecurityException) e).getUserAction().getActionIntent();
                }
            }
            if (intent != null) {
                IntentSenderRequest request = new IntentSenderRequest.Builder(intent.getIntentSender()).build();
                mDeleteLauncher.launch(request);
            } else {//此处就是删除失败。不做处理
                onDeleteException(new Exception("onDeleteMediaStore Exception"));
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            onDeleteException(e1);
        }
    }

    /**
     * 删除取消
     */
    private void onDeleteCancel() {
        if (DeleteSuccessPublish != null) {
            DeleteSuccessPublish.onComplete();
        }
    }

    private void onDispatchDeleteResult(List<String> paths) {
        LoadingDialogComponent.dismissCompat();
        if (paths == null) {
            onDeleteException(new Exception("paths is null"));
        } else {
            if (mSongs.size() == paths.size()) {//全部删除成功
                onDeleteSuccessResult(paths);
            } else if (mSongs.size() > 1 && paths.size() > 1) {//删除多首歌曲,部分删除成功,部分删除失败
                onMultiDeleteFailed(paths);
            } else {//删除一首歌曲删除失败,或者删除多首歌曲全部失败
                onDeleteFailed();
            }
        }
    }

    /**
     * 删除成功
     *
     * @param paths
     */
    private void onDeleteSuccessResult(List<String> paths) {
        TrackerMultiple.onEvent("Delete", "DeleteSuccess");
        final String message = ResourceUtils.makeLabel(mActivity, R.plurals.NNNtracksdeleted, paths.size());
        ToastCompat.makeText(mActivity, message).show();
        LoadingDialogComponent.dismissCompat();
        if (DeleteSuccessPublish != null) {
            DeleteSuccessPublish.onNext(new DeleteActionSuccess(true, paths));
            DeleteSuccessPublish.onComplete();
        }
    }


    /**
     * 其他失败情况
     *
     * @param e
     */
    private void onDeleteException(Throwable e) {
        ToastCompat.makeText(mActivity, false, R.string.delete_failed).show();
        if (DeleteSuccessPublish != null) {
            DeleteSuccessPublish.onComplete();
        }
    }

    /**
     * 一首歌曲删除失败
     */
    private void onDeleteFailed() {
        DeleteFailedDialog.show(mActivity, new SAFDeletePermissionRequest());
    }


    /**
     * 多首歌曲删除失败
     *
     * @param paths 成功的歌曲路径
     */
    private void onMultiDeleteFailed(List<String> paths) {
        //成功的歌曲数量
        List<Song> success = this.mSongs.stream().filter(song -> paths.contains(song.path)).collect(Collectors.toList());
        //剔除已经删除成功的歌曲
        this.mSongs = this.mSongs.stream().filter(song -> !paths.contains(song.path)).collect(Collectors.toList());
        if (DeleteSuccessPublish != null) {
            DeleteSuccessPublish.onNext(new DeleteActionSuccess(false, paths));
        }
        DeleteResultDialog.show(mActivity, success, new SAFDeletePermissionRequest());
    }


    /**
     * 执行真正的文件删除
     */
    private void onRealDelete() {
        LoadingDialogComponent.show(mActivity);
        mDisposable.add(Single.fromCallable(() -> {
                    List<String> paths = new ArrayList<>();
                    List<Long> ids = new ArrayList<>();
                    for (Song song : mSongs) {
                        File file = new File(song.path);
                        if (song.path == PlayerManager.INSTANCE.getPath()) {
                            PlayerManager.INSTANCE.pause();
                        }

                        boolean isSuccess = !file.exists() || file.delete();
                        //尝试SAF进行删除
                        if (!isSuccess && DocumentPermissions.getDocumentTree() != null) {
                            Uri docUri = Uri.parse(DocumentPermissions.getDocumentTree());
                            DocumentFile document = DocumentPermissions.getDocumentFile(docUri, file);
                            if (document != null) {
                                isSuccess = document.delete();
                            }
                        }
                        if (isSuccess) {
                            MediaUtils.scanMedia(mActivity, song.path);
                            ids.add(song.id);
                            paths.add(song.path);
                        }
                    }
                    long[] tracks = new long[ids.size()];
                    for (int i = 0; i < ids.size(); i++) {
                        tracks[i] = ids.get(i);
                    }
                    return paths;//删除成功的路径
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(this::onDispatchDeleteResult, this::onDeleteException));
    }

    /**
     * SD卡权限请求
     */
    private class SAFDeletePermissionRequest implements ISAFPermissionRequest {

        @Override
        public void onAccessPermissionsRequest() {
            mRequestDocumentLauncher.launch(null);
        }

        @Override
        public void onDismissCancel() {
            onDeleteCancel();
        }
    }

}
