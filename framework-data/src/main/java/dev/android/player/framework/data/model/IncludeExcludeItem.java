package dev.android.player.framework.data.model;


import androidx.annotation.NonNull;


public class IncludeExcludeItem {
    public long id;
    @NonNull
    public String path;
    @Type
    public int type;


    public IncludeExcludeItem() {

    }

    public IncludeExcludeItem(@NonNull String path, @Type int type) {
        this.path = path;
        this.type = type;
    }

    public @interface Type {
        int INCLUDE = 0;
        int EXCLUDE = 1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}