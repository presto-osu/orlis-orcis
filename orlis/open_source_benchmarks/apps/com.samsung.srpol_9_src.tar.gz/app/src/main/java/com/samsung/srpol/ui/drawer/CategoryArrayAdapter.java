/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.ui.drawer;

import java.util.Collection;

import com.samsung.srpol.R;
import com.samsung.srpol.data.Category;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryArrayAdapter extends ArrayAdapter<Category> {
    private final Context mContext;
    private String mTextPrefix ;
    private String mCountTextPrefix;
    private int mSpanTextColor;

    private static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
    }
    
    public CategoryArrayAdapter(Context context) {
        super(context, R.layout.drawer_list_item);
        mTextPrefix = context.getResources().getString(R.string.drawer_header_text_prefix);
        mCountTextPrefix = context.getResources().getString(R.string.items_string_quantity);
        mSpanTextColor = context.getResources().getColor(R.color.text_menu);
        mContext = context;
    }

    @Override
    public void addAll(Collection<? extends Category> collection) {
        for (Category category : collection) {
            super.add(category);
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, parent,
                    false);
            holder = new ViewHolder();
            holder.textView1 = (TextView) convertView.findViewById(R.id.text1);
            holder.textView2 = (TextView) convertView.findViewById(R.id.text2);
            holder.textView3 = (TextView) convertView.findViewById(R.id.text3);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category category = getItem(position);
        if (category.getIconRes() > 0)
            holder.imageView.setImageResource(category.getIconRes());
        holder.textView1.setText(mTextPrefix + category.getHeader());
        holder.textView2.setText(category.getShortDescription());
        holder.textView3.setText(createSpannableCountText(category.getCurrentlyVisible()));
        return convertView;
    }

    private Spannable createSpannableCountText(int count){
        
        Spannable spannable = new SpannableString(mCountTextPrefix + count);
        spannable.setSpan(new ForegroundColorSpan(mSpanTextColor),mCountTextPrefix.length(), spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
