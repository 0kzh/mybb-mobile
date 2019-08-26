package com.polunom.hfmobile.threaddisplay;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.polunom.hfmobile.FixedWebView;
import com.polunom.hfmobile.R;

public class PParentViewHolder {
    public final View mView;
    public final ImageView mAvatar, mUserbar;
    public final FixedWebView mContentView;
    public final TextView mAuthorView, mStatusView ,mTimestampView;
    public final LinearLayout mUserbarHolder, mUserMenu;
    public static PostDisplayFragment.Post mItem = null;

    public PParentViewHolder(View view) {
        mView = view;
        mAvatar = (ImageView) view.findViewById(R.id.avatar);
        mUserbar = (ImageView) view.findViewById(R.id.userbar);
        mContentView = (FixedWebView) view.findViewById(R.id.postContent);
        mContentView.getSettings().setJavaScriptEnabled(true);
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            //disable scrolling
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        mContentView.setBackgroundColor(Color.argb(1, 0, 0, 0));
        mAuthorView = (TextView) view.findViewById(R.id.username);
        mStatusView = (TextView) view.findViewById(R.id.status);
        mTimestampView = (TextView) view.findViewById(R.id.timestamp);
        mUserbarHolder = (LinearLayout) view.findViewById(R.id.userbarHolder);
        mUserMenu = (LinearLayout) view.findViewById(R.id.userMenu);

        //disable interaction
        mUserbar.setOnClickListener(null);
        mTimestampView.setOnClickListener(null);
        mContentView.setOnClickListener(null);

    }

//    @Override
//    public String toString() {
//        return super.toString() + " '" + mContentView + "'";
//    }
}
