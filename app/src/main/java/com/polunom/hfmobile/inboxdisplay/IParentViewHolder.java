package com.polunom.hfmobile.inboxdisplay;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment.Message;

public class IParentViewHolder extends ParentViewHolder {
    public final View mView;
    public final TextView mTitleView, mSenderView, mDateView;
    public final ImageView mImageView, mIconView;
    public final LinearLayout mContentWrapper;

    public IParentViewHolder(View view) {
        super(view);
        mView = view;
        mImageView = (ImageView) view.findViewById(R.id.pmIcon);
        mIconView = (ImageView) view.findViewById(R.id.goPMIcon);
        mTitleView = (TextView) view.findViewById(R.id.pmTitle);
        mSenderView = (TextView) view.findViewById(R.id.pmSender);
        mDateView = (TextView) view.findViewById(R.id.pmDate);
        mContentWrapper = (LinearLayout) view.findViewById(R.id.mContentWrapper);
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
        return super.toString() + " '" + mTitleView.getText() + "'";
    }
}
