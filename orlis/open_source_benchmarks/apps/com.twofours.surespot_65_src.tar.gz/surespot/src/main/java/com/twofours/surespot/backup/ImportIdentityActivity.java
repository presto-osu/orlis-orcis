package com.twofours.surespot.backup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.security.PrivateKey;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import com.loopj.android.http.AsyncHttpResponseHandler;

import com.twofours.surespot.R;
import com.twofours.surespot.activities.MainActivity;
import com.twofours.surespot.common.FileUtils;
import com.twofours.surespot.common.SurespotConstants;
import com.twofours.surespot.common.SurespotLog;
import com.twofours.surespot.common.Utils;
import com.twofours.surespot.identity.IdentityController;
import com.twofours.surespot.identity.IdentityOperationResult;
import com.twofours.surespot.network.IAsyncCallback;
import com.twofours.surespot.ui.SingleProgressDialog;
import com.twofours.surespot.ui.UIUtils;
import com.twofours.surespot.chat.ChatUtils;
import com.twofours.surespot.encryption.EncryptionController;
import com.twofours.surespot.identity.SurespotIdentity;
import com.twofours.surespot.activities.MainActivity;
import com.twofours.surespot.SurespotApplication;

public class ImportIdentityActivity extends SherlockActivity {
	private static final String TAG = null;
	private boolean mSignup;

	private TextView mAccountNameDisplay;
	private boolean mShowingLocal;
	
	private ListView mDriveListview;
	private SingleProgressDialog mSpd;
	private SingleProgressDialog mSpdLoadIdentities;
	public static final String[] ACCOUNT_TYPE = new String[] { "dummy" };
	private static final String ACTION_DRIVE_OPEN = "com.google.android.apps.drive.DRIVE_OPEN";
	private static final String EXTRA_FILE_ID = "resourceId";
	private String mFileId;
	private int mMode;
	private static final int MODE_NORMAL = 0;
	private static final int MODE_DRIVE = 1;
	private ViewSwitcher mSwitcher;
	private SimpleAdapter mDriveAdapter;
	private AlertDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_identity);
		Utils.configureActionBar(this, getString(R.string.identity), getString(R.string.restore), true);

		Intent intent = getIntent();
		
		mMode = MODE_NORMAL;
		mSwitcher = (ViewSwitcher) findViewById(R.id.restoreViewSwitcher);
		RadioButton rbRestoreLocal = (RadioButton) findViewById(R.id.rbRestoreLocal);
		
		final View v001 = (View) findViewById(R.id.rbRestoreDrive);
		v001.setVisibility(View.INVISIBLE);
		
		mShowingLocal = true;
		rbRestoreLocal.setTag("local");
		rbRestoreLocal.setChecked(true);
		setupLocal();
	}

	private void setupLocal()
	{

		ListView lvIdentities = (ListView) findViewById(R.id.lvLocalIdentities);
		lvIdentities.setEmptyView(findViewById(R.id.no_local_identities));

		List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();

		// query the filesystem for identities
		final File exportDir = FileUtils.getIdentityExportDir();
		File[] files = IdentityController.getExportIdentityFiles(this, exportDir.getPath());

		TextView tvLocalLocation = (TextView) findViewById(R.id.restoreLocalLocation);

		if (files != null) {
			TreeMap<Long, File> sortedFiles = new TreeMap<Long, File>(new Comparator<Long>() {
				public int compare(Long o1, Long o2) {
					return o2.compareTo(o1);
				}
			});

			for (File file : files) {
				sortedFiles.put(file.lastModified(), file);
			}

			for (File file : sortedFiles.values()) {
				long lastModTime = file.lastModified();
				String date = DateFormat.getDateFormat(this).format(lastModTime) + " " + DateFormat.getTimeFormat(this).format(lastModTime);

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", IdentityController.getIdentityNameFromFile(file));
				map.put("date", date);
				items.add(map);
			}
		}

		final SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.identity_item, new String[] { "name", "date" }, new int[] {
				R.id.identityBackupName, R.id.identityBackupDate });
		tvLocalLocation.setText(exportDir.toString());
		lvIdentities.setVisibility(View.VISIBLE);

		lvIdentities.setAdapter(adapter);
		lvIdentities.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (IdentityController.getIdentityCount(ImportIdentityActivity.this) >= SurespotConstants.MAX_IDENTITIES) {
					Utils.makeLongToast(ImportIdentityActivity.this, getString(R.string.login_max_identities_reached, SurespotConstants.MAX_IDENTITIES));
					return;
				}

				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) adapter.getItem(position);

				final String user = map.get("name");			

				// make sure file we're going to save to is writable before we
				// start
				if (!IdentityController.ensureIdentityFile(ImportIdentityActivity.this, user, true))
				{
					Utils.makeToast(ImportIdentityActivity.this, getString(R.string.could_not_import_identity));
					if (mMode == MODE_DRIVE) {
						finish();
					}
					return;
				}

				UIUtils.passwordDialog(ImportIdentityActivity.this, getString(R.string.restore_identity, user), getString(R.string.enter_password_for, user),
						new IAsyncCallback<String>()
						{
							@Override
							public void handleResponse(String result)
							{
								final String new_pass = result;
								final String user2 = user;
								
								if (!TextUtils.isEmpty(result))
								{
									IdentityController.importIdentity(ImportIdentityActivity.this, exportDir, user, result,
											new IAsyncCallback<IdentityOperationResult>()
											{

												@Override
												public void handleResponse(IdentityOperationResult response)
												{

													Utils.makeLongToast(ImportIdentityActivity.this, response.getResultText());

													if (response.getResultSuccess())
													{
														Thread t = new Thread(new Runnable()
														{
															public void run()
															{
																// change PW to random PW
																changePassword(user2, new_pass, SurespotApplication.PW_INSECURE, SurespotApplication.PW_INSECURE);
															}
														});
														t.start();					

														// wait a bit
														try
														{
															Thread.sleep(3500);
														}
														catch (Exception exex)
														{
														}

														// if launched
														// from
														// signup and
														// successful
														// import, go to
														// login
														// screen
														if (mSignup)
														{
															IdentityController.logout();

															Intent intent = new Intent(ImportIdentityActivity.this, MainActivity.class);
															intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
															startActivity(intent);
														}

													}

												}
											});
								}
								else
								{
									Utils.makeToast(ImportIdentityActivity.this, getString(R.string.no_identity_imported));
								}

							}
						});

			}

		});

	}

	private void restoreExternal(boolean firstTime) {
	}

	private void populateDriveIdentities(boolean firstAttempt) {
	}

