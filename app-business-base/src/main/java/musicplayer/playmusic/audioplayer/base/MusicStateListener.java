package musicplayer.playmusic.audioplayer.base;

/**
 * Listens for playback changes to send the the fragments bound to this activity
 */
public interface MusicStateListener {

    void onRestartLoader();

    void onPlaylistChanged();

    void onMetaChanged();

    void onServiceConnected();
}
