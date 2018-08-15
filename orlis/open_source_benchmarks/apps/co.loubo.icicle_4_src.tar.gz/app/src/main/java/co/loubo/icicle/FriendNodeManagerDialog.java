package co.loubo.icicle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class FriendNodeManagerDialog extends DialogFragment {

	public static interface NodeManagerDialogListener {
        public void doPositiveClick(FriendNode friendNode);
        public void doNegativeClick();
	}

    private FriendNode friendNode;
    private EditText lnName;
    private Spinner lnTrust;
    private Spinner lnVisibility;

    public FriendNodeManagerDialog() {
        //setViewResource(R.layout.node_dialog_layout);
    }
    
    public static FriendNodeManagerDialog newInstance(int title, FriendNode n) {
    	FriendNodeManagerDialog frag = new FriendNodeManagerDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putSerializable("friendNode",n);
        frag.setArguments(args);
        return frag;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        this.friendNode = (FriendNode) getArguments().getSerializable("friendNode");
        LinearLayout mView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.friend_node_dialog_layout, null, false);
        
        
        this.lnName =(EditText) mView.findViewById(R.id.node_name_value);
        this.lnTrust =(Spinner) mView.findViewById(R.id.trust_spinner);
        this.lnVisibility =(Spinner) mView.findViewById(R.id.visibility_spinner);
        
        
        lnName.setText(friendNode.getName());
        ArrayAdapter<String> adapterT = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.TrustValues);
        this.lnTrust.setAdapter(adapterT);
        ArrayAdapter<String> adapterV = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.VisibilityValues);
        this.lnVisibility.setAdapter(adapterV);
        this.lnTrust.setSelection(Constants.TrustValues.indexOf(this.friendNode.getTrust()));
        this.lnVisibility.setSelection(Constants.VisibilityValues.indexOf(this.friendNode.getVisibility()));
        
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.save,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            
                            friendNode.setName(String.valueOf(lnName.getText()));
                            friendNode.setTrust(lnTrust.getSelectedItem().toString());
                            friendNode.setVisibility(lnVisibility.getSelectedItem().toString());
                            ((NodeManagerDialogListener)getActivity()).doPositiveClick(friendNode);
                        }
                    }
                )
                .setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ((NodeManagerDialogListener)getActivity()).doNegativeClick();
                        }
                    }
                )
                .setView(mView)
                .create();
    }
}
