/*
    This file is part of the Diaspora Native WebApp.

    Diaspora Native WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora Native WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.redpanal.android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class CustomActionBarActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getLocalClassName(), "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(this.getLocalClassName(), "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(this.getLocalClassName(), "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(this.getLocalClassName(), "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(this.getLocalClassName(), "onStope");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(this.getLocalClassName(), "onDestroy");
    }

}
