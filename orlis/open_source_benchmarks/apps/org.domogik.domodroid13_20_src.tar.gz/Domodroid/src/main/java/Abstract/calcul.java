/*
 * This file is part of Domodroid.
 *
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 *
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */

package Abstract;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class calcul {

    public static double Round_double(double value) {
        if (2 < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float Round_float(float Rval) {
        float p = (float) Math.pow(10, 2);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return tmp / p;
    }
}
