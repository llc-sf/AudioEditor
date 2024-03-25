package musicplayer.playmusic.audioplayer.base;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import dev.android.player.business.service.MusicPlayer;
import dev.android.player.framework.rx.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SlideTrackSwitcher implements View.OnTouchListener {

    private static final int SWIPE_THRESHOLD = 200;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;


    private static final int SWIPE_LEFT = 1;
    private static final int SWIPE_RIGHT = 2;
    private static final int SWIPE_UP = 3;
    private static final int SWIPE_DOWN = 4;


    private GestureDetector mDetector;
    private View mView;
    private Handler mHandler;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public SlideTrackSwitcher() {
    }

    public void attach(@NonNull View v) {
        mView = v;
        mHandler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.what == SWIPE_LEFT) {
                onSwipeLeft();
            } else if (msg.what == SWIPE_RIGHT) {
                onSwipeRight();
            } else if (msg.what == SWIPE_UP) {
                onSwipeUp();
            } else if (msg.what == SWIPE_DOWN) {
                onSwipeDown();
            }
            return false;
        });
        mDetector = new GestureDetector(v.getContext(), new SwipeListener());
        v.setOnTouchListener(this);
    }

    public void detach() {
        disposables.clear();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mView != null) {
            mView.setOnTouchListener(null);
            mView = null;
        }
        mDetector = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {
        disposables.add(RxUtils.completableOnSingle(() -> MusicPlayer.previous(true)));
    }

    public void onSwipeLeft() {
        disposables.add(RxUtils.completableOnSingle(MusicPlayer::next));
    }

    public void onSwipeUp() {
    }


    public void onSwipeDown() {
    }

    private void sendSwipeMessage(int what) {
        Log.d("SlideTrackSwitcher", "sendSwipeMessage() called with: what = [" + what + "]");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(what, 200);
        }
    }

    public void onClick() {

    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.d("SlideTrackSwitcher", "onFling() onSwipeRight");
                            sendSwipeMessage(SWIPE_RIGHT);
                        } else {
                            Log.d("SlideTrackSwitcher", "onFling() onSwipeLeft");
                            sendSwipeMessage(SWIPE_LEFT);
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        Log.d("SlideTrackSwitcher", "onFling() onSwipeDown");
                        sendSwipeMessage(SWIPE_DOWN);
                    } else {
                        Log.d("SlideTrackSwitcher", "onFling() onSwipeUp");
                        sendSwipeMessage(SWIPE_UP);
                    }
                    result = true;
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            disposables.add(RxUtils.completableOnSingle(MusicPlayer::playOrPause));
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onClick();
            return super.onSingleTapConfirmed(e);
        }
    }
}
