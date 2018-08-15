package im.r_c.android.commonrecyclerviewadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * CommonRecyclerViewAdapter
 * Created by richard on 16/1/18.
 */
public abstract class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    private Context mContext;
    private T[] mDataArray;
    private int mLayoutId;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public CommonRecyclerViewAdapter(Context context, T[] dataArray, int layoutId) {
        mContext = context;
        mDataArray = dataArray;
        mLayoutId = layoutId;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, holder.getLayoutPosition());
                }
            });
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mOnItemLongClickListener.onItemLongClick(v, holder.getLayoutPosition());
                }
            });
        }
        onItemViewAppear(holder, mDataArray[position], position);
    }

    @Override
    public int getItemCount() {
        return mDataArray.length;
    }

    public abstract void onItemViewAppear(ViewHolder holder, T t, int position);

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}
