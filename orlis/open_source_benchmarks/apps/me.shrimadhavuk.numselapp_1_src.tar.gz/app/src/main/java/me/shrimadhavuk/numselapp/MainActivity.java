package me.shrimadhavuk.numselapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn1;
    Button btn2;
    EditText txt1;
    String WHATSAPP_PKG_NAME = "com.whatsapp";
    String msg = "me.shrimadhavuk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        txt1 = (EditText) findViewById(R.id.editText);
        Intent intent = getIntent();
        String action = intent.getAction();

        if(action.equals("android.intent.action.VIEW")) {
            listenIntent(intent);
        }

    }

    public void listenIntent(Intent intent){
        // http://stackoverflow.com/questions/2958701/launch-custom-android-application-from-android-browser
        Uri data = intent.getData();
        String scheme = data.getScheme();
        if(scheme.equals("me.shrimadhavuk.whatsapp")){
            if(isPackageInstalled(WHATSAPP_PKG_NAME)){
                String open = data.getHost();
                if(open.equals("open")){
                    List<String> params = data.getPathSegments();
                    try {
                        String phoneno = params.get(0); // "XXXXXXXXXXXXXX"
                        openWhatsappContact(phoneno);
                    }
                    catch(Exception e){

                    }
                }
            }
            else{
                Toast.makeText(MainActivity.this, "WhatsApp not installed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void abtAcnpfm(View v){
        Toast.makeText(MainActivity.this, "https://github.com/SpEcHiDe/NoWhatOpen", Toast.LENGTH_LONG).show();
    }

    public void launchAcnPfm(View v){
        String phonenumber = txt1.getText().toString();
        if(isPackageInstalled(WHATSAPP_PKG_NAME)){
            openWhatsappContact(phonenumber);
        }
    }

    public void openWhatsappContact(String number) {
        // http://stackoverflow.com/questions/15462874/sending-message-through-whatsapp
        Uri uri = Uri.parse("smsto:" + number);
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage(WHATSAPP_PKG_NAME);
        startActivity(Intent.createChooser(i, ""));
        Log.i(msg, "WhatsApp started");
    }

    private boolean isPackageInstalled(String uri) {
        // http://stackoverflow.com/questions/18752202/check-if-application-is-installed-android
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            Log.i(msg, "WhatsApp installed");
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(msg, "WhatsApp NOT installed");
            app_installed = false;
        }
        return app_installed;
    }

    /** Called when the activity is about to become visible. */
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(msg, "The onStart() event");
        setContentView(R.layout.activity_main);
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(msg, "The onResume() event");
        setContentView(R.layout.activity_main);
    }

    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(msg, "The onPause() event");
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(msg, "The onStop() event");
    }

    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(msg, "The onDestroy() event");
    }

}
