package dev.android.player.app.business.sort;

/**
 * Created by guyacong on 2015/9/28.
 */
public final class Pinyin {

    /**
     * 将输入字符转为拼音
     *
     * @param c 输入字符
     * @return return pinyin if c is chinese in uppercase, String.valueOf(c) otherwise.
     */
    public static String toPinyin(char c) {
        if (isChinese(c)) {
            if (c == PinyinData.CHAR_12295) {
                return PinyinData.PINYIN_12295;
            } else if (c == PinyinData.CHAR_35205) {//修正覅字拼音
                return PinyinData.PINYIN_35205;
            } else {
                return PinyinData.PINYIN_TABLE[getPinyinCode(c)];
            }
        } else {
            return String.valueOf(c);
        }
    }


    /**
     * 判断输入字符是否为汉字
     *
     * @param c 输入字符
     * @return return whether c is chinese
     */
    public static boolean isChinese(char c) {
        return c == PinyinData.CHAR_35205//覅 \u8985
                || (PinyinData.MIN_VALUE <= c && c <= PinyinData.MAX_VALUE && getPinyinCode(c) > 0)
                || PinyinData.CHAR_12295 == c;
    }

    private static int getPinyinCode(char c) {
        int offset = c - PinyinData.MIN_VALUE;
        if (0 <= offset && offset < PinyinData.PINYIN_CODE_1_OFFSET) {
            return decodeIndex(PinyinCode1.PINYIN_CODE_PADDING, PinyinCode1.PINYIN_CODE, offset);
        } else if (PinyinData.PINYIN_CODE_1_OFFSET <= offset && offset < PinyinData.PINYIN_CODE_2_OFFSET) {
            return decodeIndex(PinyinCode2.PINYIN_CODE_PADDING, PinyinCode2.PINYIN_CODE, offset - PinyinData.PINYIN_CODE_1_OFFSET);
        } else {
            return decodeIndex(PinyinCode3.PINYIN_CODE_PADDING, PinyinCode3.PINYIN_CODE,
                    offset - PinyinData.PINYIN_CODE_2_OFFSET);
        }
    }

    private static short decodeIndex(byte[] paddings, byte[] indexes, int offset) {
        //CHECKSTYLE:OFF
        int index1 = offset / 8;
        int index2 = offset % 8;
        short realIndex;
        realIndex = (short) (indexes[offset] & 0xff);
        //CHECKSTYLE:ON
        if ((paddings[index1] & PinyinData.BIT_MASKS[index2]) != 0) {
            realIndex = (short) (realIndex | PinyinData.PADDING_MASK);
        }
        return realIndex;
    }
}
