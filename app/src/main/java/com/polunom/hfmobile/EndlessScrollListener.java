package com.polunom.hfmobile;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import static com.polunom.hfmobile.forumdisplay.ThreadDisplayFragment.mAdapter;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessScrollListener.class.getSimpleName();
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    public static boolean loading = false; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 6; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private int current_page = 1;
    private FloatingActionButton fab;
    private LinearLayoutManager mLinearLayoutManager;
    public EndlessScrollListener(LinearLayoutManager linearLayoutManager, FloatingActionButton fab) {
        this.mLinearLayoutManager = linearLayoutManager;
        this.fab = fab;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        super.onScrolled(recyclerView, dx, dy);

        if(mAdapter != null) {
            if (mAdapter.snackbar != null) {
                mAdapter.snackbar.dismiss();
            }
        }

        if(dy <= 0){
            if(fab != null) {
                if (!fab.isShown()) {
                    fab.show();
                }
            }
            return;
        }
        if(fab != null) {
            if (fab.isShown()) {
                fab.hide();
            }
        }

        // check for scroll down
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        synchronized (this) {
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached, Do something
                current_page++;
                onLoadMore(current_page);
                loading = true;
            }
        }
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public abstract void onLoadMore(int current_page);
}