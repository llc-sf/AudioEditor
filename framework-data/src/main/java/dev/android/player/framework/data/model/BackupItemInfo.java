package dev.android.player.framework.data.model;

import java.util.List;

/***
 * 播放列表的备份信息
 */
public class BackupItemInfo {
    public String name;//播放列表的名称

    public List<String> paths;//播放列表内的歌曲路径

    public BackupItemInfo(String name, List<String> paths) {
        this.name = name;
        this.paths = paths;
    }
}
