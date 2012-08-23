package com.example.org.smaphotag;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropBoxSync {

	// /////////////////////////////////////////////////////////////////////////
	// Your app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// Replace this with your app key and secret assigned by Dropbox.
	// Note that this is a really insecure way to do this, and you shouldn't
	// ship code which contains your key & secret in such an obvious way.
	// Obfuscation is good.
	final static private String APP_KEY = "q95rn3ygpjq6xk5";
	final static private String APP_SECRET = "fl2edwx0bicphdk";

	// If you'd like to change the access type to the full Dropbox instead of
	// an app folder, change this value.
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	// /////////////////////////////////////////////////////////////////////////
	// End app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// You don't need to change these, leave them alone.
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private Activity mContext;
  private boolean mLoggedIn=false;
  
	private DropboxAPI<AndroidAuthSession> mApi;

	public DropBoxSync(Activity ctx) {
		mContext = ctx;
		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		checkAppKeySetup();
	}

	public void startAuth() {
		mApi.getSession().startAuthentication(mContext);
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private void checkAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
			showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
			// finish();
			return;
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = mContext.getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			showToast("URL scheme in your app's " + "manifest is not set up correctly. You should have a+" + "com.dropbox.client2.android.AuthActivity with the " + "scheme: " + scheme);
			// finish();
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a
	 * local store, rather than storing user name & password, and
	 * re-authenticating each time (which is not to be done, ever).
	 * 
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
		SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	public void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}

	private File getFileByIndex(int index) {
		return new File(SmaphotagEnv.path + "track" + index + ".gpx");
	}

	public SharedPreferences getSP() {
		return mContext.getSharedPreferences("sync_store", Context.MODE_PRIVATE);
		
	}
	
	public void doSync() {
		try {
			int i = getSP().getInt("last_sync_id",0);

			File act_file = getFileByIndex(i);
			while (act_file.exists()) {
				FileInputStream fis = new FileInputStream(act_file);
				// mApi.metadata(arg0, arg1, arg2, arg3, arg4)
				UploadRequest mRequest = mApi.putFileOverwriteRequest(act_file.getName(), fis, act_file.length(), null);

				if (mRequest != null) {
					mRequest.upload();
				}

				getSP().edit().putInt("last_sync_id", i);
				
				i++;
				act_file = getFileByIndex(i);

			}

		} catch (Exception e) {
			showToast(" problem uploading " + e);
		}
	}

	public DropboxAPI<AndroidAuthSession> getAPI() {
		return mApi;
	}
	
  /**
   * Convenience function to change UI state based on being logged in
   */
  public void setLoggedIn(boolean loggedIn) {
      mLoggedIn = loggedIn;
  }

}
