/*
 * This file is part of Domodroid.
 *
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 *
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */

package Abstract;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import activities.Graphics_Manager;
import misc.tracerengine;


public abstract class display_sensor_info {

    public static void display(tracerengine Tracer, String loc_Value, Long Value_timestamp, String mytag, String parameters, TextView value, RelativeTimeTextView timestamp,
                               Activity context, LinearLayout LL_featurePan, Typeface typefaceweather, Typeface typefaceawesome,
                               String state_key, TextView state_key_view, String stateS, String test_unite) {
        TextView value1;
        timestamp.setReferenceTime(Value_timestamp);
        try {
            float formatedValue = 0;
            if (loc_Value != null) {
                formatedValue = calcul.Round_float(Float.parseFloat(loc_Value));
                Tracer.v(mytag, " Round_float the value: " + loc_Value + " to " + formatedValue);
            }
            if (!test_unite.equals("")) {
                //Basilic add, number feature has a unit parameter
                //#30 add Scale value if too big for byte, ko and Wh unit
                switch (test_unite) {
                    case "b":
                        value.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(loc_Value)));
                        break;
                    case "ko":
                        value.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(loc_Value) * 1024));
                        break;
                    case "Wh":
                        //#30
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        break;
                    case "°":
                        //TODO find how to update the rotate when a new value is receive from events or mq
                        //remove the textView from parent LinearLayout
                        LL_featurePan.removeView(value);
                        LL_featurePan.removeView(timestamp);
                        //Display an arrow with font-awesome
                        value.setTypeface(typefaceweather, Typeface.NORMAL);
                        value.setText("\uf0b1");
                        //display the real value in smaller font
                        value1 = new TextView(context);
                        value1.setTextSize(14);
                        value1.setTextColor(Color.BLACK);
                        value1.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        //Create a rotate animation for arrow with formatedValue as angle
                        RotateAnimation animation = new RotateAnimation(0.0f, formatedValue, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(0);
                        animation.setFillEnabled(true);
                        animation.setFillAfter(true);
                        animation.setFillBefore(true);
                        //apply animation to textView
                        value.startAnimation(animation);
                        //apply gravity and size to textview with font-awesome
                        value.setMinimumHeight(LL_featurePan.getHeight());
                        value.setMinimumWidth(100);
                        value.setGravity(Gravity.CENTER);
                        //Create an empty linearlayout that will contains the value
                        LinearLayout LL_Temp = new LinearLayout(context);
                        //Re-add the view in parent's one
                        LL_Temp.addView(value1);
                        LL_Temp.addView(value);
                        LL_featurePan.addView(LL_Temp);
                        LL_featurePan.addView(timestamp);
                        break;
                    default:
                        if (state_key.equalsIgnoreCase("current_wind_speed")) {
                            state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf03e;"), TextView.BufferType.SPANNABLE);
                        } else if (state_key.equalsIgnoreCase("current_humidity")) {
                            state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf07a;"), TextView.BufferType.SPANNABLE);
                        } else if (state_key.equalsIgnoreCase("current_barometer_value")) {
                            state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf079;"), TextView.BufferType.SPANNABLE);
                        } else if (state_key.contains("temperature")) {
                            state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf053;"), TextView.BufferType.SPANNABLE);
                        }
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        break;
                }
            } else {
                //It has no unit in database or in json
                if (state_key.equalsIgnoreCase("temperature"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " °C");
                else if (state_key.equalsIgnoreCase("pressure"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " hPa");
                else if (state_key.equalsIgnoreCase("humidity"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " %");
                else if (state_key.equalsIgnoreCase("percent"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " %");
                else if (state_key.equalsIgnoreCase("visibility"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " km");
                else if (state_key.equalsIgnoreCase("chill"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " °C");
                else if (state_key.equalsIgnoreCase("speed"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " km/h");
                else if (state_key.equalsIgnoreCase("drewpoint"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " °C");
                else if (state_key.equalsIgnoreCase("condition-code") || state_key.toLowerCase().contains("condition_code") || state_key.toLowerCase().contains("current_code")) {
                    //Add try catch to avoid other case that make #1794
                    try {
                        //use xml and weather fonts here
                        value.setTypeface(typefaceweather, Typeface.NORMAL);
                        value.setText(Graphics_Manager.Names_conditioncodes(context, (int) formatedValue));
                    } catch (Exception e1) {
                        Tracer.i(mytag, "no translation for: " + loc_Value);
                        value.setText(loc_Value);
                    }
                } else if (state_key.equalsIgnoreCase("callerid")) {
                    value.setText(phone_convertion(Tracer, mytag, loc_Value));
                } else value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value));
            }
        } catch (Exception e) {
            // It's probably a String that could not be converted to a float
            Tracer.d(mytag, "Handler exception : new value <" + loc_Value + "> not numeric !");
            try {
                Tracer.d(mytag, "Try to get value translate from R.STRING");
                //todo #90
                if (loc_Value.startsWith("AM") && loc_Value.contains("/PM")) {
                    Tracer.d(mytag, "Try to split: " + loc_Value + " in two parts to translate it");
                    StringTokenizer st = new StringTokenizer(loc_Value, "/");
                    String AM = st.nextToken();
                    String PM = st.nextToken();
                    try {
                        AM = AM.replace("AM ", "");
                        AM = context.getResources().getString(Graphics_Manager.getStringIdentifier(context, AM.toLowerCase()));
                    } catch (Exception amexception) {
                        Tracer.d(mytag, "no translation for: " + AM);
                    }
                    try {
                        PM = PM.replace("PM ", "");
                        PM = context.getResources().getString(Graphics_Manager.getStringIdentifier(context, PM.toLowerCase()));
                    } catch (Exception pmexception) {
                        Tracer.d(mytag, "no translation for: " + PM);
                    }
                    value.setText(R.string.am + " " + AM + "/" + R.string.pm + " " + PM);
                } else {
                    value.setText(Graphics_Manager.getStringIdentifier(context, loc_Value.toLowerCase()));
                }
            } catch (Exception e1) {
                Tracer.d(mytag, "no translation for: " + loc_Value);
                if (state_key.equalsIgnoreCase("current_sunset")) {
                    state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                    state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf052;"), TextView.BufferType.SPANNABLE);
                    // Convert value to hour and in local language
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
                    Date testDate = null;
                    try {
                        testDate = sdf.parse(loc_Value);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Tracer.e(mytag + "Date conversion", "Error: " + ex.toString());
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String newFormat = formatter.format(testDate);
                    value.setText(newFormat);
                } else if (state_key.equalsIgnoreCase("current_sunrise")) {
                    state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                    state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf051;"), TextView.BufferType.SPANNABLE);
                    // Convert value to hour and in local language
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
                    Date testDate = null;
                    try {
                        testDate = sdf.parse(loc_Value);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Tracer.e(mytag + "Date conversion", "Error: " + ex.toString());
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String newFormat = formatter.format(testDate);
                    value.setText(newFormat);
                } else if (state_key.equalsIgnoreCase("current_last_updated")) {
                    // convert value to translated date in locale settings
                    try {
                        loc_Value = loc_Value.substring(0, loc_Value.lastIndexOf(" "));
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a", Locale.ENGLISH);
                        Date testDate = sdf.parse(loc_Value);
                        Tracer.d(mytag + "Date conversion", "Works");
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE dd MMM yyyy HH:mm", Locale.getDefault());
                        String newFormat = formatter.format(testDate);
                        value.setText(newFormat);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Tracer.e(mytag + "Date conversion", "Error: " + ex.toString());
                        value.setText(loc_Value);
                    }
                } else if (state_key.equalsIgnoreCase("callerid")) {
                    value.setText(phone_convertion(Tracer, mytag, loc_Value));
                } else {
                    value.setText(loc_Value);
                }
            }
        }
    }

    public static String phone_convertion(tracerengine Tracer, String mytag, String phone) {
        try {
            String convert_phone = PhoneNumberUtils.formatNumber(phone);
            return convert_phone;
        } catch (Exception ex) {
            ex.printStackTrace();
            Tracer.e(mytag + "Phone conversion", "Error: " + ex.toString());
            return phone;
        }
    }

    public static String value_convertion(tracerengine Tracer, String mytag, Float number, String origin_number) {
        try {
            String convert_number = NumberFormat.getInstance().format(number);
            return convert_number;
        } catch (Exception ex) {
            ex.printStackTrace();
            Tracer.e(mytag + "value_convertion", "Error: " + ex.toString());
            return origin_number;
        }
    }

}
