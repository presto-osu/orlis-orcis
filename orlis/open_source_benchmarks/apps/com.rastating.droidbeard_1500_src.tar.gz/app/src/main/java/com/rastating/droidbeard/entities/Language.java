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

package com.rastating.droidbeard.entities;

import com.rastating.droidbeard.R;

public class Language {
    private String mCode;

    public Language(String languageCode) {
        mCode = languageCode;
    }

    public String getCode() {
        return mCode;
    }

    public int getIconResId() {
        if (mCode.equalsIgnoreCase("cs")) {
            return R.drawable.cs;
        }
        else if (mCode.equalsIgnoreCase("da")) {
            return R.drawable.da;
        }
        else if (mCode.equalsIgnoreCase("de")) {
            return R.drawable.de;
        }
        else if (mCode.equalsIgnoreCase("el")) {
            return R.drawable.el;
        }
        else if (mCode.equalsIgnoreCase("en")) {
            return R.drawable.en;
        }
        else if (mCode.equalsIgnoreCase("es")) {
            return R.drawable.es;
        }
        else if (mCode.equalsIgnoreCase("fi")) {
            return R.drawable.fi;
        }
        else if (mCode.equalsIgnoreCase("fr")) {
            return R.drawable.fr;
        }
        else if (mCode.equalsIgnoreCase("he")) {
            return R.drawable.he;
        }
        else if (mCode.equalsIgnoreCase("hr")) {
            return R.drawable.hr;
        }
        else if (mCode.equalsIgnoreCase("hu")) {
            return R.drawable.hu;
        }
        else if (mCode.equalsIgnoreCase("it")) {
            return R.drawable.it;
        }
        else if (mCode.equalsIgnoreCase("ja")) {
            return R.drawable.ja;
        }
        else if (mCode.equalsIgnoreCase("ko")) {
            return R.drawable.ko;
        }
        else if (mCode.equalsIgnoreCase("nl")) {
            return R.drawable.nl;
        }
        else if (mCode.equalsIgnoreCase("no")) {
            return R.drawable.no;
        }
        else if (mCode.equalsIgnoreCase("pl")) {
            return R.drawable.pl;
        }
        else if (mCode.equalsIgnoreCase("pt")) {
            return R.drawable.pt;
        }
        else if (mCode.equalsIgnoreCase("ru")) {
            return R.drawable.ru;
        }
        else if (mCode.equalsIgnoreCase("sl")) {
            return R.drawable.sl;
        }
        else if (mCode.equalsIgnoreCase("sv")) {
            return R.drawable.sv;
        }
        else if (mCode.equalsIgnoreCase("tr")) {
            return R.drawable.tr;
        }
        else if (mCode.equalsIgnoreCase("zh")) {
            return R.drawable.zh;
        }
        else {
            return R.drawable.en;
        }
    }
}
