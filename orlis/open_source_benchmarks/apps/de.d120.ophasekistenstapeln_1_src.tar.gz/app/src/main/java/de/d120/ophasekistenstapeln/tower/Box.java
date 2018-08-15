/**
 *   This file is part of Ophasenkistenstapeln.
 *
 *   Ophasenkistenstapeln is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Ophasenkistenstapeln is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Ophasenkistenstapeln.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.d120.ophasekistenstapeln.tower;

import java.io.Serializable;

/**
 * A Box is a simple object with a specific value
 *
 * @author saibot2013
 */
public class Box implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private float value;

    /**
     * Creates a new box with given value
     *
     * @param val the value of the box
     */
    public Box(float val) {
        this.value = val;
    }

    /**
     * Returns the value of this box
     *
     * @return the value of
     */
    public float getValue() {
        return this.value;
    }
}
