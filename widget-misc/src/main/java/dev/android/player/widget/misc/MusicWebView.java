package dev.android.player.widget.misc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;


/**
 * Created by lisao on 12/7/20
 */
public class MusicWebView extends WebView {


    private OnProgressListener mListener;

    public MusicWebView(Context context) {
        super(context);
        init();
    }

    public MusicWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MusicWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//Js与Android交互

        getSettings().setAllowContentAccess(true);//是否允许访问文件
        getSettings().setAllowFileAccessFromFileURLs(true);

//        getSettings().setAppCacheEnabled(true);//设置H5缓存，默认关闭

        //设置Webview自适屏屏幕大小
        getSettings().setUseWideViewPort(true);
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);


        getSettings().setLoadWithOverviewMode(true);
        getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        getSettings().setDomStorageEnabled(true);


        setLayerType(View.LAYER_TYPE_NONE, null);
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        getSettings().setTextZoom(100);

        setWebContentsDebuggingEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, false);
        }

        this.setWebViewClient(new WebClient());
        this.setWebChromeClient(new ChromeClient());
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    WebSettingsCompat.setForceDark(getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    WebSettingsCompat.setForceDark(getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                    break;
            }
        }
    }

    public void setListener(OnProgressListener listener) {
        this.mListener = listener;
    }

    private class WebClient extends WebViewClient {


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mListener != null) mListener.onPageFinished(view, url);
        }
    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (mListener != null) mListener.onProgressChanged(view, newProgress);
        }
    }

    public interface OnProgressListener {

        void onProgressChanged(WebView view, int progress);

        void onPageFinished(WebView view, String url);
    }
}
