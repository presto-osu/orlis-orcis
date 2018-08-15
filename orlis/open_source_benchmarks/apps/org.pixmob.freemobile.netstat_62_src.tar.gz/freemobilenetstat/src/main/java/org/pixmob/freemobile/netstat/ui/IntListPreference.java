/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * {@link ListPreference} extension for storing an <code>int</code> value in the
 * preferences.
 * @author Pixmob
 */
public class IntListPreference extends ListPreference {
    public IntListPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setEntryValues(int[] entryValues) {
        final CharSequence[] stringValues = new CharSequence[entryValues.length];
        for (int i = 0; i < entryValues.length; ++i) {
            stringValues[i] = String.valueOf(entryValues[i]);
        }
        setEntryValues(stringValues);
    }
    
    @Override
    protected boolean persistString(String value) {
        return value != null && persistInt(Integer.valueOf(value));
    }
    
    @Override
    protected String getPersistedString(String defaultReturnValue) {
        final int value = getSharedPreferences().getInt(getKey(), 0);
        return String.valueOf(value);
    }
    
    public void setValue(int value) {
        setValue(String.valueOf(value));
    }
}
