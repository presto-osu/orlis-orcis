/*
 * Copyright (C) 2015 Matthias Gabriel
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

package com.gabm.fancyplaces.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gabm.fancyplaces.FancyPlacesApplication;
import com.gabm.fancyplaces.R;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.functional.IMapHandler;
import com.gabm.fancyplaces.functional.LocationHandler;
import com.gabm.fancyplaces.functional.OsmMapHandler;
import com.gabm.fancyplaces.functional.ScrollViewListener;

import org.osmdroid.views.MapView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class ShowEditPlace extends AppCompatActivity implements LocationHandler.OnLocationUpdatedListener {


    static public final int MODE_VIEW = 0;
    static public final int MODE_EDIT = 1;
    static public final int MODE_PREVIEW = 2;
    static public final int RESULT_DATA_NOT_CHANGED = 0;
    static public final int RESULT_DATA_CHANGED = 1;
    static private final int LOCATION_CHANGED_GPS = 0;
    static private final int LOCATION_CHANGED_USER = 1;
    static private final int LOCATION_CHANGED_INIT = 2;
    static private final int REQUEST_IMAGE_CAPTURE = 0;
    static private FancyPlacesApplication curAppContext = null;
    ViewElements currentViewElements = new ViewElements();
    private SEPState currentState = new SEPState();
    private LocationHandler locationHandler = null;
    private IMapHandler mapHandler = null;
    private Menu curMenu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_edit_place);

        curAppContext = (FancyPlacesApplication) getApplicationContext();

        updateElementIDs();

        setupFadingToolbar();

        mapHandler = new OsmMapHandler(currentViewElements.mapView, null);
        currentViewElements.mapView.setEnabled(false);
        currentViewElements.mapView.setMultiTouchControls(false);
        currentViewElements.mapView.setClickable(true);

        currentState.result_code = RESULT_DATA_NOT_CHANGED;

        // if no saved instance, initialize with intent
        if (savedInstanceState == null)
            setStateFromIntent(getIntent());

        locationHandler = ((FancyPlacesApplication) getApplicationContext()).getLocationHandler();
        locationHandler.addOnLocationUpdatedListener(this);

        // if initialized from intent, update ui
        if (savedInstanceState == null)
            onActivityModeChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHandler.removeOnLocationUpdatedListener(this);
    }

    protected void setupFadingToolbar() {
        // inflate toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.sep_toolbar);

        final float[] hsv = new float[3];
        Color.colorToHSV(getResources().getColor(R.color.ColorPrimary), hsv);
        final float orig_hsv_value = hsv[2];
        hsv[2] = 0;

        final ColorDrawable cd = new ColorDrawable(Color.HSVToColor(hsv));
        cd.setAlpha(40);

        toolbar.setBackground(cd);

        if (Build.VERSION.SDK_INT >= 19) {
            int padding_right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            toolbar.setPadding(toolbar.getPaddingLeft(), curAppContext.getStatusBarHeight(), padding_right, 0);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        currentViewElements.scrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
                hsv[2] = getHSVValueForView(scrollView.getScrollY(), orig_hsv_value);
                cd.setColor(Color.HSVToColor(hsv));
                cd.setAlpha(getAlphaForView(scrollView.getScrollY()));
                currentViewElements.imageView.setTranslationY(scrollView.getScrollY() * 0.25f);

            }
        });
    }

    private float getHSVValueForView(int position, float maxHSVValue) {
        int imageHeight = findViewById(R.id.sep_image).getLayoutParams().height - curAppContext.getStatusBarHeight() - findViewById(R.id.sep_toolbar).getHeight();
        float minHSVValue = 0.0f;
        float hsvValue = minHSVValue; // min alpha

        if (position > imageHeight)
            hsvValue = maxHSVValue;
        else if (position < 0)
            hsvValue = minHSVValue;
        else {
            hsvValue += (((float) position) / imageHeight) * (maxHSVValue - minHSVValue);
        }

        return hsvValue;
    }

    private int getAlphaForView(int position) {
        int imageHeight = findViewById(R.id.sep_image).getLayoutParams().height - curAppContext.getStatusBarHeight() - findViewById(R.id.sep_toolbar).getHeight();
        float minAlpha = 40.0f, maxAlpha = 255f;
        float alpha = minAlpha; // min alpha

        if (position > imageHeight)
            alpha = maxAlpha;
        else if (position < 0)
            alpha = minAlpha;
        else {
            alpha += (((float) position) / imageHeight) * (maxAlpha - minAlpha);
        }

        return (int) (alpha);
    }

    protected void onNotesChanged() {
        if (currentState.mode == MODE_EDIT) {
            currentViewElements.notesEditText.setText(currentState.data.getNotes());
        } else if (currentState.mode == MODE_VIEW) {
            currentViewElements.notesTextView.setText(currentState.data.getNotes());
        }

    }

    protected void onImageChanged() {
        if (currentState.image != null) {
            currentViewElements.imageView.setImageBitmap(currentState.image);
        }
    }

    protected void onLocationChanged(int reason) {
        String title = getString(R.string.new_fancy_place);
        if (!currentState.data.getTitle().equals(""))
            title = currentState.data.getTitle();

        double lat = Double.valueOf(currentState.data.getLocationLat());
        double lng = Double.valueOf(currentState.data.getLocationLong());

        setMarker(lat, lng, title);

        switch (reason) {
            case LOCATION_CHANGED_GPS:
                mapHandler.animateCamera(lat, lng, com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_ZOOM_NEAR, com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_DURATION);
                break;
            case LOCATION_CHANGED_USER:
                mapHandler.animateCamera(lat, lng, com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_DURATION);
                break;
            case LOCATION_CHANGED_INIT:
                mapHandler.setCamera(lat, lng, com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_ZOOM_NEAR);
                break;
        }
    }

    protected void onActivityModeChanged() {

        final FancyPlace data = currentState.data;

        SEPState.ViewElementVisibility visibility = currentState.viewElementVisibility;
        ViewElements viewElements = currentViewElements;


        int mode = currentState.mode;

        ///////////////////////////////////
        // Title
        if (mode == MODE_VIEW) {
            setTitle(data.getTitle());
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        } else if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            viewElements.titleEditText.setText(data.getTitle());
            visibility.titleEditTextVisibility = View.VISIBLE;
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        ///////////////////////////////////
        // Map
        visibility.mapCardVisibility = View.VISIBLE;
        if (data.isLocationSet()) {
            onLocationChanged(LOCATION_CHANGED_INIT);
        } else if (!data.isLocationSet() && mode == MODE_EDIT) {
            locationHandler.updateLocation(false);
        }

        if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            visibility.mapUpdateButtonVisibility = View.VISIBLE;
            currentViewElements.mapView.setOnClickListener(null);
        } else {
            visibility.mapUpdateButtonVisibility = View.GONE;
        }


        ///////////////////////////////////
        // Notes
        visibility.notesCardVisibility = View.VISIBLE;
        if (mode == MODE_VIEW) {
            visibility.notesTextViewVisibility = View.VISIBLE;
            visibility.notesEditTextVisibility = View.GONE;

        } else if (mode == MODE_EDIT || mode == MODE_PREVIEW) {
            visibility.notesTextViewVisibility = View.GONE;
            visibility.notesEditTextVisibility = View.VISIBLE;
        }

        onNotesChanged();
        onImageChanged();
        onVisibilitiesChanged();
    }

    protected void onVisibilitiesChanged() {
        SEPState.ViewElementVisibility viewElementVisibility = currentState.viewElementVisibility;
        ViewElements viewElements = currentViewElements;

        // title
        viewElements.titleEditText.setVisibility(viewElementVisibility.titleEditTextVisibility);

        // map
        viewElements.mapCard.setVisibility(viewElementVisibility.mapCardVisibility);
        viewElements.mapUpdateButton.setVisibility(viewElementVisibility.mapUpdateButtonVisibility);

        // notes
        viewElements.notesCard.setVisibility(viewElementVisibility.notesCardVisibility);
        viewElements.notesTextView.setVisibility(viewElementVisibility.notesTextViewVisibility);
        viewElements.notesEditText.setVisibility(viewElementVisibility.notesEditTextVisibility);
    }

    protected void updateElementIDs() {
        // title
        currentViewElements.titleEditText = (EditText) findViewById(R.id.sep_title_edit_text);

        // map
        currentViewElements.mapCard = (VerticalCardView) findViewById(R.id.sep_map_card);
        currentViewElements.mapUpdateButton = (Button) findViewById(R.id.sep_map_update_button);
        currentViewElements.mapView = (MapView) findViewById(R.id.sep_map);

        // notes
        currentViewElements.notesCard = (VerticalCardView) findViewById(R.id.sep_notes_card);
        currentViewElements.notesEditText = (EditText) findViewById(R.id.sep_notes_edit_text);
        currentViewElements.notesTextView = (TextView) findViewById(R.id.sep_notes_text_view);

        // image
        currentViewElements.imageView = (ImageView) findViewById(R.id.sep_image);

        // scrollview
        currentViewElements.scrollView = (ObservableScrollView) findViewById(R.id.sep_scroll_view);
    }

    protected void updateMenuItemVisibility() {
        if (currentState.mode == MODE_VIEW) {
            curMenu.findItem(R.id.sep_action_edit).setVisible(true);
            curMenu.findItem(R.id.sep_action_confirm).setVisible(false);
            curMenu.findItem(R.id.sep_show_on_map).setVisible(true);
            curMenu.findItem(R.id.sep_action_take_image).setVisible(false);
        } else {
            curMenu.findItem(R.id.sep_action_edit).setVisible(false);
            curMenu.findItem(R.id.sep_action_confirm).setVisible(true);
            curMenu.findItem(R.id.sep_show_on_map).setVisible(false);
            curMenu.findItem(R.id.sep_action_take_image).setVisible(true);
        }
    }

    protected void setStateFromIntent(Intent intent) {
        if (intent == null)
            return;

        Bundle extras = intent.getExtras();
        currentState.mode = extras.getInt("mode");
        currentState.data = extras.getParcelable("data");
        currentState.image = currentState.data.getImage().loadFullSizeImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_edit_place, menu);

        curMenu = menu;

        updateMenuItemVisibility();

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        saveInputFieldsToState();
        bundle.putParcelable("state", currentState);
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        currentState = bundle.getParcelable("state");
        onActivityModeChanged();
        super.onRestoreInstanceState(bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.sep_action_edit:
                currentState.mode = MODE_EDIT;

                onActivityModeChanged();
                updateMenuItemVisibility();

                break;
            case R.id.sep_action_confirm:
                saveInputFieldsToState();
                if (!currentState.data.isValid()) {
                    Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();

                    return false;
                }

                currentState.result_code = RESULT_DATA_CHANGED;
                finish();
                break;
            case R.id.sep_show_on_map:
                final FancyPlace curFP = currentState.data;
                String loc = curFP.getLocationLat() + "," + curFP.getLocationLong();
                try {
                    String uriString =
                            "geo:" + loc + "?q="
                                    + loc + "(" + URLEncoder.encode(curFP.getTitle(), "UTF-8") + ")&d="
                                    + URLEncoder.encode(curFP.getNotes(), "UTF-8");

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                    startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.sep_action_take_image:
                // create Intent to take a picture and return control to the calling application
                Intent cam_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cam_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + currentState.data.getImage().getFileName())); // set the image file name


                // start the image capture Intent
                startActivityForResult(cam_intent, REQUEST_IMAGE_CAPTURE);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentState.mode == MODE_EDIT) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // Go back
                            ShowEditPlace.super.onBackPressed();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FPAlertDialogStyle);
            builder.setMessage(R.string.alert_discard_changes)
                    .setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    public void finish() {
        Bundle conData = new Bundle();
        conData.putParcelable("data", currentState.data);
        Intent intent = new Intent();
        intent.putExtras(conData);

        setResult(currentState.result_code, intent);
        super.finish();
    }

    protected void saveInputFieldsToState() {
        if (currentState.mode == MODE_EDIT || currentState.mode == MODE_PREVIEW) {
            currentState.data.setTitle(currentViewElements.titleEditText.getText().toString());
            currentState.data.setNotes(currentViewElements.notesEditText.getText().toString());
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sep_map_update_button:
                locationHandler.updateLocation(true);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            currentState.data.getImage().scaleDown(com.gabm.fancyplaces.FancyPlacesApplication.TARGET_PIX_SIZE);
            currentState.image = currentState.data.getImage().loadFullSizeImage();
            onActivityModeChanged();
        }
    }

    @Override
    public void onLocationUpdated(Location location) {

        // state can be null if called before restore!
        if (currentState.data != null) {
            currentState.data.setLocationLat(String.valueOf(location.getLatitude()));
            currentState.data.setLocationLong(String.valueOf(location.getLongitude()));

            onLocationChanged(LOCATION_CHANGED_GPS);
        }
    }

    @Override
    public void onLocationUpdating() {
        Toast.makeText(this, R.string.updating_location, Toast.LENGTH_SHORT).show();
    }

    protected void setMarker(double lat, double lng, String title) {
        mapHandler.clearMarkers();
        mapHandler.addMarker(lat, lng, title, true);
        mapHandler.animateCamera(lat, lng, com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_ZOOM_NEAR, com.gabm.fancyplaces.FancyPlacesApplication.MAP_DEFAULT_DURATION);
    }

    protected class ViewElements {
        // title
        public EditText titleEditText = null;

        // map
        public VerticalCardView mapCard = null;
        public Button mapUpdateButton = null;
        public MapView mapView = null;

        // notes
        public VerticalCardView notesCard = null;
        public TextView notesTextView = null;
        public EditText notesEditText = null;


        // image
        public ImageView imageView = null;

        // scroll view
        public ObservableScrollView scrollView = null;
    }
}
