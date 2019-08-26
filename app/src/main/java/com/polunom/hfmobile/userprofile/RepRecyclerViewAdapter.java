package com.polunom.hfmobile.userprofile;

import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.polunom.hfmobile.ProfileActivity;
import com.polunom.hfmobile.forumdisplay.ForumRecyclerViewAdapter;
import com.polunom.hfmobile.userprofile.ReputationFragment.Reputation;

import com.polunom.hfmobile.R;

import java.util.List;

public class RepRecyclerViewAdapter extends RecyclerView.Adapter<RepRecyclerViewAdapter.ViewHolder> {

    private List<Reputation> items;
    private final int ITEM = 0;
    private final int PROG = 1;
    private final int END = 2;

    public RepRecyclerViewAdapter(List<Reputation> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        if(viewType == ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_reputation, parent, false);
            vh = new ViewHolder(v);
        }else if(viewType == PROG){
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);
            vh = new ProgressViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.end_of_page, parent, false);
            vh = new ThreadEndViewHolder(view);
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position){
        try {
            //check for identifier
            if (items.get(position).authorId >= 0) {
                return ITEM;
            } else if (items.get(position).authorId == -1){
                return PROG;
            }
            return END;
        }catch(IndexOutOfBoundsException exp){
            return PROG;
        }
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            final ViewHolder holder1 = holder;
            final Reputation entry = items.get(position);
            String rating = entry.message.split(":")[0];
            String message = entry.message.split(":")[1];

            if (rating.contains("Positive")) {
                rating = "<font color=#32CD32>" + rating + "</font>: " + message;
            } else if (rating.contains("Negative")) {
                rating = "<font color=#CC3333>" + rating + "</font>: " + message;
            } else if (rating.contains("Neutral")) {
                rating = "<font color=#666666>" + rating + "</font>: " + message;
            }

            String group = entry.authorGroup;
            String username = entry.author;
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

            String reputation = "";
            if (entry.authorRep == null) {
                //not defined (Staff)
                reputation = username;
            } else if (entry.authorRep > 0) {
                reputation = username + "<font color=#ffffff> (</font><font color=#32CD32>" + entry.authorRep + "</font><font color=#ffffff>)</font>";
            } else if (entry.authorRep < 0) {
                reputation = username + "<font color=#ffffff> (</font><font color=#CC3333>" + entry.authorRep + "</font><font color=#ffffff>)</font>";
            } else if (entry.authorRep == 0) {
                reputation = username + "<font color=#ffffff> (</font><font color=#666666>" + entry.authorRep + "</font><font color=#ffffff>)</font>";
            }

            if (Build.VERSION.SDK_INT >= 24) {
                holder.message.setText(Html.fromHtml(rating, Html.FROM_HTML_MODE_LEGACY));
                holder.giver.setText(Html.fromHtml(reputation, Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.message.setText(Html.fromHtml(rating));
                holder.giver.setText(Html.fromHtml(reputation));
            }

            holder.date.setText(entry.date);

            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popupMenu = new PopupMenu(holder1.parent.getContext(), v);
                    final View view = v;
                    popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals("View profile")) {
                                Intent i = new Intent(view.getContext(), ProfileActivity.class);
                                i.putExtra("USER_ID", entry.authorId);
                                view.getContext().startActivity(i);
                            } else if (item.getTitle().equals("View reputation")) {
                                //TODO: Add view reputation functionality
                            }
                            return true;
                        }
                    });

                    popupMenu.show();
                }
            });
        }catch(Exception e){

        }
    }

    @Override public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message, giver, date;
        public LinearLayout parent;

        public ViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.reputationText);
            giver = (TextView) itemView.findViewById(R.id.giver);
            date = (TextView) itemView.findViewById(R.id.date);
            parent = (LinearLayout) itemView.findViewById(R.id.repParent);
        }
    }

    public static class ProgressViewHolder extends ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.loadMore);
            progressBar.setOnClickListener(null);
        }
    }

    public static class ThreadEndViewHolder extends ViewHolder {
        public LinearLayout linearLayout;

        public ThreadEndViewHolder(View v) {
            super(v);
            linearLayout = (LinearLayout) v.findViewById(R.id.endOfPage);
            linearLayout.setOnClickListener(null);
        }
    }
}