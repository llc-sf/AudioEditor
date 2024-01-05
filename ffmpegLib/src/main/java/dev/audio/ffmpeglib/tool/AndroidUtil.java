package dev.audio.ffmpeglib.tool;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.provider.Settings.EXTRA_APP_PACKAGE;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AndroidUtil {


    /**
     * 获取当前系统的版本号
     */
    public static int getCurrentSystemVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取当前系统的版本名称
     *
     * @return 版本名称
     */
    public static String getCurrentSystemVersionName() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取应用版本号
     *
     * @param context
     */
    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取应用版本名称
     *
     * @param context
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取国家信息
     */
    public static String getCountry(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();
    }

    /**
     * 获取语言信息
     */
    public static String getLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    /**
     * 获取应用名称
     *
     * @param context
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            appName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.loadLabel(context.getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    /**
     * 获取应用图标
     *
     * @param context
     */
    public static Drawable getAppIcon(Context context) {
        Drawable appIcon = null;
        try {
            appIcon = context.getPackageManager().getApplicationIcon(context.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appIcon;
    }

    /**
     * 获取
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null && !runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }
        return "";
    }


    /**
     * 是否已经安装某个应用
     *
     * @param context
     */
    public static boolean isInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 是否安装Google play
     *
     * @param context
     */
    public static boolean isInstallGooglePlay(Context context) {
        return isInstalled(context, "com.android.vending");
    }

    /**
     * 是否安装Facebook
     */
    public static boolean isInstallFacebook(Context context) {
        return isInstalled(context, "com.facebook.katana");
    }

    /**
     * 是否安装Gmail
     *
     * @param context Android Context
     */
    public static boolean isInstallGmail(Context context) {
        return isInstalled(context, "com.google.android.gm");
    }

    /**
     * 是否安装Twitter
     *
     * @param context Android Context
     */
    public static boolean isInstallTwitter(Context context) {
        return isInstalled(context, "com.twitter.android");
    }

    /**
     * 是否安装Instagram
     *
     * @param context Android Context
     */
    public static boolean isInstallInstagram(Context context) {
        return isInstalled(context, "com.instagram.android");
    }

    /**
     * ContentUri 转换为 FilePath
     *
     * @param context Context
     */
    public static String getFilePathFromContentUri(Context context, Uri uri) {
        String filePath = "";
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        android.database.Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    /**
     * 获取手机存储的路径列表
     *
     * @param context
     */
    public static List<String> getStorageDirectories(Context context) {
        List<String> storageDirectories = new ArrayList<String>();
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
            getVolumePathsMethod.setAccessible(true);
            Object[] invoke = (Object[]) getVolumePathsMethod.invoke(sm);
            for (Object path : invoke) {
                if (path != null) {
                    storageDirectories.add(path.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storageDirectories;
    }


    /**
     * 获取内置SD卡路径
     *
     * @param context
     */
    public static String getInnerSDCardPath(Context context) {
        List<String> storageDirectories = getStorageDirectories(context);
        for (String path : storageDirectories) {
            if (!path.toLowerCase().contains("otg")) {
                return path;
            }
        }
        return null;
    }

    /**
     * 获取外置SD卡路径
     *
     * @param context
     */
    public static String getExternalSDCardPath(Context context) {
        List<String> storageDirectories = getStorageDirectories(context);
        for (String path : storageDirectories) {
            if (path.toLowerCase().contains("otg")) {
                return path;
            }
        }
        return null;
    }

    /**
     * 获取内置手机存储路径
     */
    public static String getInnerPhonePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    /**
     * 判断App是否是是debug
     *
     * @param context
     * @return
     */
    public static boolean isAppIsDebug(Context context) {
        if (context == null) {
            return false;
        }
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是手机设备
     *
     * @param context
     */
    public static boolean isPhone(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 判断是否是平板
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public static boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static String getStoragePermissionPermissionStringAdapter33() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            return Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
    }

    public static String getPicPermissionPermissionStringAdapter33() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            return Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
    }

    public static boolean hasStoragePermissionAdapter33(Context context) {
        return hasPermission(context, getStoragePermissionPermissionStringAdapter33());
    }


    public static boolean hasPicPermissionAdapter33(Context context) {
        return hasPermission(context, getPicPermissionPermissionStringAdapter33());
    }


    /**
     * 是否允许通知
     * return true 有通知权限 else 没有通知权限
     */
    public static boolean hasNotificationPermission(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 获取设备剩余存储空间
     *
     * @return
     */
    public static long getInternalStorageFreeSpace() {
        long freeSpace = 0;
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            freeSpace = availableBlocks * blockSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return freeSpace;
    }

    /**
     * 获取设备全部存储空间
     *
     * @return 设备全部存储空间
     */
    public static long getInternalStorageTotalSpace() {
        long space = 0;
        try {

            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            space = totalBlocks * blockSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return space;
    }


    /**
     * 打开应用消息通知设置页面
     *
     * @param context
     */
    public static void goAndroidSettingNotify(Context context) {
        Intent intent = new Intent();
        try {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            intent.putExtra(EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);

            //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);

            context.startActivity(intent);
        } catch (Exception e) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.putExtra("package", context.getPackageName());
            try {
                context.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 判断当前是不是 深色模式
     *
     * @param context
     * @return true 深色模式 false 亮色模式
     */
    public static boolean isDarkMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    public static String getSignInfo(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES
            );
            Signature[] signatures = packageInfo.signatures;
            Signature signature = signatures[0];
            String signatureString = signature.toCharsString();
            return signatureString;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";

    }

    public static int[] getPositionInScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return location;
    }


    public static void goSettingPage(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    /**
     * 判断是否是Oppo手机 或者 Oppo手机子品牌
     *
     * @return
     */
    public static boolean isOppoDevice() {
        String manufacturer = Build.MANUFACTURER;
        if (TextUtils.isEmpty(manufacturer)) {
            return false;
        }
        return manufacturer.toLowerCase().contains("oppo") || manufacturer.toLowerCase().contains("oneplus") || manufacturer.toLowerCase().contains("realme");
    }

    /**
     * 判断是否是VIVO手机 或者 VIVO手机子品牌
     *
     * @return
     */
    public static boolean isVIVODevice() {
        String manufacturer = Build.MANUFACTURER;
        if (TextUtils.isEmpty(manufacturer)) {
            return false;
        }
        return manufacturer.toLowerCase().contains("vivo") || manufacturer.toLowerCase().contains("bbk") || manufacturer.toLowerCase().contains("iqoo")
                || manufacturer.toLowerCase().contains("vsmart"); // vsmart 是vivo的子品牌
    }

}


