package com.polunom.hfmobile;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ScrollView;

//Custom ScrollView that detects up/down scroll
//From: https://stackoverflow.com/a/33140842/5538168
public class ObservableScrollView extends ScrollView {
    private static final int DEFAULT_THRESHOLD_DP = 4;

    private ScrollDirectionListener scrollDirectionListener;
    private int scrollThreshold;

    public ObservableScrollView(Context context) {
        this(context, null);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scrollThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_THRESHOLD_DP, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        if (Math.abs(y - oldY) > scrollThreshold && scrollDirectionListener != null) {
            if (y > oldY) {
                scrollDirectionListener.onScrollUp(Math.abs(y - oldY));
            } else {
                scrollDirectionListener.onScrollDown(Math.abs(y - oldY));
            }
        }
    }

    public void setOnScrollDirectionListener(ScrollDirectionListener listener) {
        scrollDirectionListener = listener;
    }

    public interface ScrollDirectionListener {
        void onScrollDown(int pixels);
        void onScrollUp(int pixels);
    }
}