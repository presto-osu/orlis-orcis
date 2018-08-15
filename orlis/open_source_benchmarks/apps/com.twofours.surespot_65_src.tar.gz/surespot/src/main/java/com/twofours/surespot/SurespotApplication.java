package com.twofours.surespot;

import java.security.Security;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Application;
import android.app.Activity;
import android.view.View;
import android.graphics.Bitmap;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.FileOutputStream;
import com.google.android.gcm.GCMRegistrar;
import com.twofours.surespot.billing.BillingController;
import com.twofours.surespot.chat.EmojiParser;
import com.twofours.surespot.common.FileUtils;
import com.twofours.surespot.common.SurespotConfiguration;
import com.twofours.surespot.common.SurespotConstants;
import com.twofours.surespot.common.SurespotLog;
import com.twofours.surespot.common.Utils;
import com.twofours.surespot.network.IAsyncCallback;
import com.twofours.surespot.services.CredentialCachingService;

public class SurespotApplication extends Application
{
	private static final String TAG = "SurespotApplication";
	private static CredentialCachingService mCredentialCachingService;
	private static StateController mStateController = null;
	private static String mVersion;
	private static BillingController mBillingController;
	private static String mUserAgent;
	public static String PW_INSECURE = null; // "x5j36dFg9jv5!?nMK";
	public static long rollkeysTS = -1L;

	public static final int CORE_POOL_SIZE = 24;
	public static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
	public static final int KEEP_ALIVE = 1;

	public static SharedPreferences global_prefs = null;
	public static int screenshot = 0;

	// ------------------------
	// ------------------------
	public static final int DEBUG_CI = 0;
	// ------------------------
	// ------------------------

	// create our own thread factory to handle message decryption where we have potentially hundreds of messages to decrypt
	// we need a tall queue and a slim pipe
	public static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "surespot #" + mCount.getAndIncrement());
		}
	};

	public static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	/**
	 * An {@link Executor} that can be used to execute tasks in parallel.
	 */
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue,
			sThreadFactory);

	public void onCreate() {
		super.onCreate();

		EmojiParser.init(this);

		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;

		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			mVersion = info.versionName;
		}
		catch (NameNotFoundException e) {
			mVersion = "unknown";
		}

		global_prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// load rollkeys timestamp
		rollkeysTS = global_prefs.getLong("rollkeysTS", -1);
		// load PW
		PW_INSECURE = global_prefs.getString("pwstring", null);
		// check PW
		if (PW_INSECURE == null)
		{
			// generate PW
			PW_INSECURE = PassString.randomString(16);
			// save PW
			SharedPreferences.Editor editor = global_prefs.edit();
			editor.putString("pwstring", PW_INSECURE);
			editor.commit();
		}

		// !!! show PW for debugging ONLY !!!
		// !!! show PW for debugging ONLY !!!
		//        Toast.makeText(SurespotApplication.this, "PW=" + PW_INSECURE, Toast.LENGTH_LONG).show();
		// !!! show PW for debugging ONLY !!!
		// !!! show PW for debugging ONLY !!!

		mUserAgent = "surespot/" + SurespotApplication.getVersion() + " (Android)";

		Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

		SurespotConfiguration.LoadConfigProperties(getApplicationContext());
		mStateController = new StateController(this);

		try {
			// device without GCM throws exception
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);

			// final String regId = GCMRegistrar.getRegistrationId(this);
		//	boolean registered = GCMRegistrar.isRegistered(this);
		//	boolean registeredOnServer = GCMRegistrar.isRegisteredOnServer(this);
		//	if (versionChanged(this) || !registered || !registeredOnServer) {
				SurespotLog.v(TAG, "Registering for GCM.");
				GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
//			}
//			else {
//				SurespotLog.v(TAG, "GCM already registered.");
//			}
		}
		catch (Exception e) {
			SurespotLog.w(TAG, "onCreate", e);
		}

		boolean cacheCleared = Utils.getSharedPrefsBoolean(this, "cacheCleared65");

		if (!cacheCleared) {

			//wipe the cache
			StateController.clearCache(this, new IAsyncCallback<Void>() {
				@Override
				public void handleResponse(Void result) {
					SurespotLog.d(TAG, "cache cleared");
					Utils.putSharedPrefsBoolean(SurespotApplication.this, "cacheCleared65", true);
				}
			});
		}

		// NetworkController.unregister(this, regId);

		SurespotLog.v(TAG, "starting cache service");
		Intent cacheIntent = new Intent(this, CredentialCachingService.class);

		startService(cacheIntent);
		mBillingController = new BillingController(this);
						
		FileUtils.wipeImageCaptureDir(this);
	}

	private boolean versionChanged(Context context) {

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.

		String registeredVersion = Utils.getSharedPrefsString(context, SurespotConstants.PrefNames.APP_VERSION);
		SurespotLog.v(TAG, "registeredversion: %s, currentVersion: %s", registeredVersion, getVersion());
		if (!getVersion().equals(registeredVersion)) {
			SurespotLog.i(TAG, "App version changed.");
			return true;
		}
		return false;
	}

	public static CredentialCachingService getCachingService() {
		return mCredentialCachingService;
	}

	public static void setCachingService(CredentialCachingService credentialCachingService) {
		SurespotApplication.mCredentialCachingService = credentialCachingService;
	}

	public static StateController getStateController() {
		return mStateController;
	}

	public static String getVersion() {
		return mVersion;
	}

	public static BillingController getBillingController() {
		return mBillingController;
	}

	public static String getUserAgent() {
		return mUserAgent;
	}

	public static void take_phone_screenshot(Activity a, String dir_name, String name_base)
	{
		try
		{
			System.out.println("-DEVICE-SCREENSHOT-:a=" + a);
			View v1 = a.getWindow().getDecorView().getRootView();
			System.out.println("-DEVICE-SCREENSHOT-:v1=" + v1);
			v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			v1.setDrawingCacheEnabled(true);
			Bitmap bm = Bitmap.createBitmap(v1.getDrawingCache());
			System.out.println("-DEVICE-SCREENSHOT-:bm=" + bm);

			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream(dir_name + "/" + name_base + ".png");
				System.out.println("xxyy--DEVICE-SCREENSHOT--xxyy:" + dir_name + "/" + name_base + ".png");
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("TSCR:004 " + e.getMessage());
			}
			finally
			{
				v1.setDrawingCacheEnabled(false);

				try
				{
					if (out != null)
					{
						out.close();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e4)
		{
		}
	}

}
