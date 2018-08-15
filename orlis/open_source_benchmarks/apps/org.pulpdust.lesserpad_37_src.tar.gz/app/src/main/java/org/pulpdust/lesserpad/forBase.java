package org.pulpdust.lesserpad;

import android.content.Context;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
public class forBase {
	public static void doCopy(Context c, CharSequence text){
		ClipboardManager clipbd = (ClipboardManager)
		c.getSystemService(Context.CLIPBOARD_SERVICE);
		clipbd.setText(text);
	}
	public static CharSequence getCopy(Context c){
		ClipboardManager clipbd = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
    	return clipbd.getText();
	}

}
