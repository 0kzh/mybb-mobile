package com.polunom.hfmobile.forumdisplay;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.flist.TabFragment;

public class TChildViewHolder extends ChildViewHolder {

    public final View mView;
    public final TextView mLastTime;
    public final TextView mLastAuthor;
    public final LinearLayout mChildWrapper;
    public final ImageView mHide, mProfile, mBrowser;
    public static TabFragment.Subforum mItem = null;

    public TChildViewHolder(View view) {
        super(view);
        mView = view;
        mLastTime = (TextView) view.findViewById(R.id.lastTime);
        mLastAuthor = (TextView) view.findViewById(R.id.lastAuthor);
        mChildWrapper = (LinearLayout) view.findViewById(R.id.childTWrapper);
        mHide = (ImageView) view.findViewById(R.id.hide);
        mProfile = (ImageView) view.findViewById(R.id.profile);
        mBrowser = (ImageView) view.findViewById(R.id.browser);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mLastTime.getText() + "'";
    }
}
