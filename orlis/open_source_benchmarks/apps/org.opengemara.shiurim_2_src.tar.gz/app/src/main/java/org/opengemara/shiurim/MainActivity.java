package org.opengemara.shiurim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{

    final int GRACH_NAEH = 0;
    final int CHAZON_ISH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup opinionSpinner
        Spinner opinionSpinner = (Spinner) findViewById(R.id.OpinionSpinner);
        ArrayAdapter<CharSequence> opinionAdapter = ArrayAdapter.createFromResource(this,
                R.array.opinionList, android.R.layout.simple_spinner_item);
        opinionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opinionSpinner.setAdapter(opinionAdapter);


        //setup typeSpinner
        Spinner typeSpinner = (Spinner) findViewById(R.id.TypeSpinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeList, android.R.layout.simple_spinner_item);
        opinionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        //setup hebrewSpinner
        Spinner hebrewSpinner = (Spinner) findViewById(R.id.HebrewSpinner);
        ArrayAdapter<CharSequence> hebrewAdapter = ArrayAdapter.createFromResource(this,
                R.array.hebrewList, android.R.layout.simple_spinner_item);
        hebrewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hebrewSpinner.setAdapter(hebrewAdapter);


        //setup secularSpinner
        Spinner secularSpinner = (Spinner) findViewById(R.id.SecularSpinner);
        ArrayAdapter<CharSequence> secularAdapter = ArrayAdapter.createFromResource(this,
                R.array.secularList, android.R.layout.simple_spinner_item);
        secularAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secularSpinner.setAdapter(secularAdapter);

        //finished editing hebrew
        EditText hebrewEd = (EditText) findViewById(R.id.HebrewEd);
        opinionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                MainActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                MainActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        hebrewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                MainActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        secularSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                MainActivity.this.update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        hebrewEd.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {

                MainActivity.this.update();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }
        });
    }

    void update()
    {
        EditText hebrewEdit = (EditText) findViewById(R.id.HebrewEd);
        try
        {
            double heb = Double.parseDouble(hebrewEdit.getText().toString());
            double mmPerSeorahs = 0;
            String[] convertToSeorahFactors = getResources().getStringArray(R.array.convertToSeorah);
            String[] convertFromMMFactors = getResources().getStringArray(R.array.convertFromMM);
            Spinner HebrewSpinner = (Spinner) findViewById(R.id.HebrewSpinner);
            Spinner SecularSpinner = (Spinner) findViewById(R.id.SecularSpinner);
            int hebrewSpinnerPos = HebrewSpinner.getSelectedItemPosition();
            double seorahs = heb * Double.parseDouble(convertToSeorahFactors[hebrewSpinnerPos]);

            if (((Spinner) findViewById(R.id.OpinionSpinner)).getSelectedItemPosition() == Integer.parseInt(getResources().getString(R.string.GRACH_NAEH)))
            {
                mmPerSeorahs = Double.parseDouble(getResources().getString(R.string.GRACH_NAEH_CF));
            }
            if (((Spinner) findViewById(R.id.OpinionSpinner)).getSelectedItemPosition() == Integer.parseInt(getResources().getString(R.string.CHAZON_ISH)))
            {
                mmPerSeorahs = Double.parseDouble(getResources().getString(R.string.CHAZON_ISH_CF));

            }

            double mm = seorahs * mmPerSeorahs;
            int secularSpinnerPos = SecularSpinner.getSelectedItemPosition();
            double secularUnits = mm / Double.parseDouble(convertFromMMFactors[secularSpinnerPos]);
            System.out.println("" + Double.parseDouble(convertFromMMFactors[secularSpinnerPos]));
            ((TextView) findViewById(R.id.SecularView)).setText(Double.toString(secularUnits));

        }catch (Exception e){}
    }
}
