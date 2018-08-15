/**
 * Copyright (C) 2013 Damien Chazoule
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

package com.doomy.overflow;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.doomy.library.DiscreteSeekBar;

public class MessageActivity extends Activity {

    // Declare your view and variables
    private ActionBar mActionBar;
    private String mRecipient;
    private int mColor;
    private int mColorDark;
    private TextView mTextViewQuantity;
    private TextView mTextViewDelay;
    private DiscreteSeekBar mDiscreteSeekBarQuantity;
    private DiscreteSeekBar mDiscreteSeekBarDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Bundle mExtra = getIntent().getExtras();

        mColor = mExtra.getInt("color");
        mColorDark = mExtra.getInt("colordark");
        mRecipient = getString(R.string.recipient) + " " + mExtra.getString("fullname");

        initializeView(mColor, mColorDark, mRecipient);

        MessageFragment mFragment = new MessageFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment).commit();
    }

    private void initializeView(int myColor, int myColorDark, String myRecipient) {

        Window mWindow = getWindow();
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mActionBar = getActionBar();

        ColorDrawable mColorDrawable = new ColorDrawable(getResources().getColor(myColor));
        mWindow.setStatusBarColor(getResources().getColor(myColorDark));

        mActionBar.setBackgroundDrawable(mColorDrawable);
        mActionBar.setTitle(myRecipient);

        mTextViewQuantity = (TextView) findViewById(R.id.textViewQuantity);
        mTextViewQuantity.setTextColor(getResources().getColor(myColorDark));

        mTextViewDelay = (TextView) findViewById(R.id.textViewDelay);
        mTextViewDelay.setTextColor(getResources().getColor(myColorDark));

        mDiscreteSeekBarQuantity = (DiscreteSeekBar) findViewById(R.id.discreteSeekBarQuantity);
        mDiscreteSeekBarQuantity.setScrubberColor(getResources().getColor(myColor));

        mDiscreteSeekBarDelay = (DiscreteSeekBar) findViewById(R.id.discreteSeekBarDelay);
        mDiscreteSeekBarDelay.setScrubberColor(getResources().getColor(myColor));
    }
}