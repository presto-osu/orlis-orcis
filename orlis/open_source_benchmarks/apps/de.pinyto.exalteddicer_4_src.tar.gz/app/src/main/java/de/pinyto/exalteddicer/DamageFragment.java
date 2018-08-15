package de.pinyto.exalteddicer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.lang.reflect.Field;

import de.pinyto.exalteddicer.dicing.Dicer;
import de.pinyto.exalteddicer.move.ShakeListener;
import de.pinyto.exalteddicer.move.ShakeListener.OnShakeListener;

public class DamageFragment extends Fragment {

    private Activity mActivity;

    View rootView;
    NumberPicker[] numberPickerRow;
    TextView resultField;
    SharedPreferences sharedPreferences;

    private boolean shakingEnabled;
    private boolean vibrationEnabled;

    // for Shaking
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeListener mShakeDetector;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        loadPreferences();

        rootView = inflater.inflate(R.layout.fragment_damage, container, false);

        initNumberPicker();

        resultField = (TextView) rootView.findViewById(R.id.textViewDM);

        Button rollDiceButton = (Button) rootView.findViewById(R.id.buttonDM);
        rollDiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                evaluate(vibrator);
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) mActivity
                .getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeListener();
        mShakeDetector.setOnShakeListener(new OnShakeListener() {

            public void onShake(int count) {

                if (shakingEnabled) {
                    Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    evaluate(vibrator);
                }
            }
        });
        return rootView;
    }

    public void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
        shakingEnabled = sharedPreferences.getBoolean("enable_shaking", true);
        vibrationEnabled = sharedPreferences.getBoolean("enable_vibration", true);
    }

    public void initNumberPicker() {

        numberPickerRow = new NumberPicker[2];
        numberPickerRow[0] = (NumberPicker) rootView
                .findViewById(R.id.numberPickerDMLeft);
        numberPickerRow[1] = (NumberPicker) rootView
                .findViewById(R.id.numberPickerDMRight);

        String[] numbers = new String[10];

        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.toString(i);
        }
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();

        for (int i = 0; i < 2; i++) {
            numberPickerRow[i].setMinValue(0);
            numberPickerRow[i].setMaxValue(9);
            numberPickerRow[i].setWrapSelectorWheel(true);
            numberPickerRow[i].setDisplayedValues(numbers);
            setNumberPickerTextColor(numberPickerRow[i]);
            numberPickerRow[i].setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            for (Field field : pickerFields) {
                if (field.getName().equals("mSelectionDivider")) {
                    field.setAccessible(true);
                    try {
                        field.set(numberPickerRow[i], getResources().getDrawable(R.drawable.np_numberpicker_selection_divider));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;

                }


            }
            numberPickerRow[1].setValue(1);
            numberPickerRow[0].setValue(0);


        }
    }

    public boolean setNumberPickerTextColor(NumberPicker numberPicker) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(Color.parseColor("#ffffff"));
                    ((EditText) child).setTextColor(Color.parseColor("#ffffff"));
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    Log.w("setNumberPickerTextColor", e);
                } catch (IllegalAccessException e) {
                    Log.w("setNumberPickerTextColor", e);
                } catch (IllegalArgumentException e) {
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

    public int getPoolSize() {

        String poolSize = String.valueOf(numberPickerRow[0].getValue())
                + (numberPickerRow[1].getValue());
        return Integer.parseInt(poolSize);
    }

    public void evaluate (Vibrator vibrator) {
        Dicer dicer = new Dicer();
        dicer.setPoolSize(getPoolSize());
        int success = dicer.evaluateDamage();
        checkBotched(success, success);
        if (vibrationEnabled) {
            vibrator.vibrate(50);
        }
    }

    public void flashResult(TextView textView) {

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        animation.setStartOffset(20);
        animation.setRepeatMode(Animation.REVERSE);
        textView.startAnimation(animation);

    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public void checkBotched(int result, int success) {

        if (result == -1) {
            resultField.setText(getString(R.string.botch));
        } else {
            resultField.setText(String.valueOf(success));
        }
        flashResult(resultField);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
        shakingEnabled = sharedPreferences.getBoolean("enable_shaking", true);
        vibrationEnabled = sharedPreferences.getBoolean("enable_vibration", true);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

}


