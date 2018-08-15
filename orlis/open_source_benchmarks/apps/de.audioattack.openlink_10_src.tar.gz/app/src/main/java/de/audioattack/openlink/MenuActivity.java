/* 
 * Copyright 2014 Marc Nause <marc.nause@gmx.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see  http:// www.gnu.org/licenses/. 
 */
package de.audioattack.openlink;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Handles sharing of selected text.
 *
 * @author Marc nause
 */
public class MenuActivity extends Activity {

    /**
     * Removes invisible separators (\p{Z}) and punctuation characters including
     * brackets (\p{P}). See http://www.regular-expressions.info/unicode.html for
     * more details.
     */
    private final static String REGEX_REMOVE_FROM_URL = "[\\p{Z}\\p{P}]";

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSendText(intent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }

    }

    /**
     * Handles ACTION_SEND intent received via press on share item. Extracts
     * URls from input text (contained in Intent) and opens found URLs.
     *
     * @param intent contains shared text
     */
    private void handleSendText(final Intent intent) {

        final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {

            final String[] uris = getUris(sharedText);
            final List<String> errors = new ArrayList<>();

            for (final String uri : uris) {

                try {

                    if (URLUtil.isValidUrl(uri)) {

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                    }
                } catch (Exception ex) {
                    errors.add(uri);
                }

            }

            if (!errors.isEmpty()) {
                final StringBuilder sb = new StringBuilder();
                final String lineSeparator = System.getProperty("line.separator");

                for (final String s : errors) {
                    if (sb.length() > 0) {
                        sb.append(lineSeparator);
                    }
                    sb.append(s);
                }

                Toast.makeText(this, this.getString(R.string.error_unable_to_open, sb.toString()), Toast.LENGTH_LONG)
                        .show();
            } else if (uris.length == 0) {
                Toast.makeText(this, this.getString(R.string.error_no_url_found), Toast.LENGTH_LONG).show();
            }

            this.finish();
        }
    }

    /**
     * Retrieves all Strings which look remotely like URLs from a text.
     *
     * @param sharedText text to scan for URLs.
     * @return potential URLs
     */
    private String[] getUris(final String sharedText) {

        final Collection<String> result = new HashSet<>();

        if (sharedText != null) {

            final String[] array = sharedText.split("\\p{Space}");

            for (String s : array) {

                s = trim(s);

                if (s.length() != 0) {
                    if (s.matches(".+://.+")) {
                        result.add(removeHeadingGibberish(s));
                    } else if (s.matches(".+\\..+")) {
                        result.add("http://" + s);
                    }
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private static String removeHeadingGibberish(final String input) {

        int start = 0;

        for (int i = input.indexOf("://") - 1; i >= 0; i--) {

            if (!input.substring(i, i + 1).matches("\\p{L}")) {

                start = i + 1;
                break;
            }
        }

        return input.substring(start, input.length());
    }

    private static String trim(final String input) {

        if (input == null || input.length() < 1) {
            return input;
        } else {

            String output = input;

            while (output.length() > 0 && output.substring(0, 1).matches(REGEX_REMOVE_FROM_URL)) {
                output = output.substring(1);
            }

            while (output.length() > 0
                    && output.substring(output.length() - 1, output.length()).matches(REGEX_REMOVE_FROM_URL)) {
                output = output.substring(0, output.length() - 1);
            }

            return output;
        }
    }

}
