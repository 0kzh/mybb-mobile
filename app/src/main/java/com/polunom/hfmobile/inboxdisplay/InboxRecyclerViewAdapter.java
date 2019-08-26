package com.polunom.hfmobile.inboxdisplay;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment.Message;
import com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment.MessageDetails;
import com.polunom.hfmobile.inboxdisplay.InboxDisplayFragment.OnPMInteractionListener;

import java.util.List;

public class InboxRecyclerViewAdapter extends ExpandableRecyclerAdapter<Message, MessageDetails, ParentViewHolder, ChildViewHolder> {

    private final List<Message> mValues;
    private View view;
    private final int PARENT_ITEM = 0;
    private final int PARENT_PROG = 1;
    private final int PARENT_END = 2;
    private final int CHILD_NORMAL = 3;
    private Context appContext;
    private final OnPMInteractionListener mListener;

    public InboxRecyclerViewAdapter(Context context, List<Message> items, OnPMInteractionListener listener) {
        super(items);
        mValues = items;
        mListener = listener;
        appContext = context;
    }

    @Override
    public int getParentViewType(int parentPosition) {
        try {
            //check for identifier added at end of listThreads()
            if (mValues.get(parentPosition).id >= 0) {
                return PARENT_ITEM;
            } else if (mValues.get(parentPosition).id == -1){
                return PARENT_PROG;
            }
            return PARENT_END;
        }catch(IndexOutOfBoundsException exp){
            return PARENT_END;
        }
    }

    @Override
    public int getChildViewType(int parentPosition, int childPosition) {
        return CHILD_NORMAL;
    }

    @Override
    public boolean isParentViewType(int viewType) {
        return viewType == PARENT_ITEM || viewType == PARENT_PROG || viewType == PARENT_END;
    }

    @Override
    public ParentViewHolder onCreateParentViewHolder(ViewGroup parent, int viewType) {
        ParentViewHolder vh;
        if (viewType == PARENT_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_inbox, parent, false);
            vh = new IParentViewHolder(view);
        } else if (viewType == PARENT_PROG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);
            vh = new ProgressViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.end_of_page, parent, false);
            vh = new ThreadEndViewHolder(view);
        }
        return vh;
    }

    @Override
    public IChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_inbox_child, parent, false);
        return new IChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(ParentViewHolder holder, int position, Message t) {
        if(holder instanceof IParentViewHolder) {
            IParentViewHolder holder1 = (IParentViewHolder) holder;
                final Message message = mValues.get(position);
                if (message.icon.equals("https://hackforums.net/images/modern_bl/new_pm.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.new_pm);
                } else if (message.icon.equals("https://hackforums.net/images/modern_bl/fw_pm.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.fw_pm);
                } else if (message.icon.equals("https://hackforums.net/images/modern_bl/old_pm.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.old_pm);
                } else if (message.icon.equals("https://hackforums.net/images/modern_bl/re_pm.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.re_pm);
                }

                String group = message.senderGroup;
                String username = message.sender;
                if (group.equals("group7")) {
                    //banned
                    username = "<b><font color=#000000>" + username + "</font></b>";
                } else if (group == null || group.isEmpty() || group.equals("null")) {
                    //closed, not in a group
                    username = "<b><font color=#555555>" + username + "</font></b>";
                } else if (group.equals("group2")) {
                    //normal user
                    username = "<b><font color=#EFEFEF>" + username + "</font></b>";
                } else if (group.equals("group29")) {
                    //Ub3r
                    username = "<b><font color=#00AAFF>" + username + "</font></b>";
                } else if (group.equals("group9")) {
                    //L33t
                    username = "<b><font color=#99FF00>" + username + "</font></b>";
                } else if (group.equals("group3")) {
                    //Staff
                    username = "<b><font color=#9999FF>" + username + "</font></b>";
                } else if (group.equals("group4")) {
                    //Admin
                    username = "<b><font color=#FF66FF>" + username + "</font></b>";
                } else {
                    //Custom usergroup
                    username = "<b><font color=#FFFFFF>" + username + "</font></b>";
                }

                String title = message.getName();
                //make Re: and Fw: underlined
                title = title.replaceFirst("Re:", "<b>Re:</b>");
                title = title.replaceFirst("Fw:", "<b>Fw:</b>");

                if (Build.VERSION.SDK_INT >= 24) {
                    holder1.mSenderView.setText(Html.fromHtml(username, Html.FROM_HTML_MODE_LEGACY));
                    holder1.mTitleView.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    holder1.mSenderView.setText(Html.fromHtml(username));
                    holder1.mTitleView.setText(Html.fromHtml(title));
                }

                holder1.mDateView.setText(message.date);
                holder1.mIconView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(message);
                        }
                    }
                });
                t.setInitiallyExpanded(false);
        }
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder h, int parentPos, int childPos, MessageDetails details) {
        if(h instanceof IChildViewHolder) {
            IChildViewHolder holder = (IChildViewHolder) h;
            final MessageDetails td = mValues.get(parentPos).getChildList().get(0);
            final int tid = td.getId();

            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            holder.mForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            holder.mReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    class ProgressViewHolder extends ParentViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.loadMore);
            progressBar.setOnClickListener(null);
            if (isExpanded()) {
                collapseView();
            }
        }
    }

    class ThreadEndViewHolder extends ParentViewHolder {
        public LinearLayout linearLayout;

        public ThreadEndViewHolder(View v) {
            super(v);
            linearLayout = (LinearLayout) v.findViewById(R.id.endOfPage);
            linearLayout.setOnClickListener(null);
            if (isExpanded()) {
                collapseView();
            }
        }
    }
}
