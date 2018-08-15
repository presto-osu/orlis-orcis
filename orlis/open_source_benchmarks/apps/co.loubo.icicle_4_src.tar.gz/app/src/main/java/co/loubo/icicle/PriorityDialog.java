package co.loubo.icicle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class PriorityDialog extends DialogFragment {

	public static interface PriorityDialogListener {
        public void doPositiveClick(String identifier, int priority);
        public void doNegativeClick();
	}

    public PriorityDialog() {
    }
    
    public static PriorityDialog newInstance(int title, String identifier) {
    	PriorityDialog frag = new PriorityDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("identifier", identifier);
        frag.setArguments(args);
        return frag;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        LinearLayout mView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.priority_dialog_layout, null, false);

        final AlertDialog ad = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((PriorityDialogListener) getActivity()).doNegativeClick();
                            }
                        }
                )
                .setView(mView)
                .create();

        RadioGroup rg = (RadioGroup) mView.findViewById(R.id.priority_radio_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = group.findViewById(checkedId);
                int priority = group.indexOfChild(radioButton);
                ((PriorityDialogListener)getActivity()).doPositiveClick(getArguments().getString("identifier"),priority);
                ad.dismiss();
            }
        });
        
        return ad;
    }
}
