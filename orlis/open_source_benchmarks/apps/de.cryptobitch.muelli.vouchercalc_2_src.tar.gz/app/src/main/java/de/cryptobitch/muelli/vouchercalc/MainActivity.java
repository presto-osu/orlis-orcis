package de.cryptobitch.muelli.vouchercalc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextWatcher tw = new MyTextWatcher();

        {
            EditText e = (EditText) findViewById(R.id.price);
            e.addTextChangedListener(tw);
        }

        {
            EditText e = (EditText) findViewById(R.id.available_vouchers);
            e.addTextChangedListener(tw);
        }

        // Just toggling the initial calculation
        tw.onTextChanged("",0,0,0);
    }

    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }


        public  int get_available_vouchers() {
            EditText e = (EditText) findViewById(R.id.available_vouchers);
            int availabe_vouchers;
            try {
                availabe_vouchers = Integer.parseInt(e.getText().toString());
            } catch (NumberFormatException ex) {
                availabe_vouchers = 0;
            }

            return availabe_vouchers;
        }

        public  double get_price() {
            EditText e = (EditText) findViewById(R.id.price);
            double price;
            try {
                price = Double.parseDouble(e.getText().toString());
            } catch (NumberFormatException ex) {
                price = 1;
            }

            return price;
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try {
                double value = get_price();
                double voucher_value = get_voucher_value();
                int available_vouchers = this.get_available_vouchers();

                double exact = value / voucher_value;
                long lower = Math.min(available_vouchers == 0 ? 999 : available_vouchers, Math.round(Math.floor(exact)));
                double lower_value = lower * voucher_value;
                double lower_difference = value - lower_value;
                long upper = Math.min(available_vouchers == 0 ? 999 : available_vouchers,  Math.round(Math.ceil(exact)));
                double upper_value = upper * voucher_value;
                double upper_difference = upper_value - value;

                double tip = 100 * (upper_difference / value);

//                    Toast.makeText(MainActivity.this, "Lower: " + lower + " upper: " + upper, Toast.LENGTH_SHORT).show();

                String format = "Value %4.02f\n" +
                        "Makes at least   %d   vouchers\n"+
                        "%d x %4.02f =  %4.02f\n" +
                        "%4.02f - %4.02f =    -%4.02f\n" +
                        "\n" +
                        "%d vouchers give +%4.02f\n" +
                        "which makes  %4.2f%% tip\n" +
                        ""
                        ;
                String formatted = String.format(format, value,
                        lower,
                        lower, voucher_value, lower_value,
                        value, lower_value, lower_difference,
                        upper, upper_difference,
                        tip
                );
                TextView tv = (TextView)  findViewById(R.id.show);
                tv.setText(formatted);

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "no number", Toast.LENGTH_SHORT).show();
            }

//                Toast.makeText(MainActivity.this, "hallo " + charSequence, Toast.LENGTH_SHORT).show();

        }

        private double get_voucher_value() {
            return 5.77;
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
