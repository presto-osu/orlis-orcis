/*
 * AndroidLibraryPreferences.java is a part of DailybRead
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

package tk.elevenk.dailybread.preferences;

import android.content.SharedPreferences;
import tk.elevenk.olapi.data.EbookType;
import tk.elevenk.olapi.data.LibraryPreferences;

/**
 * Handles the app preferences
 *
 * Created by John Krause on 1/5/15.
 */
public class AndroidLibraryPreferences implements LibraryPreferences {

    private static final String PREFERRED_EBOOK = "preferredEbook";

    private final SharedPreferences preferences;

    public AndroidLibraryPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public EbookType getPreferredEbookType() {
        return EbookType.valueOf(preferences.getString(PREFERRED_EBOOK, EbookType.EPUB.name()));
    }

    @Override
    public void setPreferredEbookType(EbookType type) {
        preferences.edit().putString(PREFERRED_EBOOK, type.name()).apply();
    }
}
