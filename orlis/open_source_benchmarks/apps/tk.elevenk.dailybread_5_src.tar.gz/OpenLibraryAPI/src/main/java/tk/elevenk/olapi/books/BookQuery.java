/*
 * BookQuery.java is a part of DailybRead
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

package tk.elevenk.olapi.books;

import java.util.HashMap;

/**
 * Query used when calling a request to the Book API
 *
 * Created by John Krause on 12/26/14.
 */
public class BookQuery extends HashMap<String, String> {

    private static final String BIBKEYS = "bibkeys",
            FORMAT = "format",
            CALLBACK = "callback",
            JSCMD = "jscmd";

    public void isbn(String value) {
        this.put(BIBKEYS, "ISBN:" + value);
    }

    public void oclc(String value) {
        this.put(BIBKEYS, "OCLC:" + value);
    }

    public void lccn(String value) {
        this.put(BIBKEYS, "LCCN:" + value);
    }

    public void olid(String value) {
        this.put(BIBKEYS, "OLID:" + value);
    }

    public void format(String value) {
        this.put(FORMAT, value);
    }

    public void callback(String value) {
        this.put(CALLBACK, value);
    }

    public void jscmd(String value) {
        this.put(JSCMD, value);
    }

}
