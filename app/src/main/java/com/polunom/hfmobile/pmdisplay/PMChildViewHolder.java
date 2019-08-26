package com.polunom.hfmobile.pmdisplay;

import android.view.View;
import android.widget.ImageView;

import com.polunom.hfmobile.R;

//duplicate of IChildViewHolder without extending ChildViewHolder
public class PMChildViewHolder {

    public final View mView;
    public final ImageView mDelete, mForward, mReply;

    public PMChildViewHolder(View view) {
        mView = view;
        mDelete = (ImageView) view.findViewById(R.id.delete);
        mForward = (ImageView) view.findViewById(R.id.forward);
        mReply = (ImageView) view.findViewById(R.id.reply);
    }
}
