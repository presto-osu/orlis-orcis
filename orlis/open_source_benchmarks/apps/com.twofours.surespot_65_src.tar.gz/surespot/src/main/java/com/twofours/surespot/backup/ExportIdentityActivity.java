package com.twofours.surespot.backup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import java.security.PrivateKey;


import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.loopj.android.http.AsyncHttpResponseHandler;

import com.twofours.surespot.SurespotApplication;
import com.twofours.surespot.R;
import com.twofours.surespot.common.FileUtils;
import com.twofours.surespot.common.SurespotConstants;
import com.twofours.surespot.common.SurespotLog;
import com.twofours.surespot.common.Utils;
import com.twofours.surespot.identity.IdentityController;
import com.twofours.surespot.network.IAsyncCallback;
import com.twofours.surespot.network.IAsyncCallbackTuple;
import com.twofours.surespot.ui.SingleProgressDialog;
import com.twofours.surespot.ui.UIUtils;
import com.twofours.surespot.chat.ChatUtils;
import com.twofours.surespot.encryption.EncryptionController;
import com.twofours.surespot.ui.MultiProgressDialog;
import com.twofours.surespot.identity.SurespotIdentity;
import com.twofours.surespot.activities.MainActivity;
import com.twofours.surespot.PassString;

public class ExportIdentityActivity extends SherlockActivity
{
    private static final String TAG = "ExportIdentityActivity";
    private List<String> mIdentityNames;
    
    private Spinner mSpinner;

    private TextView mAccountNameDisplay;
    public static final String[] ACCOUNT_TYPE = new String[]{"dummy"};
    private SingleProgressDialog mSpd;
    private SingleProgressDialog mSpdBackupDir;
    private AlertDialog mDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static Button exportToSdCardButton = null;

	TextViewUpdater textViewUpdater = new TextViewUpdater();
	Handler textViewUpdaterHandler = new Handler(Looper.getMainLooper());
	
	private class TextViewUpdater implements Runnable
	{
		private String txt;
	
		@Override
		public void run()
		{
			Utils.makeToast(ExportIdentityActivity.this, txt);
			exportToSdCardButton.setText(txt);
		}
	
