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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a List of {@link HighscoreEntry}.
 *
 * @author saibot2013
 */
public class Highscore implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List<HighscoreEntry> scores;

    public Highscore() {
        this.scores = new ArrayList<HighscoreEntry>();
    }

    /**
     * Removes all items from highscore
     */
    public void clear() {
        this.scores.clear();
    }

    /**
     * Adds an entry to the Highscore
     *
     * @param he The entry which schould be added
     */
    public void add(HighscoreEntry he) {
        this.scores.add(he);
    }

    /**
     * Returns a list of {@link HighscoreEntry} where l.get(0) is highest
     * ranking.
     *
     * @return
     */
    public List<HighscoreEntry> getEntries() {
        HighscoreComparator comparator = new HighscoreComparator();
        Collections.sort(this.scores, comparator);

        return this.scores;
    }

    /**
     * Returns an entry at specific position
     *
     * @param position The position on which the entry should be
     * @return the entry@position, or null
     */
    public HighscoreEntry getEntry(int position) {
        if (position < 0 || position >= this.getEntries().size()) {
            return null;
        }
        return this.getEntries().get(position);
    }
}
