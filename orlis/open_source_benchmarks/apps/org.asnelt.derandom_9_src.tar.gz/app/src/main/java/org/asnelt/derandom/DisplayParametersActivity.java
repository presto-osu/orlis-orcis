/*
 * Copyright (C) 2015 Arno Onken
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

package org.asnelt.derandom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.math.BigInteger;

/**
 * This class implements an activity that displays all parameter values of a single random number
 * generator.
 */
public class DisplayParametersActivity extends AppCompatActivity {
    /**
     * Initializes the activity by adding elements for all generator parameters.
     * @param savedInstanceState Bundle containing all parameters and parameter names
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        View view = inflater.inflate(R.layout.activity_display_parameters, rootView, false);

        // Extract parameters and names from bundle
        Bundle extras = getIntent().getExtras();
        String name = extras.getString(MainActivity.EXTRA_GENERATOR_NAME);
        String[] parameterNames =
                extras.getStringArray(MainActivity.EXTRA_GENERATOR_PARAMETER_NAMES);
        long[] parameters = extras.getLongArray(MainActivity.EXTRA_GENERATOR_PARAMETERS);
        if (parameterNames == null || parameters == null) {
            parameterNames = new String[0];
            parameters = new long[0];
        }

        ScrollView scrollViewParameters = (ScrollView) view.findViewById(
                R.id.scroll_view_parameters);
        // Add layout
        LinearLayout layoutParameters = new LinearLayout(this);
        layoutParameters.setOrientation(LinearLayout.VERTICAL);

        TextView textGeneratorName = new TextView(this);
        textGeneratorName.setText(name);
        layoutParameters.addView(textGeneratorName);

        // Get auto-detect settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String parameterBase = sharedPreferences.getString(SettingsActivity.KEY_PREF_PARAMETER_BASE,
                "");

        int parametersLength = parameters.length;
        if (parameterNames.length < parametersLength) {
            parametersLength = parameterNames.length;
        }
        TextView[] textParameterNames = new TextView[parametersLength];
        // Add fields for parameters
        EditText[] textParameters = new EditText[parametersLength];
        for (int i = 0; i < parametersLength; i++) {
            textParameterNames[i] = new TextView(this);
            textParameterNames[i].setText(parameterNames[i]);
            layoutParameters.addView(textParameterNames[i]);

            textParameters[i] = new EditText(this);
            switch (parameterBase) {
                case "8":
                    textParameters[i].setText(String.format("0%s",
                            Long.toOctalString(parameters[i])));
                    break;
                case "16":
                    textParameters[i].setText(String.format("0x%s",
                            Long.toHexString(parameters[i])));
                    break;
                default:
                    String unsignedLongString;
                    if (parameters[i] >= 0) {
                        unsignedLongString = Long.toString(parameters[i]);
                    } else {
                        BigInteger number = BigInteger.valueOf(parameters[i]);
                        number = number.add(BigInteger.ONE.shiftLeft(Long.SIZE));
                        unsignedLongString = number.toString();
                    }
                    textParameters[i].setText(unsignedLongString);
            }
            textParameters[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            // Remove the following line to make fields editable
            textParameters[i].setKeyListener(null);
            layoutParameters.addView(textParameters[i]);
        }
        scrollViewParameters.addView(layoutParameters);
        setContentView(view);
    }

    /**
     * Callback method for options menu creations.
     * @param menu the menu to inflate
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_parameters, menu);
        return true;
    }
}
