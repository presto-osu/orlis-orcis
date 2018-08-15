/*
 * BookList.java is a part of DailybRead
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

package tk.elevenk.olapi.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tk.elevenk.olapi.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Book List data type class
 *
 * Created by John Krause on 12/26/14.
 */
public class BookList extends ArrayList<BookData> {

    public BookList() {
        super();
    }

    public BookList(JSONArray jsonArray) {
        this();
        this.processBooks(jsonArray);
    }

    private List<BookData> processBooks(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                this.add(BookData.bookFromSearch((JSONObject) jsonArray.get(i)));
            } catch (JSONException e) {
                //TODO
                Log.e("", e);
            }
        }
        return this;
    }

    @Override
    public BookData get(int index) {
        BookData book;
        if (index < this.size()) {
            book = super.get(index);
        } else {
            book = BookData.bookFromSearch(new JSONObject());
        }

        return book;
    }
}
