package com.idunnololz.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {
	private static final String TAG = AlertDialogFragment.class.getSimpleName();
	
	private static final String ARGS_TITLE = "title";
	private static final String ARGS_MESSAGE = "message";
	
	public static class Builder {
		private Bundle args = new Bundle();
		
		public Builder setTitle(String title) {
			args.putString(ARGS_TITLE, title);
			return this;
		}
		
		public Builder setTitle(int title) {
			args.putInt(ARGS_TITLE, title);
			return this;
		}
		
		public Builder setMessage(String message) {
			args.putString(ARGS_MESSAGE, message);
			return this;
		}
		
		public Builder setMessage(int message) {
			args.putInt(ARGS_MESSAGE, message);
			return this;
		}
		
		public AlertDialogFragment create() {
			AlertDialogFragment frag = new AlertDialogFragment();
			frag.setArguments(args);
			return frag;
		}
	}
	
	private String tryGetString(Bundle b, String argName) {
		Object o = b.get(argName);
		
		if (o instanceof String) {
			return (String) o;
		} else if (o instanceof Integer) {
			return getString((Integer) o);
		}
		return null;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setMessage(tryGetString(args, ARGS_MESSAGE))
        .setTitle(tryGetString(args, ARGS_TITLE))
        .setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dismiss();
                }
            }
        ).create();
        
        
        
        return dialog;
    }
}
