package com.polunom.hfmobile.flist;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.polunom.hfmobile.R;

public class SFChildViewHolder extends ChildViewHolder {

    public final View mView;
    public final TextView mContentView;
    public final LinearLayout mChildWrapper;
    public static TabFragment.Subforum mItem = null;

    public SFChildViewHolder(View view) {
        super(view);
        mView = view;
        mContentView = (TextView) view.findViewById(R.id.childTextView);
        mChildWrapper = (LinearLayout) view.findViewById(R.id.childWrapper);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mContentView.getText() + "'";
    }
}
