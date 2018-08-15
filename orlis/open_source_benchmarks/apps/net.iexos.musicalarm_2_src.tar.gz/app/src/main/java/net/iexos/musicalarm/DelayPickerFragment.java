package net.iexos.musicalarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;


public final class DelayPickerFragment extends DialogFragment {
    public DelayPickerFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final NumberPicker picker = new NumberPicker(getActivity());
        picker.setMaxValue(60);
        picker.setMinValue(1);
        picker.setValue(bundle.getInt(AlarmViewActivity.PREF_DELAY));
        builder.setTitle(R.string.set_delay);
        builder.setView(picker);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmViewActivity activity = (AlarmViewActivity) getActivity();
                activity.onDelayChosen(picker.getValue());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
