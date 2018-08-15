package com.jparkie.aizoban.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.jparkie.aizoban.R;

public class DisclaimerFragment extends DialogFragment{
    public static final String TAG = DisclaimerFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater dialogInflater = getActivity().getLayoutInflater();
        View disclaimerView = dialogInflater.inflate(R.layout.fragment_disclaimer, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(disclaimerView)
                .setPositiveButton(R.string.disclaimer_dialog_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DisclaimerFragment.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }
}
