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

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_about, null);
        final TextView textViewContent = (TextView) dialogView.findViewById(R.id.dialog_about_content);
        if (textViewContent != null) {
            textViewContent.setMovementMethod(LinkMovementMethod.getInstance());
            Linkify.addLinks(textViewContent, Linkify.WEB_URLS);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.dialog_about_title)
                .setView(dialogView)
                .setInverseBackgroundForced(true);

        return builder.create();
    }
}
