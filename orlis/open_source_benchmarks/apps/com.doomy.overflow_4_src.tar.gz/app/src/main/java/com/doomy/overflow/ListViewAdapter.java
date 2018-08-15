/**
 * Copyright (C) 2013 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.overflow;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<Contact> implements Filterable {

    // Declare your view and variables
    private Activity mActivity;
    private List<Contact> mItems, mDatas;
    private int mRow;
    private Contact mContact;
    private SearchFilter mFilter;

    public ListViewAdapter(Activity activity, int row, List<Contact> items) {
        super(activity, row, items);

        this.mActivity = activity;
        this.mRow = row;
        this.mItems = items;
        this.mDatas = items;
    }

    public int getCount()
    {
        return mItems.size();
    }

    public Contact getItem(int position)
    {
        return mItems.get(position);
    }

    public long getItemId(int position)
    {
        return mItems.get(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View mView = convertView;
        ViewHolder mHolder;
        if (mView == null) {
            LayoutInflater mInflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = mInflater.inflate(mRow, null);

            mHolder = new ViewHolder();
            mView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) mView.getTag();
        }

        if ((mItems == null) || ((position + 1) > mItems.size()))
            return mView;

        mContact = mItems.get(position);

        mHolder.myFullName = (TextView) mView.findViewById(R.id.textViewName);
        mHolder.myPhoneNumber = (TextView) mView.findViewById(R.id.textViewNumb);
        mHolder.myImageContact = (ImageView) mView.findViewById(R.id.imageViewLogo);

        if (mHolder.myFullName != null && null != mContact.getFullName()
                && mContact.getFullName().trim().length() > 0) {
            mHolder.myFullName.setText(Html.fromHtml(mContact.getFullName()));
        }
        if (mHolder.myPhoneNumber != null && null != mContact.getPhoneNumber()
                && mContact.getPhoneNumber().trim().length() > 0) {
            mHolder.myPhoneNumber.setText(Html.fromHtml(mContact.getPhoneNumber()));
        }
        mHolder.myFullName.setTextColor(getContext().getResources().getColor(mContact.getColorDark()));
        mHolder.myImageContact.setColorFilter(getContext().getResources().getColor(mContact.getColorDark()));
        return mView;
    }

    public class ViewHolder {
        public ImageView myImageContact;
        public TextView myFullName, myPhoneNumber;
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SearchFilter();
        }
        return mFilter;
    }

    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            String mConstraint = constraint.toString().toLowerCase();

            FilterResults mResults = new FilterResults();

            List<Contact> mFilteredContacts;

            if (constraint != null && constraint.length() > 0) {

                mFilteredContacts = new ArrayList<>();

                for (Contact mContact : mDatas)
                {
                    if (mContact.getFullName().toLowerCase().contains(mConstraint))
                    {
                        mFilteredContacts.add(mContact);
                    }
                }
            } else {
                mFilteredContacts = mDatas;
            }

            synchronized (this)
            {
                mResults.count = mFilteredContacts.size();
                mResults.values = mFilteredContacts;
            }

            return mResults ;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            List<Contact> mFilteredContacts = (List<Contact>) results.values;
            mItems = mFilteredContacts;
            notifyDataSetChanged();
        }
    }
}
