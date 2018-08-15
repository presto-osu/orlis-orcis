/*
 * BookReaderFragment.java is a part of DailybRead
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

package tk.elevenk.dailybread.fragment.reader;

/**
 * Interface for book reader fragment
 *
 * Created by John Krause on 12/26/14.
 */
public interface BookReaderFragment {
    public int load(Object book);

    public boolean next();

    public boolean previous();

    public boolean gotoPage(int page);

    interface OnPageLoadedListener{
        public void onPageLoaded(int pageNum);
    };

    public OnPageLoadedListener getListener();

    public void setListener(OnPageLoadedListener listener);
}
