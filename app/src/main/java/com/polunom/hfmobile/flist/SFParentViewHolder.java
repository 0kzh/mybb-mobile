package com.polunom.hfmobile.flist;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.polunom.hfmobile.R;

public class SFParentViewHolder extends ParentViewHolder {
    public final View mView;
    public final ImageView mImageView;
    public final TextView mContentView;
    public final ImageView mIconView;
    public final LinearLayout mContentWrapper;
    public static TabFragment.Forum mItem = null;

    public SFParentViewHolder(View view) {
        super(view);
        mView = view;
        mImageView = (ImageView) view.findViewById(R.id.forumIcon);
        mContentView = (TextView) view.findViewById(R.id.content);
        mIconView = (ImageView) view.findViewById(R.id.goIcon);
        mContentWrapper = (LinearLayout) view.findViewById(R.id.contentWrapper);
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
