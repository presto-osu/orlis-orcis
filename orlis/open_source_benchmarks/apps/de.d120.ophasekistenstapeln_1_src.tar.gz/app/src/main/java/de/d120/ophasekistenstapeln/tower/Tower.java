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
import java.util.ArrayList;
import java.util.List;

/**
 * A Tower is a list of boxes which are ordered.
 *
 * @author saibot2013
 */
public class Tower implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<Box> boxes;
    private String name;

    public Tower() {
        this.boxes = new ArrayList<Box>();
        this.name = "";
    }

    /**
     * Add the given box to tower
     *
     * @param b the given box
     * @return true, if box successfully added, else false
     */
    public boolean add(Box b) {
        return this.boxes.add(b);
    }

    /**
     * Removes box on top of the tower
     *
     * @return true if box removed, otherwise false
     */
    public boolean deleteLastBox() {
        if (this.boxes.size() == 0) {
            return false;
        }
        return this.boxes.remove(this.boxes.size() - 1) != null;
    }

    /**
     * Get all boxes of this tower where getBoxes().get(0) is the box at the
     * bottom
     *
     * @return get all boxes of this tower
     */
    public List<Box> getBoxes() {
        return this.boxes;
    }

    /**
     * Returns the current number of floors
     *
     * @return count of floors
     */
    public int getNumFloors() {
        return this.boxes.size();
    }

    /**
     * Returns the value of this tower each box-value is multiplied by it's
     * floor
     */
    public float getValue() {
        float val = 0;
        int floor = 0;
        for (Box box : this.getBoxes()) {
            if (box == null) {
                continue;
            }
            floor++;
            val += box.getValue() * floor;
        }
        return val;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Turm]: {");
        for (Box box : this.getBoxes()) {
            sb.append("[" + box.getValue() + "] ");
        }
        sb.append("}");
        return sb.toString();
    }
}
