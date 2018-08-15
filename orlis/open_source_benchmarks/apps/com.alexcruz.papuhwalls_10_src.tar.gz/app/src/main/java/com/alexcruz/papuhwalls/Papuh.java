package com.alexcruz.papuhwalls;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.alexcruz.papuhwalls.Walls.AbsWalls;


public class Papuh extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Papuh.this, MainActivity.class);
        startActivity(intent);
    }
}

