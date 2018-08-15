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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnVideo = ((Button) findViewById(R.id.btn_video));
        btnVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_video))));

            }
        });

        final Button btnSource = ((Button) findViewById(R.id.btn_source_code));
        btnSource.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_source))));

            }
        });

        final TextView tv_copyright = ((TextView) findViewById(R.id.copyright));
        tv_copyright.setText(Html.fromHtml(getString(R.string.tv_copyright)));
        tv_copyright.setMovementMethod(LinkMovementMethod.getInstance());

    }

}
