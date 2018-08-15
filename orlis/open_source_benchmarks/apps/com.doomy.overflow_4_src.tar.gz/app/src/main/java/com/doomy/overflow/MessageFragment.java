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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.doomy.library.DiscreteSeekBar;

import com.getbase.floatingactionbutton.FloatingActionButton;

public class MessageFragment extends Fragment {

    // Declare your view and variables
    private FloatingActionButton mFAB;
	private Vibrator mVibe;
    private DiscreteSeekBar mDiscreteSeekBarQuantity;
    private DiscreteSeekBar mDiscreteSeekBarDelay;
    private EditText mEditText;
    private int mProgressQuantity;
    private int mProgressDelay;
    private DataBase mDB;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate
     * the fragment (e.g. upon screen orientation changes).
     */
    public MessageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDB = new DataBase(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mView =inflater.inflate(R.layout.fragment_message,container,false);

        mProgressQuantity = 1;
        mProgressDelay = 1;
		mVibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        mEditText = (EditText) mView.findViewById(R.id.editTextMessage);

        mDiscreteSeekBarQuantity = (DiscreteSeekBar) getActivity().findViewById(R.id.discreteSeekBarQuantity);
        mDiscreteSeekBarQuantity.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                mProgressQuantity = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                updateEditText();
            }
        });

        mDiscreteSeekBarDelay = (DiscreteSeekBar) getActivity().findViewById(R.id.discreteSeekBarDelay);
        mDiscreteSeekBarDelay.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                mProgressDelay = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                int mTemp = mProgressQuantity;
                updateToast();
                if (mTemp >= mDiscreteSeekBarQuantity.getMax()) {
                    mDiscreteSeekBarQuantity.setProgress(0);
                    mDiscreteSeekBarQuantity.setProgress(mDiscreteSeekBarQuantity.getMax());
                } else if (mTemp < mDiscreteSeekBarQuantity.getMax()) {
                    mDiscreteSeekBarQuantity.setProgress(0);
                    mDiscreteSeekBarQuantity.setProgress(mTemp);
                }
                updateEditText();
            }
        });

        mFAB = (FloatingActionButton) mView.findViewById(R.id.sendMessage);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle mExtra = getActivity().getIntent().getExtras();

                String mFullName = mExtra.getString("fullname");
                String mPhoneNumber = mExtra.getString("phonenumber");
                String mMessage = mEditText.getText().toString().trim();
                int mColorDark = mExtra.getInt("colordark");
                int mQuantity = mProgressQuantity;
                int mDelay = mProgressDelay;

                if (!mMessage.equals("")) {
                    Intent mServiceIntent = new Intent(getActivity(), SendService.class);
                    mServiceIntent.putExtra("fullname", mFullName);
                    mServiceIntent.putExtra("phonenumber", mPhoneNumber);
                    mServiceIntent.putExtra("message", mMessage);
                    mServiceIntent.putExtra("quantity", mQuantity);
                    mServiceIntent.putExtra("delay", mDelay);
                    getActivity().startService(mServiceIntent);

                    mVibe.vibrate(50);
                    Message mDBMessage = new Message(mFullName, mColorDark, mMessage, "(" + mQuantity + ")");
                    mDB.addOne(mDBMessage);
                    ContactActivity.getInstance().finish();
                    getActivity().finish();
                    MainActivity.syncRows();
                } else {
                    Toast mToast = Toast.makeText(getActivity(), getString(R.string.empty), Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 475);
                    mToast.show();
                }
            }
        });

        return mView;
    }

    private void updateEditText() {
        if (mProgressQuantity == 1) {
            mEditText.setHint(getString(R.string.write));
        } else {
            mEditText.setHint(getString(R.string.write) + "  x" + mProgressQuantity);
        }

    }

    private void updateToast() {
        Toast mToast = Toast.makeText(getActivity(), "Error !", Toast.LENGTH_LONG);
        if (mProgressDelay == 1) {
            mDiscreteSeekBarQuantity.setMax(30);
            mToast = Toast.makeText(getActivity(), mProgressDelay + " " + getString(R.string.second), Toast.LENGTH_SHORT);
        } else if (mProgressDelay >= 2&&mProgressDelay < 15) {
            mDiscreteSeekBarQuantity.setMax(30);
            mToast = Toast.makeText(getActivity(), mProgressDelay + " " + getString(R.string.seconds), Toast.LENGTH_SHORT);
        } else if (mProgressDelay >= 15&&mProgressDelay < 30) {
            mDiscreteSeekBarQuantity.setMax(25);
            mToast =  Toast.makeText(getActivity(), mProgressDelay + " " + getString(R.string.seconds), Toast.LENGTH_SHORT);
        } else if (mProgressDelay >= 30&&mProgressDelay < 45) {
            mDiscreteSeekBarQuantity.setMax(20);
            mToast = Toast.makeText(getActivity(), mProgressDelay + " " + getString(R.string.seconds), Toast.LENGTH_SHORT);
        } else if (mProgressDelay >= 45&&mProgressDelay < 60) {
            mDiscreteSeekBarQuantity.setMax(15);
            mToast = Toast.makeText(getActivity(), mProgressDelay + " " + getString(R.string.seconds), Toast.LENGTH_SHORT);
        } else if (mProgressDelay == 60) {
            mDiscreteSeekBarQuantity.setMax(10);
            mToast = Toast.makeText(getActivity(), "1 " + getString(R.string.minute), Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 475);
        mToast.show();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
