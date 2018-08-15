package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.HashSet;
import java.util.Set;

public class DnsEditText extends AutoCompleteTextView{
    private SharedPreferences mPreference;

    private Context context;

    public DnsEditText(Context c){
        this(c, null);
    }

    public DnsEditText(Context c, AttributeSet attr){
        super(c, attr);

        context = c;
        mPreference = PreferenceManager.getDefaultSharedPreferences(c);

        setSingleLine(true);

        int input_type;
        int max_length;
        if(mPreference.getBoolean(ValueConstants.KEY_PREF_FULL_KEYBOARD, false)) {
            input_type = InputType.TYPE_CLASS_TEXT;
            max_length = 43;
        }else{
            input_type = InputType.TYPE_CLASS_NUMBER;
            max_length = 21;
        }
        setRawInputType(input_type);

        setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length)});
        setThreshold(1);

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDropDown();
                }
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDown();
            }
        });

        setCompletingList();

    }

    private void setCompletingList(){
        Set<String> dnslist = mPreference.getStringSet(ValueConstants.KEY_DNS_LIST, new HashSet<String>());
        ArrayAdapter<String> dnsListAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line,
                dnslist.toArray(new String[dnslist.size()]));
        setAdapter(dnsListAdapter);
    }

}
