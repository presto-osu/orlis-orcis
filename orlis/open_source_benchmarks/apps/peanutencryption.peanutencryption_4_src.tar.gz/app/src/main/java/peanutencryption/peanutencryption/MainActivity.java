/*
 * @author Gabriel Oexle
 * 2015.
 */
package peanutencryption.peanutencryption;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;


import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

import android.util.*;


import com.tozny.crypto.android.AesCbcWithIntegrity;


import peanutencryption.peanutencryption.SQL.CodeObject;
import peanutencryption.peanutencryption.SQL.SQLiteHelper;

public class MainActivity extends AppCompatActivity {

    private static String LOG_str = "peanutencryption";

    private String MY_PREF;


    private AesCbcWithIntegrity.SecretKeys globalSecretKey;
    private MyRecyclerVAdapter myRecyclerVAdapter;


    private SQLiteHelper sqLiteHelper;

    private boolean initializationInProgress = false;
    private boolean newCodeInProgress = false;


    private boolean loggedIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        //myToolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(myToolbar);


        MY_PREF = getString(R.string.sharedPref);


        sqLiteHelper = new SQLiteHelper(this, "peanutEncryption");

        SharedPreferences settings = getSharedPreferences(MY_PREF, 0);
        boolean isInitialized = settings.getBoolean("isInitialized", false);

