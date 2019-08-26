package com.polunom.hfmobile.threaddisplay;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.flist.TabFragment;

public class PChildViewHolder {

    public final View mView;
    public final ImageView mProfile, mLink, mReply;

    public PChildViewHolder(View view) {
        mView = view;

        mProfile = (ImageView) view.findViewById(R.id.toUID);
        mLink = (ImageView) view.findViewById(R.id.link);
        mReply = (ImageView) view.findViewById(R.id.reply);
    }
}
