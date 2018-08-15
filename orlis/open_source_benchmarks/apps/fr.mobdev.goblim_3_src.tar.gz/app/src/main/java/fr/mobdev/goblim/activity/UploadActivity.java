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

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.mobdev.goblim.Database;
import fr.mobdev.goblim.listener.NetworkAdapter;
import fr.mobdev.goblim.NetworkManager;
import fr.mobdev.goblim.objects.Img;
import fr.mobdev.goblim.objects.Server;
import fr.mobdev.goblim.R;

/*
 * Activity used to handle sharing pictures from other app that user want to upload on a lutim instance
 * if user launch the app by itself he can also pick a pictures from his device and upload it as well.
 * This Activity let user access to the others activities in order to manage history, servers and after an upload
 * the shared options of the given link
 */
public class UploadActivity extends AppCompatActivity {

    private NetworkAdapter listener;
    private Uri imageUri;
    private List<String> urls;
    private List<Integer> deletedDays;
    private ProgressDialog progressDialog;
    private Bitmap bt;

    //static value to handle storage durations options
    private static final int NEVER = 0;
    private static final int ONE = 1;
    private static final int SEVEN = 7;
    private static final int THIRTY = 30;
    private static final int YEAR = 365;

    private static final int THUMB_MAX_SIDE = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.upload_pict);

        //prepare data used for upload
        imageUri = null;
        urls = new ArrayList<>();
        deletedDays = new ArrayList<>();
        deletedDays.add(NEVER);
        deletedDays.add(ONE);
        deletedDays.add(SEVEN);
        deletedDays.add(THIRTY);
        deletedDays.add(YEAR);

        updateServerList();
        ImageButton resetButton = (ImageButton) findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
            }
        });

        //retrieve previous state if it exist
        if(savedInstanceState != null) {
            int selectedServer = savedInstanceState.getInt("selectedServer");
            imageUri = savedInstanceState.getParcelable("imageURI");
            if (selectedServer < urls.size()) {
                Spinner servers = (Spinner) findViewById(R.id.servers_spinner);
                servers.setSelection(selectedServer);
            }
            displayImage();
        }

        //prepare the listener that handle upload result
        listener = new NetworkAdapter() {
            @Override
            public void fileUploaded(final Img image) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bt.compress(Bitmap.CompressFormat.JPEG,70,outputStream);
                        image.setThumbData(outputStream.toByteArray());

                        //add uploaded img to history
                        Long id = Database.getInstance(getApplicationContext()).addImage(image);
                        //dismiss progressDialog
                        if(progressDialog!=null)
                            progressDialog.dismiss();
                        resetImage();
                        //launch LinkActivity
                        Intent linkIntent = new Intent(UploadActivity.this,LinkActivity.class);
                        linkIntent.putExtra("imageId", id);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        startActivity(linkIntent);
                    }
                });
            }

            @Override
            public void fileUploadError(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //display toast error
                        Toast.makeText(UploadActivity.this, error, Toast.LENGTH_SHORT).show();
                        if(progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                });
            }
        };

        //prepare for upload
        Button uploadBt = (Button) findViewById(R.id.upload_button);
        uploadBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                        uploadImage();
                    }
                }).start();
            }
        });

        //prepare for asking user the image he want share
        Button selectBt = (Button) findViewById(R.id.select_button);
        selectBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFile();
            }
        });

        //have we receive image from share or do you need to ask it to the user if we haven't ask for it before (screen rotation)
        Intent receiveIntent = getIntent();
        if((receiveIntent == null || receiveIntent.getType() == null || !receiveIntent.getType().contains("image/")) && imageUri == null) {
            uploadBt.setVisibility(View.GONE);
            resetButton.setVisibility(View.GONE);
        }
        else {
            selectBt.setVisibility(View.GONE);
            if(receiveIntent != null && imageUri == null) {
                imageUri = receiveIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
            displayImage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update server list to manage change in ServerActivity
        updateServerList();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        //save imageURI and selected server position
        savedInstanceState.putParcelable("imageURI", imageUri);
        Spinner selectedServer = (Spinner) findViewById(R.id.servers_spinner);
        int pos = selectedServer.getSelectedItemPosition();
        savedInstanceState.putInt("selectedServer", pos);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateServerList() {
        Spinner serversSpinner = (Spinner) findViewById(R.id.servers_spinner);
        //retrieve the selected server name in case it change his place in list
        String selectedServer = (String) serversSpinner.getSelectedItem();

        List<Server> servers = Database.getInstance(getApplicationContext()).getServers(true);
        urls.clear();
        int pos = 0;
        //create the string list of server name from database data
        for(Server server : servers) {
            if(server.getUrl().equals(selectedServer)) {
                pos = urls.size();
            }
            urls.add(server.getUrl());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,urls);
        serversSpinner.setAdapter(adapter);

        //select the previous selected server
        serversSpinner.setSelection(pos);
    }

    private void displayImage() {
        if(imageUri != null) {
            ContentResolver contentResolver = getContentResolver();
            //display it in the imageView
            try {
                bt = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
                bt = generateThumb(bt);
                ImageView view = (ImageView) findViewById(R.id.thumbnail_main);
                view.setImageBitmap(bt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        //what server we use
        Spinner urlSpinner = (Spinner)findViewById(R.id.servers_spinner);
        int pos = urlSpinner.getSelectedItemPosition();
        if(urls.size() < pos) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(UploadActivity.this, getString(R.string.server_list_error), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        String url = urls.get(pos);

        //How long the server need to store image
        Spinner deleteSpinner = (Spinner)findViewById(R.id.delete_day_spinner);
        pos = deleteSpinner.getSelectedItemPosition();
        int delete = deletedDays.get(pos);

        //read image as bytes to upload it
        byte[] bytearray = null;

        //create a fileStream from the file path
        InputStream stream = null;
        try{
            stream = getContentResolver().openInputStream(imageUri);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //read data from the file and store it in a byte array
        if(stream != null) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int readed = 0;
            byte[] buffer = new byte[1024];
            while(readed != -1) {
                try {
                    readed = stream.read(buffer);
                    if(readed != -1)
                        outStream.write(buffer,0,readed);
                } catch (IOException e) {
                    e.printStackTrace();
                    readed = -1;
                }
            }
            bytearray = outStream.toByteArray();
        }
        //upload image and display a progress bar
        if(bytearray != null && bytearray.length > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(UploadActivity.this);
                    progressDialog.setMessage(getString(R.string.upload_progress));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });
            NetworkManager.getInstance(listener, getApplicationContext()).upload(url, delete, null, bytearray);
        }
        else {
            listener.fileUploadError(getString(R.string.empty_file));
        }
    }

    private void resetImage(){
        imageUri = null;
        ImageView view = (ImageView) findViewById(R.id.thumbnail_main);
        view.setImageBitmap(null);
        ImageButton resetButton = (ImageButton) findViewById(R.id.reset_button);
        resetButton.setVisibility(View.GONE);
        Button selectButton = (Button) findViewById(R.id.select_button);
        selectButton.setVisibility(View.VISIBLE);
        Button uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setVisibility(View.GONE);
    }

    private void requestFile() {
        //ask for image file
        Intent requestFileIntent = new Intent(Intent.ACTION_PICK);
        requestFileIntent.setType("image/*");
        startActivityForResult(requestFileIntent, 0);

    }

    private Bitmap generateThumb(Bitmap original){

        int ratio = 1;
        if(original.getWidth()>THUMB_MAX_SIDE || original.getHeight() > THUMB_MAX_SIDE){
            if(original.getWidth()>original.getHeight())
                ratio = original.getWidth()/THUMB_MAX_SIDE;
            else
                ratio = original.getHeight()/THUMB_MAX_SIDE;
        }

        int newWidth = original.getWidth()/ratio;
        int newHeight = original.getHeight()/ratio;

        Bitmap thumb = Bitmap.createScaledBitmap(original,newWidth,newHeight,false);
        return thumb;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent returnIntent) {
        if(resultCode == RESULT_OK){
            //retrieve uri from the request image activity and prepare
            imageUri = returnIntent.getData();

            Button uploadBt = (Button) findViewById(R.id.upload_button);
            uploadBt.setVisibility(View.VISIBLE);

            Button selectButton = (Button) findViewById(R.id.select_button);
            selectButton.setVisibility(View.GONE);

            ImageButton resetButton = (ImageButton) findViewById(R.id.reset_button);
            resetButton.setVisibility(View.VISIBLE);
            displayImage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent newIntent = null;
        if (id == R.id.action_manage_server) {
            newIntent = new Intent(this,ServersActivity.class);
        }
        if(newIntent != null)
        {
            startActivity(newIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
