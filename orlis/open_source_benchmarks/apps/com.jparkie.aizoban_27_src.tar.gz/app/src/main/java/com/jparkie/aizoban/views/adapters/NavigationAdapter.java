package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.utils.wrappers.NavigationWrapper;

import java.util.ArrayList;
import java.util.List;

public class NavigationAdapter extends BaseAdapter {
    private Context mContext;

    private List<NavigationWrapper> mNavigationItems;
    private int mCurrentPosition;

    public NavigationAdapter(Context context, List<NavigationWrapper> navigationItems, int currentPosition) {
        mContext = context;

        mNavigationItems = navigationItems;
        if (mNavigationItems == null) {
            mNavigationItems = new ArrayList<NavigationWrapper>();
        }

        mCurrentPosition = currentPosition;
        if (mCurrentPosition < 0 || mCurrentPosition > mNavigationItems.size() - 1) {
            mCurrentPosition = 0;
        }
    }

    @Override
    public int getCount() {
        return mNavigationItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mNavigationItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View currentView = convertView;

        if (currentView == null) {
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_navigation, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        NavigationWrapper currentNavigationItem = mNavigationItems.get(position);
        if (position == mCurrentPosition) {
            viewHolder.renderView(mContext, currentNavigationItem, mContext.getResources().getColor(R.color.accentPinkA200), mContext.getResources().getColor(R.color.accentPinkA200));
        } else {
            viewHolder.renderView(mContext, currentNavigationItem, mContext.getResources().getColor(R.color.icon), mContext.getResources().getColor(R.color.secondaryText));
        }

        return currentView;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int newPosition) {
        mCurrentPosition = newPosition;

        notifyDataSetInvalidated();
    }

    private static class ViewHolder {
        private ImageView mIconImageView;
        private TextView mTitleTextView;

        public ViewHolder(View itemView) {
            mIconImageView = (ImageView)itemView.findViewById(R.id.iconImageView);
            mTitleTextView = (TextView)itemView.findViewById(R.id.titleTextView);
        }

        public void renderView(Context context, NavigationWrapper navigationItem, int iconColor, int textColor) {
            setIcon(context.getResources().getDrawable(navigationItem.getIconResource()), iconColor);
            setTitle(context.getResources().getString(navigationItem.getTitleResource()), textColor);
        }

        private void setIcon(Drawable icon, int iconColor) {
            mIconImageView.setImageDrawable(icon);
            mIconImageView.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        }

        private void setTitle(String text, int textColor) {
            mTitleTextView.setText(text);
            mTitleTextView.setTextColor(textColor);
        }
    }
}