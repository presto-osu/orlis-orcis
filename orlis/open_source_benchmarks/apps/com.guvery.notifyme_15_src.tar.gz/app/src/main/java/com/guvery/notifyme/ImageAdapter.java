package com.guvery.notifyme;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    // references to our images
    // KEEP THIS PUBLIC
    public static Integer[] mThumbIds = {
            R.drawable.ic_lightbulb, R.drawable.ic_coffee,
            R.drawable.ic_bookmark_on, R.drawable.ic_star_on,
            R.drawable.ic_help, R.drawable.ic_email_unread,
            R.drawable.ic_warning, R.drawable.ic_flag,
            R.drawable.ic_money, R.drawable.ic_search,
            R.drawable.ic_legal, R.drawable.ic_list
    };
    private Context mContext;
    private static Integer[] mThumbIdsDark = {
            R.drawable.ic_lightbulb_dark, R.drawable.ic_coffee_dark,
            R.drawable.ic_bookmark_on_dark, R.drawable.ic_star_on_dark,
            R.drawable.ic_help_dark, R.drawable.ic_email_unread_dark,
            R.drawable.ic_warning_dark, R.drawable.ic_flag_dark,
            R.drawable.ic_money_dark, R.drawable.ic_search_dark,
            R.drawable.ic_legal_dark, R.drawable.ic_list_dark
    };

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setId(mThumbIdsDark[position]);
        imageView.setImageResource(mThumbIdsDark[position]);
        return imageView;
    }

    public int getIndexOfLight(int id) {
        for (int i = 0; i < mThumbIds.length; i++) {
            if (mThumbIds[i] == id)
                return i;
        }
        return id;
    }

    public static int getDarkFromLight(int light) {
        for (int i = 0; i < mThumbIds.length; i++) {
            if (mThumbIds[i] == light)
                return mThumbIdsDark[i];
        }
        return mThumbIds[0];
    }

    public static boolean isAnIcon(int id) {
        for (int i = 0; i < mThumbIds.length; i++) {
            if (mThumbIds[i] == id)
                return true;
        }
        return false;
    }
}