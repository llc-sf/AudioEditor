package dev.android.player.framework.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 */
public class DateUtil {
    /**
     * 格式化时间
     *
     * @param time
     * @return 格式化后的时间
     */
    public static String formatTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * @param unix unix时间戳
     * @return
     */
    public static String makeDateString(long unix) {
        Calendar current = Calendar.getInstance();

        Calendar temp = Calendar.getInstance();
        Date date = new Date(unix * 1000);
        temp.setTime(date);
        if (current.get(Calendar.YEAR) == temp.get(Calendar.YEAR)) {
            return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(date);
        } else {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        }
    }

    public static String convertTimestamp2String(long unix) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(unix * 1000));
    }
}
