/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
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
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ViewsAdapter extends BaseAdapter
{
    private List<View> mViews;

    ViewsAdapter(List<View> views)
    {
        super();
        mViews = views;
    }

    @Override
    public int getCount()
    {
        return mViews.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mViews.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return mViews.get(position);
    }

    @Override
    public int getViewTypeCount()
    {
        return getCount();
    }
}
