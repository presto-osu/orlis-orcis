package org.schabi.sharewithnewpipe;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Christian Schabesberger on 02.03.16.
 * and Copyright 2014 Marc Nause <marc.nause@gmx.de>
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * RelayActivity.java is part of NewPipe.
 *
 * ShareWithNewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ShareWithNewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ShareWithNewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class RelayActivity extends Activity {

    private static final String PACKAGE_NAME="org.schabi.newpipe";
    private static final String ACTIVITY=".VideoItemDetailActivity";

    /**
     * Removes invisible separators (\p{Z}) and punctuation characters including
     * brackets (\p{P}). See http://www.regular-expressions.info/unicode.html for
     * more details.
     */
    private final static String REGEX_REMOVE_FROM_URL = "[\\p{Z}\\p{P}]";

    @Override
    public void onCreate(Bundle savedInstanceBundel) {

        super.onCreate(savedInstanceBundel);
        Intent intent = getIntent();

        String url = "";
        try {
            url = getUris(intent.getStringExtra(Intent.EXTRA_TEXT))[0];
            if(!checkIfValidYoutubeUrl(url)) {
                throw new Exception("not a valid youtube url: " + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.not_a_valid_youtube_url, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            Toast.makeText(this, R.string.np_not_installed, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent startNpIntent = new Intent();
        startNpIntent.setComponent(new ComponentName(PACKAGE_NAME, PACKAGE_NAME + ACTIVITY));
        startNpIntent.setData(Uri.parse(url));
        startActivity(startNpIntent);
        finish();
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

    private boolean checkIfValidYoutubeUrl(String url) {
        if(url.contains("youtube.com")
                || url.contains("youtu.be")
                || url.contains("vnd.youtube")) {
            return true;
        } else {
            return false;
        }
    }
}
