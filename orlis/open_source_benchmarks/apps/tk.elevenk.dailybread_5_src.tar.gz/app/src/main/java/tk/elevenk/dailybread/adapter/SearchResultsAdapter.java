/*
 * SearchResultsAdapter.java is a part of DailybRead
 *     Copyright (C) 2015  John Krause, Eleven-K Software
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.elevenk.dailybread.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import tk.elevenk.dailybread.R;
import tk.elevenk.olapi.data.BookData;
import tk.elevenk.olapi.data.BookList;
import tk.elevenk.olapi.logging.Log;

/**
 * Search Results list adapter to show cover and details
 *
 * Created by John Krause on 1/18/15.
 */
public class SearchResultsAdapter extends BaseAdapter implements ListAdapter {

    private final BookList results;
    private final Context context;

    public SearchResultsAdapter(BookList resultsList, Context context) {
        this.context = context;
        this.results = resultsList;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_search_results, parent, false);
        }

        BookData data = results.get(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView author = (TextView) convertView.findViewById(R.id.author);
        TextView year = (TextView) convertView.findViewById(R.id.year);
        ImageView cover = (ImageView) convertView.findViewById(R.id.cover_image);

        try {
            if (data.getCoverImage() != null && ((Bitmap) data.getCoverImage()).getHeight() > 10) {
                cover.setImageBitmap((Bitmap) data.getCoverImage());
            } else {
                cover.setImageResource(R.drawable.cover_generic);
            }
        } catch (Exception e) {
            Log.e("Unable to set cover image", e);
        }
        try {
            title.setText(data.getTitle());
        } catch (Exception e) {
            Log.e("Unable to set title", e);
        }
        try {
            author.setText(data.getAuthorNames().get(0));
        } catch (Exception e) {
            Log.e("Unable to set author", e);
        }
        try {
            year.setText(data.getFirstPublishYear());
        } catch (Exception e) {
            Log.e("Unable to set Published Year", e);
        }

        return convertView;
    }
}
