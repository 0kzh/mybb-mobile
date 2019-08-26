package com.polunom.hfmobile.flist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.polunom.hfmobile.R;

import java.util.List;

public class IndexItemRecyclerViewAdapter extends ExpandableRecyclerAdapter<TabFragment.Forum, TabFragment.Subforum, SFParentViewHolder, SFChildViewHolder> {

    private final List<TabFragment.Forum> mValues;
    private View view;
    private LayoutInflater mInflater;
    private ImageView mImageView, mIconView;
    private TextView mTextView;
    private final TabFragment.OnForumInteractionListener mListener;

    public IndexItemRecyclerViewAdapter(Context context, List<TabFragment.Forum> items, TabFragment.OnForumInteractionListener listener) {
        super(items);
        mValues = items;
        mListener = listener;
    }

    @Override
    public SFParentViewHolder onCreateParentViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        mImageView = (ImageView) view.findViewById(R.id.forumIcon);
        return new SFParentViewHolder(view);
    }

    @Override
    public SFChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_child, parent, false);
        return new SFChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(SFParentViewHolder holder, int position, TabFragment.Forum f) {
        holder.mItem = mValues.get(position);
        Glide.with(view.getContext()).load(mValues.get(position).image).apply(new RequestOptions().override(mImageView.getMaxWidth(), mImageView.getMaxHeight())).into(mImageView);
        holder.mContentView.setText(mValues.get(position).name);
        final TabFragment.Forum forum = f;
        holder.mIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(forum);
                }
            }
        });
        f.setInitiallyExpanded(false);
    }

    @Override
    public void onBindChildViewHolder(SFChildViewHolder holder, int parentPos, int childPos, TabFragment.Subforum sf) {
        holder.mContentView.setText(sf.getName());
        final TabFragment.Subforum mySf = sf;
        holder.mChildWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabFragment.Forum myForum = new TabFragment.Forum(mySf.getId(), mySf.getName(), null, null);
                if (null != mListener) {
                    mListener.onListFragmentInteraction(myForum);
                }
            }
        });
    }
}
