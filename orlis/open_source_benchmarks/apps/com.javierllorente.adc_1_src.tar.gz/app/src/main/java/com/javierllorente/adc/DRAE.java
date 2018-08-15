/*
 * Copyright (C) 2016 Javier Llorente <javier@opensuse.org>
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

package com.javierllorente.adc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DRAE {

    private static final String RAE_SERVER = "dle.rae.es";
    public static final String RAE_URL = "http://" + RAE_SERVER + "/?w=";

    public String encode(String termino) {

        String encoded = null;
        try {
            encoded = URLEncoder.encode(termino, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encoded;
    }
}
