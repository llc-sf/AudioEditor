package dev.android.player.framework.utils;
/*
 * Copyright 2015 Mike Penz All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by mikepenz on 14.03.15.
 * This class implements a hack to change the layout padding on bottom if the keyboard is shown
 * to allow long lists with editTextViews
 * Basic idea for this solution found here: http://stackoverflow.com/a/9108219/325479
 */
public class KeyboardUtil {
    private View decorView;
    private View contentView;

    KeyBoardListener listener;

    public KeyboardUtil(Activity act, View contentView) {
        this.decorView = act.getWindow().getDecorView();
        this.contentView = contentView;

        //only required on newer android versions. it was working on API level 19
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void setListener(KeyBoardListener listener) {
        this.listener = listener;
    }

    public void enable() {
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void disable() {
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }


    //a small helper to allow showing the editText focus
    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        private int dd = -9527;

        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            decorView.getWindowVisibleDisplayFrame(r);

            //get screen height and calculate the difference with the useable area from the r
            int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
            int diff = height - r.bottom;

            if (dd == diff) return;
            dd = diff;

            //if it could be a keyboard add the padding to the view
            if (diff != 0) {
                // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
                //check if the padding is 0 (if yes set the padding for the keyboard)
                if (contentView.getPaddingBottom() != diff) {
                    //set the padding of the contentView for the keyboard
                    contentView.setPadding(0, 0, 0, diff);
                }
            } else {
                //check if the padding is != 0 (if yes reset the padding)
                if (contentView.getPaddingBottom() != 0) {
                    //reset the padding of the contentView
                    contentView.setPadding(0, 0, 0, 0);
                }
            }

            if (listener != null) {
                listener.onKeyboardChange(diff > 0, diff);
            }
        }
    };


    /**
     * Helper to hide the keyboard
     *
     * @param act
     */
    public static void hideKeyboard(Activity activity) {
        try {
            if (activity != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                if (view == null) {
                    view = new View(activity);
                }

                boolean result = inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Log.d("KeyboardUtil", "hideKeyboard result: " + result);

                if (!result) {
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    Log.d("KeyboardUtil", "toggleSoftInput called to hide keyboard");
                }
            } else {
                Log.d("KeyboardUtil", "activity is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hideKeyboard(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            InputMethodManager inputMethod = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view == null) view = activity.getWindow().getDecorView();
            if (view != null) {
                inputMethod.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void hideKeyboardFrom(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyBoard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);//SHOW_FORCED，RESULT_HIDDEN
    }

    public static void showOrHidden(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//SHOW_FORCED，RESULT_HIDDEN
    }

    public interface KeyBoardListener {
        /**
         * call back
         *
         * @param isShow         true is show else hidden
         * @param keyboardHeight keyboard height
         */
        void onKeyboardChange(boolean isShow, int keyboardHeight);
    }
}