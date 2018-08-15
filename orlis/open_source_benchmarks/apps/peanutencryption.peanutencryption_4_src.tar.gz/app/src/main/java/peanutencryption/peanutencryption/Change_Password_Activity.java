package peanutencryption.peanutencryption;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import peanutencryption.peanutencryption.SQL.CodeObject;
import peanutencryption.peanutencryption.SQL.SQLiteHelper;

public class Change_Password_Activity extends AppCompatActivity {

    private static String LOG_str = "peanutencryption";
    private String MY_PREF;
    private SQLiteHelper sqLiteHelper;

    private int countWrongPassword = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change__password_);


        MY_PREF = getString(R.string.sharedPref);


        sqLiteHelper = new SQLiteHelper(this, "peanutEncryption");
    }

    @Override
    public void onBackPressed() {

    }

    public void clickButtonCPConfirm(View v) {
        Log.i(LOG_str, "Change password start");

        TextInputLayout inputLayoutPasswordOld = (TextInputLayout) findViewById(R.id.textInputLayout_Password_Old);
        TextInputLayout inputLayoutPasswordFirst = (TextInputLayout) findViewById(R.id.textInputLayoutCP_Password_First);
        TextInputLayout inputLayoutPasswordSecond = (TextInputLayout) findViewById(R.id.textInputLayoutCP_Password_Second);

        String oldPassword = inputLayoutPasswordOld.getEditText().getText().toString();
        String firstPassword = inputLayoutPasswordFirst.getEditText().getText().toString();
        String secondPassword = inputLayoutPasswordSecond.getEditText().getText().toString();


        if (firstPassword.isEmpty()) {
            inputLayoutPasswordFirst.setError(getString(R.string.Init_App_Empty_password));
            inputLayoutPasswordSecond.setError(" ");
            Log.i(LOG_str, "Change password: password is empty");

        } else {

            if (firstPassword.contentEquals(secondPassword)) {
                Button btn = (Button) findViewById(R.id.Change_Password_ConfirmBtn);
                btn.setEnabled(false);

                new CheckOldKeyTask().execute(new String[]{oldPassword, firstPassword});


            } else {
                Log.e(LOG_str, "Change Psw: new Passwords do not match");
                inputLayoutPasswordFirst.getEditText().setText("");
                inputLayoutPasswordSecond.getEditText().setText("");
                inputLayoutPasswordFirst.setError(getString(R.string.Init_App_Password_do_not_match));
                inputLayoutPasswordSecond.setError(" ");
            }
        }
    }

    private class CheckOldKeyTask extends AsyncTask<String, Void, Integer> {


        protected Integer doInBackground(String... args) {
            SharedPreferences settings = getSharedPreferences(MY_PREF, 0);
            String myOldSalt = settings.getString("passwordSalt", null);
            String encryptedCheckPhrase = settings.getString("checkPassword", null);

            String oldPassword = args[0];
            String newPassword = args[1];
            //Load old key from old password
            AesCbcWithIntegrity.SecretKeys oldSecKey;
            String plainTestPhrase = null;
            try {
                oldSecKey = AesCbcWithIntegrity.generateKeyFromPassword(oldPassword, myOldSalt);

                plainTestPhrase = AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(encryptedCheckPhrase), oldSecKey);

            } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                MainActivity.printExceptionToLog(e);
                Log.e(LOG_str, "Failed to load old key");
                return 1;
            }

            //Check if old password encrypts correclty
            if (!plainTestPhrase.contentEquals("poqumxs45")) {

                return 1;
            }

            //Generate new Key
            String newSalt = null;
            AesCbcWithIntegrity.SecretKeys newSEC_KEY;
            try {
                newSalt = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt());

                newSEC_KEY = AesCbcWithIntegrity.generateKeyFromPassword(newPassword, newSalt);

            } catch (GeneralSecurityException e) {
                MainActivity.printExceptionToLog(e);
                Log.e(LOG_str, "Failed to create new Key");
                return 2;
            }

            //Convert old encrypted codes to new encrypted codes
            ArrayList<CodeObject> codeObjectOldFromDB = sqLiteHelper.getAllCodes();
            try {

                for (CodeObject codeItem: codeObjectOldFromDB)
                {


                        String oldEncrypt = codeItem.getCode();
                        String plainCode = AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(oldEncrypt), oldSecKey);
                        String newEncrypt = AesCbcWithIntegrity.encrypt(plainCode, newSEC_KEY).toString();
                        codeItem.setCode(newEncrypt);
                }



            } catch (UnsupportedEncodingException | GeneralSecurityException e) {
                MainActivity.printExceptionToLog(e);
                Log.e(LOG_str, "Failed to decrypt with old key and encrypt with new key");
                return 3;
            }

            //update codes in database
            SQLiteDatabase db = sqLiteHelper.updateCodes(codeObjectOldFromDB);
            if(db == null)
            {
                Log.e(LOG_str, "Failed to update codes in database");
                return 4;
            }
            try {


                ArrayList<CodeObject> nEWcodeObjects = sqLiteHelper.getAllCodes();

                //Check if new codes in database equals to inserted
                for (CodeObject codeItemOld: codeObjectOldFromDB)
                {
                    boolean isequal = false;
                    for(CodeObject codeItemNew : nEWcodeObjects)
                    {
                        if(codeItemOld.getCode().equals(codeItemNew.getCode()))
                        {
                            isequal = true;
                        }
                    }
                    if(!isequal)
                        throw new Exception("codeObj in dbCodeList does not match with new items from db");
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("checkPassword", AesCbcWithIntegrity.encrypt("poqumxs45", newSEC_KEY).toString());
                editor.putString("passwordSalt", newSalt);
                editor.commit();
                db.setTransactionSuccessful();

            }
            catch (Exception e){
                MainActivity.printExceptionToLog(e);
                Log.e(LOG_str, "Failed to decrypt with old key and encrypt with new key");
                return 5;
            }
            finally {
                db.endTransaction();
            }

            Log.i(LOG_str, "Successful changed password");
            return 0;



        }

        protected void onProgressUpdate() {

        }

        protected void onPostExecute(Integer success) {
            if (success == 0) {
                Intent intent = new Intent();

                setResult(RESULT_OK, intent);
                finish();
            }
            else if (success == 1)
            {
                wrongPassword();
                Button btn = (Button) findViewById(R.id.Change_Password_ConfirmBtn);
                btn.setEnabled(true);
                TextInputLayout inputLayoutPasswordOld = (TextInputLayout) findViewById(R.id.textInputLayout_Password_Old);

                inputLayoutPasswordOld.setError(getString(R.string.Log_In_Act_Wrong_Password));

            }
            else if(success == 2)
            {
                setResult(RESULT_CANCELED, new Intent());
                finish();
            }
            else
            {
                setResult(RESULT_CANCELED, new Intent());
                finish();
            }



        }
    }




    private void wrongPassword() {

        countWrongPassword++;
        Log.i(LOG_str, "Enterd wrong password. Count=" + countWrongPassword);
        if (countWrongPassword >= 3) {
            Log.e(LOG_str, "ERROR: Entered wrong password three time!! Shutdown App");

            Intent broadcastIntent = new Intent();
            setResult(RESULT_CANCELED, broadcastIntent);
            finish();

        }
    }
}
