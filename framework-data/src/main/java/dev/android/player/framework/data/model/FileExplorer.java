package dev.android.player.framework.data.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * 文件浏览
 */
public class FileExplorer {
    /**
     * 文件显示的名字
     */
    public String name;
    /**
     * 文件路径
     */
    public String path;

    /**
     * 文件大小
     */
    public long size;


    public boolean isFile;

    public boolean isSdcard;

    public boolean isRoot;//是否是根目录

    //是否是文件夹
    public boolean isDirectory;
    //子文件夹
    public ArrayList<File> subDirectories = new ArrayList<>();

    //子文件夹个数
    public int childDirCount;

    //子文件
    public ArrayList<File> subFiles = new ArrayList<>();

    //子文件个数
    public int childFileCount;

    private final File file;

    public Song audio;//当前文件的音频文件

    public boolean isLoaded = false;


    /**
     * @param file
     */
    public FileExplorer(File file, boolean isSdcard, boolean isRoot) {
        this.file = file;
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        this.size = file.length();
        this.isFile = file.isFile();
        this.isDirectory = file.isDirectory();
        this.isSdcard = isSdcard;
        this.isRoot = isRoot;
    }


    public void onLoadChildFile(FileFilter filter) {
        if (isDirectory) {
            File[] files = file.listFiles(filter);
            this.subFiles.clear();
            this.subDirectories.clear();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        subDirectories.add(f);
                    } else {
                        subFiles.add(f);
                    }
                }
            }
            childDirCount = subDirectories.size();
            childFileCount = subFiles.size();
        }
    }


    @Override
    public String toString() {
        return "FileExplorer{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", isFile=" + isFile +
                ", subDirectories=" + subDirectories +
                ", childDirCount=" + childDirCount +
                ", subFiles=" + subFiles +
                ", childFileCount=" + childFileCount +
                ", file=" + file +
                '}';
    }
}
