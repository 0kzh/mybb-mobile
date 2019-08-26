package com.polunom.hfmobile.inboxdisplay;

import android.view.View;
import android.widget.ImageView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.polunom.hfmobile.R;

public class IChildViewHolder extends ChildViewHolder {

    public final View mView;
    public final ImageView mDelete, mForward, mReply;

    public IChildViewHolder(View view) {
        super(view);
        mView = view;
        mDelete = (ImageView) view.findViewById(R.id.delete);
        mForward = (ImageView) view.findViewById(R.id.forward);
        mReply = (ImageView) view.findViewById(R.id.reply);
    }
}
