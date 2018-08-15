package im.r_c.android.jigsaw.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Jigsaw
 * Created by richard on 16/5/15.
 */
public class SquareGridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpacing;
    private int mSpanCount;

    public SquareGridSpacingItemDecoration(int spacing, int spanCount) {
        mSpacing = spacing;
        mSpanCount = spanCount;
    }

    public SquareGridSpacingItemDecoration(@NonNull Context context, @DimenRes int spacingId, int spanCount) {
        this(context.getResources().getDimensionPixelSize(spacingId), spanCount);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        int row = position / mSpanCount;
        int column = position % mSpanCount;

        outRect.set(mSpacing, mSpacing / 2, mSpacing, mSpacing / 2);

//        if (row == 0) {
//            outRect.bottom = mSpacing / 2;
//        } else if (row == mSpanCount - 1) {
//            outRect.top = mSpacing / 2;
//        } else {
//            outRect.top = mSpacing / 2;
//            outRect.bottom = mSpacing / 2;
//        }
//
//        if (column == 0) {
//            outRect.right = mSpacing / 2;
//        } else if (column == mSpanCount - 1) {
//            outRect.left = mSpacing / 2;
//        } else {
//            outRect.right = mSpacing / 2;
//            outRect.left = mSpacing / 2;
//        }
    }
}
