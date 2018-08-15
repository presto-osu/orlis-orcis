package com.samsandberg.mtafarebuster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.results_list);
        final EditText amountOnCard = (EditText) findViewById(R.id.amount_on_card);
        Button buttonGo = (Button) findViewById(R.id.button_go);

        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountOnCard.getText().toString().equals("")) {
                    amountOnCard.setText(getString(R.string.amount_on_card_default_value));
                }
                float amountOnCardValue = Float.valueOf(amountOnCard.getText().toString());
                ArrayList<MtaUtil.MtaUtilResult> results = MtaUtil.amountToAdd(amountOnCardValue);
                ArrayList<String> strings = new ArrayList<String>();
                for (MtaUtil.MtaUtilResult result : results) {
                    strings.add(result.toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, strings);
                listView.setAdapter(adapter);
            }
        });
    }
}
