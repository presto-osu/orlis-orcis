package com.example.tobiastrumm.freifunkautoconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class RemovalFailedDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_removal_failed_title)
                .setMessage(R.string.dialog_removal_failed_message)
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }
}
