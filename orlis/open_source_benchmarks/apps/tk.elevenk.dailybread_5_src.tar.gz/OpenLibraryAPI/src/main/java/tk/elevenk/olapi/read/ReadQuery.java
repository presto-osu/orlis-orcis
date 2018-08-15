/*
 * ReadQuery.java is a part of DailybRead
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

package tk.elevenk.olapi.read;

import java.util.HashMap;

/**
 * Query object used when calling the Read API
 *
 * Created by John Krause on 12/26/14.
 */
public class ReadQuery extends HashMap<String, String> {

    // *****************************************
    // KEYS
    // *****************************************

    public static final String ISBN = "isbn", LCCN = "lccn", OCLC = "oclc", OLID = "olid";

    public void olid(String value) {
        this.put(OLID, value);
    }

    public void isbn(String value) {
        this.put(ISBN, value);
    }

    public void lccn(String value) {
        this.put(LCCN, value);
    }

    public void oclc(String value) {
        this.put(OCLC, value);
    }

}
