package ch.ihdg.calendarcolor;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class ColorPickerActivity extends Activity {

    static final String ARG_NAME = "arg_name";
    static final String ARG_ID = "arg_id";
    static final String ARG_COLOR = "arg_color";
    int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        currentColor = getIntent().getIntExtra(ARG_COLOR, 0);
        String name = getIntent().getStringExtra(ARG_NAME);
        final int cal_id = getIntent().getIntExtra(ARG_ID, 0);

        setTitle( name );

        final ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);

        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        picker.setColor(currentColor);
        picker.setOldCenterColor(currentColor);

        //write color hex code in text field
        final EditText hexText = (EditText) findViewById(R.id.hexText);
        String red = String.format("%02x", (currentColor >> 16) & 0xFF ),
               green = String.format("%02x", (currentColor >> 8) & 0xFF),
               blue = String.format("%02x", currentColor & 0xFF);
        hexText.setText( red + green + blue );
        findViewById(R.id.dummy).requestFocus();

        //update hex value when color changes
        final ColorPicker.OnColorChangedListener colorPickerListener = new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                //store new color
                currentColor = color;
                //write color hex code in text field
                String hex = String.format("%02x", (color >> 16) & 0xFF ) +
                             String.format("%02x", (color >> 8) & 0xFF) +
                             String.format("%02x", color & 0xFF);
                if(!hexText.getText().toString().equals(hex)) {
                    hexText.setText( hex );
                }
            }
        };
        picker.setOnColorChangedListener( colorPickerListener );

        //update color picker when a valid hex code is entered
        hexText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable s) {
                if( s.length() == 6 )
                {
                    try{
                        int color = Color.parseColor('#' + s.toString());
                        if( picker.getColor() != color ) {
                            //Do not update hex field from color picker, after typing the value
                            picker.setOnColorChangedListener(null);
                            picker.setColor(color);
                            currentColor = color;
                            picker.setOnColorChangedListener( colorPickerListener );
                        }
                        hexText.setTextColor( Color.BLACK );
                    }
                    catch ( IllegalArgumentException e ) {
                        //ignore
                        hexText.setTextColor( Color.RED );
                    }
                }
            }
        });

        final Button buttoncancel = (Button) findViewById(R.id.buttoncancel);
        buttoncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final Button buttonsave = (Button) findViewById(R.id.buttonsave);
        buttonsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                // set the new color for the calendar
                values.put(CalendarContract.Calendars.CALENDAR_COLOR, currentColor);
                Uri updateUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, cal_id);
                getContentResolver().update(updateUri, values, null, null);

                finish();
            }
        });
    }
}
