package dev.android.player.framework.utils;

import java.util.Locale;
import java.util.regex.Pattern;

public class StringsUtils {

    /**
     * 数字格式化
     * · 4 位数以下直接显示数字
     * <p>
     * · 4~6 位数显示单位 K
     * <p>
     * · 6~9 位数以上显示单位 M
     * <p>
     * · 9 位数以上显示单位 B
     */
    public static String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format(Locale.getDefault(), "%d", number / 1000) + "K";
        } else if (number < 1000000000) {
            return String.format(Locale.getDefault(), "%d", number / 1000000) + "M";
        } else {
            return String.format(Locale.getDefault(), "%d", number / 1000000000) + "B";
        }
    }

    /**
     * 通过搜索的文字获取正则表达式 特殊字符按文字匹配
     */
    public static Pattern getPattern(String query, boolean isCaseSensitive) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            builder.append(Pattern.quote(query.substring(i, i + 1)));
        }
        if (isCaseSensitive) {
            return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
        } else {
            return Pattern.compile(builder.toString());
        }
    }

    public static Pattern getPattern(String query) {
        return getPattern(query, true);
    }

}
