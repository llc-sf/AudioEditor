package musicplayer.playmusic.audioplayer.base;

import androidx.annotation.IntDef;

import com.google.android.material.appbar.AppBarLayout;

/**
 * Appbar 滑动监听
 */
public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {


    private int state = CollapsingToolbarLayoutState.IDLE;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int total = appBarLayout.getTotalScrollRange();
        if (offset == 0) {
            if (state != CollapsingToolbarLayoutState.EXPANDED) {
                state = CollapsingToolbarLayoutState.EXPANDED;//修改状态标记为展开
                onStateChange(state, offset, total);
            }
        } else if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
            if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                state = CollapsingToolbarLayoutState.COLLAPSED;//修改状态标记为折叠
                onStateChange(state, offset, total);
            }
        } else {
            if (state != CollapsingToolbarLayoutState.IDLE) {
                state = CollapsingToolbarLayoutState.IDLE;//修改状态标记为中间
                onStateChange(state, offset, total);
            }
        }
        onOffsetChange(offset, total);
    }

    public abstract void onStateChange(int state, int offset, int total);

    public abstract void onOffsetChange(int offset, int total);

    /**
     * 定义可折叠工具栏状态
     */
    @IntDef({
            CollapsingToolbarLayoutState.COLLAPSED,
            CollapsingToolbarLayoutState.EXPANDED,
            CollapsingToolbarLayoutState.IDLE
    })
    public @interface CollapsingToolbarLayoutState {
        int EXPANDED = 1;//展开状态
        int COLLAPSED = -1;//折叠状态
        int IDLE = 0;//
    }
}

