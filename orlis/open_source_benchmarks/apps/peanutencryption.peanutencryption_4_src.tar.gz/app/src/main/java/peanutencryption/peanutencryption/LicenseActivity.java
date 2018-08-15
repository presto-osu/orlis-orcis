/*
 * @author Gabriel Oexle
 * 2015.
 */

package peanutencryption.peanutencryption;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        TextView textView = (TextView) findViewById(R.id.license_TextView);
        textView.setText(R.string.License_Text);
    }
}
