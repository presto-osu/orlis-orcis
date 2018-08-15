package com.luorrak.ouroboros.util;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SaveReplyText implements TextWatcher {
    public final static String nameEditTextKey = "nameEditTextKey";
    public final static String emailEditTextKey = "emailEditTextKey";
    public final static String subjectEditTextKey = "subjectEditTextKey";
    public final static String commentEditTextKey = "commentEditTextKey";
    private SharedPreferences sharedPreferences;
    private String field;

        public SaveReplyText(SharedPreferences sharedPreferences, String field){
            this.sharedPreferences = sharedPreferences;
            this.field = field;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            sharedPreferences.edit().putString(field, s.toString()).apply();
        }

        public void deleteSharedPrefs(){
            sharedPreferences.edit().remove(nameEditTextKey);
            sharedPreferences.edit().remove(emailEditTextKey);
            sharedPreferences.edit().remove(subjectEditTextKey);
            sharedPreferences.edit().remove(commentEditTextKey);
            sharedPreferences.edit().apply();
        }
    }

