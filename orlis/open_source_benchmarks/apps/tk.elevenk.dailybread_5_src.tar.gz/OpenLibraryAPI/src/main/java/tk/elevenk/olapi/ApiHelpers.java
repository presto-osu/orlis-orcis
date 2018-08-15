/*
 * ApiHelpers.java is a part of DailybRead
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

package tk.elevenk.olapi;

import java.io.*;

/**
 * Some helpers for the API
 *
 * Created by John Krause on 12/28/14.
 */
public class ApiHelpers {

    public static String convertStreamToString(InputStream is) throws Exception {
        //java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        //return s.hasNext() ? s.next() : "";

        // opted for this method since it is faster
        char[] buff = new char[1024];
        Writer stringWriter = new StringWriter();

        try {

            Reader bReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = bReader.read(buff)) != -1) {
                stringWriter.write(buff, 0, n);
            }
        } finally {
            stringWriter.close();
            is.close();
        }
        return stringWriter.toString();
    }

}
