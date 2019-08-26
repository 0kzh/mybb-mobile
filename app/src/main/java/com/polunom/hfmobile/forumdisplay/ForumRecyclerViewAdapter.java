package com.polunom.hfmobile.forumdisplay;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.google.gson.Gson;
import com.polunom.hfmobile.ForumDisplayActivity;
import com.polunom.hfmobile.HFBrowser;
import com.polunom.hfmobile.ProfileActivity;
import com.polunom.hfmobile.R;
import com.polunom.hfmobile.SharedPreference;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.OnForumInteractionListener;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ForumThread;
import com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.ThreadDetails;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.THREADS;

public class ForumRecyclerViewAdapter extends ExpandableRecyclerAdapter<ForumThread, ThreadDetails, ParentViewHolder, ChildViewHolder> {

    private final List<ForumThread> mValues;
    private View view;
    private final int PARENT_ITEM = 0;
    private final int PARENT_PROG = 1;
    private final int PARENT_END = 2;
    private final int CHILD_NORMAL = 3;
    private Context appContext;
    private ImageView mImageView;
    private TextView author, replies;
    private final OnForumInteractionListener mListener;
    public static Snackbar snackbar;

    public ForumRecyclerViewAdapter(Context context, List<ForumThread> items, OnForumInteractionListener listener) {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_forum, parent, false);
            mImageView = (ImageView) view.findViewById(R.id.threadIcon);
            author = (TextView) view.findViewById(R.id.author);
            replies = (TextView) view.findViewById(R.id.replies);

            vh = new TParentViewHolder(view);
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
    public TChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_forum_child, parent, false);
        return new TChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(ParentViewHolder holder, int position, ForumThread t) {
        if(holder instanceof TParentViewHolder) {
            TParentViewHolder holder1 = (TParentViewHolder) holder;
                holder1.mItem = mValues.get(position);
                final ForumThread thread = mValues.get(position);
                if (thread.icon.equals("https://hackforums.net/images/modern_bl/dot_folder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.dot_folder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/folder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.folder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/hotfolder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.hotfolder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/hotlockfolder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.hotlockfolder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/lockfolder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.lockfolder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/newhotfolder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.newhotfolder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/newlockfolder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.newlockfolder);
                } else if (thread.icon.equals("https://hackforums.net/images/modern_bl/newfolder.gif")) {
                    holder1.mImageView.setImageResource(R.drawable.newfolder);
                }

                String name = thread.name;
                if(thread.stickied){
                    name = "<b><font color=#99cc00>" + name + "</font></b>";
                }else if(!thread.icon.contains("new") && !thread.icon.equals("https://hackforums.net/images/modern_bl/dot_folder.gif") && thread.action.equals("forum")){
                    //TODO: Add SimpleDateFormat parsing to only make threads before x days purple

                    name = "<font color=#a7a7f9>" + name + "</font>";
                }else{
                    name = "<font color=#ffffff>" + name + "</font>";
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    holder1.mContentView.setText(Html.fromHtml(name, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    holder1.mContentView.setText(Html.fromHtml(name));
                }

                if(thread.action.equals("forum") || thread.action.equals("thread")){
                    holder1.mAuthorView.setText(thread.author);
                    holder1.mPostsView.setText(thread.posts + " replies");
                }else{
                    //or else post id is passed in thread.action
                    holder1.mPostsView.setVisibility(View.GONE);
                    holder1.mAuthorView.setText(thread.author + "\n\n" + thread.posts + " replies");
                }

                holder1.mIconView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            mListener.onListFragmentInteraction(thread, false);
                        }
                    }
                });
                t.setInitiallyExpanded(false);

        }
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder h, int parentPos, int childPos, ThreadDetails details) {
        if(h instanceof TChildViewHolder) {
            TChildViewHolder holder = (TChildViewHolder) h;
            final ThreadDetails td = mValues.get(parentPos).getChildList().get(0);
            final int tid = td.getId();
            final String profileURL = td.getAuthorProf();
            final int lastPosterId = td.getLastPosterId();
            holder.mLastTime.setText(td.lastPostTime);
            holder.mLastAuthor.setText(td.lastPostAuthor);
            holder.mChildWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        if(td.getParent().action.equals("forum") || td.getParent().action.equals("thread")) {
                            mListener.onListFragmentInteraction(td.getParent(), true);
                        }
                    }
                }
            });
            holder.mHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View v1 = v;
                    final SharedPreference sharedPreference = new SharedPreference();
                    final ForumThread ft = td.getParent();
                    final List<ThreadDetails> temp = ft.mChildrenList;
                    ft.mChildrenList = null; //avoid circular reference when converting to json
                    sharedPreference.addThread(v.getContext(), td.getParent());

                    //remove post from parent activity
                    THREADS.remove(ft);
                    //if returning from thread, update as read
                    ((ForumDisplayActivity) v.getContext()).updateList();

                    //prompt with undo if accident
                    snackbar = Snackbar.make(((ForumDisplayActivity) v.getContext()).findViewById(R.id.coordinatorLayout),
                                              "Thread hidden.", Snackbar.LENGTH_INDEFINITE);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimary));
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sharedPreference.removeThread(v1.getContext(), td.getParent());
                            Snackbar snackbar1 = Snackbar.make(((ForumDisplayActivity) v1.getContext()).findViewById(R.id.coordinatorLayout),
                                    "Restored. Refresh for changes to take effect.", Snackbar.LENGTH_LONG);
                            snackbar1.getView().setBackgroundColor(ContextCompat.getColor(v1.getContext(), R.color.colorPrimary));
                            snackbar1.show();
                        }
                    });
                    snackbar.show();
                }
            });

            if(!td.lastPostTime.equals("In forum:")) {
                holder.mProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(view.getContext(), ProfileActivity.class);
                        i.putExtra("USER_ID", Integer.parseInt(profileURL.split("uid=")[1]));
                        view.getContext().startActivity(i);
                    }
                });
            }else{
                holder.mProfile.setVisibility(View.GONE);
            }

            holder.mBrowser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://hackforums.net/showthread.php?tid=" + tid));
                    appContext.startActivity(browserIntent);
                }
            });
        }
    }

    public static class ProgressViewHolder extends ParentViewHolder {
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

    public static class ThreadEndViewHolder extends ParentViewHolder {
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
