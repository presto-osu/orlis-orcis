package com.github.mofosyne.instantreadme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void launchReadme( View v ){
        Intent readmeIntent = new Intent( this, ReadMe.class );
        startActivity( readmeIntent );

        Toast.makeText(this, "Opening Readme!", Toast.LENGTH_SHORT).show();

    }

}
