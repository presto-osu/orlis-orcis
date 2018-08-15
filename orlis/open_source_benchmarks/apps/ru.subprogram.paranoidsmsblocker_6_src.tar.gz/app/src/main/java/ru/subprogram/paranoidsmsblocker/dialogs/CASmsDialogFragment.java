package ru.subprogram.paranoidsmsblocker.dialogs;

import java.text.DateFormat;

import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import ru.subprogram.paranoidsmsblocker.database.entities.CASms;
import ru.subprogram.paranoidsmsblocker.database.entities.TAContactStatus;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CASmsDialogFragment extends DialogFragment {

	private static final String ARG_CONTACT = "contact";
	private static final String ARG_SMS = "sms";

	private IASmsDialogObserver mObserver;

	public static CASmsDialogFragment newInstance(CAContact contact, CASms sms) {
		CASmsDialogFragment frag = new CASmsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT, contact);
        args.putSerializable(ARG_SMS, sms);
        frag.setArguments(args);
        return frag;
	}
	
	public static CASmsDialogFragment newInstance(CASms sms) {
		CASmsDialogFragment frag = new CASmsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SMS, sms);
        frag.setArguments(args);
        return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mObserver = (IASmsDialogObserver) activity;
	}

	@Override
	public void onDetach() {
		mObserver = null;
		super.onDetach();
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CAContact contact = (CAContact) getArguments().getSerializable(ARG_CONTACT);
		String message = "";
        int positiveButton;
		int negativeButton;
        String title = null;
		DateFormat df = DateFormat.getDateTimeInstance();
		if(contact!=null) {
			Cursor cursor = getActivity().getContentResolver().query(Uri.parse("content://sms/inbox"), 
					null, "address=?", new String[] {contact.getAddress()}, null);
			
			if(cursor!=null) {
				try {
					if(cursor.moveToNext()) {
				       message  = getString(cursor, "body");
				       title = df.format(getLong(cursor, "date"));
					}
				}
				finally {
					cursor.close();
				}
			}
			if(title==null) {
	            CASms sms = (CASms) getArguments().getSerializable(ARG_SMS);
	            if(sms!=null) {
	            	message = sms.getText();
	    			title = df.format(sms.getDate());
	            }
	            else {
	            	message = "";
	            	title = "";
	            }
			}
	        if(contact.getStatus()== TAContactStatus.EBlackList) {
	        	positiveButton = R.string.move_to_white_list_button;
	        	negativeButton = R.string.this_is_spam_button;
	        }
	        else {
	        	positiveButton = R.string.move_to_black_list_button;
	        	negativeButton = R.string.cancel_button;
	        }
        }
        else {
            CASms sms = (CASms) getArguments().getSerializable(ARG_SMS);
            message = sms.getText();
            
    		title = df.format(sms.getDate());

            positiveButton = -1;
            negativeButton = R.string.close_button;
        }
        
		
		
        Builder builder = new AlertDialog.Builder(getActivity())
        //.setIcon(R.drawable.alert_dialog_icon)
        .setTitle(title)
		.setMessage(message);
        if(positiveButton>0)
        	builder.setPositiveButton(positiveButton,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	if(mObserver!=null)
	                		mObserver.smsDialogMoveToWhiteListButtonClick(contact);
	                }
	            }
	        );
        
        if(negativeButton>0)
        	builder.setNegativeButton(negativeButton, null);

        return builder.create();
    }
	
	private String getString(Cursor cursor, String colName) {
		return cursor.getString(cursor.getColumnIndex(colName));
	}

	private long getLong(Cursor cursor, String colName) {
		return cursor.getLong(cursor.getColumnIndex(colName));
	}

}
