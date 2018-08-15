package com.example.tobiastrumm.freifunkautoconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class RemoveAllRemovalFailedDialogFragment extends DialogFragment{

    public final static String ARGUMENT_NUMBER_FAILED_REMOVAL = "number_failed_removal";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        int number_failed_removal = arguments.getInt(ARGUMENT_NUMBER_FAILED_REMOVAL, 0);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_remove_all_removal_failed_title)
                .setPositiveButton(R.string.ok, null);
        if(number_failed_removal == 1){
            builder.setMessage(R.string.dialog_remove_all_removal_failed_message_singular);
        }
        else{
            builder.setMessage(getString(R.string.dialog_remove_all_removal_failed_message_plural, number_failed_removal));
        }
        return builder.create();
    }
}
