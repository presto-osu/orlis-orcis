package fr.tvbarthel.apps.simpleweatherforcast.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import fr.tvbarthel.apps.simpleweatherforcast.R;


public class LicenseDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        final View dialogView = inflater.inflate(R.layout.dialog_license, null);

        if (dialogView != null) {
            final TextView textViewContent = (TextView) dialogView.findViewById(R.id.dialog_license_content);
            if (textViewContent != null) {
                textViewContent.setMovementMethod(LinkMovementMethod.getInstance());
                Linkify.addLinks(textViewContent, Linkify.WEB_URLS);
            }
        }

        dialogBuilder.setCancelable(true)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.dialog_license_title)
                .setInverseBackgroundForced(true);

        return dialogBuilder.create();
    }
}
