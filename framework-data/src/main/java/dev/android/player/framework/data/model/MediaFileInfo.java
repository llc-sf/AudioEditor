package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import java.io.File;
import java.util.List;
import java.util.Locale;

public class MediaFileInfo extends IndexModel implements Parcelable, IDeleteFileBean {
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final int MEDIA_TYPE_AUDIO = 2;

    private long id;
    private String mFilePath;
    private String mFileName;
    private int mMediaType;
    private long mDuration = -1;
    private long mDateModified;
    private String mVideoTime = null;

    public long fileSize;

    private boolean isPrivate;

    public String originalPath;

    public int width;

    public int height;

    private String resolution;
    private ExMusicInfo exMusicInfo;
    private String parentPath;

    private DBBean dbBean;

    //清理文件时用到
    public boolean isSelect = false;
    public String watchedDataText;
    //删除重复文件时用到
    private List<MediaFileInfo> child;

    public static final String TITLE = "mFileName";
    public static final String ADDTIEM = "mDateModified";
    public static final String DURATION = "mDuration";
    public static final String FILESIZE = "fileSize";

    private MediaFileInfo(Parcel in) {
        mFilePath = in.readString();
        mFileName = in.readString();
        width = in.readInt();
        height = in.readInt();
        resolution = in.readString();
        mMediaType = in.readInt();
        mDuration = in.readLong();
        mDateModified = in.readLong();
        mVideoTime = in.readString();
        watchedDataText = in.readString();
        fileSize = in.readLong();
        isPrivate = in.readByte() != 0;
        isSelect = in.readByte() != 0;
        originalPath = in.readString();
        dbBean = in.readParcelable(DBBean.class.getClassLoader());
        exMusicInfo = in.readParcelable(ExMusicInfo.class.getClassLoader());
        parentPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFilePath);
        dest.writeString(mFileName);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(resolution);
        dest.writeInt(mMediaType);
        dest.writeLong(mDuration);
        dest.writeLong(mDateModified);
        dest.writeString(mVideoTime);
        dest.writeString(watchedDataText);
        dest.writeLong(fileSize);
        dest.writeByte((byte) (isPrivate ? 1 : 0));
        dest.writeByte((byte) (isSelect ? 1 : 0));
        dest.writeString(originalPath);
        dest.writeParcelable(dbBean, flags);
        dest.writeParcelable(exMusicInfo, flags);
        dest.writeString(parentPath);
    }

    public void setExMusicInfo(ExMusicInfo exMusicInfo) {
        this.exMusicInfo = exMusicInfo;
    }

    public ExMusicInfo getExMusicInfo() {
        return exMusicInfo;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    private void setParentPath() {
        if (TextUtils.isEmpty(mFilePath))
            return;
        File file = new File(mFilePath);
        File parentFile = file.getParentFile();
        if (parentFile != null)
            this.parentPath = parentFile.getAbsolutePath();
    }

    public List<MediaFileInfo> getChild() {
        return child;
    }

    public void setChild(List<MediaFileInfo> child) {
        this.child = child;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaFileInfo> CREATOR = new Creator<MediaFileInfo>() {
        @Override
        public MediaFileInfo createFromParcel(Parcel in) {
            return new MediaFileInfo(in);
        }

        @Override
        public MediaFileInfo[] newArray(int size) {
            return new MediaFileInfo[size];
        }
    };

    public void setDBBean(DBBean dbBean) {
        this.dbBean = dbBean;
        if (dbBean != null) setDuration(dbBean.duration);
    }

    public DBBean getDBBean() {
        return dbBean;
    }

    public long getSeenTime() {
        return dbBean == null ? 0 : dbBean.seenTime;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public MediaFileInfo() {

    }


    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
        if (mFileName == null) {
            mFileName = getNameOfUrl(filePath);
        }
        setParentPath();
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public int getMediaType() {
        return mMediaType;
    }

    public void setMediaType(int mediaType) {
        this.mMediaType = mediaType;
    }

    public void setDuration(long duration) {
        mDuration = duration;
        mVideoTime = formatTime(duration);
    }

    public long getDuration() {
        return mDuration;
    }

    public String getVideoTime() {
        return mVideoTime;
    }

    public long getDateModified() {
        return mDateModified;
    }

    public void setDateModified(long dateModified) {
        this.mDateModified = dateModified;
    }

    public long getTempId() {
        return mFilePath.hashCode();
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    @NonNull
    @Override
    public String toString() {
        return "MediaFileInfo{" +
                "mFilePath='" + mFilePath + '\'' +
                ", mFileName='" + mFileName + '\'' +
                ", mMediaType=" + mMediaType +
                ", width=" + width +
                ", height=" + height +
                ", mDuration=" + mDuration +
                ", mDateModified=" + mDateModified +
                ", mVideoTime='" + mVideoTime + '\'' +
                ", fileSize=" + fileSize +
                ", isPrivate=" + isPrivate +
                ", originalPath='" + originalPath + '\'' +
                ", exMusicInfo=" + exMusicInfo +
                ", parentPath='" + parentPath + '\'' +
                ", dbBean=" + dbBean +
                ", isSelect=" + isSelect +
                ", watchedDataText='" + watchedDataText + '\'' +
                ", child=" + child +
                ",resolution='" + resolution + '\'' +
                '}';
    }

    private String formatTime(long tick) {
        int secs = (int) (tick / 1000);
        int hour = secs / 60 / 60;
        int min = (secs / 60) % 60;
        int sec = secs % 60;
        if (hour == 0 && min == 0 && sec == 0)
            sec = 0;
        if (tick < 3600000)
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        else
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, min, sec);
    }

    private String getNameOfUrl(String url, String defaultName) {
        if (url == null) return defaultName;
        String name = null;
        int pos = Math.max(url.lastIndexOf('/'), url.lastIndexOf(':'));
        if (pos >= 0) {
            int index = url.lastIndexOf('?');
            if (index > pos + 1) name = url.substring(pos + 1, index);
            else name = url.substring(pos + 1);
        }

        if (TextUtils.isEmpty(name))
            name = defaultName;

        return name;
    }

    public String getNameOfUrl(String url) {
        return getNameOfUrl(url, "");
    }

    public MediaFileInfo newInstance() {
        MediaFileInfo mediaFileInfo = new MediaFileInfo();
        mediaFileInfo.mFilePath = mFilePath;
        mediaFileInfo.width = width;
        mediaFileInfo.height = height;
        mediaFileInfo.mDuration = mDuration;
        mediaFileInfo.mFileName = mFileName;
        mediaFileInfo.mMediaType = mMediaType;
        mediaFileInfo.mDateModified = mDateModified;
        mediaFileInfo.mVideoTime = mVideoTime;
        mediaFileInfo.fileSize = fileSize;
        mediaFileInfo.isPrivate = isPrivate;
        mediaFileInfo.originalPath = originalPath;
        mediaFileInfo.exMusicInfo = exMusicInfo;
        mediaFileInfo.parentPath = parentPath;
        mediaFileInfo.dbBean = dbBean;
        mediaFileInfo.isSelect = isSelect;
        mediaFileInfo.watchedDataText = watchedDataText;
        mediaFileInfo.child = child;
        mediaFileInfo.resolution = resolution;
        return mediaFileInfo;
    }

    @NonNull
    @Override
    public String getDeleteFilePath() {
        return mFilePath;
    }

    @Override
    public long getDeleteId() {
        return mFilePath.hashCode();
    }

    @NonNull
    @Override
    public String getDeleteFileName() {
        return mFileName;
    }

    @Override
    public int getFileType() {
        return IDeleteFileBean.TYPE_VIDEO;
    }
}