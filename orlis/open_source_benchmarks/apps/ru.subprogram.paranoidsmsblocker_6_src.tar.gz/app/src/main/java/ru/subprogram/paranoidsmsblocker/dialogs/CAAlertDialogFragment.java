package ru.subprogram.paranoidsmsblocker.dialogs;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ru.subprogram.paranoidsmsblocker.R;

public class CAAlertDialogFragment extends DialogFragment {

	private static final String ARG_TEXT = "text";

	private IAAlertDialogObserver mObserver;

	public static CAAlertDialogFragment newInstance(String text, Bundle args) {
		CAAlertDialogFragment frag = new CAAlertDialogFragment();
		args.putString(ARG_TEXT, text);
		frag.setArguments(args);
		return frag;
	}

	public static CAAlertDialogFragment newInstance(String text) {
		return newInstance(text, new Bundle());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mObserver = (IAAlertDialogObserver) activity;
	}

	@Override
	public void onDetach() {
		mObserver = null;
		super.onDetach();
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String text = getArguments().getString(ARG_TEXT);

        Builder builder = new Builder(getActivity())
        //.setIcon(R.drawable.alert_dialog_icon)
        //.setTitle(title)
		.setMessage(text);
		builder.setPositiveButton(R.string.yes_button,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if(mObserver!=null)
						mObserver.alertDialogOkButtonClick(getTag(), getArguments());
				}
			}
		);
        
       	builder.setNegativeButton(R.string.no_button, null);

        return builder.create();
    }
	
}
