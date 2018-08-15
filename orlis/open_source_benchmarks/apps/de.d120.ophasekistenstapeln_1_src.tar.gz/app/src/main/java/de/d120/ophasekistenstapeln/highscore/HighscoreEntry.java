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

package de.d120.ophasekistenstapeln.highscore;

import java.io.Serializable;

import de.d120.ophasekistenstapeln.tower.Tower;

/**
 * A HighscoreEntry is a tower bound to a group
 *
 * @author saibot2013
 */
public class HighscoreEntry implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Tower tower;
    private int groupId;

    /**
     * Creates a new HighscoreEntry
     *
     * @param groupId the Id of Group
     * @param tower   The tower of the group
     */
    public HighscoreEntry(int groupId, Tower tower) {
        this.groupId = groupId;
        this.tower = tower;
    }

    /**
     * Returns the score of this entry
     *
     * @return
     */
    public float getScore() {
        return this.tower.getValue();
    }

    /**
     * Returns the GroupId of this entry
     *
     * @return
     */
    public int getGroupId() {
        return this.groupId;
    }

    /**
     * Returns the tower of this entry
     *
     * @return
     */
    public Tower getTower() {
        return this.tower;
    }
}
