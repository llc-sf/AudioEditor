package dev.android.player.framework.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.List;

public class EmailUtils {
    public final static String PACKAGE_GMAIL = "com.google.android.gm";
    public final static String PACKAGE_EMAIL_APP = "com.android.email";
    private static EmailUtils utils;

    private EmailUtils() {

    }

    public synchronized static EmailUtils getInstance() {
        if (utils == null) {
            utils = new EmailUtils();
        }
        return utils;
    }

    /**
     * 检测是否安装有gmail
     *
     * @param context
     */
    public boolean hasGmail(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            if (packs != null && packs.size() > 0) {
                for (PackageInfo p : packs) {
                    if (!TextUtils.isEmpty(p.packageName) && p.packageName.equals(PACKAGE_GMAIL)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检测是否有email客户端，如果没有Gmail的情况下要使用该方法
     *
     * @return
     */
    public boolean hasEmailApp(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            if (packs != null && packs.size() > 0) {
                for (PackageInfo p : packs) {
                    if (!TextUtils.isEmpty(p.packageName) && p.packageName.equals(PACKAGE_EMAIL_APP)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return false;
    }

}