		public void setText(String txt)
		{
			this.txt = txt;
		}
	
	}

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_identity);

        Utils.configureActionBar(this, getString(R.string.identity), getString(R.string.backup), true);
        final String identityDir = FileUtils.getIdentityExportDir().toString();

        final TextView tvPath = (TextView) findViewById(R.id.backupLocalLocation);
        mSpinner = (Spinner) findViewById(R.id.identitySpinner);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_item);
        adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        mIdentityNames = IdentityController.getIdentityNames(this);

        for (String name : mIdentityNames)
        {
            adapter.add(name);
        }

        mSpinner.setAdapter(adapter);

        String backupUsername = getIntent().getStringExtra("backupUsername");
        getIntent().removeExtra("backupUsername");

        mSpinner.setSelection(adapter.getPosition(backupUsername == null ? IdentityController.getLoggedInUser() : backupUsername));
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String identityFile = identityDir + File.separator + IdentityController.caseInsensitivize(adapter.getItem(position))
                        + IdentityController.IDENTITY_EXTENSION;
                tvPath.setText(identityFile);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        exportToSdCardButton = (Button) findViewById(R.id.bExportSd);
	final Button b22 = exportToSdCardButton;
        exportToSdCardButton.setEnabled(FileUtils.isExternalStorageMounted());
        // exportToSdCardButton.setText(" ... export ...");
        exportToSdCardButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String user = (String) mSpinner.getSelectedItem();

		if (1 == 3 - 1)
		{
                mDialog = UIUtils.passwordDialog(ExportIdentityActivity.this, getString(R.string.backup_identity, user),
                        getString(R.string.enter_password_for, user), new IAsyncCallback<String>()
                        {
                            @Override
                            public void handleResponse(String result)
                            {
                                if (!TextUtils.isEmpty(result))
                                {
					b22.setEnabled(false);

                                	// change PW to user entered PW
                                	//Utils.makeToast(ExportIdentityActivity.this, "1 1 1");
                                	//b22.setText("...");
                                	//changePassword(user, SurespotApplication.PW_INSECURE, result, result);

					final String newpass = result;

					Thread t = new Thread(new Runnable()
					{
						public void run()
						{
							// wait a bit
							//try
							//{
							//	Thread.sleep(4500);
							//}
							//catch (Exception exex)
							//{
							//}

							// export key with user entered PW
							//textViewUpdater.setText("... 2 ...");
							//textViewUpdaterHandler.post(textViewUpdater);
							exportIdentity(user, newpass);

							//try
							//{
							//	Thread.sleep(4500);
							//}
							//catch (Exception exex)
							//{
							//}

							// change PW back to random PW
							//textViewUpdater.setText("... 3 ...");
							//textViewUpdaterHandler.post(textViewUpdater);
							//changePassword(user, newpass, SurespotApplication.PW_INSECURE, SurespotApplication.PW_INSECURE);
							// ready
							textViewUpdater.setText(newpass);
							textViewUpdaterHandler.post(textViewUpdater);
						}
					});
					t.start();
                                }
                                else
                                {
                                    Utils.makeToast(ExportIdentityActivity.this, getString(R.string.no_identity_exported));
                                }
                            }
                        });
		}
		else
		{
			b22.setEnabled(false);

                	// change PW to new random password
                	Utils.makeToast(ExportIdentityActivity.this, "1 1 1");
                	b22.setText("...");
                	
			// generate PW
			//*// String PW_INSECURE2 = PassString.randomString(16);
                	//*// String result = PW_INSECURE2;
			// change PW                                	
                	//*// changePassword(user, SurespotApplication.PW_INSECURE, PW_INSECURE2, PW_INSECURE2);
			// remember new PW
                	//*// SurespotApplication.PW_INSECURE = PW_INSECURE2;

			// save PW
			//*// SharedPreferences.Editor editor = SurespotApplication.global_prefs.edit();
			//*// editor.putString("pwstring", PW_INSECURE2);
			//*// editor.commit();

			String result = SurespotApplication.PW_INSECURE;
			final String newpass = result;

			Thread t = new Thread(new Runnable()
			{
				public void run()
				{
					// wait a bit
					try
					{
						Thread.sleep(5500);
					}
					catch (Exception exex)
					{
					}

					// export key with user entered PW
					textViewUpdater.setText("... 2 ...");
					textViewUpdaterHandler.post(textViewUpdater);
					exportIdentity(user, newpass);

					//try
					//{
					//	Thread.sleep(4500);
					//}
					//catch (Exception exex)
					//{
					//}

					// change PW back to random PW
					//textViewUpdater.setText("... 3 ...");
					//textViewUpdaterHandler.post(textViewUpdater);
					//changePassword(user, newpass, SurespotApplication.PW_INSECURE, SurespotApplication.PW_INSECURE);
					// ready
					textViewUpdater.setText(newpass);
					textViewUpdaterHandler.post(textViewUpdater);
				}
			});
			t.start();
		}
            }
        });


        final TextView t001 = (TextView) findViewById(R.id.drive_001);
        t001.setVisibility(View.INVISIBLE);

        final Button b001 = (Button) findViewById(R.id.bBackupDrive);
        b001.setVisibility(View.INVISIBLE);

        final View v001 = (View) findViewById(R.id.drive_002);
        v001.setVisibility(View.INVISIBLE);

    }

	private class ChangePasswordWrapper
	{
	
		public String tokenSig;
		public String authSig;
		public String keyVersion;
		public String password;
		public String salt;
		
		public ChangePasswordWrapper(String password, String salt, String tokenSig, String authSig, String keyVersion)
		{
			super();
			this.password = password;
			this.salt = salt;
			this.tokenSig = tokenSig;
			this.authSig = authSig;
			this.keyVersion = keyVersion;
		}
	
	}

    // //////// Local
    private void exportIdentity(String user, String password)
    {
        IdentityController.exportIdentity(ExportIdentityActivity.this, user, password, new IAsyncCallback<String>() {
            @Override
            public void handleResponse(String response) {
                if (response == null) {
                    Utils.makeToast(ExportIdentityActivity.this, getString(R.string.no_identity_exported));
                }
                else {
                    Utils.makeLongToast(ExportIdentityActivity.this, response);
                }

            }
        });
    }

    private void changePassword(final String username, final String currentPassword, final String newPassword, final String confirmPassword)
    {
        final SurespotIdentity identity = IdentityController.getIdentity(this, username, currentPassword);

        final String version = identity.getLatestVersion();
	final PrivateKey pk = identity.getKeyPairDSA().getPrivate();

	// create auth sig
	byte[] saltBytes = ChatUtils.base64DecodeNowrap(identity.getSalt());
	final String dPassword = new String(ChatUtils.base64EncodeNowrap(EncryptionController.derive(currentPassword, saltBytes)));

	final String authSignature = EncryptionController.sign(pk, username, dPassword);
	SurespotLog.v(TAG, "generatedAuthSig: " + authSignature);

	// get a key update token from the server
	MainActivity.getNetworkController().getPasswordToken(username, dPassword, authSignature, new AsyncHttpResponseHandler()
	{
		@Override
		public void onSuccess(int statusCode, final String passwordToken)
		{
			new AsyncTask<Void, Void, ChangePasswordWrapper>()
			{
				@Override
				protected ChangePasswordWrapper doInBackground(Void... params)
				{
					SurespotLog.v(TAG, "received password token: " + passwordToken);

					byte[][] derived = EncryptionController.derive(newPassword);
					final String newSalt = new String(ChatUtils.base64EncodeNowrap(derived[0]));
					final String dNewPassword = new String(ChatUtils.base64EncodeNowrap(derived[1]));

					// create token sig
					final String tokenSignature = EncryptionController.sign(pk, ChatUtils.base64DecodeNowrap(passwordToken),
							dNewPassword.getBytes());

					SurespotLog.v(TAG, "generatedTokenSig: " + tokenSignature);

					return new ChangePasswordWrapper(dNewPassword, newSalt, tokenSignature, authSignature, version);
				}

				protected void onPostExecute(final ChangePasswordWrapper result)
				{
					if (result != null)
					{
						// upload all this crap to the server
						MainActivity.getNetworkController().changePassword(username, dPassword, result.password, result.authSig,
								result.tokenSig, result.keyVersion, new AsyncHttpResponseHandler()
						{
							public void onSuccess(int statusCode, String content)
							{
								// update the password
								IdentityController.updatePassword(ExportIdentityActivity.this, identity, username, currentPassword,
										newPassword, result.salt);
								Utils.makeLongToast(ExportIdentityActivity.this, getString(R.string.password_changed));
							};

							@Override
							public void onFailure(Throwable error, String content)
							{
								SurespotLog.i(TAG, error, "changePassword");
								Utils.makeLongToast(ExportIdentityActivity.this, getString(R.string.could_not_change_password));
							}
						});
					}
					else
					{
						Utils.makeLongToast(ExportIdentityActivity.this, getString(R.string.could_not_change_password));
					}

				};
			}.execute();

		}

		@Override
		public void onFailure(Throwable error, String content)
		{
			Utils.makeLongToast(ExportIdentityActivity.this, getString(R.string.could_not_change_password));
		}
	});
    }

    // //////// DRIVE

    private void chooseAccount(boolean ask) {
    }

    private void removeAccount() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    private void backupIdentityDrive(final boolean firstAttempt) {
    }

    public String ensureDriveIdentityDirectory() {
        String identityDirId = null;
        return identityDirId;
    }

    public boolean updateIdentityDriveFile(String idDirId, String username, byte[] identityData) {
        return false;
    }

/*
    private ChildReference getIdentityFile(String identityDirId, String username) throws IOException {
        ChildReference idFile = null;
        return idFile;
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }


    public void openOptionsMenuDeferred() {
        mHandler.post(new Runnable() {
                          @Override
                          public void run() {
                              openOptionsMenu();
                          }
                      }
        );
    }
}
