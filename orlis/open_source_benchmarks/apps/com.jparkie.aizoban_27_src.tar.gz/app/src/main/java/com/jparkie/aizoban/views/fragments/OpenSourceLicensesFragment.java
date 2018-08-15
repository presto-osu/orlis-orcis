package com.jparkie.aizoban.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.jparkie.aizoban.R;

public class OpenSourceLicensesFragment extends DialogFragment {
    public static final String TAG = OpenSourceLicensesFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater dialogInflater = getActivity().getLayoutInflater();
        View openSourceLicensesView = dialogInflater.inflate(R.layout.fragment_open_source_licenses, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(openSourceLicensesView)
                .setNeutralButton(android.R.string.ok, null);

        return dialogBuilder.create();
    }
}