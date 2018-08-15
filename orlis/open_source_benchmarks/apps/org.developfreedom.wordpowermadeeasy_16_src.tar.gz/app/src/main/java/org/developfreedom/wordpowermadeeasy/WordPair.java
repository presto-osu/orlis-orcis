/**
 *   Word Power Made Easy - A vocabulary building application
 *
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *
 *   This file is part of WordPowerMadeEasy.
 *
 *   WordPowerMadeEasy is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   WordPowerMadeEasy is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with WordPowerMadeEasy.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.developfreedom.wordpowermadeeasy;

public class WordPair {
    //private variables
    private String _word, _meaning;
    private int _index, _score;

    //Empty Constructor
    public WordPair() {
    }

    public WordPair(String word, String meaning) {
        this.setWord(word); //XXX need new?
        this.setMeaning(meaning);
        this.setIndex(-1);  //index only used for retrieving,
        this.setScore(0);
    }

    public WordPair(String word, String meaning, int index, int score) {
        this.setWord(word);
        this.setMeaning(meaning);
        this.setIndex(index);
        this.setScore(score);
    }

    // getter & setter Word
    public String getWord() {
        return this._word;
    }

    public void setWord(String word) {
        this._word = word;
    }

    // getter & setter Meaning
    public String getMeaning() {
        return this._meaning;
    }

    public void setMeaning(String meaning) {
        this._meaning = meaning;
    }

    // getter & setter Index
    public int getIndex() {
        return this._index;
    }

    public void setIndex(int index) {
        this._index = index;
    }

    // getter & setter Score
    public int getScore() {
        return this._score;
    }

    public void setScore(int score) {
        this._score = score;
    }

}
// vim: set ts=4 sw=4 tw=0 et :
