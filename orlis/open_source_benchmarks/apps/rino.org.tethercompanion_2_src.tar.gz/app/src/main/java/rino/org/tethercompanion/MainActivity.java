package rino.org.tethercompanion;

/*
 * This is the source code of Tether companion for Android.
 * It is licensed under GNU GPL v. 3 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Rinat Kurmaev, 2015-2016.
 */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    ToggleButton servButton;
    MyHttpServer ws;
    boolean state = false;
    TextView hint;
    Intent WssIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ws = new MyHttpServer(this);
        servButton = (ToggleButton) findViewById(R.id.toggleButtonServer);
        hint = (TextView) findViewById(R.id.textViewHint);
        WssIntent = new Intent(MainActivity.this, WebServerService.class);
        servButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!state )
                {
                    state = true;
                    startService(WssIntent);
                    hint.setText(getResources().getString(R.string.started_on) + " " + getWifiApIpAddress() + ":8000");
                }
                else
                {
                    state = false;
                    stopService(WssIntent);
                    hint.setText(getResources().getString(R.string.disabled));
                }
            }
        });

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.donate) {


            final Button PayPalLink = new Button(getApplicationContext());
            PayPalLink.setText("PayPal");
            PayPalLink.setTextColor(Color.BLACK);
            PayPalLink.setBackgroundColor(Color.TRANSPARENT);
            PayPalLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/RinatKurmaev"));
                    startActivity(browserIntent);
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Donation")
                    .setView(PayPalLink)
                    .setCancelable(true)
                    .setNegativeButton("Close",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ex", ex.toString());
        }
        return null;
    }

}


