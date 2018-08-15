/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.comparators;

import com.rastating.droidbeard.entities.TVShowSummary;

import java.util.Comparator;

public class TVShowSummaryComparator implements Comparator<TVShowSummary> {
    @Override
    public int compare(TVShowSummary tvShowSummary, TVShowSummary tvShowSummary2) {
        return tvShowSummary.getName().compareTo(tvShowSummary2.getName());
    }
}
