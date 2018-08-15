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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckableLinearLayout extends LinearLayout implements Checkable
{
    private Checkable mCheckable;

    public CheckableLinearLayout(Context context)
    {
        super(context);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public static CheckableLinearLayout build(Context context, int id, String title, String summary)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CheckableLinearLayout v = (CheckableLinearLayout) inflater.inflate(R.layout.checkable_list_item_2, null);
        v.setId(id);
        TextView textView;
        textView = (TextView) v.findViewById(android.R.id.title);
        textView.setText(title);
        textView = (TextView) v.findViewById(android.R.id.summary);
        textView.setText(summary);
        return v;
    }

    public boolean isChecked()
    {
        return mCheckable.isChecked();
    }

    public void setChecked(boolean isChecked)
    {
        mCheckable.setChecked(isChecked);
    }

    public void toggle()
    {
        mCheckable.toggle();
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        findCheckableView(this);
    }

    private boolean findCheckableView(ViewGroup vg)
    {
        for (int i = 0; i < vg.getChildCount(); ++i)
        {
            View child = vg.getChildAt(i);
            if (child instanceof ViewGroup)
            {
                if (findCheckableView((ViewGroup) child))
                    return true;
            }
            if (child instanceof Checkable)
            {
                mCheckable = (Checkable) child;
                return true;
            }
        }
        return false;
    }
}
