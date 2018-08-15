package im.r_c.android.commonrecyclerviewadapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 * CommonRecyclerViewAdapter
 * Created by richard on 16/1/18.
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;

    public ViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);

        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }

        return (T) view;
    }

    public ViewHolder setViewText(int viewId, CharSequence text) {
        ReflectHelper.invokeMethodIfExists("setText", getView(viewId), new Class[]{CharSequence.class}, new Object[]{text});
        return this;
    }

    public ViewHolder setViewImageResource(int viewId, int resId) {
        ReflectHelper.invokeMethodIfExists("setImageResource", getView(viewId), new Class[]{int.class}, new Object[]{resId});
        return this;
    }

    public ViewHolder setViewImageBitmap(int viewId, Bitmap bitmap) {
        ReflectHelper.invokeMethodIfExists("setImageBitmap", getView(viewId), new Class[]{Bitmap.class}, new Object[]{bitmap});
        return this;
    }

    public ViewHolder setViewChecked(int viewId, boolean checked) {
        ReflectHelper.invokeMethodIfExists("setChecked", getView(viewId), new Class[]{boolean.class}, new Object[]{checked});
        return this;
    }

    public ViewHolder setViewOnClickListener(int viewId, View.OnClickListener listener) {
        ReflectHelper.invokeMethodIfExists("setOnClickListener", getView(viewId), new Class[]{View.OnClickListener.class}, new Object[]{listener});
        return this;
    }

    public ViewHolder setViewVisibility(int viewId, int visibility) {
        ReflectHelper.invokeMethodIfExists("setVisibility", getView(viewId), new Class[]{int.class}, new Object[]{visibility});
        return this;
    }

    public ViewHolder setViewTextColor(int viewId, int color) {
        ReflectHelper.invokeMethodIfExists("setTextColor", getView(viewId), new Class[]{int.class}, new Object[]{color});
        return this;
    }
}