        if (!isInitialized) {
            try {
                String newSalt = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt());
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("passwordSalt", newSalt);
                editor.commit();

            } catch (GeneralSecurityException e) {
                printExceptionToLog(e);
                Toast.makeText(getApplicationContext(), "Error producing salt for key", Toast.LENGTH_SHORT).show();
                return;
            }
            initializationInProgress = true;
            Intent intentInitialization = new Intent(this, Initialization.class);
            startActivityForResult(intentInitialization, INITALIZATION_ID);


        }

        loadDataFromDatabase();


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!loggedIn && !initializationInProgress && !newCodeInProgress) {
            authenticate();

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!newCodeInProgress) {
            logout();
        }

    }


    private void authenticate() {
        enterPassword();
    }

    private void finishAuthentication(AesCbcWithIntegrity.SecretKeys secretKeys) {
        if (secretKeys != null) {
            Log.d(LOG_str, "secret Key successful stored");
            globalSecretKey = secretKeys;
            loggedIn = true;
            loadDataFromDatabase();
        } else {
            loggedIn = false;
            Log.e(LOG_str, "secret Key null");
            shutdown();
        }


    }

    private void logout() {
        loggedIn = false;
        globalSecretKey = null;
        myRecyclerVAdapter.clearData();
    }


    private void enterPassword() {
        Intent intent = new Intent(this, Log_In.class);
        startActivityForResult(intent, LOG_IN_ID);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.shutdown: {
                this.shutdown();
                return true;

            }

            case R.id.showLicense:
                Intent showLicenseIntend = new Intent(this, LicenseActivity.class);
                startActivity(showLicenseIntend);
                return true;

            case R.id.change_password_menu:
                Intent changePsw = new Intent(this, Change_Password_Activity.class);
                startActivity(changePsw);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void loadDataFromDatabase() {
        Log.i(LOG_str, "load data from database and set adapter");
        ArrayList<CodeObject> ArrayOfObjectsFromDatabase = sqLiteHelper.getAllCodes();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);


        myRecyclerVAdapter = new MyRecyclerVAdapter(ArrayOfObjectsFromDatabase, new RecyclerViewClickListener(), new RecyclerViewLongClickListener());

        recyclerView.setAdapter(myRecyclerVAdapter);


    }

    public interface ItemClickListener {
        void onClick(View view, CodeObject codeObject);
    }

    public interface ItemLongClickListener {
        boolean onLongClick(View view, CodeObject codeObject);
    }

    public class MyRecyclerVAdapter extends RecyclerView.Adapter<MyRecyclerVAdapter.CodeViewHolder> {

        private final ItemClickListener itemClickListener;
        private final ItemLongClickListener itemLongClickListener;

        List<CodeObject> codeObjects;

        MyRecyclerVAdapter(List<CodeObject> codeObjectList, ItemClickListener _itemClickListener, ItemLongClickListener _itemLongClickListener) {
            codeObjects = codeObjectList;
            itemClickListener = _itemClickListener;
            itemLongClickListener = _itemLongClickListener;
        }

        @Override
        public int getItemCount() {
            return codeObjects.size();
        }


        @Override
        public CodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_code, viewGroup, false);

            return new CodeViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CodeViewHolder codeViewHolder, int i) {
            codeViewHolder.bindListener(codeObjects.get(i), itemClickListener);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public void clearData() {
            codeObjects.clear();
            notifyDataSetChanged();
        }


        public class CodeViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView CodeName;
            TextView CodeSec;
            long dataId;
            String codeSec;

            CodeViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.CardView_Code);
                CodeName = (TextView) itemView.findViewById(R.id.CardView_CodeName);
                CodeSec = (TextView) itemView.findViewById(R.id.CardView_CodeSec);
            }

            public void bindListener(final CodeObject codeObject, final ItemClickListener itemClickListener) {
                this.CodeName.setText(codeObject.getCodeName());
                this.CodeSec.setText("*****");
                this.dataId = codeObject.getDataID();

                //set ClickListener for click
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemClickListener.onClick(v, codeObject);
                    }

                });
                //Set ClickListener for long click
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return itemLongClickListener.onLongClick(v, codeObject);
                    }
                });

            }


        }
    }

    class RecyclerViewLongClickListener implements ItemLongClickListener {
        @Override
        public boolean onLongClick(View view, CodeObject codeObject) {
            delete_Code(codeObject);
            return true;
        }
    }

    class RecyclerViewClickListener implements ItemClickListener {
        @Override
        public void onClick(View v, CodeObject codeObject) {

            if (loggedIn) {


                TextView textView = (TextView) v.findViewById(R.id.CardView_CodeSec);

                if (textView.getText().equals("*****")) {
                    textView.setText(decrypt(codeObject.getCode()));
                } else {
                    textView.setText("*****");
                }


                //TextView textView = (TextView) v;
                //Toast.makeText(getApplicationContext(),textView.getText(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "not logged in", Toast.LENGTH_SHORT).show();
                Log.i(LOG_str, "onItemClick: not loged in");
                authenticate();
            }

        }

    }


    public void onClickBtnNewCode(View v) {
        add_new_Code();
    }

    private void add_new_Code() {

        Intent intent = new Intent(this, NewCodeActivity.class);
        newCodeInProgress = true;
        startActivityForResult(intent, NEWCODE_ID);


    }

    private void delete_Code(final CodeObject codeObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);


        builder.setMessage(getString(R.string.main_Act_delete_Code_text) + "   " + codeObject.getCodeName() + " ?");


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                sqLiteHelper.deleteItemFromDatabase(codeObject.getDataID());
                loadDataFromDatabase();
                //Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    private String encrypt(String message) {

        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;
        try {
            cipherTextIvMac = AesCbcWithIntegrity.encrypt(message, globalSecretKey);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(LOG_str, "EXCEPTION " + e.getClass() + " THROWN. MESSAGE:" + e.getMessage());
            return null;
        }
        return cipherTextIvMac.toString();
    }

    private String decrypt(String decryptedMessage) {

        try {
            return AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(decryptedMessage), globalSecretKey);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e(LOG_str, "EXCEPTION " + e.getClass() + " THROWN. MESSAGE:" + e.getMessage());
            return null;
        }
    }


    private AesCbcWithIntegrity.SecretKeys loadSecretKeys(String password) {
        try {
            SharedPreferences settings = getSharedPreferences(MY_PREF, 0);
            String mySalt = settings.getString("passwordSalt", null);
            return AesCbcWithIntegrity.generateKeyFromPassword(password, mySalt);
        } catch (Exception e) {
            printExceptionToLog(e);
            return null;
        }
    }


    private void shutdown() {
        Log.i(LOG_str, "Shutdown");
        loggedIn = false;
        finish();

    }




    public final int INITALIZATION_ID = 1;
    public final int LOG_IN_ID = 2;
    public final int NEWCODE_ID = 3;
    public final int CHANGE_PSW_ID = 4;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case INITALIZATION_ID: {
                initializationInProgress = false;
                if (resultCode == RESULT_OK) {
                    String password = data.getStringExtra("password");
                    AesCbcWithIntegrity.SecretKeys secKeys = loadSecretKeys(password);
                    if (secKeys != null) {
                        SharedPreferences settings = getSharedPreferences(MY_PREF, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        try {
                            editor.putString("checkPassword", AesCbcWithIntegrity.encrypt("poqumxs45", secKeys).toString());
                        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
                            printExceptionToLog(e);
                            return;
                        }

                        editor.commit();
                        globalSecretKey = secKeys;
                        loggedIn = true;
                    } else {
                        Log.e(LOG_str, "Initialize: load key after generation failed");

                    }
                }

                break;

            }
            case LOG_IN_ID: {

                if (resultCode == RESULT_OK) {
                    AesCbcWithIntegrity.SecretKeys secKeys = (AesCbcWithIntegrity.SecretKeys) data.getSerializableExtra("secKey");

                    if (secKeys != null) {
                        finishAuthentication(secKeys);
                    } else {
                        Log.e(LOG_str, "MainActivity: SecKey from Log_In is empty");
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Log.i(LOG_str, "Log_In Activity returned with result_canceled: Wrong password 3 times");
                    shutdown();
                } else {
                    Log.e(LOG_str, "EnterPassword: Activity return false");
                }
                break;

            }

            case NEWCODE_ID: {
                newCodeInProgress = false;
                if (resultCode == RESULT_OK) {
                    String CodeName = data.getStringExtra("CodeName");
                    String Code = data.getStringExtra("Code");

                    if (CodeName.isEmpty() || Code.isEmpty() || CodeName.contentEquals("") || Code.contentEquals("")) {
                        Log.e(LOG_str, "CodeName or Code is null");
                        return;
                    }

                    long id = sqLiteHelper.insertIntoDataTable(CodeName, encrypt(Code));
                    if (id != 0)
                        Log.i(LOG_str, "Successful added code in Database. ID=" + id);
                    else
                        Log.e(LOG_str, "Failed to insert Code into Database");

                    loadDataFromDatabase();
                    break;
                } else {
                    break;
                }
            }
            case CHANGE_PSW_ID:
                if (resultCode == RESULT_OK) {
                    Log.i(LOG_str, "Change Password Activity returned successful");

                } else if (resultCode == RESULT_CANCELED) {
                    Log.i(LOG_str, "Change Password Activity returned with result_canceled: Wrong password 3 times");
                    shutdown();
                } else {
                    Log.e(LOG_str, "ChangePassword: Activity return false");
                }
                break;

            default:
                break;
        }
    }

    static public void printExceptionToLog(Exception e) {
        Log.e(LOG_str, "EXCEPTION " + e.getClass() + " THROWN. MESSAGE:" + e.getMessage());
    }
}
