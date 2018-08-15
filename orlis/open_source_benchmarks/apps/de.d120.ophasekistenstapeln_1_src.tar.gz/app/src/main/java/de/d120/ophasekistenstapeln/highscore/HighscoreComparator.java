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

import java.util.Comparator;

/**
 * This comparator compares two {@link HighscoreEntry} to each other.
 *
 * @author saibot2013
 */
public class HighscoreComparator implements Comparator<HighscoreEntry> {
    public int compare(HighscoreEntry he1, HighscoreEntry he2) {

        float sc1 = he1.getScore();
        float sc2 = he2.getScore();

        if (sc1 > sc2) {
            return -1;
        } else if (sc1 < sc2) {
            return +1;
        } else {
            return 0;
        }
    }

}
