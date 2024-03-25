package dev.android.player.framework.data.model;

/**
 * @author Alan
 */
public class DatabaseConst {

    // 这里的值会存储到db，以后版本值不可改变现有值

    public static final byte PLAYER_ENTRY_FROM_NORMAL = 0x00;
    public static final byte PLAYER_ENTRY_FROM_PRIVATE = 0x01;
    public static final byte PLAYER_ENTRY_FROM_OTHER_APP = 0x02;
    public static final byte PLAYER_ENTRY_FROM_NETWORK_STREAM = 0x03;
    public static final byte PLAYER_ENTRY_FROM_OTG = 0x04;
    public static final int DB_MEDIA_TYPE_VIDEO = 0x000;
    public static final int DB_MEDIA_TYPE_MUSIC = 0x100;

    public static final int PLAYLIST_ID_NONE = -1;
    public static final int PLAYLIST_ID_ALL = -2; // 表示来自 music tab
    public static final int PLAYLIST_ID_FOLDER = -3; // 表示来自 music folder tab
    public static final int PLAYLIST_ID_ALBUM = -4; // 表示来自 music album tab
    public static final int PLAYLIST_ID_ARTIST = -5; // 表示来自 music artist tab
}
