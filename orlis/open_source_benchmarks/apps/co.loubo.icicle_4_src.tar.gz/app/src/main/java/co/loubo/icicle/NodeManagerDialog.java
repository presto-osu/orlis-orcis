package co.loubo.icicle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

public class NodeManagerDialog extends DialogFragment {
	
	public static interface NodeManagerDialogListener {
        public void doPositiveClick(LocalNode localNode, boolean edit);
        public void doNegativeClick();
	}

    private LinearLayout mView;
    private LocalNode localNode;

    public NodeManagerDialog() {
        //setViewResource(R.layout.node_dialog_layout);
    }
    
    public static NodeManagerDialog newInstance(int title, LocalNode n, boolean edit) {
    	NodeManagerDialog frag = new NodeManagerDialog();
    	frag.setLocalNode(n);
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putBoolean("edit", edit);
        frag.setArguments(args);
        return frag;
    }

	private void setLocalNode(LocalNode n) {
		this.localNode = n;
		
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        mView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.node_dialog_layout, null, false);
        
        
        EditText lnName =(EditText) mView.findViewById(R.id.node_name_value);
        EditText lnAddress =(EditText) mView.findViewById(R.id.node_address_value);
        EditText lnPort =(EditText) mView.findViewById(R.id.node_port_value);
        
        
        lnName.setText(localNode.getName());
        lnAddress.setText(localNode.getAddress());
        if(localNode.getPort() != 0){
        	lnPort.setText(String.valueOf(localNode.getPort()));
        }
        
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.save,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	EditText lnName =(EditText) mView.findViewById(R.id.node_name_value);
                            EditText lnAddress =(EditText) mView.findViewById(R.id.node_address_value);
                            EditText lnPort =(EditText) mView.findViewById(R.id.node_port_value);
                            
                            localNode.setName(String.valueOf(lnName.getText()));
                            localNode.setAddress(String.valueOf(lnAddress.getText()));
                            try{
                            	localNode.setPort(Integer.parseInt(String.valueOf(lnPort.getText())));
                            }catch(NumberFormatException e){
                            	localNode.setPort(Constants.DEFAULT_FCP_PORT);
                            }
                            
                        	
                            ((NodeManagerDialogListener)getActivity()).doPositiveClick(localNode,getArguments().getBoolean("edit"));
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
