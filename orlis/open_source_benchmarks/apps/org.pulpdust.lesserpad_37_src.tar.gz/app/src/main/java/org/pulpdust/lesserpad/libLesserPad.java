package org.pulpdust.lesserpad;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.text.Editable;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class libLesserPad {
	final String TAG = "libLesserPad";
	final static int FILE_NEW = 0;
	final static int FILE_OPEN = 1;
	final static int REQ_PASS_FOR_ENC = 2;
	final static int REQ_PASS_FOR_DEC = 3;
	final static int REQ_PASS_FOR_OPEN = 4;
	final static String LPAD_EDIT = "org.pulpdust.lesserpad.EDIT";
	final static String LPAD_NEW = "org.pulpdust.lesserpad.NEW";
	final static String DA_LAUNCH = "org.pulpdust.da.action.LAUNCH";
	final static String DA_EX_TEXT = "org.pulpdust.da.extra.TEXT";
	final static String DA_EX_THEME = "org.pulpdust.da.extra.THEME";
	final static String DA_EX_RETURN = "org.pulpdust.da.extra.RETURN";
	final static String CRYPT_PASS = "org.pulpdust.lesserpad.crypt.PASS";
	final static String CRYPT_FILE = "org.pulpdust.lesserpad.crypt.FILE";
	final static String REQ_PASS_MODE = "org.pulpdust.lesserpad.crypt.MODE";

    public void listDir(File path, ArrayAdapter<String> adirs, List<String> dirs, Spinner ebox, 
    		Activity av, String action){
    	adirs.clear();
    	File base = path.getParentFile();
    	String cur = path.getName();
    	String list[] = base.list();
    	Arrays.sort(list);
    	for(int index = 0 ; index < list.length; index++){
    		File who = new File(base, list[index]);
    		if (who.isDirectory() && !list[index].matches("^\\.{1}.+$")){
    			adirs.add(list[index]);
    		}
    	}
    	int pos = dirs.indexOf(cur);
    	if (ebox != null){
    		ebox.setSelection(pos);
    	}
    	if (Build.VERSION.SDK_INT >= 11
				&& Build.VERSION.SDK_INT < 21
				&& av != null){
    		if ((action != null && !action.equals(Intent.ACTION_EDIT) && !action.equals(Intent.ACTION_VIEW)) 
    				|| (action == null) 
    				|| (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 13)){
    			forHoneycomb fhc = new forHoneycomb();
    			fhc.setSelection(av, pos);
    		}
    	}
    }

    public void goSearch(Context context, EditText etxt){
		Intent gosearch = new Intent();
		String forsearch = getSelection(false, etxt);
		gosearch.setAction(Intent.ACTION_SEARCH);
		gosearch.putExtra(SearchManager.QUERY, forsearch);
		gosearch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(gosearch);
    }
    public String getSelection(boolean deletion, EditText etxt){
		Editable edit = etxt.getEditableText();
		int start = etxt.getSelectionStart();
		int end = etxt.getSelectionEnd();
		char select[] = new char[Math.max( start, end ) - Math.min( start, end )];
		edit.getChars(Math.min( start, end ), Math.max( start, end ), select, 0);
		if (deletion){
			edit.delete(Math.min( start, end ), Math.max( start, end ));
		}
    	return String.valueOf(select);
    }
    public Intent daLaunch(Activity av, EditText etxt, int look){
		int start = etxt.getSelectionStart();
		int end = etxt.getSelectionEnd();
    	SharedPreferences props = av.getPreferences(Context.MODE_PRIVATE);
    	SharedPreferences.Editor pedit = props.edit();
    	pedit.putInt("select_start", start);
    	pedit.putInt("select_end", end);
    	pedit.commit();
		Intent dal = new Intent(DA_LAUNCH);
		dal.putExtra(DA_EX_TEXT, getSelection(false, etxt));
		dal.putExtra(DA_EX_THEME, look);
    	return dal;
    }
    public boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    public String getSum(String text, int trim)
    		throws GeneralSecurityException, UnsupportedEncodingException {
    	byte[] bin;
    	byte[] dg;
    	String ret;
    	bin = text.getBytes("UTF-8");
    	MessageDigest md = MessageDigest.getInstance("SHA-512");
    	md.update(bin);
    	dg = md.digest();
    	StringBuffer sb = new StringBuffer();
    	for (byte b : dg){
    		sb.append(String.format("%02x", b & 0xff));
    	}
		ret = sb.toString();
    	if (trim > 0){
    		ret = ret.substring(0, trim);
    	}
    	return ret;
    }

}
