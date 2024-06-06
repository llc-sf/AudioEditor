package com.san.audioeditor.delete;

import android.app.Activity;

import java.util.List;

import dev.android.player.framework.data.model.Song;


public class DeleteAction {
    private Activity activity;
    private List<Song> songsToDelete;

    public DeleteAction(Activity activity, List<Song> songsToDelete) {
        this.activity = activity;
        this.songsToDelete = songsToDelete;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public List<Song> getSongsToDelete() {
        return songsToDelete;
    }

    public void setSongsToDelete(List<Song> songsToDelete) {
        this.songsToDelete = songsToDelete;
    }
}
