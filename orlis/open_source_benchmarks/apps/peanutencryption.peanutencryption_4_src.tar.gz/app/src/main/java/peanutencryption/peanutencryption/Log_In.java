/*
 * @author Gabriel Oexle
 * 2015.
 */
package peanutencryption.peanutencryption;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;


public class Log_In extends AppCompatActivity {

    private String LOG_str = "peanutencryption";

    private String MY_PREF;

    private int countWrongPassword = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logg_in);

        MY_PREF  = getString(R.string.sharedPref);

        TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.start_editText_Password);
        //String pwd = textInputLayout.getEditText().getText().toString();

        //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(textInputLayout, InputMethodManager.SHOW_IMPLICIT);

        textInputLayout.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onClickBtnLogIn(v);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    public void onClickBtnLogIn(View v)
    {
        TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.start_editText_Password);
        String pwd = textInputLayout.getEditText().getText().toString();

        new GenerateKeyAsynTask().execute(new String[]{pwd});


    }



    private void wrongPassword()
    {

        countWrongPassword++;
        Log.i(LOG_str,"Enterd wrong password. Count="+countWrongPassword);
        if(countWrongPassword>=3)
        {
            Log.e(LOG_str, "ERROR: Entered wrong password three time!! Shutdown App");

            Intent broadcastIntent = new Intent();
            setResult(RESULT_CANCELED, broadcastIntent);
            finish();

        }
    }

    private class GenerateKeyAsynTask extends AsyncTask<String, Void, String> {

        private AesCbcWithIntegrity.SecretKeys secretKeys;

        protected String doInBackground(String... args) {
            SharedPreferences settings = getSharedPreferences(MY_PREF, 0);
            String mySalt = settings.getString("passwordSalt", null);
            String encryptedCheckPhrase = settings.getString("checkPassword",null);

            AesCbcWithIntegrity.SecretKeys secKey;
            String plainTestPhrase = null;
            try {
                secKey = AesCbcWithIntegrity.generateKeyFromPassword(args[0],mySalt);

                plainTestPhrase = AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(encryptedCheckPhrase), secKey);
            } catch (GeneralSecurityException |UnsupportedEncodingException e) {
                MainActivity.printExceptionToLog(e);
                secretKeys = null;
                return null;
            }

            if(plainTestPhrase.contentEquals("poqumxs45"))
            {
                secretKeys =secKey;
                return args[0];
            }
            else
            {
                secretKeys = null;
                return null;
            }





        }

        protected void onProgressUpdate() {

        }

        protected void onPostExecute(String password) {
            if( password != null ) {

                Intent broadcastIntent = new Intent();
                broadcastIntent.putExtra("secKey",secretKeys);
                setResult(RESULT_OK, broadcastIntent);
                finish();

            }
            else {
                wrongPassword();
                TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.start_editText_Password);

                textInputLayout.setError(getString(R.string.Log_In_Act_Wrong_Password));

            }


        }
    }


}
