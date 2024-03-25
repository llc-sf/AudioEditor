package dev.android.player.framework.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 播放列表备份信息
 */
public class BackupInfo implements Parcelable, Serializable {

    public boolean isManualBackup; // 是否手动备份

    public long time;//备份时间

    public int count;//备份播放列表数量

    public int totals;//备份歌曲总数

    /**
     * 是否过期 12小时过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - time > 12 * 60 * 60 * 1000;
    }

    public BackupInfo(long time, int count, int totals, boolean isManualBackup) {
        this.time = time;
        this.count = count;
        this.totals = totals;
        this.isManualBackup = isManualBackup;
    }

    public String toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("time", time);
            object.put("count", count);
            object.put("totals", totals);
            object.put("isManualBackup", isManualBackup);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static BackupInfo fromJson(String json) {
        BackupInfo backupInfo = new BackupInfo(0, 0, 0, false);
        try {
            JSONObject jsonObject = new JSONObject(json);
            backupInfo.time = jsonObject.optLong("time", 0);
            backupInfo.count = jsonObject.optInt("count", 0);
            backupInfo.totals = jsonObject.optInt("totals", 0);
            backupInfo.isManualBackup = jsonObject.optBoolean("isManualBackup", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return backupInfo;
    }

    protected BackupInfo(Parcel in) {
        isManualBackup = in.readByte() != 0;
        time = in.readLong();
        count = in.readInt();
        totals = in.readInt();
    }

    public static final Creator<BackupInfo> CREATOR = new Creator<BackupInfo>() {
        @Override
        public BackupInfo createFromParcel(Parcel in) {
            return new BackupInfo(in);
        }

        @Override
        public BackupInfo[] newArray(int size) {
            return new BackupInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isManualBackup ? 1 : 0));
        dest.writeLong(time);
        dest.writeInt(count);
        dest.writeInt(totals);
    }
}
