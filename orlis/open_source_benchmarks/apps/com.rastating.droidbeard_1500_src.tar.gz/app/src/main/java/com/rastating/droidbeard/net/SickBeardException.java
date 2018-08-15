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

package com.rastating.droidbeard.net;

public class SickBeardException {
    private Exception mException;
    private String mData;

    public SickBeardException(String data, Exception exception) {
        mException = exception;
        mData = data;
    }

    public Exception getException() {
        return mException;
    }

    public String getData() {
        return mData;
    }
}