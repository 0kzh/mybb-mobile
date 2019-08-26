package com.polunom.hfmobile;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telecom.Call;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class LightboxActivity extends Activity {

    private String source;
    private PhotoView lightbox;

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_lightbox);
        source = getIntent().getStringExtra("source");

        lightbox = (PhotoView) findViewById(R.id.lightboxView);
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(lightbox);
        mAttacher.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                finish();
                LightboxActivity.this.overridePendingTransition(0, 0);
            }
        });
        Glide.with(this).load(source).into(lightbox);

        if (shouldDimUi()) {
            lightbox.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    /**
     * Determine whether the System UI should be dimmed whilst this Activity is
     * shown. Override the default value in a subclass should you wish to change it
     * @return
     */
    public boolean shouldDimUi() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        HFBrowser.activityResumed();
        overridePendingTransition(0,0);
    }

    @Override
    public void onPause() {
        super.onPause();
        HFBrowser.activityPaused();
        overridePendingTransition(0,0);
    }
}