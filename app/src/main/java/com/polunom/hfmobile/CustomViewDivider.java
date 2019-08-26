package com.polunom.hfmobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

public class CustomViewDivider extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int mHeightDp;

    public CustomViewDivider(Context context) {
        this(context, Color.argb((int) (255 * 0.2), 0, 0, 0), 1f);
    }

    public CustomViewDivider(Context context, int color, float heightDp) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        mHeightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int viewType = parent.getAdapter().getItemViewType(position);
        if (viewType == 0) {
            outRect.set(0, 0, 0, mHeightDp);
        } else {
            outRect.setEmpty();
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            int viewType = parent.getAdapter().getItemViewType(position);
            if (viewType == 0) {
                c.drawRect(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + mHeightDp, mPaint);
            }
        }
    }
}