package org.schabi.openhitboxstreams;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christian Schabesberger on 07.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * MenuActivity.java is part of NewPipe.
 *
 * OpenHitboxStreams is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenHitboxStreams is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenHitboxStreams.  If not, see <http://www.gnu.org/licenses/>.
 */

public class MenuActivity extends Activity {

    private static final String CHANNEL_SELECTOR="www.hitbox.tv/([A-Za-z0-9-_]*)";
    private static final String STREAM_TEMPLAYTE="http://api.hitbox.tv/player/hls/#channel#.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        // if invalid url
        if(uri == null) {
            Toast.makeText(this, R.string.invalide_url, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // select channel
        String url = uri.toString();
        String channel;
        try {
            channel = matchGroup1(CHANNEL_SELECTOR, url);
        } catch (Exception e) {
            Toast.makeText(this, R.string.invalide_url, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
            return;
        }

        // start player
        Intent startPlayerIntent = new Intent(Intent.ACTION_VIEW);
        startPlayerIntent.setData(Uri.parse(STREAM_TEMPLAYTE.replace("#channel#", channel)));
        startActivity(Intent.createChooser(startPlayerIntent, getString(R.string.watch_with)));
        finish();
    }

    private static String matchGroup1(String pattern, String input) throws Exception {
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);
        boolean foundMatch = mat.find();
        if (foundMatch) {
            return mat.group(1);
        }
        else {
            //Log.e(TAG, "failed to find pattern \""+pattern+"\" inside of \""+input+"\"");
            throw new Exception("failed to find pattern \""+pattern+" inside of "+input+"\"");
        }
    }
}
