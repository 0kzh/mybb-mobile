package com.polunom.hfmobile.forumdisplay;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;
import com.polunom.hfmobile.flist.TabFragment;

public class TParentViewHolder extends ParentViewHolder {
    public final View mView;
    public final ImageView mImageView;
    public final TextView mContentView, mPostsView, mAuthorView;
    public final ImageView mIconView;
    public final LinearLayout mContentWrapper;
    public static ForumThread mItem = null;

    public TParentViewHolder(View view) {
        super(view);
        mView = view;
        mImageView = (ImageView) view.findViewById(R.id.threadIcon);
        mContentView = (TextView) view.findViewById(R.id.threadContent);
        mIconView = (ImageView) view.findViewById(R.id.goTIcon);
        mPostsView = (TextView) view.findViewById(R.id.replies);
        mAuthorView = (TextView) view.findViewById(R.id.author);
        mContentWrapper = (LinearLayout) view.findViewById(R.id.tContentWrapper);
        mContentWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded()) {
                    collapseView();
                } else {
                    expandView();
                }
            }
        });

        mIconView.setOnClickListener(null);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mContentView.getText() + "'";
    }
}