/*
	private ChildList getIdentityFiles(String identityDirId) {
		ChildList identityFileList = null;
		return identityFileList;
	}
*/
	public String ensureDriveIdentityDirectory() {
		String identityDirId = null;
		return identityDirId;
	}

	// //////// DRIVE
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	private void chooseAccount(boolean ask) {
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
	public void onPause() {
		super.onPause();
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	private class ChangePasswordWrapper
	{

		public String tokenSig;
		public String authSig;
		public String keyVersion;
		public String password;
		public String salt;

		public ChangePasswordWrapper(String password, String salt, String tokenSig, String authSig, String keyVersion) {
			super();
			this.password = password;
			this.salt = salt;
			this.tokenSig = tokenSig;
			this.authSig = authSig;
			this.keyVersion = keyVersion;
		}

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
											IdentityController.updatePassword(ImportIdentityActivity.this, identity, username, currentPassword,
													newPassword, result.salt);
											Utils.makeLongToast(ImportIdentityActivity.this, getString(R.string.password_changed));
										};

										@Override
										public void onFailure(Throwable error, String content)
										{
											SurespotLog.i(TAG, error, "changePassword");
											Utils.makeLongToast(ImportIdentityActivity.this, getString(R.string.could_not_change_password));
										}
									});
						}
						else
						{
							Utils.makeLongToast(ImportIdentityActivity.this, getString(R.string.could_not_change_password));
						}

					};
				}.execute();

			}

			@Override
			public void onFailure(Throwable error, String content)
			{
				Utils.makeLongToast(ImportIdentityActivity.this, getString(R.string.could_not_change_password));
			}
		});
    }
}
