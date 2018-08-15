/*
 * Copyright (C) 2015  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package fr.mobdev.goblim.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.ClipboardManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.mobdev.goblim.Database;
import fr.mobdev.goblim.NetworkManager;
import fr.mobdev.goblim.R;
import fr.mobdev.goblim.listener.NetworkAdapter;
import fr.mobdev.goblim.objects.Img;

/*
 * Activity display the link of the uploaded picture and allow user to share it with other app
 * or copy it to cleapboard
 */
public class LinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.link);

        Toolbar toolbar = (Toolbar) findViewById(R.id.link_toolbar);
        setSupportActionBar(toolbar);

        //get url information
        Intent receiveIntent = getIntent();
        Long imageId = receiveIntent.getLongExtra("imageId", -1);
        final Img image = Database.getInstance(getApplicationContext()).getImage(imageId);
        String url = image.getUrl();
        String shortHash = image.getShortHash();
        String realShortHash = image.getRealShortHash();
        String token = image.getToken();
        ImageView iv = (ImageView) findViewById(R.id.thumbnail_link);
        iv.setImageBitmap(image.getThumb());


        //add a / at the end of the url before adding the hash
        if(!url.endsWith("/"))
            url = url.concat("/");
        final String sharedUrl = url.concat(shortHash);
        final String deleteUrl = url.concat("d/"+realShortHash+"/"+token);

        //manage the sharing button
        ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, sharedUrl);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        //manage the clipboard button
        ImageButton copyClipboardButton = (ImageButton) findViewById(R.id.copy_clipboard_button);
        copyClipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied URL", sharedUrl);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(LinkActivity.this,getString(R.string.copy_to_clipboard),Toast.LENGTH_SHORT).show();
            }
        });

        final NetworkAdapter listener = new NetworkAdapter() {

            @Override
            public void deleteSucceed()
            {
                List<Img> images = new ArrayList<>();
                images.add(image);
                Database.getInstance(getApplicationContext()).deleteImg(images);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LinkActivity.this, R.string.delete_succeed, Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
            }

            @Override
            public void deleteError(final String error)
            {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(LinkActivity.this, error, Toast.LENGTH_SHORT).show();
                   }
               });
            }
        };

        ImageButton deleteImageButton = (ImageButton) findViewById(R.id.delete_button);
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LinkActivity.this);
                builder.setMessage(getString(R.string.delete_this_image))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int storageDuration = image.getStorageDuration();
                                Calendar date = image.getDate();
                                Calendar today = Calendar.getInstance();
                                long millis = today.getTimeInMillis() - date.getTimeInMillis();
                                long days = millis / (24*60*60*1000);
                                //storage duration has ended or not?
                                if(storageDuration == 0 || storageDuration - days >= 0)
                                    NetworkManager.getInstance(listener, getApplicationContext()).deleteImage(deleteUrl);
                                else {
                                    //image is no more on the server, delete is only local now
                                    listener.deleteSucceed();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        //add the url to the textview
        TextView link = (TextView) findViewById(R.id.link);
        link.setText(sharedUrl);
    }

}
