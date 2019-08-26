package com.polunom.hfmobile;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class FixedWebView extends WebView {

    private LongPressListener longPressListener;

    public FixedWebView(Context context) {
        super(context);
    }

    public FixedWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FixedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);

        HitTestResult result = getHitTestResult();
        if (result.getType() == HitTestResult.IMAGE_TYPE || result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            String type;
            if(result.getType() == HitTestResult.IMAGE_TYPE){
                type = "image";
            }else{
                type = "link";
            }

            String src = result.getExtra();
            longPressListener.longPressed(type, src);
        }
    }

    public void setOnLongPressListener(LongPressListener listener) {
        longPressListener = listener;
    }

    public interface LongPressListener {
        void longPressed(String type, String source);
    }
}
